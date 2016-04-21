package com.cisco.cmxmobile.model;

import com.cisco.cmxmobile.cacheService.client.annotations.Key;

public class FloorPathInfo {
    // Parent Keys
    // Keys
    @Key(index = 1)
    private String venueUdId;

    @Key(index = 2)
    private String mseFloorId;

    // Key
    @Key(index = 4)
    private String id;

    private String mseUdId;

    private String mseVenueId;

    private String floorPathInfo;

    // Access Keys
    public static final String VENUE_UDID = "venueUdId";

    public static final String MSE_FLOORID = "mseFloorId";

    public static final String ID = "id";

    public String getVenueUdId() {
        return venueUdId;
    }

    public void setVenueUdId(String venueUdId) {
        this.venueUdId = venueUdId;
    }

    public String getFloorPathInfo() {
        return floorPathInfo;
    }

    public void setFloorPathInfo(String floorPathInfo) {
        this.floorPathInfo = floorPathInfo;
    }

    public String getMseUdId() {
        return mseUdId;
    }

    public void setMseUdId(String mseUdId) {
        this.mseUdId = mseUdId;
    }

    public String getMseVenueId() {
        return mseVenueId;
    }

    public void setMseVenueId(String mseVenueId) {
        this.mseVenueId = mseVenueId;
    }

    public String getMseFloorId() {
        return mseFloorId;
    }

    public void setMseFloorId(String mseFloorId) {
        this.mseFloorId = mseFloorId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
