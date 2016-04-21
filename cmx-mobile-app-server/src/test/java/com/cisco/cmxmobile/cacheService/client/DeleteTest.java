package com.cisco.cmxmobile.cacheService.client;

import static org.testng.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cisco.cmxmobile.model.AssociationMap;

@ContextConfiguration(locations={"classpath:spring/application-context.xml"})
public class DeleteTest extends AbstractTestNGSpringContextTests
{    
    @Autowired
    private MobileServerCacheRedisClient mobileServerCacheRedisClient;
    
    private AssociationMap associationMap1;
    private AssociationMap associationMap2;
    private AssociationMap associationMap3;

    private static final String ASSOCIATION_MAP_1_ID = "abcd-9999";
    private static final String ASSOCIATION_MAP_2_ID = "dddd-4453";
    private static final String ASSOCIATION_MAP_3_ID = "xyz-1000";
            
    //@BeforeClass
    public void setUp() {
        // code that will be invoked when this test is instantiated
        
        String associationMap_1_Key = ASSOCIATION_MAP_1_ID;
        
        associationMap1 = new AssociationMap(associationMap_1_Key);
        associationMap1.addAssociation("abc", "alphabet");
        associationMap1.addAssociation("123", "number");
        associationMap1.addAssociation("apple", "fruit");

        String associationMap_2_Key = ASSOCIATION_MAP_2_ID;
        
        associationMap2 = new AssociationMap(associationMap_2_Key);
        associationMap2.addAssociation("1000_abc", "1000_alphabet");
        associationMap2.addAssociation("1000_123", "1000_number");
        associationMap2.addAssociation("1000_apple", "1000_fruit");
        
        String associationMap_3_Key = ASSOCIATION_MAP_3_ID;
        
        associationMap3 = new AssociationMap(associationMap_3_Key);
        associationMap3.addAssociation("xxx_abc", "yyy_alphabet");
        associationMap3.addAssociation("xxx_123", "yyy_number");
        associationMap3.addAssociation("xxx_apple", "yyy_fruit");

        try {
            mobileServerCacheRedisClient.save(associationMap1);
            mobileServerCacheRedisClient.save(associationMap2);
            mobileServerCacheRedisClient.save(associationMap3);
        } catch (Exception e) {
            fail("Failed to Save Associated Map", e);
        }       
    }
        
    //@Test(priority = 1)
    public void delete_one_associationMap()
    {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(AssociationMap.ID, associationMap1.getId());
        AssociationMap associationMapFromCache = 
            mobileServerCacheRedisClient.get(AssociationMap.class, properties);
        
        Assert.notNull(associationMapFromCache);
        
        try {
            mobileServerCacheRedisClient.delete(associationMap1);
            
            //Try Again to get from Cache after deletion
            associationMapFromCache = 
                mobileServerCacheRedisClient.get(AssociationMap.class, properties);
            
            Assert.isNull(associationMapFromCache);

        }
        catch (CachePersistenceException e) {
            fail("Failed to Delete Associated Map", e);
        }
    }
    
    //@Test(priority = 2)
    public void delete_all_associationMap()
    {
        List<AssociationMap> associationMapList = mobileServerCacheRedisClient.getObjectsByClass(AssociationMap.class);
        
        Assert.isTrue(associationMapList.size() == 2);
        
        try {
            mobileServerCacheRedisClient.delete(associationMap2);
            mobileServerCacheRedisClient.delete(associationMap3);
            
            //Try Again to get from Cache after deletion
            associationMapList = mobileServerCacheRedisClient.getObjectsByClass(AssociationMap.class);
            
            Assert.isTrue(associationMapList.size() == 0);
        }
        catch (CachePersistenceException e) {
            fail("Failed to Delete Associated Map", e);
        }
    }


}
