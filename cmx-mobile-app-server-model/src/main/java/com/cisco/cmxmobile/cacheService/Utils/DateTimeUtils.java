package com.cisco.cmxmobile.cacheService.Utils;

public final class DateTimeUtils {

    private DateTimeUtils() {
    }

    public static long getTimeDiffInMinutes(long lastTimeinMilies) {
        return (System.currentTimeMillis() - lastTimeinMilies) / (60 * 1000) % 60;
    }

}
