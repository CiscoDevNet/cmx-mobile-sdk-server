package com.cisco.cmxmobile.server.stats;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.cmxmobile.model.ConnectAndEngageServerStats;

import java.util.concurrent.atomic.AtomicLong;

public final class ConnectAndEngageStats {

    private static ConnectAndEngageStats instance = new ConnectAndEngageStats();

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectAndEngageStats.class);

    private final AtomicLong configureUpdateCount = new AtomicLong();

    private final AtomicLong campaignUpdateCount = new AtomicLong();

    private final AtomicLong floorImageUploadCount = new AtomicLong();

    private final AtomicLong poiImageUploadCount = new AtomicLong();

    private final AtomicLong certificateUploadCount = new AtomicLong();

    private final AtomicLong bannerImageUploadCount = new AtomicLong();

    private final AtomicLong configureUpdateLastTime = new AtomicLong();

    private final AtomicLong campaignUpdateLastTime = new AtomicLong();

    private final AtomicLong floorImageUploadLastTime = new AtomicLong();

    private final AtomicLong poiImageUploadLastTime = new AtomicLong();

    private final AtomicLong certificateUploadLastTime = new AtomicLong();

    private final AtomicLong bannerImageUploadLastTime = new AtomicLong();
    
    private final DateFormat dateFormat = new SimpleDateFormat("dd MMM YYYY HH:mm:ss");
    
    private long startTime;

    private String startTimeDate;

    private ConnectAndEngageStats() {
        resetAllCounters();
        resetStartDate();
    }

    public static ConnectAndEngageStats getInstance() {
        return instance;
    }

    private void resetStartDate() {
        startTime = System.currentTimeMillis();
        startTimeDate = dateFormat.format(new Date(System.currentTimeMillis()));
    }

    private void resetAllCounters() {
        configureUpdateCount.set(0);
        campaignUpdateCount.set(0);
        floorImageUploadCount.set(0);
        poiImageUploadCount.set(0);
        certificateUploadCount.set(0);
        bannerImageUploadCount.set(0);
        configureUpdateLastTime.set(0);
        campaignUpdateLastTime.set(0);
        floorImageUploadLastTime.set(0);
        poiImageUploadLastTime.set(0);
        certificateUploadLastTime.set(0);
        bannerImageUploadLastTime.set(0);
    }
    
    public void resetAllStats() {
        resetAllCounters();
        resetStartDate();
    }
    
    public void incrementConfigureUpdateCount() {
        configureUpdateCount.incrementAndGet();
        configureUpdateLastTime.set(System.currentTimeMillis());
    }

    public void incrementCampaignUpdateCount() {
        campaignUpdateCount.incrementAndGet();
        campaignUpdateLastTime.set(System.currentTimeMillis());
    }

    public void incrementFloorImageUploadCount() {
        floorImageUploadCount.incrementAndGet();
        floorImageUploadLastTime.set(System.currentTimeMillis());
    }

    public void incrementPoiImageUploadCount() {
        poiImageUploadCount.incrementAndGet();
        poiImageUploadLastTime.set(System.currentTimeMillis());
    }
    
    public void incrementCertificateUploadCount() {
        certificateUploadCount.incrementAndGet();
        certificateUploadLastTime.set(System.currentTimeMillis());
    }
    
    public void incrementBannerImageUploadCount() {
        bannerImageUploadCount.incrementAndGet();
        bannerImageUploadLastTime.set(System.currentTimeMillis());
    }
    
    public ConnectAndEngageServerStats getConnectAndEngageServerStats() {
        ConnectAndEngageServerStats stats = new ConnectAndEngageServerStats();
        long timeElapsedSec = (System.currentTimeMillis() - startTime) / 1000;
        if (timeElapsedSec == 0) {
            timeElapsedSec = 1;
        }
        if (configureUpdateLastTime.get() == 0) {
            stats.setConfigureUpdateLastTime("Never");
        } else {
            stats.setConfigureUpdateLastTime(dateFormat.format(new Date(configureUpdateLastTime.get())));
        }
        stats.setConfigureUpdateCount(configureUpdateCount.get());
        stats.setConfigureUpdateCountPerSecond(configureUpdateCount.get() / timeElapsedSec);
        
        if (campaignUpdateLastTime.get() == 0) {
            stats.setCampaignUpdateLastTime("Never");
        } else {
            stats.setCampaignUpdateLastTime(dateFormat.format(new Date(campaignUpdateLastTime.get())));
        }
        stats.setCampaignUpdateCount(campaignUpdateCount.get());
        stats.setCampaignUpdateCountPerSecond(campaignUpdateCount.get() / timeElapsedSec);
        
        if (floorImageUploadLastTime.get() == 0) {
            stats.setFloorImageUploadLastTime("Never");
        } else {
            stats.setFloorImageUploadLastTime(dateFormat.format(new Date(floorImageUploadLastTime.get())));
        }
        stats.setFloorImageUploadCount(floorImageUploadCount.get());
        stats.setFloorImageUploadCountPerSecond(floorImageUploadCount.get() / timeElapsedSec);
        
        if (poiImageUploadLastTime.get() == 0) {
            stats.setPoiImageUploadLastTime("Never");
        } else {
            stats.setPoiImageUploadLastTime(dateFormat.format(new Date(poiImageUploadLastTime.get())));
        }
        stats.setPoiImageUploadCount(poiImageUploadCount.get());
        stats.setPoiImageUploadCountPerSecond(poiImageUploadCount.get() / timeElapsedSec);
        
        if (certificateUploadLastTime.get() == 0) {
            stats.setCertificateUploadLastTime("Never");
        } else {
            stats.setCertificateUploadLastTime(dateFormat.format(new Date(certificateUploadLastTime.get())));
        }
        stats.setCertificateUploadCount(certificateUploadCount.get());
        stats.setCertificateUploadCountPerSecond(certificateUploadCount.get() / timeElapsedSec);

        if (bannerImageUploadLastTime.get() == 0) {
            stats.setBannerImageUploadLastTime("Never");
        } else {
            stats.setBannerImageUploadLastTime(dateFormat.format(new Date(bannerImageUploadLastTime.get())));
        }
        stats.setBannerImageUploadCount(bannerImageUploadCount.get());
        stats.setBannerImageUploadCountPerSecond(bannerImageUploadCount.get() / timeElapsedSec);
        return stats;
    }

    public void dumpStats() {
        long timeElapsedSec = (System.currentTimeMillis() - startTime) / 1000;
        if (timeElapsedSec == 0) {
            timeElapsedSec = 1;
        }
        LOGGER.info("------- Connect And Engage Stats -------");
        LOGGER.info("Server Start Time: {}", startTimeDate);
        
        if (configureUpdateLastTime.get() == 0) {
            LOGGER.info("Last Configure Update: Never");
        } else {
            LOGGER.info("Last Configure Update: {}", dateFormat.format(new Date(configureUpdateLastTime.get())));
        }
        LOGGER.info("Total Configure Updates: {}", configureUpdateCount.get());
        LOGGER.info("Configure Updates Per Second: {}", configureUpdateCount.get() / timeElapsedSec);
        
        if (campaignUpdateLastTime.get() == 0) {
            LOGGER.info("Last Campaign Update: Never");
        } else {
            LOGGER.info("Last Campaign Update: {}", dateFormat.format(new Date(campaignUpdateLastTime.get())));
        }
        LOGGER.info("Total Campaign Updates: {}", campaignUpdateCount.get());
        LOGGER.info("Campaign Updates Per Second: {}", campaignUpdateCount.get() / timeElapsedSec);
        
        if (floorImageUploadLastTime.get() == 0) {
            LOGGER.info("Last Floor Image Upload: Never");
        } else {
            LOGGER.info("Last Floor Image Upload: {}", dateFormat.format(new Date(floorImageUploadLastTime.get())));
        }
        LOGGER.info("Total Floor Image Uploads: {}", floorImageUploadCount.get());
        LOGGER.info("Floor Image Uploads Per Second: {}", floorImageUploadCount.get() / timeElapsedSec);
        
        if (poiImageUploadLastTime.get() == 0) {
            LOGGER.info("Last Point Of Interest Image Upload: Never");
        } else {
            LOGGER.info("Last Point Of Interest Image Upload: {}", dateFormat.format(new Date(poiImageUploadLastTime.get())));
        }
        LOGGER.info("Total Point Of Interest Image Uploads: {}", poiImageUploadCount.get());
        LOGGER.info("Point Of Interest Image Uploads Per Second: {}", poiImageUploadCount.get() / timeElapsedSec);
        
        if (certificateUploadLastTime.get() == 0) {
            LOGGER.info("Last Certificate Upload: Never");
        } else {
            LOGGER.info("Last Certificate Upload: {}", dateFormat.format(new Date(certificateUploadLastTime.get())));
        }
        LOGGER.info("Total Certificate Uploads: {}", certificateUploadCount.get());
        LOGGER.info("Certificate Uploads Per Second: {}", certificateUploadCount.get() / timeElapsedSec);

        if (bannerImageUploadLastTime.get() == 0) {
            LOGGER.info("Last Banner Image Upload: Never");
        } else {
            LOGGER.info("Last Banner Image Upload: {}", dateFormat.format(new Date(bannerImageUploadLastTime.get())));
        }
        LOGGER.info("Total Banner Image Uploads: {}", bannerImageUploadCount.get());
        LOGGER.info("Banner Image Uploads Per Second: {}", bannerImageUploadCount.get() / timeElapsedSec);
    }
}