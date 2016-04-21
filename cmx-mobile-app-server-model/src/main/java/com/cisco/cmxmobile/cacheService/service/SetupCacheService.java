package com.cisco.cmxmobile.cacheService.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.cacheService.client.CachePersistenceException;
import com.cisco.cmxmobile.cacheService.client.MobileServerCacheClient;
import com.cisco.cmxmobile.model.AssociationMap;

@Component
public class SetupCacheService 
{
    @Autowired
    private MobileServerCacheClient mobileServerCacheClient;
    
    private static final String SETUPSOURCE_UNIQUEKEY = "setupSource:2485948161615890";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SetupCacheService.class);
    
    private AssociationMap setupAssociationMap;
    
    public AssociationMap getSetupSourceAssociationMap()
    {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(AssociationMap.ID, SETUPSOURCE_UNIQUEKEY);
        return mobileServerCacheClient.get(AssociationMap.class, properties);
    }
    
    public String getAssociatedSource(String mseUdid) 
    {
        synchronized (this) {
            if (setupAssociationMap == null) {
                setupAssociationMap = getSetupSourceAssociationMap();
            }
            
            if (setupAssociationMap == null) {
                LOGGER.info("Setup Association Map is not present");
                return null;
            }
            
            return setupAssociationMap.getAssociatedEntity(mseUdid);
        }
    }
    
    public void updateOrAddSetupSource(String mseUdid, String source) 
        throws CachePersistenceException 
    {
        synchronized (this) {
            if (setupAssociationMap == null) {
                setupAssociationMap = new AssociationMap(SETUPSOURCE_UNIQUEKEY);
            }
            
            setupAssociationMap.addAssociation(mseUdid, source);
            
            mobileServerCacheClient.save(setupAssociationMap);
        } 
    }
}
