package com.cisco.cmxmobile.model;

import java.util.ArrayList;
import java.util.List;

import com.cisco.cmxmobile.cacheService.client.annotations.Key;
import com.cisco.cmxmobile.cacheService.client.annotations.PersistableStrategy;

public class UsertoMACMapping {

    public static final String USER_ID = "userId";

    @Key(index = 1)
    private String userId;

    @PersistableStrategy(AsJson = true)
    private List<String> macAddressList;
    
    private String venueUdId;

    private String floorId;

    private float xCoordinate;

    private float yCoordinate;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getMacAddressList() {
        return macAddressList;
    }

    public void setMacAddressList(List<String> macAddressList) {
        this.macAddressList = macAddressList;
    }
    
    public void addMacAddress(String macAddress) {
        if (this.macAddressList == null) {
            this.macAddressList = new ArrayList<String>();
        }
        if (!this.macAddressList.contains(macAddress)) {
            this.macAddressList.add(macAddress);
        }
    }
    
    public String getVenueUdId() {
        return venueUdId;
    }

    public void setVenueUdId(String venueUdId) {
        this.venueUdId = venueUdId;
    }

    public String getFloorId() {
        return floorId;
    }

    public void setFloorId(String floorId) {
        this.floorId = floorId;
    }

    public float getX() {
        return xCoordinate;
    }

    public void setX(float x) {
        xCoordinate = x;
    }

    public float getY() {
        return yCoordinate;
    }

    public void setY(float y) {
        yCoordinate = y;
    }

}
