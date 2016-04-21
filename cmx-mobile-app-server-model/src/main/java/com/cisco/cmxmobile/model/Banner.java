package com.cisco.cmxmobile.model;

import com.cisco.cmxmobile.cacheService.client.annotations.Key;

public class Banner {
    // Key Properties
    @Key(index = 1)
    private String mseUdid;

    @Key(index = 2)
    private String id;

    private String displayName;

    private String message;

    private String description;

    private String title;

    private String type;

    private String status;

    private String logoUrl;

    private String largeLogoUrl;

    private String targetUrl;

    private String displayId;

    private String fullDisplayName;

    private String displayDate;

    private String keywords;

    private String displayType;

    private String currentWorkflowStatus;

    private String initDisplaySeq;

    private String storeId;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getLargeLogoUrl() {
        return largeLogoUrl;
    }

    public void setLargeLogoUrl(String largeLogoUrl) {
        this.largeLogoUrl = largeLogoUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getDisplayId() {
        return displayId;
    }

    public void setDisplayId(String displayId) {
        this.displayId = displayId;
    }

    public String getFullDisplayName() {
        return fullDisplayName;
    }

    public void setFullDisplayName(String fullDisplayName) {
        this.fullDisplayName = fullDisplayName;
    }

    public String getDisplayDate() {
        return displayDate;
    }

    public void setDisplayDate(String displayDate) {
        this.displayDate = displayDate;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getDisplayType() {
        return displayType;
    }

    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    public String getCurrentWorkflowStatus() {
        return currentWorkflowStatus;
    }

    public void setCurrentWorkflowStatus(String currentWorkflowStatus) {
        this.currentWorkflowStatus = currentWorkflowStatus;
    }

    public String getInitDisplaySeq() {
        return initDisplaySeq;
    }

    public void setInitDisplaySeq(String initDisplaySeq) {
        this.initDisplaySeq = initDisplaySeq;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getMseUdid() {
        return mseUdid;
    }

    public void setMseUdid(String mseUdid) {
        this.mseUdid = mseUdid;
    }
}
