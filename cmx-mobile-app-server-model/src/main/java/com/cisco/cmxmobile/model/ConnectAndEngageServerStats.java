package com.cisco.cmxmobile.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectAndEngageServerStats {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectAndEngageServerStats.class);

    private long configureUpdateCount;

    private long campaignUpdateCount;

    private long floorImageUploadCount;

    private long poiImageUploadCount;

    private long certificateUploadCount;

    private long bannerImageUploadCount;

    private long configureUpdateCountPerSecond;

    private long campaignUpdateCountPerSecond;

    private long floorImageUploadCountPerSecond;

    private long poiImageUploadCountPerSecond;

    private long certificateUploadCountPerSecond;

    private long bannerImageUploadCountPerSecond;

    private String configureUpdateLastTime;

    private String campaignUpdateLastTime;

    private String floorImageUploadLastTime;

    private String poiImageUploadLastTime;

    private String certificateUploadLastTime;

    private String bannerImageUploadLastTime;

    public long getConfigureUpdateCount() {
        return configureUpdateCount;
    }

    public void setConfigureUpdateCount(long configureUpdateCount) {
        this.configureUpdateCount = configureUpdateCount;
    }

    public long getCampaignUpdateCount() {
        return campaignUpdateCount;
    }

    public void setCampaignUpdateCount(long campaignUpdateCount) {
        this.campaignUpdateCount = campaignUpdateCount;
    }

    public long getFloorImageUploadCount() {
        return floorImageUploadCount;
    }

    public void setFloorImageUploadCount(long floorImageUploadCount) {
        this.floorImageUploadCount = floorImageUploadCount;
    }

    public long getPoiImageUploadCount() {
        return poiImageUploadCount;
    }

    public void setPoiImageUploadCount(long poiImageUploadCount) {
        this.poiImageUploadCount = poiImageUploadCount;
    }

    public long getCertificateUploadCount() {
        return certificateUploadCount;
    }

    public void setCertificateUploadCount(long certificateUploadCount) {
        this.certificateUploadCount = certificateUploadCount;
    }

    public long getBannerImageUploadCount() {
        return bannerImageUploadCount;
    }

    public void setBannerImageUploadCount(long bannerImageUploadCount) {
        this.bannerImageUploadCount = bannerImageUploadCount;
    }

    public long getConfigureUpdateCountPerSecond() {
        return configureUpdateCountPerSecond;
    }

    public void setConfigureUpdateCountPerSecond(long configureUpdateCountPerSecond) {
        this.configureUpdateCountPerSecond = configureUpdateCountPerSecond;
    }

    public long getCampaignUpdateCountPerSecond() {
        return campaignUpdateCountPerSecond;
    }

    public void setCampaignUpdateCountPerSecond(long campaignUpdateCountPerSecond) {
        this.campaignUpdateCountPerSecond = campaignUpdateCountPerSecond;
    }

    public long getFloorImageUploadCountPerSecond() {
        return floorImageUploadCountPerSecond;
    }

    public void setFloorImageUploadCountPerSecond(long floorImageUploadCountPerSecond) {
        this.floorImageUploadCountPerSecond = floorImageUploadCountPerSecond;
    }

    public long getPoiImageUploadCountPerSecond() {
        return poiImageUploadCountPerSecond;
    }

    public void setPoiImageUploadCountPerSecond(long poiImageUploadCountPerSecond) {
        this.poiImageUploadCountPerSecond = poiImageUploadCountPerSecond;
    }

    public long getCertificateUploadCountPerSecond() {
        return certificateUploadCountPerSecond;
    }

    public void setCertificateUploadCountPerSecond(long certificateUploadCountPerSecond) {
        this.certificateUploadCountPerSecond = certificateUploadCountPerSecond;
    }

    public long getBannerImageUploadCountPerSecond() {
        return bannerImageUploadCountPerSecond;
    }

    public void setBannerImageUploadCountPerSecond(long bannerImageUploadCountPerSecond) {
        this.bannerImageUploadCountPerSecond = bannerImageUploadCountPerSecond;
    }

    public String getConfigureUpdateLastTime() {
        return configureUpdateLastTime;
    }

    public void setConfigureUpdateLastTime(String configureUpdateLastTime) {
        this.configureUpdateLastTime = configureUpdateLastTime;
    }

    public String getCampaignUpdateLastTime() {
        return campaignUpdateLastTime;
    }

    public void setCampaignUpdateLastTime(String campaignUpdateLastTime) {
        this.campaignUpdateLastTime = campaignUpdateLastTime;
    }

    public String getFloorImageUploadLastTime() {
        return floorImageUploadLastTime;
    }

    public void setFloorImageUploadLastTime(String floorImageUploadLastTime) {
        this.floorImageUploadLastTime = floorImageUploadLastTime;
    }

    public String getPoiImageUploadLastTime() {
        return poiImageUploadLastTime;
    }

    public void setPoiImageUploadLastTime(String poiImageUploadLastTime) {
        this.poiImageUploadLastTime = poiImageUploadLastTime;
    }

    public String getCertificateUploadLastTime() {
        return certificateUploadLastTime;
    }

    public void setCertificateUploadLastTime(String certificateUploadLastTime) {
        this.certificateUploadLastTime = certificateUploadLastTime;
    }

    public String getBannerImageUploadLastTime() {
        return bannerImageUploadLastTime;
    }

    public void setBannerImageUploadLastTime(String bannerImageUploadLastTime) {
        this.bannerImageUploadLastTime = bannerImageUploadLastTime;
    }
    
    public void logStats() {
        LOGGER.info("");
        LOGGER.info("--------- Connect And Engage Stats ---------");
        LOGGER.info("Last Configure Update                      : {}", this.configureUpdateLastTime);
        LOGGER.info("Total Configure Updates                    : {}", this.configureUpdateCount);
        LOGGER.info("Configure Updates Per Second               : {}", this.configureUpdateCountPerSecond);
        LOGGER.info("");
        
        LOGGER.info("Last Campaign Update                       : {}", this.campaignUpdateLastTime);
        LOGGER.info("Total Campaign Updates                     : {}", this.campaignUpdateCount);
        LOGGER.info("Campaign Updates Per Second                : {}", this.campaignUpdateCountPerSecond);
        LOGGER.info("");
        
        LOGGER.info("Last Floor Image Upload                    : {}", this.floorImageUploadLastTime);
        LOGGER.info("Total Floor Image Uploads                  : {}", this.floorImageUploadCount);
        LOGGER.info("Floor Image Uploads Per Second             : {}", this.floorImageUploadCountPerSecond);
        LOGGER.info("");
        
        LOGGER.info("Last Point Of Interest Image Upload        : {}", this.poiImageUploadLastTime);
        LOGGER.info("Total Point Of Interest Image Uploads      : {}", this.poiImageUploadCount);
        LOGGER.info("Point Of Interest Image Uploads Per Second : {}", this.poiImageUploadCountPerSecond);
        LOGGER.info("");
        
        LOGGER.info("Last Certificate Upload                    : {}", this.certificateUploadLastTime);
        LOGGER.info("Total Certificate Uploads                  : {}", this.certificateUploadCount);
        LOGGER.info("Certificate Uploads Per Second             : {}", this.certificateUploadCountPerSecond);
        LOGGER.info("");

        LOGGER.info("Last Banner Image Upload                   : {}", this.bannerImageUploadLastTime);
        LOGGER.info("Total Banner Image Uploads                 : {}", this.bannerImageUploadCount);
        LOGGER.info("Banner Image Uploads Per Second            : {}", this.bannerImageUploadCountPerSecond);
    }
}