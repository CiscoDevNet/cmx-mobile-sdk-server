package com.cisco.cmxmobile.cacheService.client;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;

import com.cisco.cmxmobile.cacheService.client.annotations.Key;
import com.cisco.cmxmobile.cacheService.client.annotations.MaskLogEntry;
import com.cisco.cmxmobile.cacheService.client.annotations.PersistableStrategy;
import com.cisco.cmxmobile.model.DeviceType;

@Primary
public class MobileServerCacheRedisClient implements MobileServerCacheClient {
    // Get Logger handler
    private static final Logger LOGGER = LoggerFactory.getLogger(MobileServerCacheRedisClient.class);

    private static final int DEFAULT_INT_VALUE = 0;
    
    private static final long DEFAULT_LONG_VALUE = 0L;
    
    private static final double DEFAULT_DOUBLE_VALUE = 0L;
    
    private static final boolean DEFAULT_BOOLEAN_VALUE = false;
    
    private static final float DEFAULT_FLOAT_VALUE = 0L;
    
    @Autowired
    private RedisClient redisClient;
    
    @Autowired
    private DeleteOperation deleteOperation;

    public String save(final Object model) throws CachePersistenceException {
    	return save(model, -1);
    }
    
    @SuppressWarnings("rawtypes")
    public String save(final Object model, final int expireTimeInSeconds) throws CachePersistenceException {
        if (model == null) {
            LOGGER.trace("Attempted to save a model which is null. This object will ignored and not saved");
            return null;
        }
        try {
            final Map<String, String> hashedObject = new HashMap<String, String>();
            Map<Integer, String> keysMap = new HashMap<Integer, String>();
            List<String> maskLogEntries = new ArrayList<String>();
            for (Field field : model.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (!ReflectionUtil.validateField(field)) {
                    LOGGER.trace("Validation failed for  : '{}'", field.getName());
                    continue;
                }                
                LOGGER.trace("Save Field : '{}' of Object of type '{}' : ", field.getName(), model.getClass());
                
                String fieldValue = "";
                if (field.getType() == int.class) {
                    fieldValue = Integer.toString((Integer) field.get(model));
                }
                else if (field.getType() == String.class) {
                    fieldValue = (String) field.get(model);
                }
                else if (field.getType() == long.class) {
                    fieldValue = Long.toString((Long) field.get(model));
                }
                else if (field.getType() == boolean.class) {
                    fieldValue = Boolean.toString((Boolean) field.get(model));
                }
                else if (field.getType() == java.util.List.class) {
                    PersistableStrategy persistableStrategy = field.getAnnotation(PersistableStrategy.class);
                    if (persistableStrategy != null && persistableStrategy.AsJson()) {
                        //Save as JSON
                        fieldValue = ReflectionUtil.getFieldValueAsJson((List) field.get(model));
                    }
                    else {
                        List myList = (List) field.get(model);
                        String listofKeys = "";
                        for (int i = 0; myList != null && i < myList.size(); i++) {
                            Object objectInList = myList.get(i);
                            listofKeys += getKey(objectInList) + ",";

                            save(objectInList);
                        }

                        fieldValue = listofKeys;
                    }
                }
                else if (field.getType() == java.util.HashMap.class) {
                    Map map = (Map) field.get(model);

                    // MapKey: get the key for the model and then append the
                    // variable name
                    String persistentMapKey = Map.class.getSimpleName() + ":" + getKey(model) + ":" + field.getName();

                    Map persistentMap = new HashMap();

                    // SaveMap
                    if (map != null) {
                        Iterator iterator = map.entrySet().iterator();
                        boolean wasKeySet = false;
                        while (iterator.hasNext()) {
                            Map.Entry mapEntry = (Map.Entry) iterator.next();

                            // TODO: Partial Code for supporting persisting
                            // Hash<Object, Object>
                            /*
                             * String mapKey = ""; String mapValue = ""; if
                             * (!mapEntry.getKey().getClass().isPrimitive()) {
                             * mapKey = save(mapEntry.getKey()); } else { mapKey =
                             * mapEntry.getKey().toString(); } if
                             * (!mapEntry.getValue().getClass().isPrimitive()) {
                             * mapValue = save(mapEntry.getValue()); } else {
                             * mapValue = mapEntry.getValue().toString(); }
                             */

                            if (mapEntry.getKey() != null) {
                                wasKeySet = true;
                                persistentMap.put(mapEntry.getKey(), mapEntry.getValue());
                            }
                        }

                        if (wasKeySet) {
                            redisClient.saveObjectAsHashMap(persistentMapKey, persistentMap, expireTimeInSeconds);
                        }
                    }
                    else {
                        // The Value is Null
                        persistentMapKey = null;
                    }

                    fieldValue = persistentMapKey;
                }
                else if (field.getType() == float.class) {
                    fieldValue = Float.toString((Float) field.get(model));
                }
                else if (field.getType() == double.class) {
                    fieldValue = Double.toString((Double) field.get(model));
                }
                else if (field.getType() == UUID.class) {
                    fieldValue = field.get(model).toString();
                }
                else if (field.getType() == DeviceType.class) {
                    fieldValue = field.get(model).toString();
                }
                else if (!field.getType().isPrimitive()) {
                    fieldValue = save(field.get(model));
                }
                else {
                    String fieldTypeError = new StringBuffer("Field Type").append(field.getType()).
                        append(" not supported yet - This field will not be persisted.").toString();

                    LOGGER.error(fieldTypeError);

                    throw new CachePersistenceException(fieldTypeError);
                }

                if (field.isAnnotationPresent(Key.class)) {
                    keysMap.put((((Key) field.getAnnotation(Key.class))).index(), fieldValue);
                }
                
                if (field.isAnnotationPresent(MaskLogEntry.class)) {
                    maskLogEntries.add(field.getName());
                }

                fieldValue = fieldValue == null ? "" : fieldValue;
                hashedObject.put(field.getName(), fieldValue);
            }

            printHash(hashedObject, maskLogEntries);

            String modelKey = getKey(model.getClass(), keysMap);
            
            LOGGER.trace("Saving Model Object : '{}' with Key '{}'", model.getClass(), modelKey);
            
            redisClient.saveObjectAsHashMap(modelKey, hashedObject, expireTimeInSeconds);
            return modelKey;
        }
        catch (IllegalArgumentException e) {
            throw new CachePersistenceException(e);
        }
        catch (IllegalAccessException e) {
            throw new CachePersistenceException(e);
        }
        catch (Exception e) {
            throw new CachePersistenceException(e);
        }
    }

    // Right now it is only getting objects stored as Hashes
    public <T> T get(Class<T> clazz, Map<String, String> properties) {
        // Look of Attributes marked as keys
        Map<Integer, String> keysMap = new HashMap<Integer, String>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Key.class)) {
                keysMap.put((((Key) field.getAnnotation(Key.class))).index(), properties.get(field.getName()));
            }
        }

        /*
         * //Get the Object //[Key,Map<Key,Value>] ShardedJedis shardedJedis =
         * shardedJedisPool.getResource(); //Get the Objects with defaults
         * String modelKey = getKey(clazz, keysMap); Map<String, String>
         * clazzFields = shardedJedis.hgetAll(modelKey);
         * shardedJedisPool.returnResource(shardedJedis);
         */

        String modelKey = getKey(clazz, keysMap);
        Map<String, String> clazzFields = redisClient.getObjectAsHash(modelKey);

        return getObject(clazz, clazzFields);
    }

    public Object getObjectByKey(String key) {
        if (key == null || key.length() <= 0) {
            LOGGER.trace("Attempted to get object by key which is null. This get will not be done");
            return null;
        }
        // 1. First get value from Redis Cache
        /*
         * ShardedJedis shardedJedis = shardedJedisPool.getResource();
         * Map<String, String> modelClazzFields = shardedJedis.hgetAll(key);
         * shardedJedisPool.returnResource(shardedJedis);
         */
        Map<String, String> modelClazzFields = redisClient.getObjectAsHash(key);

        // 2. From the Key get the class
        String[] splitKey = key.split(":", 2);
        try {
            Class<?> clazz = Class.forName(splitKey[0]);
            return getObject(clazz, modelClazzFields);
        }
        catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            LOGGER.error("Class not found for {}", key);
            LOGGER.error("ClassNotFoundException", e);
        }

        return null;
    }

    public boolean exists(Object modelObject) throws IllegalArgumentException, IllegalAccessException {
        String key = getKey(modelObject);

        /*
         * ShardedJedis shardedJedis = shardedJedisPool.getResource(); boolean
         * exists = shardedJedis.exists(key);
         * shardedJedisPool.returnResource(shardedJedis);
         */

        return redisClient.exists(key);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <T> T getObject(Class<T> clazz, Map<String, String> modelHashMap) {
        T classInstance = null;

        if (modelHashMap != null && !modelHashMap.isEmpty()) {
            // Construct the Object
            try {
                classInstance = clazz.newInstance();

                for (Field field : classInstance.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    if (Modifier.isFinal(field.getModifiers())) {
                        continue;
                    }
                    
                    String fieldValue = modelHashMap.get(field.getName());
                    if (field.getType() == int.class) {
                        if (fieldValue == null) {
                            field.setInt(classInstance, DEFAULT_INT_VALUE);
                        } else {
                            field.setInt(classInstance, new Integer(modelHashMap.get(field.getName())).intValue());
                        }
                    }
                    else if (field.getType() == String.class) {
                        field.set(classInstance, modelHashMap.get(field.getName()));
                    }
                    else if (field.getType() == long.class) {
                        if (fieldValue == null) {
                            field.setLong(classInstance, DEFAULT_LONG_VALUE);
                        } else {
                            field.setLong(classInstance, new Long(modelHashMap.get(field.getName())).longValue());
                        }
                    }
                    else if (field.getType() == double.class) {
                        if (fieldValue == null) {
                            field.setDouble(classInstance, DEFAULT_DOUBLE_VALUE);
                        } else {
                            field.setDouble(classInstance, new Double(modelHashMap.get(field.getName())).doubleValue());
                        }
                    }
                    else if (field.getType() == boolean.class) {
                        if (fieldValue == null) {
                            field.setBoolean(classInstance, DEFAULT_BOOLEAN_VALUE);
                        } else {
                            field.setBoolean(classInstance, new Boolean(modelHashMap.get(field.getName())).booleanValue());
                        }
                    }
                    else if (field.getType() == List.class) {
                        PersistableStrategy persistableStrategy = field.getAnnotation(PersistableStrategy.class);
                        if (persistableStrategy != null && persistableStrategy.AsJson()) {
                            Class<?> listType = ReflectionUtil.getClassForList(field);
                            ObjectMapper mapper = new ObjectMapper();
                            String abcVal = modelHashMap.get(field.getName());
                            if (abcVal != null) {
                                try {
                                    List<?> myObjects = mapper.readValue(abcVal, mapper.getTypeFactory().constructCollectionType(List.class, listType));
                                    field.set(classInstance, myObjects);
                                }
                                catch (JsonParseException e) {
                                    // TODO Auto-generated catch block
                                    LOGGER.error("JsonParseException", e);
                                }
                                catch (JsonMappingException e) {
                                    // TODO Auto-generated catch block
                                    LOGGER.error("JsonMappingException", e);
                                }
                                catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    LOGGER.error("IOException", e);
                                }
                            }
                            // List<?> myObjects = mapper.readValue(abcVal, );
                        }
                        else {
                            List list = new ArrayList();
                            String myVal = modelHashMap.get(field.getName());
                            String[] entries = myVal.split(",");
                            for (int i = 0; i < entries.length; i++) {
                                if (entries[i].length() > 0) {
                                    Object listObject = getObjectByKey(entries[i]);
                                    if (listObject != null) {
                                        list.add(listObject);
                                    }
                                }
                            }
                            field.set(classInstance, list);
                        }
                    }
                    else if (field.getType() == HashMap.class) {
                        Map map = null;
                        String hashKey = modelHashMap.get(field.getName());

                        if (!hashKey.isEmpty()) {
                            map = new HashMap();

                            Map<String, String> hashFields = redisClient.getObjectAsHash(hashKey);

                            Iterator iterator = hashFields.entrySet().iterator();
                            while (iterator.hasNext()) {
                                Map.Entry<String, String> mapEntry = (Map.Entry) iterator.next();

                                String keyFromPersistentHash = mapEntry.getKey();
                                String valueFromPersistentHash = mapEntry.getValue();

                                // TODO: Make it more generic to retrieve
                                // Objects of any kind
                                /*
                                 * Object key = ""; Object value = ""; if
                                 * (keyFromPersistentHash ->> Check if its a
                                 * class then traverse) { key =
                                 * getObject(keyFromPersistentHash); }
                                 */

                                map.put(keyFromPersistentHash, valueFromPersistentHash);
                            }

                        }
                        field.set(classInstance, map);
                    }
                    else if (field.getType() == float.class) {
                        if (fieldValue == null) {
                            field.setFloat(classInstance, DEFAULT_FLOAT_VALUE);
                        } else {
                            field.setFloat(classInstance, new Float(modelHashMap.get(field.getName())).floatValue());
                        }
                    }
                    else if (field.getType() == UUID.class) {
                        field.set(classInstance, UUID.fromString(modelHashMap.get(field.getName())));
                    }
                    else if (field.getType() == DeviceType.class) {
                        field.set(classInstance, DeviceType.fromString(modelHashMap.get(field.getName())));
                    }
                    else if (!field.getType().isPrimitive()) {
                        LOGGER.trace("Getting object by key for: {}", field.getName());
                        field.set(classInstance, getObjectByKey(modelHashMap.get(field.getName())));
                    }
                    else {
                        LOGGER.equals("Field Type : " + field.getType() + " not supported yet - This field will not be persisted.");
                    }
                }
            }
            catch (InstantiationException e) {
                // TODO Auto-generated catch block
                LOGGER.error("InstantiationException", e);
            }
            catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                LOGGER.error("IllegalAccessException", e);
            }
        }
        // Return the object
        return classInstance;
    }

    // Get Key from Hash Of Keys
    private String getKey(Class<?> clazz, Map<Integer, String> keysMap) {
        String modelKey = clazz.getName();
        SortedSet<Integer> keys = new TreeSet<Integer>(keysMap.keySet());
        for (Integer key : keys) {
            modelKey += ":" + keysMap.get(key);
        }
        return modelKey;
    }

    // Get Key from Object
    // TODO: Add some checking here so key are only primitive
    private String getKey(Object object) throws IllegalArgumentException, IllegalAccessException {
        if (object == null) {
            LOGGER.trace("Attempted to get key which is null. This get will not be done");
            return null;
        }
        Map<Integer, String> keysMap = new HashMap<Integer, String>();
        for (Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            String fieldValue = "";
            if (field.getType() == int.class) {
                fieldValue = Integer.toString((Integer) field.get(object));
            }
            else if (field.getType() == String.class) {
                fieldValue = (String) field.get(object);
            }
            else {
                LOGGER.trace("Field value not available for type {}", field.toString());
            }

            if (field.isAnnotationPresent(Key.class)) {
                keysMap.put((((Key) field.getAnnotation(Key.class))).index(), fieldValue);
            }
        }
        return getKey(object.getClass(), keysMap);
    }

    public <T> T getObjectByHashValue(Class<T> clazz, Field hashField, Object hashValue) {
        Set<String> modelObjectKeys = redisClient.getAllModelObjectKeys(clazz.getName());
        Iterator<String> iterator = modelObjectKeys.iterator();
        hashField.setAccessible(true);
        while (iterator.hasNext()) {
            String modelObjectKey = iterator.next();

            if (redisClient.exists(modelObjectKey)) {
                T searchObject = (T) getObjectByKey(modelObjectKey);
                try {
                    if (hashField.get(searchObject).equals(hashValue)) {
                        return searchObject;
                    }
                } catch (Exception ex) {
                    LOGGER.trace("Unable to find hash field value {}", ex);                    
                }
            }
        }
        return null;
    }
    
    public <T> long getCountByClass(Class<T> clazz) {
        return redisClient.getAllModelObjectCount(clazz.getName());
    }

    public <T> List<T> getObjectsByClass(Class<T> clazz) {
        Set<String> modelObjectKeys = redisClient.getAllModelObjectKeys(clazz.getName());
        List<T> objectsByClassList = new ArrayList<T>();
        Iterator<String> iterator = modelObjectKeys.iterator();
        while (iterator.hasNext()) {
            String modelObjectKey = iterator.next();

            if (redisClient.exists(modelObjectKey)) {
                objectsByClassList.add((T) getObjectByKey(modelObjectKey));
            }
        }

        return objectsByClassList;
    }
    
    @Override
    public void delete(Object modelObject) throws CachePersistenceException {
        deleteOperation.delete(modelObject);
    }

    /*
     * private class ObjectWrapper { private String clazz; public
     * ObjectWrapper(String clazz){ this.clazz } }
     */

    private void printHash(Map<String, String> hashedObject, List<String> maskLogEntries) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Display the key/value of HashMap.");
            Iterator mapIterator = hashedObject.entrySet().iterator();
            while (mapIterator.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) mapIterator.next();
                String keyValue = (String) mapEntry.getKey();
                String value = (String) mapEntry.getValue();
                if (maskLogEntries.contains(keyValue)) {
                    value = "*******";
                }
                LOGGER.trace("Key : " + keyValue + "= Value : " + value);
            }
        }
    }
}
