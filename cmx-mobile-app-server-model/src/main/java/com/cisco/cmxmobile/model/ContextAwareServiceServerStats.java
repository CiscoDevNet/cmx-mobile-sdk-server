package com.cisco.cmxmobile.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextAwareServiceServerStats {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextAwareServiceServerStats.class);
            
    private long locationEventsCount;

    private long associationEventsCount;

    private long unauthorizedEventsCount;

    private long locationEventsCountPerSecond;

    private long unauthorizedEventsCountPerSecond;

    private long associationEventsCountPerSecond;

    private String locationEventLastUpdatedTime;

    private String associationEventLastUpdatedTime;

    private String unauthorizedEventLastUpdatedTime;
    
    private List<UnauthorizedServerStats> unauthorizedServers;
    
    public ContextAwareServiceServerStats() {
        unauthorizedServers = new ArrayList<UnauthorizedServerStats>();
    }

    public long getLocationEventsCount() {
        return locationEventsCount;
    }

    public void setLocationEventsCount(long locationEventsCount) {
        this.locationEventsCount = locationEventsCount;
    }

    public long getAssociationEventsCount() {
        return associationEventsCount;
    }

    public void setAssociationEventsCount(long associationEventsCount) {
        this.associationEventsCount = associationEventsCount;
    }

    public long getLocationEventsCountPerSecond() {
        return locationEventsCountPerSecond;
    }

    public void setLocationEventsCountPerSecond(long locationEventsCountPerSecond) {
        this.locationEventsCountPerSecond = locationEventsCountPerSecond;
    }

    public long getAssociationEventsCountPerSecond() {
        return associationEventsCountPerSecond;
    }

    public void setAssociationEventsCountPerSecond(long associationEventsCountPerSecond) {
        this.associationEventsCountPerSecond = associationEventsCountPerSecond;
    }

    public String getLocationEventLastUpdatedTime() {
        return locationEventLastUpdatedTime;
    }

    public void setLocationEventLastUpdatedTime(String locationEventLastUpdatedTime) {
        this.locationEventLastUpdatedTime = locationEventLastUpdatedTime;
    }

    public String getAssociationEventLastUpdatedTime() {
        return associationEventLastUpdatedTime;
    }

    public void setAssociationEventLastUpdatedTime(String associationEventLastUpdatedTime) {
        this.associationEventLastUpdatedTime = associationEventLastUpdatedTime;
    }

    public long getUnauthorizedEventsCount() {
        return unauthorizedEventsCount;
    }

    public void setUnauthorizedEventsCount(long unauthorizedEventsCount) {
        this.unauthorizedEventsCount = unauthorizedEventsCount;
    }

    public long getUnauthorizedEventsCountPerSecond() {
        return unauthorizedEventsCountPerSecond;
    }

    public void setUnauthorizedEventsCountPerSecond(long unauthorizedEventsCountPerSecond) {
        this.unauthorizedEventsCountPerSecond = unauthorizedEventsCountPerSecond;
    }

    public String getUnauthorizedEventLastUpdatedTime() {
        return unauthorizedEventLastUpdatedTime;
    }

    public void setUnauthorizedEventLastUpdatedTime(String unauthorizedEventLastUpdatedTime) {
        this.unauthorizedEventLastUpdatedTime = unauthorizedEventLastUpdatedTime;
    }

    public List<UnauthorizedServerStats> getUnauthorizedServers() {
        return unauthorizedServers;
    }

    public void setUnauthorizedServers(List<UnauthorizedServerStats> unauthorizedServers) {
        this.unauthorizedServers = unauthorizedServers;
    }

    public void addUnauthorizedServerStats(UnauthorizedServerStats server) {
        unauthorizedServers.add(server);
    }
    
    public void logStats() {
        LOGGER.info("");
        LOGGER.info("------- Context Aware Service Stats --------");        
        LOGGER.info("Location Event Last Updated                : {}", this.locationEventLastUpdatedTime);
        LOGGER.info("Total Location Events                      : {}", this.locationEventsCount);
        LOGGER.info("Location Events Per Second                 : {}", this.locationEventsCountPerSecond);
        LOGGER.info("");
        
        LOGGER.info("Association Event Last Updated             : {}", this.associationEventLastUpdatedTime);
        LOGGER.info("Total Association Events                   : {}", this.associationEventsCount);
        LOGGER.info("Association Events Per Second              : {}", this.associationEventsCountPerSecond);
        LOGGER.info("");
        
        LOGGER.info("Unauthorized Event Last Updated            : {}", this.unauthorizedEventLastUpdatedTime);
        LOGGER.info("Total Unauthorized Events                  : {}", this.unauthorizedEventsCount);
        LOGGER.info("Unauthorized Events Per Second             : {}", this.unauthorizedEventsCountPerSecond);
        if (unauthorizedServers != null && !unauthorizedServers.isEmpty()) {
            for (UnauthorizedServerStats server : unauthorizedServers) {
                LOGGER.info("");
                LOGGER.info("   ------ Unauthorized Server Info ------");        
                LOGGER.info("   Server Address                             : {}", server.getServerAddress());
                LOGGER.info("   Server ID                                  : {}", server.getServerId());
                LOGGER.info("   Unauthorized Event Last Updated            : {}", server.getUnauthorizedEventLastUpdatedTime());
                LOGGER.info("   Total Unauthorized Events                  : {}", server.getUnauthorizedCount());
                LOGGER.info("   Unauthorized Events Per Second             : {}", server.getUnauthorizedCountPerSecond());
            }
        }
    }
}