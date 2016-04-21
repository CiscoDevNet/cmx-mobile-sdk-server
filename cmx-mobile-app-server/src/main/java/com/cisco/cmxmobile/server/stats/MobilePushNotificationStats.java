package com.cisco.cmxmobile.server.stats;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.cmxmobile.model.MobilePushNotificationServerStats;

import java.util.concurrent.atomic.AtomicLong;

public final class MobilePushNotificationStats {

    private static MobilePushNotificationStats instance = new MobilePushNotificationStats();

    private static final Logger LOGGER = LoggerFactory.getLogger(MobilePushNotificationStats.class);

    private final AtomicLong applePushNotificationsCount = new AtomicLong();

    private final AtomicLong androidPushNotificationsCount = new AtomicLong();

    private final AtomicLong applePushNotificationLastUpdatedTime = new AtomicLong();

    private final AtomicLong androidPushNotificationLastUpdatedTime = new AtomicLong();

    private final DateFormat dateFormat = new SimpleDateFormat("dd MMM YYYY HH:mm:ss");
    
    private long startTime;

    private String startTimeDate;

    private MobilePushNotificationStats() {
        resetAllCounters();
        resetStartDate();
    }

    public static MobilePushNotificationStats getInstance() {
        return instance;
    }

    private void resetStartDate() {
        startTime = System.currentTimeMillis();
        startTimeDate = dateFormat.format(new Date(System.currentTimeMillis()));
    }

    private void resetAllCounters() {
        applePushNotificationsCount.set(0);
        androidPushNotificationsCount.set(0);
        applePushNotificationLastUpdatedTime.set(0);
        androidPushNotificationLastUpdatedTime.set(0);
    }
    
    public void resetAllStats() {
        resetAllCounters();
        resetStartDate();
    }

    public void incrementApplePushNotificationsCount() {
        applePushNotificationsCount.incrementAndGet();
        applePushNotificationLastUpdatedTime.set(System.currentTimeMillis());
    }

    public void incrementAndroidPushNotificationsCount() {
        androidPushNotificationsCount.incrementAndGet();
        androidPushNotificationLastUpdatedTime.set(System.currentTimeMillis());
    }

    public MobilePushNotificationServerStats getMobilePushNotificationServiceStats() {
        MobilePushNotificationServerStats stats = new MobilePushNotificationServerStats();
        long timeElapsedSec = (System.currentTimeMillis() - startTime) / 1000;
        if (timeElapsedSec == 0) {
            timeElapsedSec = 1;
        }
        
        if (applePushNotificationLastUpdatedTime.get() == 0) {
            stats.setApplePushNotificationLastUpdatedTime("Never");
        } else {
            stats.setApplePushNotificationLastUpdatedTime(dateFormat.format(new Date(applePushNotificationLastUpdatedTime.get())));
        }
        stats.setApplePushNotificationsCount(applePushNotificationsCount.get());
        stats.setApplePushNotificationsCountPerSecond(applePushNotificationsCount.get() / timeElapsedSec);
        
        if (androidPushNotificationLastUpdatedTime.get() == 0) {
            stats.setAndroidPushNotificationLastUpdatedTime("Never");
        } else {
            stats.setAndroidPushNotificationLastUpdatedTime(dateFormat.format(new Date(androidPushNotificationLastUpdatedTime.get())));
        }
        stats.setAndroidPushNotificationsCount(androidPushNotificationsCount.get());
        stats.setAndroidPushNotificationsCountPerSecond(androidPushNotificationsCount.get() / timeElapsedSec);
        return stats;
    }

    public void dumpStats() {
        long timeElapsedSec = (System.currentTimeMillis() - startTime) / 1000;
        if (timeElapsedSec == 0) {
            timeElapsedSec = 1;
        }
        LOGGER.info("------ Mobile Push Notification Stats -----");
        LOGGER.info("Server Start Time: {}", startTimeDate);
        
        if (applePushNotificationLastUpdatedTime.get() == 0) {
            LOGGER.info("Apple Push Notification Last Sent: Never");
        } else {
            LOGGER.info("Apple Push Notification Last Sent: {}", dateFormat.format(new Date(applePushNotificationLastUpdatedTime.get())));
        }
        LOGGER.info("Total Apple Push Notifications: {}", applePushNotificationsCount.get());
        LOGGER.info("Apple Push Notifications Per Second: {}", applePushNotificationsCount.get() / timeElapsedSec);
        
        if (androidPushNotificationLastUpdatedTime.get() == 0) {
            LOGGER.info("Android Push Notification Last Sent: Never");
        } else {
            LOGGER.info("Android Push Notification Last Sent: {}", dateFormat.format(new Date(androidPushNotificationLastUpdatedTime.get())));
        }
        LOGGER.info("Total Android Push Notifications: {}", androidPushNotificationsCount.get());
        LOGGER.info("Android Push Notifications Per Second: {}", androidPushNotificationsCount.get() / timeElapsedSec);
    }
}