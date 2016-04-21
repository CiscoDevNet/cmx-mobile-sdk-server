package com.cisco.cmxmobile.cacheService.client;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.cmxmobile.cacheService.client.annotations.Key;


public final class ReflectionUtil 
{
    private ReflectionUtil() {
    }
    
    public static Class<?> getClassForList(Field field)
    {
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        return (Class<?>) type.getActualTypeArguments()[0];
    }
    
    public static String getFieldValueAsJson(Object fieldValue) 
	throws CachePersistenceException
    {
	try {
	    ObjectMapper mapper = new ObjectMapper();
	    return mapper.writeValueAsString(fieldValue);
	} catch (JsonGenerationException e) {
	    throw new CachePersistenceException(e);
	} catch (JsonMappingException e) {
	    throw new CachePersistenceException(e);
	} catch (IOException e) {
	    throw new CachePersistenceException(e);
	}
    }
    
    public static boolean validateField(Field field)
    {
	int fieldModifiers = field.getModifiers();
	
	if (Modifier.isFinal(fieldModifiers)) {
	    return false;
	}
	
	//Check for JACOCO FIELD NAME
	if (Modifier.isTransient(fieldModifiers) && field.getName().equals("$jacocoData")) {
	    return false;
	}	
	
	return true;
    }
    
    // Get Key from Hash Of Keys
    public static String getKey(Class<?> clazz, Map<Integer, String> keysMap) {
        String modelKey = clazz.getName();
        SortedSet<Integer> keys = new TreeSet<Integer>(keysMap.keySet());
        for (Integer key : keys) {
            modelKey += ":" + keysMap.get(key);
        }
        return modelKey;
    }

    // Get Key from Object
    // TODO: Add some checking here so key are only primitive
    public static String getKey(Object object) throws IllegalArgumentException, IllegalAccessException {
        Logger logger = LoggerFactory.getLogger(ReflectionUtil.class);
        if (object == null) {
            logger.trace("Attempted to get key which is null. This get will not be done");
            return null;
        }
        Map<Integer, String> keysMap = new HashMap<Integer, String>();
        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Key.class)) {
                field.setAccessible(true);
                String fieldValue = "";
                if (field.getType() == int.class) {
                    fieldValue = Integer.toString((Integer) field.get(object));
                }
                else if (field.getType() == String.class) {
                    fieldValue = (String) field.get(object);
                }
                else {
                    logger.error("Field value not available for type cast {}", field.toString());
                }
                keysMap.put((((Key) field.getAnnotation(Key.class))).index(), fieldValue);
            }
        }
        return getKey(object.getClass(), keysMap);
    }
}
