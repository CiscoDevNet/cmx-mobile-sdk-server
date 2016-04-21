package com.cisco.cmxmobile.model;

import com.cisco.cmxmobile.cacheService.client.annotations.Key;

public class CasFloorInfo 
{
    @Key(index = 1)
    private String venueUdid;

    @Key(index = 2)
    private String aesUid;
    
    private String floorName;

    private String appId;
    private String mseUdi;
    private String venueId;
    private String venueName;
    
    private int width;
    private int length;
    private int height;
    private String imageName;
    private String imageType;

    public static final String CAS_VENUE_UDID = "venueUdid";
    public static final String CAS_FLOOR_ID = "aesUid";

    public String getVenueUdid() {
        return venueUdid;
    }

    public void setVenueUdid(String venueUdid) {
        this.venueUdid = venueUdid;
    }

    public String getMseUdi() {
        return mseUdi;
    }

    public void setMseUdi(String mseUdi) {
        this.mseUdi = mseUdi;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAesUid() {
        return aesUid;
    }

    public void setAesUid(String aesUid) {
        this.aesUid = aesUid;
    }
   
    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }
    
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public String getFloorName() {
        return floorName;
    }

    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }


}
