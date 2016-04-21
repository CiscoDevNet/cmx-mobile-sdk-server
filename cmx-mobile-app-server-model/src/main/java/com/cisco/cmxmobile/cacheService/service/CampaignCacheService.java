package com.cisco.cmxmobile.cacheService.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.cacheService.client.CachePersistenceException;
import com.cisco.cmxmobile.cacheService.client.MobileServerCacheClient;
import com.cisco.cmxmobile.model.Banner;
import com.cisco.cmxmobile.model.Campaign;

@Component
public class CampaignCacheService 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignCacheService.class);
    
    @Autowired
    private MobileServerCacheClient mobileServerCacheClient;

    /*
     * Save the Campaigns. Campaigns contain Banners and Rule information. 
     * 
     * @param campaignList List of Campaigns to be saved
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */
    public void saveCampaignList(List<Campaign> campaignList)     	
    {
        for (Campaign campaign : campaignList) {
            try {
                mobileServerCacheClient.save(campaign);
            } catch (CachePersistenceException e) {
                // TODO Auto-generated catch block
                LOGGER.error("CachePersistenceException", e);
            }
        }
    }
    
    /**
     * Retrieve Campaign by Campaign ID from the Data Store
     * 
     * @param campaignUdid Unique Identifier of the Campaign
     * @return The Campaign Object
     */
    public Campaign getCampaign(String campaignUdid) 
    {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(Campaign.CAMPAIGN_UDID, campaignUdid);
        return (Campaign) mobileServerCacheClient.get(Campaign.class, properties);
    }
    
    /**
     * Retrieve List of Banners stored in the Data Store corresponding to a Campaign
     * 
     * @param campaigUdid Unique Identifier of the Campaign
     * @return List of Banner Objects
     */
    public List<Banner> getAllBannersForCampaign(String campaigUdid) {
        Campaign campaign = getCampaign(campaigUdid);
        
        List<Banner> bannersForCampaignList = new ArrayList<Banner>();
        
        // offerMsgs;
        // dealMsgs;
        // sponsorshipMsgs;
        // welcomeMsgs;
        // adMsgs;
        
        bannersForCampaignList.addAll(campaign.getOfferMsgs());
        bannersForCampaignList.addAll(campaign.getDealMsgs());
        bannersForCampaignList.addAll(campaign.getSponsorshipMsgs());
        bannersForCampaignList.addAll(campaign.getWelcomeMsgs());
        bannersForCampaignList.addAll(campaign.getAdMsgs());
        
        return bannersForCampaignList;        
    }

}
