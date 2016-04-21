package com.cisco.cmxmobile.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MobileAppServerStats {

    private static final Logger LOGGER = LoggerFactory.getLogger(MobileAppServerStats.class);
    
    private long locationRequestsCount;

    private long registerRequestsCount;

    private long routeRequestsCount;

    private long mapRequestsCount;

    private long poiRequestsCount;

    private long campaignRequestsCount;

    private long venueRequestsCount;

    private long locationRequestsCountPerSecond;

    private long registerRequestsCountPerSecond;

    private long routeRequestsCountPerSecond;

    private long mapRequestsCountPerSecond;

    private long poiRequestsCountPerSecond;

    private long campaignRequestsCountPerSecond;

    private long venueRequestsCountPerSecond;

    private String locationRequestLastTime;

    private String registerRequestLastTime;

    private String routeRequestLastTime;

    private String mapRequestLastTime;

    private String poiRequestLastTime;

    private String campaignRequestLastTime;
    
    private String venueRequestLastTime;

    public long getLocationRequestsCount() {
        return locationRequestsCount;
    }

    public void setLocationRequestsCount(long locationRequestsCount) {
        this.locationRequestsCount = locationRequestsCount;
    }

    public long getRegisterRequestsCount() {
        return registerRequestsCount;
    }

    public void setRegisterRequestsCount(long registerRequestsCount) {
        this.registerRequestsCount = registerRequestsCount;
    }

    public long getRouteRequestsCount() {
        return routeRequestsCount;
    }

    public void setRouteRequestsCount(long routeRequestsCount) {
        this.routeRequestsCount = routeRequestsCount;
    }

    public long getMapRequestsCount() {
        return mapRequestsCount;
    }

    public void setMapRequestsCount(long mapRequestsCount) {
        this.mapRequestsCount = mapRequestsCount;
    }

    public long getPoiRequestsCount() {
        return poiRequestsCount;
    }

    public void setPoiRequestsCount(long poiRequestsCount) {
        this.poiRequestsCount = poiRequestsCount;
    }

    public long getCampaignRequestsCount() {
        return campaignRequestsCount;
    }

    public void setCampaignRequestsCount(long campaignRequestsCount) {
        this.campaignRequestsCount = campaignRequestsCount;
    }

    public long getVenueRequestsCount() {
        return venueRequestsCount;
    }

    public void setVenueRequestsCount(long venueRequestsCount) {
        this.venueRequestsCount = venueRequestsCount;
    }

    public long getLocationRequestsCountPerSecond() {
        return locationRequestsCountPerSecond;
    }

    public void setLocationRequestsCountPerSecond(long locationRequestsCountPerSecond) {
        this.locationRequestsCountPerSecond = locationRequestsCountPerSecond;
    }

    public long getRegisterRequestsCountPerSecond() {
        return registerRequestsCountPerSecond;
    }

    public void setRegisterRequestsCountPerSecond(long registerRequestsCountPerSecond) {
        this.registerRequestsCountPerSecond = registerRequestsCountPerSecond;
    }

    public long getRouteRequestsCountPerSecond() {
        return routeRequestsCountPerSecond;
    }

    public void setRouteRequestsCountPerSecond(long routeRequestsCountPerSecond) {
        this.routeRequestsCountPerSecond = routeRequestsCountPerSecond;
    }

    public long getMapRequestsCountPerSecond() {
        return mapRequestsCountPerSecond;
    }

    public void setMapRequestsCountPerSecond(long mapRequestsCountPerSecond) {
        this.mapRequestsCountPerSecond = mapRequestsCountPerSecond;
    }

    public long getPoiRequestsCountPerSecond() {
        return poiRequestsCountPerSecond;
    }

    public void setPoiRequestsCountPerSecond(long poiRequestsCountPerSecond) {
        this.poiRequestsCountPerSecond = poiRequestsCountPerSecond;
    }

    public long getCampaignRequestsCountPerSecond() {
        return campaignRequestsCountPerSecond;
    }

    public void setCampaignRequestsCountPerSecond(long campaignRequestsCountPerSecond) {
        this.campaignRequestsCountPerSecond = campaignRequestsCountPerSecond;
    }

    public long getVenueRequestsCountPerSecond() {
        return venueRequestsCountPerSecond;
    }

    public void setVenueRequestsCountPerSecond(long venueRequestsCountPerSecond) {
        this.venueRequestsCountPerSecond = venueRequestsCountPerSecond;
    }

    public String getLocationRequestLastTime() {
        return locationRequestLastTime;
    }

    public void setLocationRequestLastTime(String locationRequestLastTime) {
        this.locationRequestLastTime = locationRequestLastTime;
    }

    public String getRegisterRequestLastTime() {
        return registerRequestLastTime;
    }

    public void setRegisterRequestLastTime(String registerRequestLastTime) {
        this.registerRequestLastTime = registerRequestLastTime;
    }

    public String getRouteRequestLastTime() {
        return routeRequestLastTime;
    }

    public void setRouteRequestLastTime(String routeRequestLastTime) {
        this.routeRequestLastTime = routeRequestLastTime;
    }

    public String getMapRequestLastTime() {
        return mapRequestLastTime;
    }

    public void setMapRequestLastTime(String mapRequestLastTime) {
        this.mapRequestLastTime = mapRequestLastTime;
    }

    public String getPoiRequestLastTime() {
        return poiRequestLastTime;
    }

    public void setPoiRequestLastTime(String poiRequestLastTime) {
        this.poiRequestLastTime = poiRequestLastTime;
    }

    public String getCampaignRequestLastTime() {
        return campaignRequestLastTime;
    }

    public void setCampaignRequestLastTime(String campaignRequestLastTime) {
        this.campaignRequestLastTime = campaignRequestLastTime;
    }

    public String getVenueRequestLastTime() {
        return venueRequestLastTime;
    }

    public void setVenueRequestLastTime(String venueRequestLastTime) {
        this.venueRequestLastTime = venueRequestLastTime;
    }

    public void logStats() {
        LOGGER.info("");
        LOGGER.info("------------- Mobile App Stats -------------");
        LOGGER.info("Last Location Request                      : {}", this.locationRequestLastTime);
        LOGGER.info("Total Location Requests                    : {}", this.locationRequestsCount);
        LOGGER.info("Location Requests Per Second               : {}", this.locationRequestsCountPerSecond);
        LOGGER.info("");
        
        LOGGER.info("Last Register Request                      : {}", this.registerRequestLastTime);
        LOGGER.info("Total Register Requests                    : {}", this.registerRequestsCount);
        LOGGER.info("Register Requests Per Second               : {}", this.registerRequestsCountPerSecond);
        LOGGER.info("");
        
        LOGGER.info("Last Route Request                         : {}", this.routeRequestLastTime);
        LOGGER.info("Total Route Requests                       : {}", this.routeRequestsCount);
        LOGGER.info("Route Requests Per Second                  : {}", this.routeRequestsCountPerSecond);
        LOGGER.info("");
        
        LOGGER.info("Last Map Request                           : {}", this.mapRequestLastTime);
        LOGGER.info("Total Map Requests                         : {}", this.mapRequestsCount);
        LOGGER.info("Map Requests Per Second                    : {}", this.mapRequestsCountPerSecond);
        LOGGER.info("");
        
        LOGGER.info("Last Point Of Interest Request             : {}", this.poiRequestLastTime);
        LOGGER.info("Total Point Of Interest Requests           : {}", this.poiRequestsCount);
        LOGGER.info("Point Of Interest Requests Per Second      : {}", this.poiRequestsCountPerSecond);
        LOGGER.info("");

        LOGGER.info("Last Campaign Request                      : {}", this.campaignRequestLastTime);
        LOGGER.info("Total Campaign Requests                    : {}", this.campaignRequestsCount);
        LOGGER.info("Campaign Requests Per Second               : {}", this.campaignRequestsCountPerSecond);
        LOGGER.info("");

        LOGGER.info("Last Venue Request                         : {}", this.venueRequestLastTime);
        LOGGER.info("Total Venue Requests                       : {}", this.venueRequestsCount);
        LOGGER.info("Venue Requests Per Second                  : {}", this.venueRequestsCountPerSecond);        
    }
}