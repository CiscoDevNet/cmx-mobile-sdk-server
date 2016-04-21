package com.cisco.cmxmobile.cacheService.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SetupCacheAccess 
{
    @Autowired
    public static SetupCacheService setupCacheService;

    @Autowired
    public void setSetupCacheService(SetupCacheService setupCacheService) {
        SetupCacheAccess.setupCacheService = setupCacheService;
    }

}
