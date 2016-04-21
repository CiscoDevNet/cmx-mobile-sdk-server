package com.cisco.cmxmobile.cacheService.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cisco.cmxmobile.model.AssociationMap;
import com.cisco.cmxmobile.model.Campaign;
import com.cisco.cmxmobile.model.CampaignRule;

import static org.testng.Assert.*;

@ContextConfiguration(locations={"classpath:spring/application-context.xml"})
public class RedisClientTest extends AbstractTestNGSpringContextTests
{	
	@Autowired
	private MobileServerCacheRedisClient mobileServerCacheRedisClient;
	
	private static final String ASSOCIATION_MAP_ID = "abcd-9999";
	private static final String ANOTHER_ASSOCIATION_MAP_ID = "xyz-1000";
			
	//@BeforeClass
	public void setUp() {
		// code that will be invoked when this test is instantiated
		String associationMapKey = ASSOCIATION_MAP_ID;
		
		AssociationMap map = new AssociationMap(associationMapKey);
		map.addAssociation("abc", "alphabet");
		map.addAssociation("123", "number");
		map.addAssociation("apple", "fruit");

		String anotherAssociationMapKey = ANOTHER_ASSOCIATION_MAP_ID;
		
		AssociationMap anotherMap = new AssociationMap(anotherAssociationMapKey);
		anotherMap.addAssociation("1000_abc", "1000_alphabet");
		anotherMap.addAssociation("1000_123", "1000_number");
		anotherMap.addAssociation("1000_apple", "1000_fruit");

		try {
			mobileServerCacheRedisClient.save(map);
			mobileServerCacheRedisClient.save(anotherMap);
		} catch (Exception e) {
			fail("Failed to Save Associated Map", e);
		} 		
	}
	
	//@Test
    public void testHashMapAdd() 
	{
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(AssociationMap.ID, "abcd-9999");
		AssociationMap mapFromHash = (AssociationMap) mobileServerCacheRedisClient.get(AssociationMap.class, properties);
		
		assertEquals(mapFromHash.getSize(), 3);
		assertEquals(mapFromHash.getAssociatedEntity("abc"), "alphabet");		
	}
	
	//@Test
    public void testGetMembersOfSet() 
	{
        List<AssociationMap> associationMapListForTest = mobileServerCacheRedisClient.getObjectsByClass(AssociationMap.class);
		
		assertNotNull(associationMapListForTest);
		for (AssociationMap associationMap : associationMapListForTest) {
			if (associationMap.getId().equals(ASSOCIATION_MAP_ID) || 
					associationMap.getId().equals(ANOTHER_ASSOCIATION_MAP_ID)) 
			{
				associationMapListForTest.add(associationMap);
			}
		}
		
		assertEquals(associationMapListForTest.size(), 2);
	}
    
    //@Test
    public void testSavingAsJson()
    {
	Campaign campaign = new Campaign();
	campaign.setCampaignUdid("10000011111");
	
	List<CampaignRule> campaignRuleList = new ArrayList<CampaignRule>();
	
	CampaignRule rule1 = new CampaignRule();
	rule1.setId("100");
	rule1.setStartTime("Some Start Time");
	campaignRuleList.add(rule1);
	
	CampaignRule rule2 = new CampaignRule();
	rule2.setId("200");
	rule2.setStartTime("Some Second Start Time");
	campaignRuleList.add(rule2);
	
	campaign.setRules(campaignRuleList);
	try {
	    mobileServerCacheRedisClient.save(campaign);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} 
	
	List<Campaign> campaignRuleListFromCache = mobileServerCacheRedisClient.getObjectsByClass(Campaign.class);
	
	assertEquals(campaignRuleListFromCache.size(), 1);
    }
}
