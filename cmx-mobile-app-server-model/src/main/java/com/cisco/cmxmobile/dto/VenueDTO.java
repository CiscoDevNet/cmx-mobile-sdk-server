package com.cisco.cmxmobile.dto;

import java.util.List;

public class VenueDTO {

    private String venueId;
    private String name;
    private String streetAddress;
    private int locationUpdateInterval;
    private String wifiConnectionMode;
    private List<NetworkInfoDTO> preferredNetwork;
    private String imageType;
    private List<FloorInfoDTO> floors;
    
    public String getVenueId() {
        return venueId;
    }
    
    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getStreetAddress() {
        return streetAddress;
    }
    
    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }
    
    public int getLocationUpdateInterval() {
        return locationUpdateInterval;
    }
    
    public void setLocationUpdateInterval(int locationUpdateInterval) {
        this.locationUpdateInterval = locationUpdateInterval;
    }
    
    public String getWifiConnectionMode() {
        return wifiConnectionMode;
    }
    
    public void setWifiConnectionMode(String wifiConnectionMode) {
        this.wifiConnectionMode = wifiConnectionMode;
    }
    
    public List<NetworkInfoDTO> getPreferredNetwork() {
        return preferredNetwork;
    }
    
    public void setPreferredNetwork(List<NetworkInfoDTO> preferredNetwork) {
        this.preferredNetwork = preferredNetwork;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public List<FloorInfoDTO> getFloors() {
        return floors;
    }

    public void setFloors(List<FloorInfoDTO> floors) {
        this.floors = floors;
    }
}
