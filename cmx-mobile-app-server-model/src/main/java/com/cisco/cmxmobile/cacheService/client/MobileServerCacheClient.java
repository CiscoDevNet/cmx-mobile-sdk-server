package com.cisco.cmxmobile.cacheService.client;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public interface MobileServerCacheClient {
    public String save(final Object model, final int expireTimeInSeconds) throws CachePersistenceException;

    public String save(final Object model) throws CachePersistenceException;

    public <T> T get(Class<T> clazz, Map<String, String> properties);

    public Object getObjectByKey(String key);

    public boolean exists(Object modelObject) throws IllegalArgumentException, IllegalAccessException;

    public <T> List<T> getObjectsByClass(Class<T> clazz);
    
    public <T> long getCountByClass(Class<T> clazz);
    
    public <T> T getObjectByHashValue(Class<T> clazz, Field hashField, Object hashValue);
    
    public void delete(Object modelObject) throws CachePersistenceException;
}
