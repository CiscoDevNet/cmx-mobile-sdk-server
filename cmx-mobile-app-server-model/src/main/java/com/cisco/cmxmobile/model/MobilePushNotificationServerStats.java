package com.cisco.cmxmobile.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MobilePushNotificationServerStats {

    private static final Logger LOGGER = LoggerFactory.getLogger(MobilePushNotificationServerStats.class);
            
    private long applePushNotificationsCount;

    private long androidPushNotificationsCount;

    private long applePushNotificationsCountPerSecond;

    private long androidPushNotificationsCountPerSecond;

    private String applePushNotificationLastUpdatedTime;

    private String androidPushNotificationLastUpdatedTime;

    public long getApplePushNotificationsCount() {
        return applePushNotificationsCount;
    }

    public void setApplePushNotificationsCount(long applePushNotificationsCount) {
        this.applePushNotificationsCount = applePushNotificationsCount;
    }

    public long getAndroidPushNotificationsCount() {
        return androidPushNotificationsCount;
    }

    public void setAndroidPushNotificationsCount(long androidPushNotificationsCount) {
        this.androidPushNotificationsCount = androidPushNotificationsCount;
    }

    public long getApplePushNotificationsCountPerSecond() {
        return applePushNotificationsCountPerSecond;
    }

    public void setApplePushNotificationsCountPerSecond(long applePushNotificationsCountPerSecond) {
        this.applePushNotificationsCountPerSecond = applePushNotificationsCountPerSecond;
    }

    public long getAndroidPushNotificationsCountPerSecond() {
        return androidPushNotificationsCountPerSecond;
    }

    public void setAndroidPushNotificationsCountPerSecond(long androidPushNotificationsCountPerSecond) {
        this.androidPushNotificationsCountPerSecond = androidPushNotificationsCountPerSecond;
    }

    public String getApplePushNotificationLastUpdatedTime() {
        return applePushNotificationLastUpdatedTime;
    }

    public void setApplePushNotificationLastUpdatedTime(String applePushNotificationLastUpdatedTime) {
        this.applePushNotificationLastUpdatedTime = applePushNotificationLastUpdatedTime;
    }

    public String getAndroidPushNotificationLastUpdatedTime() {
        return androidPushNotificationLastUpdatedTime;
    }

    public void setAndroidPushNotificationLastUpdatedTime(String androidPushNotificationLastUpdatedTime) {
        this.androidPushNotificationLastUpdatedTime = androidPushNotificationLastUpdatedTime;
    }

    public void logStats() {
        LOGGER.info("");
        LOGGER.info("------ Mobile Push Notification Stats ------");        
        LOGGER.info("Apple Push Notification Last Sent          : {}", this.applePushNotificationLastUpdatedTime);
        LOGGER.info("Total Apple Push Notifications             : {}", this.applePushNotificationsCount);
        LOGGER.info("Apple Push Notifications Per Second        : {}", this.applePushNotificationsCountPerSecond);
        LOGGER.info("");
        
        LOGGER.info("Android Push Notification Last Sent        : {}", this.androidPushNotificationLastUpdatedTime);
        LOGGER.info("Total Android Push Notifications           : {}", this.androidPushNotificationsCount);
        LOGGER.info("Android Push Notifications Per Second      : {}", this.androidPushNotificationsCountPerSecond);
    }
}