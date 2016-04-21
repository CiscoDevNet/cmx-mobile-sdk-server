package com.cisco.cmxmobile.model;

public class ServerStats {

    private ConnectAndEngageServerStats connectAndEngageServerStats;
    
    private ContextAwareServiceServerStats contextAwareServiceServerStats;
    
    private MobileAppServerStats mobileAppServerStats;
    
    private MobilePushNotificationServerStats mobilePushNotificationServerStats;

    public ConnectAndEngageServerStats getConnectAndEngageServerStats() {
        return connectAndEngageServerStats;
    }

    public void setConnectAndEngageServerStats(ConnectAndEngageServerStats connectAndEngageServerStats) {
        this.connectAndEngageServerStats = connectAndEngageServerStats;
    }

    public ContextAwareServiceServerStats getContextAwareServiceServerStats() {
        return contextAwareServiceServerStats;
    }

    public void setContextAwareServiceServerStats(ContextAwareServiceServerStats contextAwareServiceServerStats) {
        this.contextAwareServiceServerStats = contextAwareServiceServerStats;
    }

    public MobileAppServerStats getMobileAppServerStats() {
        return mobileAppServerStats;
    }

    public void setMobileAppServerStats(MobileAppServerStats mobileAppServerStats) {
        this.mobileAppServerStats = mobileAppServerStats;
    }
    
    public MobilePushNotificationServerStats getMobilePushNotificationServerStats() {
        return mobilePushNotificationServerStats;
    }

    public void setMobilePushNotificationServerStats(MobilePushNotificationServerStats mobilePushNotificationServerStats) {
        this.mobilePushNotificationServerStats = mobilePushNotificationServerStats;
    }

    public void logStats() {
        this.contextAwareServiceServerStats.logStats();
        this.mobileAppServerStats.logStats();
        this.mobilePushNotificationServerStats.logStats();
        this.connectAndEngageServerStats.logStats();
    }
}
