package com.cisco.cmxmobile.server.stats;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.cmxmobile.model.ContextAwareServiceServerStats;
import com.cisco.cmxmobile.model.UnauthorizedServer;
import com.cisco.cmxmobile.model.UnauthorizedServerStats;
import com.cisco.cmxmobile.model.UnauthorizedServers;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public final class ContextAwareServiceStats {

    private static ContextAwareServiceStats instance = new ContextAwareServiceStats();

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextAwareServiceStats.class);

    private static final long FREQUENCY_LOG_UNAUTHORIZED_EVENT = 1000;
    
    private final AtomicLong locationEventsCount = new AtomicLong();

    private final AtomicLong associationEventsCount = new AtomicLong();

    private final AtomicLong unauthorizedEventsCount = new AtomicLong();

    private final AtomicLong locationEventLastUpdatedTime = new AtomicLong();

    private final AtomicLong associationEventLastUpdatedTime = new AtomicLong();

    private final AtomicLong unauthorizedEventLastUpdatedTime = new AtomicLong();
    
    private final UnauthorizedServers unauthorizedServers = new UnauthorizedServers();

    private final DateFormat dateFormat = new SimpleDateFormat("dd MMM YYYY HH:mm:ss");
    
    private long startTime;

    private String startTimeDate;

    private ContextAwareServiceStats() {
        resetAllCounters();
        resetStartDate();
    }

    public static ContextAwareServiceStats getInstance() {
        return instance;
    }

    private void resetStartDate() {
        startTime = System.currentTimeMillis();
        startTimeDate = dateFormat.format(new Date(System.currentTimeMillis()));
    }

    private void resetAllCounters() {
        locationEventsCount.set(0);
        associationEventsCount.set(0);
        unauthorizedEventsCount.set(0);
        locationEventLastUpdatedTime.set(0);
        associationEventLastUpdatedTime.set(0);
        unauthorizedEventLastUpdatedTime.set(0);
    }
    
    public void resetAllStats() {
        resetAllCounters();
        resetStartDate();
        unauthorizedServers.resetAll();
    }

    public void incrementLocationEventsCount() {
        locationEventsCount.incrementAndGet();
        locationEventLastUpdatedTime.set(System.currentTimeMillis());
    }

    public void incrementAassociationEventsCount() {
        associationEventsCount.incrementAndGet();
        associationEventLastUpdatedTime.set(System.currentTimeMillis());
    }

    public void incrementUnauthorizedEventsCount(String serverAddress, String serverId) {
        unauthorizedEventsCount.incrementAndGet();
        unauthorizedEventLastUpdatedTime.set(System.currentTimeMillis());
        try {
            UnauthorizedServer server = unauthorizedServers.getUnauthorizedServer(serverAddress, serverId);
            long currentCount = server.incrementUnathorizedCount();
            if (currentCount == 1) {
                LOGGER.error("First unauthorized server event from {} with server ID {}", server.getServerAddress(), server.getServerId());
            } else if (currentCount % FREQUENCY_LOG_UNAUTHORIZED_EVENT == 0) {
                LOGGER.error("Unauthorized server event from {} with server ID {} and current unauthorized count {}", server.getServerAddress(), server.getServerId(), server.getUnathorizedCount());                
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to get unauthorizedServer cache entry", ex);
        }
    }

    public ContextAwareServiceServerStats getContextAwareServiceServerStats() {
        ContextAwareServiceServerStats stats = new ContextAwareServiceServerStats();
        long timeElapsedSec = (System.currentTimeMillis() - startTime) / 1000;
        if (timeElapsedSec == 0) {
            timeElapsedSec = 1;
        }
        
        if (locationEventLastUpdatedTime.get() == 0) {
            stats.setLocationEventLastUpdatedTime("Never");
        } else {
            stats.setLocationEventLastUpdatedTime(dateFormat.format(new Date(locationEventLastUpdatedTime.get())));
        }
        stats.setLocationEventsCount(locationEventsCount.get());
        stats.setLocationEventsCountPerSecond(locationEventsCount.get() / timeElapsedSec);
        
        if (associationEventLastUpdatedTime.get() == 0) {
            stats.setAssociationEventLastUpdatedTime("Never");
        } else {
            stats.setAssociationEventLastUpdatedTime(dateFormat.format(new Date(associationEventLastUpdatedTime.get())));
        }
        stats.setAssociationEventsCount(associationEventsCount.get());
        stats.setAssociationEventsCountPerSecond(associationEventsCount.get() / timeElapsedSec);

        if (unauthorizedEventLastUpdatedTime.get() == 0) {
            stats.setUnauthorizedEventLastUpdatedTime("Never");
        } else {
            stats.setUnauthorizedEventLastUpdatedTime(dateFormat.format(new Date(unauthorizedEventLastUpdatedTime.get())));
        }
        stats.setUnauthorizedEventsCount(unauthorizedEventsCount.get());
        stats.setUnauthorizedEventsCountPerSecond(unauthorizedEventsCount.get() / timeElapsedSec);
        
        ConcurrentMap<String, UnauthorizedServer> servers = unauthorizedServers.getAllUnauthorizedServers();
        if (servers != null && !servers.isEmpty()) {
            for (UnauthorizedServer server : servers.values()) {
                UnauthorizedServerStats serverStats = new UnauthorizedServerStats(server);
                serverStats.setUnauthorizedEventLastUpdatedTime(dateFormat.format(new Date(server.getLastUnauthorizedTime())));
                serverStats.setUnauthorizedCountPerSecond(serverStats.getUnauthorizedCount() / timeElapsedSec);
                stats.addUnauthorizedServerStats(serverStats);
            }
        }
        return stats;
    }

    public void dumpStats() {
        long timeElapsedSec = (System.currentTimeMillis() - startTime) / 1000;
        if (timeElapsedSec == 0) {
            timeElapsedSec = 1;
        }
        LOGGER.info("------- Context Aware Service Stats -------");
        LOGGER.info("Server Start Time: {}", startTimeDate);
        
        if (locationEventLastUpdatedTime.get() == 0) {
            LOGGER.info("Location Event Last Updated: Never");
        } else {
            LOGGER.info("Location Event Last Updated: {}", dateFormat.format(new Date(locationEventLastUpdatedTime.get())));
        }
        LOGGER.info("Total Location Events: {}", locationEventsCount.get());
        LOGGER.info("Location Events Per Second: {}", locationEventsCount.get() / timeElapsedSec);
        
        if (associationEventLastUpdatedTime.get() == 0) {
            LOGGER.info("Association Event Last Updated: Never");
        } else {
            LOGGER.info("Association Event Last Updated: {}", dateFormat.format(new Date(associationEventLastUpdatedTime.get())));
        }
        LOGGER.info("Total Association Events: {}", associationEventsCount.get());
        LOGGER.info("Association Events Per Second: {}", associationEventsCount.get() / timeElapsedSec);

        if (unauthorizedEventLastUpdatedTime.get() == 0) {
            LOGGER.info("Unauthorized Event Last Updated: Never");
        } else {
            LOGGER.info("Unauthorized Event Last Updated: {}", dateFormat.format(new Date(unauthorizedEventLastUpdatedTime.get())));
        }
        LOGGER.info("Total Unauthorized Events: {}", unauthorizedEventsCount.get());
        LOGGER.info("Unauthorized Events Per Second: {}", unauthorizedEventsCount.get() / timeElapsedSec);
        ConcurrentMap<String, UnauthorizedServer> servers = unauthorizedServers.getAllUnauthorizedServers();
        if (servers != null && !servers.isEmpty()) {
            for (UnauthorizedServer server : servers.values()) {
                LOGGER.info("");
                LOGGER.info("   ------ Unauthorized Server Info ------");        
                LOGGER.info("   Server Address                             : {}", server.getServerAddress());
                LOGGER.info("   Server ID                                  : {}", server.getServerId());
                LOGGER.info("   Unauthorized Event Last Updated            : {}", dateFormat.format(new Date(server.getLastUnauthorizedTime())));
                LOGGER.info("   Total Unauthorized Events                  : {}", server.getUnathorizedCount());
                LOGGER.info("   Unauthorized Events Per Second             : {}", server.getUnathorizedCount() / timeElapsedSec);
            }
        }
    }
}