package com.cisco.cmxmobile.dto;

public class BannerDTO 
{
    private String zoneid;
    private String id;
    private String name;
    private String imageType;
    private String imageUri;
    private String venueid;
    private String targetUrl;
    
    public String getZoneid() {
        return zoneid;
    }
    public void setZoneid(String zoneid) {
        this.zoneid = zoneid;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getImageType() {
        return imageType;
    }
    public void setImageType(String imageType) {
        this.imageType = imageType;
    }
    public String getImageUri() {
        return imageUri;
    }
    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
    public String getVenueid() {
        return venueid;
    }
    public void setVenueid(String venueid) {
        this.venueid = venueid;
    }
    public String getTargetUrl() {
        return targetUrl;
    }
    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

}
