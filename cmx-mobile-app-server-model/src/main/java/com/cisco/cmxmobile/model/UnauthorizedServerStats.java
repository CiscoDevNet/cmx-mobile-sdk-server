package com.cisco.cmxmobile.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnauthorizedServerStats {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnauthorizedServerStats.class);
            
    private String serverAddress;
    
    private String serverId;
    
    private long unauthorizedCount;

    private long unauthorizedCountPerSecond;

    private String lastUnauthorizedTime;

    public UnauthorizedServerStats() {
        this.unauthorizedCount = 0;
        this.unauthorizedCountPerSecond = 0;
    }
    
    public UnauthorizedServerStats(UnauthorizedServer server) {
        this.serverAddress = server.getServerAddress();
        this.serverId = server.getServerId();
        this.unauthorizedCount = server.getUnathorizedCount();
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public long getUnauthorizedCount() {
        return unauthorizedCount;
    }

    public void setUnauthorizedCount(long unauthorizedCount) {
        this.unauthorizedCount = unauthorizedCount;
    }

    public long getUnauthorizedCountPerSecond() {
        return unauthorizedCountPerSecond;
    }

    public void setUnauthorizedCountPerSecond(long unauthorizedCountPerSecond) {
        this.unauthorizedCountPerSecond = unauthorizedCountPerSecond;
    }

    public String getUnauthorizedEventLastUpdatedTime() {
        return lastUnauthorizedTime;
    }

    public void setUnauthorizedEventLastUpdatedTime(String lastUnauthorizedTime) {
        this.lastUnauthorizedTime = lastUnauthorizedTime;
    }
    
    public void logStats() {
        LOGGER.info("");
        LOGGER.info("------ Unauthorized Server Info ------");        
        LOGGER.info("Server Address                             : {}", this.serverAddress);
        LOGGER.info("Server ID                                  : {}", this.serverId);
        LOGGER.info("Unauthorized Event Last Updated            : {}", this.lastUnauthorizedTime);
        LOGGER.info("Total Unauthorized Events                  : {}", this.unauthorizedCount);
        LOGGER.info("Unauthorized Events Per Second             : {}", this.unauthorizedCountPerSecond);
    }
}