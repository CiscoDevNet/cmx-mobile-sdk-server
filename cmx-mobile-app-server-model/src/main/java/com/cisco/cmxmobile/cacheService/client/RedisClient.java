package com.cisco.cmxmobile.cacheService.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

@Component
public class RedisClient {
    @Autowired
    private ShardedJedisPool shardedJedisPool;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisClient.class);

    public void saveObjectAsHashMap(String modelObjectKey, Map modelObjectMap) {
        // Save Object and dont expire
        saveObjectAsHashMap(modelObjectKey, modelObjectMap, -1);
    }

    public void saveObjectAsHashMap(String modelObjectKey, Map modelObjectMap, int expireTimeInSeconds) {
        // We have the Key and Map of Object
        // [Key,Map<Key,Value>]
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        
        try {
            // Set the Objects with defaults
            // String modelKey = getKey(model.getClass(), keysMap);
            shardedJedis.hmset(modelObjectKey, modelObjectMap);

            if (expireTimeInSeconds > 0) {
                shardedJedis.expire(modelObjectKey, expireTimeInSeconds);
            }

            String[] splitKeyArray = modelObjectKey.split(":");
            String classNameKey = splitKeyArray[0];

            // Dont save Objects of Type HashMap in a Class Set
            // TODO: See if it makes sense to make all persistable objects implement
            // Persistable
            // and then only persist those objects - For now HashMap is the one
            // encountered
            boolean isTypeHashMap = splitKeyArray[0].equals(Map.class.getSimpleName());

            if (!isTypeHashMap) {
                addToClassSet(shardedJedis, classNameKey, modelObjectKey, expireTimeInSeconds);
            }

        } catch (JedisConnectionException e) {
            
            LOGGER.error("Failed to save Object in Redis : {} ", modelObjectKey, e);
            
            if (shardedJedis != null) {
                shardedJedisPool.returnBrokenResource(shardedJedis);
            }
        } finally {
            if (shardedJedis != null) {
                shardedJedisPool.returnResource(shardedJedis);
            }
        }        
    }

    public Map<String, String> getObjectAsHash(String modelObjectKey) {
        // Get the Object
        // [Key,Map<Key,Value>]
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        
        try {
            return shardedJedis.hgetAll(modelObjectKey);
        } catch (JedisConnectionException e) {

            LOGGER.error("Failed to retrieve Object from Redis : {} ", modelObjectKey, e);

            if (shardedJedis != null) {
                shardedJedisPool.returnBrokenResource(shardedJedis);
            }
            return new HashMap<String, String>();
        } finally {
            if (shardedJedis != null) {
                shardedJedisPool.returnResource(shardedJedis);
            }
        }
    }

    public boolean exists(String modelObjectKey) 
    {
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        
        try {
            return shardedJedis.exists(modelObjectKey);
        } catch (JedisConnectionException e) {
            
            LOGGER.error("Failed to check if Object exists in Redis : {} ", modelObjectKey, e);

            if (shardedJedis != null) {
                shardedJedisPool.returnBrokenResource(shardedJedis);
            }
            return false;
        } finally {
            if (shardedJedis != null) {
                shardedJedisPool.returnResource(shardedJedis);
            }
        }
        
    }
    
    public void deleteObject(ShardedJedisPipeline pipeline, String modelObjectKey) 
    {
        //Delete the [Key,hashedObject] by key from Redis
        pipeline.del(modelObjectKey);
        
        //Delete Entry from the Class Name Set
        String[] splitKeyArray = modelObjectKey.split(":");
        String classNameKey = splitKeyArray[0];
        pipeline.zrem(classNameKey, modelObjectKey);        
    }

    
    public void deleteObject(String modelObjectKey) 
    {
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        
        try {
            //Delete the [Key,hashedObject] by key from Redis
            shardedJedis.del(modelObjectKey);
            
            //Delete Entry from the Class Name Set
            String[] splitKeyArray = modelObjectKey.split(":");
            String classNameKey = splitKeyArray[0];
            shardedJedis.zrem(classNameKey, modelObjectKey);
        } catch (JedisConnectionException e) {
            
            LOGGER.error("Failed to delete Object in Redis : {} ", modelObjectKey, e);

            if (shardedJedis != null) {
                shardedJedisPool.returnBrokenResource(shardedJedis);
                shardedJedis = null;
            }
        } finally {
            if (shardedJedis != null) {
                shardedJedisPool.returnResource(shardedJedis);
            }
        }
        
    }

    //
    // Set Operations
    //

    //
    // TODO:
    // 1. Limit the Size of Set to 100 and then add to another Set. Add a
    // pointer member to the next list list
    //

    /**
     * Add the Value to an existing Set. If the Set is empty then create a new
     * Set and add element to Set. From Redis Docs for operation on Sets : Add
     * the specified members to the set stored at key. Specified members that
     * are already a member of this set are ignored. If key does not exist, a
     * new set is created before adding the specified members. An error is
     * returned when the value stored at key is not a set.
     * 
     * @param key
     *            The Key to the List
     * @param modelObjectKeys
     *            The Value of the element to be inserted in the List
     * @return Number of Members added to the Set
     */
    public long addToClassSet(String modelObjectClassName, String modelObjectKey, int expireTimeInSeconds) {
        ShardedJedis shardedJedis = shardedJedisPool.getResource();

        long currentTimeMillis = System.currentTimeMillis();
        try {
            if (expireTimeInSeconds > 0) {
                shardedJedis.zremrangeByScore(modelObjectClassName, 0, currentTimeMillis);
                double expireTime = currentTimeMillis + (expireTimeInSeconds * 1000);
                return shardedJedis.zadd(modelObjectClassName, expireTime, modelObjectKey);
            }
            return shardedJedis.zadd(modelObjectClassName, 0, modelObjectKey); 
        } catch (JedisConnectionException e) {
            
            LOGGER.error("Failed to add class to Class Set in Redis : {} ", modelObjectClassName, e);
            
            if (shardedJedis != null) {
                shardedJedisPool.returnBrokenResource(shardedJedis);
                shardedJedis = null;
            }
            return 0;
        } finally {
            if (shardedJedis != null) {
                shardedJedisPool.returnResource(shardedJedis);
            }
        }
    }
    
    public long addToClassSet(ShardedJedis shardedJedis, String modelObjectClassName, String modelObjectKey, int expireTimeInSeconds) {
        long currentTimeMillis = System.currentTimeMillis();
        if (expireTimeInSeconds > 0) {
            shardedJedis.zremrangeByScore(modelObjectClassName, 0, currentTimeMillis);
            double expireTime = currentTimeMillis + (expireTimeInSeconds * 1000);
            return shardedJedis.zadd(modelObjectClassName, expireTime, modelObjectKey);
        }
        return shardedJedis.zadd(modelObjectClassName, 0, modelObjectKey); 
    }

    /**
     * Get all the Members of the the Set. Members can be references to other
     * objects.
     * 
     * @param key
     *            The Key of the Set
     * @return Members of the Set
     */
    public Set<String> getAllModelObjectKeys(String modelObjectClassName) {
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        
        try {
            return shardedJedis.zrange(modelObjectClassName, 0, -1);
        } catch (JedisConnectionException e) {
            
            LOGGER.error("Failed to get All Model Object Keys from Redis : {} ", modelObjectClassName, e);
            
            if (shardedJedis != null) {
                shardedJedisPool.returnBrokenResource(shardedJedis);
                shardedJedis = null;
            }
            return new HashSet<String>();
        } finally {
            if (shardedJedis != null) {
                shardedJedisPool.returnResource(shardedJedis);
            }
        }
    }

    /**
     * Get the count of all the Members in a Set. Members can be references to other
     * objects.
     * 
     * @param key
     *            The Key of the Set
     * @return Members of the Set
     */
    public long getAllModelObjectCount(String modelObjectClassName) {
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        
        try {
            return shardedJedis.zcard(modelObjectClassName);
        } catch (JedisConnectionException e) {
            
            LOGGER.error("Failed to get All Model Object Keys from Redis : {} ", modelObjectClassName, e);
            
            if (shardedJedis != null) {
                shardedJedisPool.returnBrokenResource(shardedJedis);
                shardedJedis = null;
            }
            return 0;
        } finally {
            if (shardedJedis != null) {
                shardedJedisPool.returnResource(shardedJedis);
            }
        }
    }
}
