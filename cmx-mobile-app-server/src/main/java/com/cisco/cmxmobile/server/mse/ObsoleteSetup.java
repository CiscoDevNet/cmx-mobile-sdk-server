package com.cisco.cmxmobile.server.mse;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;
import com.cisco.cmxmobile.model.CasVenue;

public class ObsoleteSetup 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Setup.class);
    
    @Autowired
    public MobileServerCacheService mobileServerCacheService;
    
    @Autowired
    public SetupHelper setupHelper;
    
    public void setupVenue(List<CasVenue> casVenueList)
    {
        if (casVenueList == null) {
            throw new IllegalArgumentException("In Delete All Venues - MSE Venue Udid should not be null");
        }

        if (casVenueList.isEmpty()) {
            LOGGER.error("Venue List is empty - No Venue to Setup");
        }
        
        //Delete the Existing venues
        deleteAllVenues(casVenueList.get(0).getMseUdid());
        
        // Save the new Venue List
        mobileServerCacheService.saveCasVenue(casVenueList);
    }
    
    public void deleteAllVenues(String mseUdId)
    {
        if (mseUdId == null) {
            throw new IllegalArgumentException("In Delete All Venues - MSE Venue Udid should not be null");
        }
        
        if (mseUdId.isEmpty()) {
            throw new IllegalArgumentException("In Delete All Venues - MSE Venue Udid should not be an empty String");
        }
        
        //Delete All the Venues for this MSE
        //The method returns a list of all the Venues Deleted for the MSE
        List<String> venuesDeleted = mobileServerCacheService.deleteAllCasVenues(mseUdId);
        
        for (String venueUdId : venuesDeleted) {
            try {
                setupHelper.deleteMapsByVenue(venueUdId);
                setupHelper.deleteMseDir(mseUdId);
            }
            catch (NoSuchAlgorithmException e) {
                LOGGER.error("Failed to delete Maps for Venue with venue ID {} ", venueUdId, e);
            }
            catch (IOException e) {
                LOGGER.error("Failed to delete Maps for Venue with venue ID {} ", venueUdId, e);
            }
        }
    }     
    
}
