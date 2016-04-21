package com.cisco.cmxmobile.model;

import java.util.List;

import com.cisco.cmxmobile.cacheService.client.annotations.Key;

public class CasVenue 
{
    @Key(index = 1)
    private String venueUdId;
    
    private String appId;
    
    private String venueName;    
    
    private String venueId;
    
    private String mseUdid;

    private List<CasFloorInfo> casFloorInfoList;
    
    public static final String CAS_VENUE_UDID = "venueUdId";

    public String getVenueUdId() {
        return venueUdId;
    }

    public void setVenueUdId(String venueUdId) {
        this.venueUdId = venueUdId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }
    
    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }

    public String getMseUdid() {
        return mseUdid;
    }

    public void setMseUdid(String mseUdid) {
        this.mseUdid = mseUdid;
    }

    public List<CasFloorInfo> getCasFloorInfoList() {
        return casFloorInfoList;
    }

    public void setCasFloorInfoList(List<CasFloorInfo> casFloorInfoList) {
        this.casFloorInfoList = casFloorInfoList;
    }

}
