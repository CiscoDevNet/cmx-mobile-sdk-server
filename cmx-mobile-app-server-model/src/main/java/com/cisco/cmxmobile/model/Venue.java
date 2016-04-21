package com.cisco.cmxmobile.model;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.cisco.cmxmobile.cacheService.client.annotations.Key;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Venue {
    // Keys
    @Key(index = 1)
    private String venueUdId;

    // BX ID
    private long id;

    private String mseUdId;

    private String mseVenueId;

    private String name;

    private String description;

    private String tags;

    private String address;

    private long campusid;

    private String categories;

    private String domainMapping;

    private String lat;

    private String lon;

    private long createdDate;

    private long updatedDate;

    private String mseIP;

    private List<Floor> floorList;
    
    private List<Ssid> ssidList;

    private String applePushNotificationKey;

    private String androidPushNotificationKey;

    private String applePushNotificationFile;
    
    private boolean appleProductionServer;
    
    private int locationUpdateInterval;
    
    private String wifiConnectionMode;
    
    private String pushNotificationMessage;
    
    private String authToken;

    public static final String VENUE_UDID = "venueUdId";

    public String getVenueUdId() {
        return venueUdId;
    }

    public void setVenueUdId(String venueUdId) {
        this.venueUdId = venueUdId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getCampusid() {
        return campusid;
    }

    public void setCampusid(long campusid) {
        this.campusid = campusid;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public String getDomainMapping() {
        return domainMapping;
    }

    public void setDomainMapping(String domainMapping) {
        this.domainMapping = domainMapping;
    }

    public String getMseVenueId() {
        return mseVenueId;
    }

    public void setMseVenueId(String mseVenueId) {
        this.mseVenueId = mseVenueId;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    /*
     * public String getCreatedBy() { return createdBy; } public void
     * setCreatedBy(String createdBy) { this.createdBy = createdBy; }
     */
    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    /*
     * public String getUpdatedBy() { return updatedBy; } public void
     * setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
     */
    public long getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(long updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getMseIP() {
        return mseIP;
    }

    public void setMseIP(String mseIP) {
        this.mseIP = mseIP;
    }

    public List<Floor> getFloorList() {
        return floorList;
    }

    public void setFloorList(List<Floor> floorList) {
        this.floorList = floorList;
    }

    public Floor getFloorById(String floorId) {
        for (Floor floor : this.floorList) {
            if (Long.toString(floor.getId()).equals(floorId)) {
                return floor;
            }
        }
        return null;
    }

    public List<Ssid> getSsidList() {
        return ssidList;
    }

    public void setSsidList(List<Ssid> ssidList) {
        this.ssidList = ssidList;
    }

    public String getMseUdId() {
        return mseUdId;
    }

    public void setMseUdId(String mseUdId) {
        this.mseUdId = mseUdId;
    }

    public String getApplePushNotificationKey() {
        return applePushNotificationKey;
    }

    public void setApplePushNotificationKey(String applePushNotificationKey) {
        this.applePushNotificationKey = applePushNotificationKey;
    }

    public String getAndroidPushNotificationKey() {
        return androidPushNotificationKey;
    }

    public void setAndroidPushNotificationKey(String androidPushNotificationKey) {
        this.androidPushNotificationKey = androidPushNotificationKey;
    }

    public String getApplePushNotificationFile() {
        return applePushNotificationFile;
    }

    public void setApplePushNotificationFile(String applePushNotificationFile) {
        this.applePushNotificationFile = applePushNotificationFile;
    }

    public boolean getAppleProductionServer() {
        return appleProductionServer;
    }

    public void setAppleProductionServer(boolean appleProductionServer) {
        this.appleProductionServer = appleProductionServer;
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

    public String getPushNotificationMessage() {
        return pushNotificationMessage;
    }

    public void setPushNotificationMessage(String pushNotificationMessage) {
        this.pushNotificationMessage = pushNotificationMessage;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }


}
