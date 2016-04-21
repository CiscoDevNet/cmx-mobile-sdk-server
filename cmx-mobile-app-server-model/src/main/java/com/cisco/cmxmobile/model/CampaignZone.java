package com.cisco.cmxmobile.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CampaignZone 
{
    // Keys
    private String venueUdId;
    private String mseFloorId;
    private long id;
    
    public String getVenueUdId() {
        return venueUdId;
    }
    
    public void setVenueUdId(String venueUdId) {
        this.venueUdId = venueUdId;
    }
    
    public String getMseFloorId() {
        return mseFloorId;
    }
    
    public void setMseFloorId(String mseFloorId) {
        this.mseFloorId = mseFloorId;
    }
    
    public long getId() {
        return id;
    }
    
    public void setid(long id) {
        this.id = id;
    }
}
