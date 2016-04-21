package com.cisco.cmxmobile.cacheService.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.cmxmobile.cacheService.client.CachePersistenceException;

public enum SetupSource 
{    
    APP_ENGAGE, LOCATION_MODULE, SOURCE_NOT_CONFIGURED;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SetupSource.class);
    
    private static final Map<String, SetupSource> STRING_TO_ENUM_MAP = new HashMap<String, SetupSource>();
    
    static {
        for (SetupSource source : values()) {
            STRING_TO_ENUM_MAP.put(source.toString(), source);
        }
    }
    
    public void apply(String mseUdid) {
        switch (this) {
            case APP_ENGAGE :
            case LOCATION_MODULE :
                try {
                    SetupCacheAccess.setupCacheService.updateOrAddSetupSource(mseUdid, toString());
                }
                catch (CachePersistenceException e) {
                    LOGGER.error("Failed to Apply Source Setup Information for {} - MSEUDID : {} ", toString(), mseUdid, e);
                }
                break;
            default :
                LOGGER.error("Update or Add wont be applied for Unknown Source - MSEUDID : {} ", mseUdid);
        }
    }    
    
    public static SetupSource getSource(String mseUdid) {
        String source = SetupCacheAccess.setupCacheService.getAssociatedSource(mseUdid);
        if (source == null) {
            return SetupSource.SOURCE_NOT_CONFIGURED;
        }
        
        if (STRING_TO_ENUM_MAP.containsKey(source)) {
            return STRING_TO_ENUM_MAP.get(source);
        }
        
        return SetupSource.SOURCE_NOT_CONFIGURED; 
    }
}
