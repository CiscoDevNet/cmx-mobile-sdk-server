package com.cisco.cmxmobile.model;

import java.util.HashMap;
import java.util.Map;

public enum DeviceType {
    ANDROID("android"), IOS("ios"), IOS6("ios6");
    private final String mType;

    private static final Map<String, DeviceType> STRING_TO_ENUM = new HashMap<String, DeviceType>();
    static {
        for (DeviceType dt : values()) {
            STRING_TO_ENUM.put(dt.toString(), dt);
        }
    }

    private DeviceType(String type) {
        mType = type;
    }

    @Override
    public String toString() {
        return mType;
    }

    public static DeviceType fromString(String in) {
        return STRING_TO_ENUM.get(in.toLowerCase());
    }
}
