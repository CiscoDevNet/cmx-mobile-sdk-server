package com.cisco.cmxmobile.cacheService.client;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import com.cisco.cmxmobile.cacheService.client.annotations.PersistableStrategy;

@Component
public class DeleteOperation 
{
    // Get Logger handler
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteOperation.class);
    
    @Autowired
    private RedisClient redisClient;
    
    @Autowired
    private ShardedJedisPool shardedJedisPool;

    public void delete(Object modelObject) throws CachePersistenceException
    {
        ShardedJedis shardedJedis = shardedJedisPool.getResource();

        try {
            
            ShardedJedisPipeline pipeline = shardedJedis.pipelined();
            
            deleteObjectGraph(pipeline, modelObject);
            
            pipeline.sync();
        
        } catch (JedisConnectionException e) {
            
            LOGGER.error("Failed to delete Object of type  : {} in Redis", modelObject.getClass().toString(), e);
    
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
    
    private void deleteObjectGraph(ShardedJedisPipeline pipeline, Object modelObject) throws CachePersistenceException
    {   
        if (modelObject == null) {
            LOGGER.trace("Attempted to delete a model which is null.");
            return;
        }
        
        try {
            for (Field field : modelObject.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (!ReflectionUtil.validateField(field) || field.getType().isPrimitive()) {
                    continue;
                }                
                
                LOGGER.trace("Delete Field : '{}'  Object of type '{}' : ", field.getName(), modelObject.getClass());
                
                if (field.getType() == java.util.List.class) {
                    PersistableStrategy persistableStrategy = field.getAnnotation(PersistableStrategy.class);
                    if (!(persistableStrategy != null && persistableStrategy.AsJson())) {
                        List myList = (List) field.get(modelObject);
                        for (int i = 0; myList != null && i < myList.size(); i++) {
                            Object objectInList = myList.get(i);
                            deleteObjectGraph(pipeline, objectInList);
                        }
                    }
                }
                else if (field.getType() == java.util.HashMap.class) {
                    //Map map = (Map) field.get(modelObject);

                    // MapKey: get the key for the model and then append the
                    // variable name
                    String persistentMapKey = Map.class.getSimpleName() + ":" + ReflectionUtil.getKey(modelObject) + ":" + field.getName();
                    
                    redisClient.deleteObject(persistentMapKey);
                }
                else if (!field.getType().isPrimitive()) {
                    deleteObjectGraph(pipeline, field.get(modelObject));
                }
            }

            
            LOGGER.trace("Deleting Model Object : '{}'", modelObject.getClass());
            
            redisClient.deleteObject(pipeline, ReflectionUtil.getKey(modelObject));
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
}
