package com.cisco.cmxmobile.server.mse;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.cacheService.Utils.EncryptionUtil;
import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;

@Component
public class SetupHelper 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Setup.class);
    
    @Autowired
    public MobileServerCacheService mobileServerCacheService;
    
    @Value("${map.location}")
    private String rootMapLocation; 

    
    public void deleteMapsByVenue(String mseVenueUdId) 
        throws NoSuchAlgorithmException, IOException
    {
        if (mseVenueUdId == null) {
            throw new IllegalArgumentException("In Delete All Venues - MSE Venue Udid should not be null");
        }
        
        if (mseVenueUdId.isEmpty()) {
            throw new IllegalArgumentException("In Delete All Venues - MSE Venue Udid should not be an empty String");
        }
        
        File venueDirectory = new File(rootMapLocation, EncryptionUtil.generateMD5(mseVenueUdId));

        // Check if the Venue Directory Exists
        if (venueDirectory.getCanonicalFile().exists() && venueDirectory.getCanonicalFile().isDirectory()) {
            LOGGER.trace("Deleted Venue Directory for {}", venueDirectory);
            // Delete the Directory
            FileUtils.deleteDirectory(venueDirectory);
        }
    }
    
    public void deleteMseDir(String mseUdId) 
        throws NoSuchAlgorithmException, IOException
    {
        if (mseUdId == null) {
            throw new IllegalArgumentException("In Delete All Venues - MSE Udid should not be null");
        }
        
        if (mseUdId.isEmpty()) {
            throw new IllegalArgumentException("In Delete MSE DIR - MSE Udid should not be an empty String");
        }
        
        File mseDirectory = new File(rootMapLocation, EncryptionUtil.generateMD5(mseUdId));

        // Check if the Venue Directory Exists
        if (mseDirectory.getCanonicalFile().exists() && mseDirectory.getCanonicalFile().isDirectory()) {
            LOGGER.trace("Deleted Venue Directory for {}", mseDirectory);
            // Delete the Directory
            FileUtils.deleteDirectory(mseDirectory);
        }
    }      
}
