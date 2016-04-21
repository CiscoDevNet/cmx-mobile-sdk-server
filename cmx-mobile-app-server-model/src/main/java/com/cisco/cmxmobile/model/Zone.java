package com.cisco.cmxmobile.model;

import java.util.List;

import com.cisco.cmxmobile.cacheService.client.annotations.Key;
import com.cisco.cmxmobile.cacheService.client.annotations.PersistableStrategy;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Zone {

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
    private String mseUdId;

    private String mseVenueId;

    private String name;

    private String description;

    private String tags;

    private String points;

    private long campusid;

    private long venueid;

    private long floorid;

    private String categories;

    private String domainMapping;

    private String aps;

    private String pushNotificationMessage;

    private String createdDate;

    private String updatedDate;

    boolean importFromPI;

    public static final String VENUE_UDID = "venueUdId";

    public static final String MSE_FLOORID = "mseFloorId";

    public static final String ID = "id";
    
    @PersistableStrategy(AsJson = true) 
    private List<String> campaignIdList;

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

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public long getCampusid() {
        return campusid;
    }

    public void setCampusid(long campusid) {
        this.campusid = campusid;
    }

    public long getVenueid() {
        return venueid;
    }

    public void setVenueid(long venueid) {
        this.venueid = venueid;
    }

    public long getFloorid() {
        return floorid;
    }

    public void setFloorid(long floorid) {
        this.floorid = floorid;
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

    public String getAps() {
        return aps;
    }

    public void setAps(String aps) {
        this.aps = aps;
    }

    public String getPushNotificationMessage() {
        return pushNotificationMessage;
    }

    public void setPushNotificationMessage(String pushNotificationMessage) {
        this.pushNotificationMessage = pushNotificationMessage;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public boolean isImportFromPI() {
        return importFromPI;
    }

    public void setImportFromPI(boolean importFromPI) {
        this.importFromPI = importFromPI;
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
    
    public List<String> getCampaignIdList() {
        return campaignIdList;
    }

    public void setCampaignIdList(List<String> campaignIdList) {
        this.campaignIdList = campaignIdList;
    }

}
