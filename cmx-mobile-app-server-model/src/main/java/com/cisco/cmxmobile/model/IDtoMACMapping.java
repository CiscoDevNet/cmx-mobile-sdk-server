package com.cisco.cmxmobile.model;

import java.util.UUID;

import com.cisco.cmxmobile.cacheService.client.annotations.Key;

public class IDtoMACMapping {

    public static final String ID = "id";

    @Key(index = 1)
    private UUID id;

    private String macAddress;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}
