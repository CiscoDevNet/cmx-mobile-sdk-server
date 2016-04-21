package com.cisco.cmxmobile.model;

import java.util.concurrent.atomic.AtomicLong;

public class UnauthorizedServer {

    private String serverAddress;
    
    private String serverId;
    
    private final AtomicLong unathorizedCount;

    private final AtomicLong lastUnauthorizedTime;

    public UnauthorizedServer() {
        unathorizedCount = new AtomicLong();
        lastUnauthorizedTime = new AtomicLong();
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

    public long getUnathorizedCount() {
        return unathorizedCount.get();
    }

    public long getLastUnauthorizedTime() {
        return lastUnauthorizedTime.get();
    }

    public long incrementUnathorizedCount() {
        lastUnauthorizedTime.set(System.currentTimeMillis());
        return unathorizedCount.incrementAndGet();
    }
}