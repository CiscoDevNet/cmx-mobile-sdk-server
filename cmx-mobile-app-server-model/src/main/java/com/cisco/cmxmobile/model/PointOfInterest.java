package com.cisco.cmxmobile.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.cisco.cmxmobile.cacheService.client.annotations.Key;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PointOfInterest {
    // TOD0: Parent Keys need to be refactored out
    // Parent Keys
    @Key(index = 1)
    private String venueUdId;

    @Key(index = 2)
    private String mseFloorId;

    // Key
    @Key(index = 3)
    private String id;

    // Attributes
    private float x;

    private float y;

    private String mseUdId;

    private String mseVenueId;

    private String description;

    private String name;

    private String status;

    private String keywords;

    private String updateDispDate;

    private String createDispDate;

    private String address;

    private String country;

    private String email;

    public static final String VENUE_UDID = "venueUdId";

    public static final String MSE_FLOORID = "mseFloorId";

    public static final String ID = "id";

    public String getVenueUdId() {
        return venueUdId;
    }

    public void setVenueUdId(String venueUdId) {
        this.venueUdId = venueUdId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getUpdateDispDate() {
        return updateDispDate;
    }

    public void setUpdateDispDate(String updateDispDate) {
        this.updateDispDate = updateDispDate;
    }

    public String getCreateDispDate() {
        return createDispDate;
    }

    public void setCreateDispDate(String createDispDate) {
        this.createDispDate = createDispDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

}
