package com.cisco.cmxmobile.server.mse;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;
import com.cisco.cmxmobile.model.Floor;
import com.cisco.cmxmobile.model.Venue;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Component
public class AuthTokenCache 
{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthTokenCache.class);
    
    private final LoadingCache<String, String> cache;
    
    @Autowired
    private MobileServerCacheService mobileServerCacheService;
    
    //Some Key Seperator
    private static final String KEY_SEPERATOR = ":233338%$%088AB:";
    
    private static final long EXPIRE_AFTER_WRITE_MINS = 60;

    private static final long MAX_CACHE_SIZE = 10000;
    
    public AuthTokenCache()
    {
        //Build The Cache
        cache = CacheBuilder.newBuilder()
                .maximumSize(MAX_CACHE_SIZE)
                .expireAfterWrite(EXPIRE_AFTER_WRITE_MINS, TimeUnit.MINUTES)
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) throws Exception {
                        
                        String mseUdid = key.substring(0, key.indexOf(KEY_SEPERATOR));
                        String floorId = key.substring(key.indexOf(KEY_SEPERATOR) + KEY_SEPERATOR.length(), key.length());
                        
                        List<String> theString  = Splitter.on(KEY_SEPERATOR).splitToList(key);
                        theString.size();
                        
                        if (mseUdid.isEmpty() || floorId.isEmpty()) {
                            LOGGER.debug("Invalid Key : {}", key);
                            throw new IllegalArgumentException("Invalid Key : " + key);
                        }
                        String venueUdid = mobileServerCacheService.getVenueUdid(mseUdid, floorId);
                        
                        if (venueUdid == null) {
                            LOGGER.debug("There is no VenueID corresponding to MSEUDID : {} Floor ID : {}", mseUdid, floorId);
                            throw new Exception("There is no VenueID corresponding to MSEUDID : " + mseUdid + " Floor ID : " + floorId);
                        }
                                                
                        Venue venue = mobileServerCacheService.getVenue(venueUdid);
                        
                        if (venue == null) {
                            LOGGER.debug("There is no Venue corresponding to MSEUDID : {} Floor ID : {}", mseUdid, floorId);
                            throw new Exception("There is no Venue corresponding to MSEUDID : " + mseUdid + " FloorID : " + floorId);
                        }
                        
                        return venue.getAuthToken();
                    }
                });
    }
    
    public String getAuthToken(String mseUdId, String floorId) 
        throws ExecutionException 
    {
        return cache.get(createCacheKey(mseUdId, floorId));
    }
    
    public void removeAuthToken(List<Venue> venueList)
    {
        if (venueList == null) {
            return;
        }
        for (Venue venue : venueList) {
            List<Floor> floorList = venue.getFloorList();
            if (floorList != null) {
                for (Floor floor : floorList) {
                    cache.invalidate(createCacheKey(floor.getMseUdId(), floor.getMseFloorId()));
                }
            }
        }
    }
    
    private String createCacheKey(String mseUdid, String floorId)
    {
        return mseUdid + KEY_SEPERATOR + floorId;
    }
    
}
