package com.cisco.cmxmobile.server.stats;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.cmxmobile.model.MobileAppServerStats;

import java.util.concurrent.atomic.AtomicLong;

public final class MobileAppStats {

    private static MobileAppStats instance = new MobileAppStats();

    private static final Logger LOGGER = LoggerFactory.getLogger(MobileAppStats.class);

    private final AtomicLong locationRequestsCount = new AtomicLong();

    private final AtomicLong registerRequestsCount = new AtomicLong();

    private final AtomicLong routeRequestsCount = new AtomicLong();

    private final AtomicLong mapRequestsCount = new AtomicLong();

    private final AtomicLong poiRequestsCount = new AtomicLong();

    private final AtomicLong campaignRequestsCount = new AtomicLong();

    private final AtomicLong venueRequestsCount = new AtomicLong();

    private final AtomicLong locationRequestLastTime = new AtomicLong();

    private final AtomicLong registerRequestLastTime = new AtomicLong();

    private final AtomicLong routeRequestLastTime = new AtomicLong();

    private final AtomicLong mapRequestLastTime = new AtomicLong();

    private final AtomicLong poiRequestLastTime = new AtomicLong();

    private final AtomicLong campaignRequestLastTime = new AtomicLong();
    
    private final AtomicLong venueRequestLastTime = new AtomicLong();

    private final DateFormat dateFormat = new SimpleDateFormat("dd MMM YYYY HH:mm:ss");
    
    private long startTime;

    private String startTimeDate;

    private MobileAppStats() {
        resetAllCounters();
        resetStartDate();
    }

    public static MobileAppStats getInstance() {
        return instance;
    }

    private void resetStartDate() {
        startTime = System.currentTimeMillis();
        startTimeDate = dateFormat.format(new Date(System.currentTimeMillis()));
    }

    private void resetAllCounters() {
        locationRequestsCount.set(0);
        registerRequestsCount.set(0);
        routeRequestsCount.set(0);
        mapRequestsCount.set(0);
        poiRequestsCount.set(0);
        campaignRequestsCount.set(0);
        venueRequestsCount.set(0);
        locationRequestLastTime.set(0);
        registerRequestLastTime.set(0);
        routeRequestLastTime.set(0);
        mapRequestLastTime.set(0);
        poiRequestLastTime.set(0);
        campaignRequestLastTime.set(0);
        venueRequestLastTime.set(0);
    }
    
    public void resetAllStats() {
        resetAllCounters();
        resetStartDate();
    }

    public void incrementLocationRequestsCount() {
        locationRequestsCount.incrementAndGet();
        locationRequestLastTime.set(System.currentTimeMillis());
    }

    public void incrementRegisterRequestsCount() {
        registerRequestsCount.incrementAndGet();
        registerRequestLastTime.set(System.currentTimeMillis());
    }

    public void incrementRouteRequestsCount() {
        routeRequestsCount.incrementAndGet();
        routeRequestLastTime.set(System.currentTimeMillis());
    }

    public void incrementMapRequestsCount() {
        mapRequestsCount.incrementAndGet();
        mapRequestLastTime.set(System.currentTimeMillis());
    }
    
    public void incrementPoiRequestsCount() {
        poiRequestsCount.incrementAndGet();
        poiRequestLastTime.set(System.currentTimeMillis());
    }
    
    public void incrementCampaignRequestsCount() {
        campaignRequestsCount.incrementAndGet();
        campaignRequestLastTime.set(System.currentTimeMillis());
    }
    
    public void incrementVenueRequestsCount() {
        venueRequestsCount.incrementAndGet();
        venueRequestLastTime.set(System.currentTimeMillis());
    }
    
    public MobileAppServerStats getMobileAppServerStats() {
        MobileAppServerStats stats = new MobileAppServerStats();
        long timeElapsedSec = (System.currentTimeMillis() - startTime) / 1000;
        if (timeElapsedSec == 0) {
            timeElapsedSec = 1;
        }
        
        if (locationRequestLastTime.get() == 0) {
            stats.setLocationRequestLastTime("Never");
        } else {
            stats.setLocationRequestLastTime(dateFormat.format(new Date(locationRequestLastTime.get())));
        }
        stats.setLocationRequestsCount(locationRequestsCount.get());
        stats.setLocationRequestsCountPerSecond(locationRequestsCount.get() / timeElapsedSec);
        
        if (registerRequestLastTime.get() == 0) {
            stats.setRegisterRequestLastTime("Never");
        } else {
            stats.setRegisterRequestLastTime(dateFormat.format(new Date(registerRequestLastTime.get())));
        }
        stats.setRegisterRequestsCount(registerRequestsCount.get());
        stats.setRegisterRequestsCountPerSecond(registerRequestsCount.get() / timeElapsedSec);
        
        if (routeRequestLastTime.get() == 0) {
            stats.setRouteRequestLastTime("Never");
        } else {
            stats.setRouteRequestLastTime(dateFormat.format(new Date(routeRequestLastTime.get())));
        }
        stats.setRouteRequestsCount(routeRequestsCount.get());
        stats.setRouteRequestsCountPerSecond(routeRequestsCount.get() / timeElapsedSec);
        
        if (mapRequestLastTime.get() == 0) {
            stats.setMapRequestLastTime("Never");
        } else {
            stats.setMapRequestLastTime(dateFormat.format(new Date(mapRequestLastTime.get())));
        }
        stats.setMapRequestsCount(mapRequestsCount.get());
        stats.setMapRequestsCountPerSecond(mapRequestsCount.get() / timeElapsedSec);
        
        if (poiRequestLastTime.get() == 0) {
            stats.setPoiRequestLastTime("Never");
        } else {
            stats.setPoiRequestLastTime(dateFormat.format(new Date(poiRequestLastTime.get())));
        }
        stats.setPoiRequestsCount(poiRequestsCount.get());
        stats.setPoiRequestsCountPerSecond(poiRequestsCount.get() / timeElapsedSec);

        if (campaignRequestLastTime.get() == 0) {
            stats.setCampaignRequestLastTime("Never");
        } else {
            stats.setCampaignRequestLastTime(dateFormat.format(new Date(campaignRequestLastTime.get())));
        }
        stats.setCampaignRequestsCount(campaignRequestsCount.get());
        stats.setCampaignRequestsCountPerSecond(campaignRequestsCount.get() / timeElapsedSec);

        if (venueRequestLastTime.get() == 0) {
            stats.setVenueRequestLastTime("Never");
        } else {
            stats.setVenueRequestLastTime(dateFormat.format(new Date(venueRequestLastTime.get())));
        }
        stats.setVenueRequestsCount(venueRequestsCount.get());
        stats.setVenueRequestsCountPerSecond(venueRequestsCount.get() / timeElapsedSec);
        return stats;
    }

    public void dumpStats() {
        long timeElapsedSec = (System.currentTimeMillis() - startTime) / 1000;
        if (timeElapsedSec == 0) {
            timeElapsedSec = 1;
        }
        LOGGER.info("------- Mobile App Stats -------");
        LOGGER.info("Server Start Time: {}", startTimeDate);
        
        if (locationRequestLastTime.get() == 0) {
            LOGGER.info("Last Location Request: Never");
        } else {
            LOGGER.info("Last Location Request: {}", dateFormat.format(new Date(locationRequestLastTime.get())));
        }
        LOGGER.info("Total Location Requests: {}", locationRequestsCount.get());
        LOGGER.info("Location Requests Per Second: {}", locationRequestsCount.get() / timeElapsedSec);
        
        if (registerRequestLastTime.get() == 0) {
            LOGGER.info("Last Register Request: Never");
        } else {
            LOGGER.info("Last Register Request: {}", dateFormat.format(new Date(registerRequestLastTime.get())));
        }
        LOGGER.info("Total Register Requests: {}", registerRequestsCount.get());
        LOGGER.info("Register Requests Per Second: {}", registerRequestsCount.get() / timeElapsedSec);
        
        if (routeRequestLastTime.get() == 0) {
            LOGGER.info("Last Route Request: Never");
        } else {
            LOGGER.info("Last Route Request: {}", dateFormat.format(new Date(routeRequestLastTime.get())));
        }
        LOGGER.info("Total Route Requests: {}", routeRequestsCount.get());
        LOGGER.info("Route Requests Per Second: {}", routeRequestsCount.get() / timeElapsedSec);
        
        if (mapRequestLastTime.get() == 0) {
            LOGGER.info("Last Map Request: Never");
        } else {
            LOGGER.info("Last Map Request: {}", dateFormat.format(new Date(mapRequestLastTime.get())));
        }
        LOGGER.info("Total Map Requests: {}", mapRequestsCount.get());
        LOGGER.info("Map Requests Per Second: {}", mapRequestsCount.get() / timeElapsedSec);
        
        if (poiRequestLastTime.get() == 0) {
            LOGGER.info("Last Point Of Interest Request: Never");
        } else {
            LOGGER.info("Last Point Of Interest Request: {}", dateFormat.format(new Date(poiRequestLastTime.get())));
        }
        LOGGER.info("Total Point Of Interest Requests: {}", poiRequestsCount.get());
        LOGGER.info("Point Of Interest Requests Per Second: {}", poiRequestsCount.get() / timeElapsedSec);

        if (campaignRequestLastTime.get() == 0) {
            LOGGER.info("Last Campaign Request: Never");
        } else {
            LOGGER.info("Last Campaign Request: {}", dateFormat.format(new Date(campaignRequestLastTime.get())));
        }
        LOGGER.info("Total Campaign Requests: {}", campaignRequestsCount.get());
        LOGGER.info("Campaign Requests Per Second: {}", campaignRequestsCount.get() / timeElapsedSec);

        if (venueRequestLastTime.get() == 0) {
            LOGGER.info("Last Venue Request: Never");
        } else {
            LOGGER.info("Last Venue Request: {}", dateFormat.format(new Date(venueRequestLastTime.get())));
        }
        LOGGER.info("Total Venue Requests: {}", venueRequestsCount.get());
        LOGGER.info("Venue Requests Per Second: {}", venueRequestsCount.get() / timeElapsedSec);
    }
}