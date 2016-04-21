package com.cisco.cmxmobile.model;

import com.cisco.cmxmobile.cacheService.client.annotations.Key;

/*
 * We don't store the location in the pending association object, since we don't
 * want to store more than we have to about users who haven't opted-in yet. This
 * might make it a bit of a bother for users who enter a venue, register their
 * app, and expect to see their location right away: the cloud server may not
 * have received a movement event (or other such location-containing event)
 * since the registration completed.
 */
public class PendingAssociationNotification {
    @Key(index = 1)
    private String apMAC;

    // TODO: verify that it's an actual IP
    @Key(index = 2)
    private String clientIP;

    // TODO: verify that it's an actual MAC address
    private String clientMAC;
    
    private long lastUpdateTime;

    public static final String CLIENT_MAC = "clientMAC";

    public static final String AP_MAC = "apMAC";

    public static final String CLIENT_IP = "clientIP";

    public String getApMAC() {
        return apMAC;
    }

    public void setApMAC(String apMAC) {
        this.apMAC = apMAC;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public String getClientMAC() {
        return clientMAC;
    }

    public void setClientMAC(String clientMAC) {
        this.clientMAC = clientMAC;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String toString() {
        return "AssociationNotification waiting for registration request: " + "apMAC:" + apMAC + " clientIP: " + clientIP + " clientMAC: " + clientMAC;
    }

}
