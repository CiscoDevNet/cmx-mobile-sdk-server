package com.cisco.cmxmobile.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.cisco.cmxmobile.cacheService.client.annotations.Key;
import com.cisco.cmxmobile.cacheService.client.annotations.MaskLogEntry;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Ssid {
    // TOD0: Parent Keys need to be refactored out
    // Parent Keys
    @Key(index = 1)
    private String venueUdId;

    // Keys
    @Key(index = 2)
    private String ssid;

    @MaskLogEntry
    private String password = "";

    public static final String VENUE_UDID = "venueUdId";

    public String getVenueUdId() {
        return venueUdId;
    }

    public void setVenueUdId(String venueUdId) {
        this.venueUdId = venueUdId;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}