package com.cisco.cmxmobile.server.mse;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;
import com.cisco.cmxmobile.model.Venue;

@Component
public class Setup 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Setup.class);
    
    @Autowired
    public MobileServerCacheService mobileServerCacheService;
    
    @Autowired
    public SetupHelper setupHelper;
    
    @Autowired
    private AuthTokenCache authTokenCache;
    
    public void setupVenue(List<Venue> venueList)
    {
        if (venueList == null) {
            throw new IllegalArgumentException("In Delete All Venues - MSE Venue Udid should not be null");
        }

        if (venueList.isEmpty()) {
            LOGGER.error("Venue List is empty - No Venue to Setup");
        }
        
        //Delete the Existing venues
        deleteAllVenues(venueList.get(0).getMseUdId());
        
        // Save the new Venue List
        mobileServerCacheService.saveVenue(venueList);
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
        List<Venue> venuesDeleted = mobileServerCacheService.deleteAllVenues(mseUdId);
        
        //Remove entries from the Auth Cache
        authTokenCache.removeAuthToken(venuesDeleted);
        
        //Delete All Maps by Venue
        deleteAllMapsByVenue(venuesDeleted);
        
        //Delete the MSE DIR
        deleteMseDir(mseUdId);
        
    }

    private void deleteMseDir(String mseUdId) {
        try {
            setupHelper.deleteMseDir(mseUdId);
        }
        catch (NoSuchAlgorithmException e) {
            LOGGER.error("Failed to delete MSE DIR {} ", mseUdId, e);
        } catch (IOException e) {
            LOGGER.error("Failed to delete MSE DIR {} ", mseUdId, e);
        }
    }

    private void deleteAllMapsByVenue(List<Venue> venuesDeleted) {
        for (Venue venue : venuesDeleted) {
            try {
                setupHelper.deleteMapsByVenue(venue.getMseVenueId());
            }
            catch (NoSuchAlgorithmException e) {
                LOGGER.error("Failed to delete Maps for Venue with venue ID {} ", venue.getVenueUdId(), e);
            }
            catch (IOException e) {
                LOGGER.error("Failed to delete Maps for Venue with venue ID {} ", venue.getVenueUdId(), e);
            }
        }
    }    
}
