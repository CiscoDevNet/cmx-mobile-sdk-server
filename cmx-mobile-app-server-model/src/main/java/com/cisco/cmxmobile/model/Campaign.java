package com.cisco.cmxmobile.model;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.cisco.cmxmobile.cacheService.client.annotations.Key;
import com.cisco.cmxmobile.cacheService.client.annotations.PersistableStrategy;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Campaign 
{
    //Keys - MSEUdid + CampaignId
    @Key(index = 1)
    private String campaignUdid;

    
    private String mseUdid;
    private String id; 
    private String venueUdId;
    
    //Attributes
    private String endDate;
    private String name;
    private String ruleCondition;
    private String startDate;
    private String status;
    private String displayId;
    private String displayName;
    private String currentWorkflowStatus;
    private String initDisplaySeq;
    private String activeDateRange;    
    private String poiId;
    
    //Needs to be mapped to POI
    @PersistableStrategy(AsJson = true)
    private List<CampaignZone> zoneList;
    
    //Banners
    @PersistableStrategy(AsJson = true) 
    private List<Banner> offerMsgs;
    @PersistableStrategy(AsJson = true) 
    private List<Banner> dealMsgs;
    @PersistableStrategy(AsJson = true) 
    private List<Banner> sponsorshipMsgs;
    @PersistableStrategy(AsJson = true) 
    private List<Banner> welcomeMsgs;
    @PersistableStrategy(AsJson = true) 
    private List<Banner> adMsgs;
    
    //Rules - JSON Strings
    @PersistableStrategy(AsJson = true) 
    private List<CampaignRule> rules;
    
    private List<String> dealMsgsRules;
    private List<String> adMsgsRules;
    private List<String> offerMsgsRules;
    private List<String> sponsorshipMsgsRules;
    private List<String> welcomeMsgsRules;
    
    public static final String CAMPAIGN_UDID = "campaignUdid";
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getEndDate() {
        return endDate;
    }
    
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getRuleCondition() {
        return ruleCondition;
    }
    
    public void setRuleCondition(String ruleCondition) {
        this.ruleCondition = ruleCondition;
    }
    
    public String getStartDate() {
        return startDate;
    }
    
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getDisplayId() {
        return displayId;
    }
    
    public void setDisplayId(String displayId) {
        this.displayId = displayId;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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
    
    public String getActiveDateRange() {
        return activeDateRange;
    }
    
    public void setActiveDateRange(String activeDateRange) {
        this.activeDateRange = activeDateRange;
    }
    
    public String getPoiId() {
        return poiId;
    }
    
    public void setStoreId(String poiId) {
        this.poiId = poiId;
    }
    
    public List<CampaignZone> getZoneList() {
        return zoneList;
    }
    
    public void setZoneList(List<CampaignZone> zoneList) {
        this.zoneList = zoneList;
    }
    
    public  List<Banner> getOfferMsgs() {
        return offerMsgs;
    }
    
    public void setOfferMsgs(List<Banner> offerMsgs) {
        this.offerMsgs = offerMsgs;
    }
    
    public  List<Banner> getDealMsgs() {
        return dealMsgs;
    }
    
    public void setDealMsgs(List<Banner> dealMsgs) {
        this.dealMsgs = dealMsgs;
    }
    
    public  List<Banner> getSponsorshipMsgs() {
        return sponsorshipMsgs;
    }
    
    public void setSponsorshipMsgs(List<Banner> sponsorshipMsgs) {
        this.sponsorshipMsgs = sponsorshipMsgs;
    }
    
    public  List<Banner> getWelcomeMsgs() {
        return welcomeMsgs;
    }
    
    public void setWelcomeMsgs(List<Banner> welcomeMsgs) {
        this.welcomeMsgs = welcomeMsgs;
    }
    
    public  List<Banner> getAdMsgs() {
        return adMsgs;
    }
    
    public void setAdMsgs(List<Banner> adMsgs) {
        this.adMsgs = adMsgs;
    }
    
    public List<CampaignRule> getRules() {
        return rules;
    }
    
    public void setRules(List<CampaignRule> rules) {
        this.rules = rules;
    }
    
    public List<String> getAdMsgsRules() {
        return adMsgsRules;
    }
    
    public void setAdMsgsRules(List<String> adMsgsRules) {
        this.adMsgsRules = adMsgsRules;
    }
    
    public List<String> getOfferMsgsRules() {
        return offerMsgsRules;
    }
    
    public void setOfferMsgsRules(List<String> offerMsgsRules) {
        this.offerMsgsRules = offerMsgsRules;
    }
    
    public List<String> getSponsorshipMsgsRules() {
        return sponsorshipMsgsRules;
    }
    
    public void setSponsorshipMsgsRules(List<String> sponsorshipMsgsRules) {
        this.sponsorshipMsgsRules = sponsorshipMsgsRules;
    }
    
    public List<String> getWelcomeMsgsRules() {
        return welcomeMsgsRules;
    }
    
    public void setWelcomeMsgsRules(List<String> welcomeMsgsRules) {
        this.welcomeMsgsRules = welcomeMsgsRules;
    }

    public String getVenueUdId() {
        return venueUdId;
    }

    public void setVenueUdId(String venueUdId) {
        this.venueUdId = venueUdId;
    }

    public List<String> getDealMsgsRules() {
        return dealMsgsRules;
    }

    public void setDealMsgsRules(List<String> dealMsgsRules) {
        this.dealMsgsRules = dealMsgsRules;
    }

    public void setPoiId(String poiId) {
        this.poiId = poiId;
    }

    public String getCampaignUdid() {
        return campaignUdid;
    }

    public void setCampaignUdid(String campaignUdid) {
        this.campaignUdid = campaignUdid;
    }

    public String getMseUdid() {
        return mseUdid;
    }

    public void setMseUdid(String mseUdid) {
        this.mseUdid = mseUdid;
    }
}

