package com.cisco.server.cmxmobile.cacheService;


import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import com.cisco.cmxmobile.cacheService.client.CachePersistenceException;
import com.cisco.cmxmobile.cacheService.client.MobileServerCacheException;
import com.cisco.cmxmobile.model.WirelessClient;
import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;
import com.cisco.cmxmobile.cacheService.client.MobileServerCacheRedisClient;
import com.cisco.cmxmobile.model.FloorPathInfo;
import com.cisco.cmxmobile.model.DeviceType;
import com.cisco.cmxmobile.model.ZonePoint;
import com.cisco.cmxmobile.model.WirelessClient;
import com.cisco.cmxmobile.model.Floor;
import com.cisco.cmxmobile.model.PointOfInterest;
import com.cisco.cmxmobile.model.Zone;
import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;

@ContextConfiguration(locations={"classpath:spring/application-context.xml"})
public class CacheFunctionalTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private MobileServerCacheService mobileServerCacheService;
	
	@Autowired
	private MobileServerCacheRedisClient mobileServerCacheRedisClient;
	
	//@BeforeMethod
	//@BeforeClass
	public void setUp() {
		// code that will be invoked when this test is instantiated
		
	}
	
	//@Test
    public void testWirelessClientAdd() 
	{
		//TODO: adding mac address hashing will break this test
		String clientMAC = "10:11:12:11:3A:7F";
		String pushToken = "uknown format";
		float xCoord = 99.9f;
		float yCoord = 88.8f;
		
		WirelessClient client = new WirelessClient(clientMAC, "100", "1000");
		client.setX(xCoord);
		client.setY(yCoord);
		client.setPushNotificationRegistrationId(pushToken);
		client.setDeviceType(DeviceType.IOS);

		
		try {
			mobileServerCacheService.addOrUpdateWirelessClient(client, -1, false);
		} catch (MobileServerCacheException e) {
			e.printStackTrace();
	        Assert.fail(e.getMessage());
		}
		
		WirelessClient clientFromCache = mobileServerCacheService.getWirelessClient(clientMAC);
        Assert.assertEquals(clientFromCache.getMacAddress(), clientMAC);
        Assert.assertEquals(clientFromCache.getX(), xCoord);
        Assert.assertEquals(clientFromCache.getY(), yCoord);
        Assert.assertNull(clientFromCache.getZoneList());
        Assert.assertEquals(clientFromCache.getDeviceType(), DeviceType.IOS);
        System.out.println("@Test - testWirelessClientAdd");
        
        WirelessClient clientFromCache2 = mobileServerCacheService.getWirelessClient(clientMAC);
        Assert.assertEquals(clientFromCache.getPassword(), clientFromCache2.getPassword());
    }
	
	//@Test
    public void testWirelessClientPresence() 
	{
		String clientMAC = "99:99:99:99:99:99";
				
		WirelessClient clientFromCache = mobileServerCacheService.getWirelessClient(clientMAC);
        Assert.assertNull(clientFromCache);
        System.out.println("@Test - testWirelessClientPresence");
    }
	
	@Test
    public void testPointOfInterests() 
	{
		/*List<PointOfInterest> poiList = new ArrayList<PointOfInterest>();
		PointOfInterest poi_conference = new PointOfInterest();
		poi_conference.setName("Conference Room");
		poi_conference.setDescription("Conference Room on the fourth floor");
		poi_conference.setVenueId(100);
		poi_conference.setFloorId(1000);
		
		MobileServerCacheService.addOrUpdatePointOfInterest(poiList);
		
		List<PointOfInterest> poiListByFloor = MobileServerCacheService.getPointOfInterestByFloor(100, 1000);
        Assert.assertEquals(poiListByFloor.size(), 1);
        
        Assert.assertEquals(poiListByFloor.get(0).getName(), "Conference Room");
		
        System.out.println("@Test - testWirelessClientPresence");*/
    }
	
	//@Test
	public void testSaveList()
	{
		//Create POI
		String mseUdId = "189";
		String venueId = "88";
		String venueUdId = mseUdId + ":" + venueId;
		String floorId = "100";
		
		Floor floor = new Floor();
		//floor.setMseVenueId(venueId);
		floor.setMseFloorId(floorId);
		floor.setVenueUdId(venueUdId);
		//floor.setMseUdId(mseUdId);
		
		List<PointOfInterest> poiList = new ArrayList<PointOfInterest>();
		
		PointOfInterest poi = new PointOfInterest();
		poi.setId("555");
		poi.setVenueUdId(floor.getVenueUdId());
		poi.setMseFloorId(floor.getMseFloorId());
		poiList.add(poi);
		
		PointOfInterest poi2 = new PointOfInterest();
		poi2.setId("556");
		poi.setVenueUdId(floor.getVenueUdId());
		poi2.setMseFloorId(floor.getMseFloorId());
		poiList.add(poi2);

		floor.setPoiList(poiList);
		
		List<Zone> zoneList = new ArrayList<Zone>();
		List<ZonePoint> points = new ArrayList<ZonePoint>();
		
		Zone zone = new Zone();
		zone.setId("111");
		zone.setName("Entry ZOne");
		zone.setMseFloorId(floor.getMseFloorId());
		zone.setVenueUdId(floor.getVenueUdId());
		
		//TODO: Fix ZONE Points
		/*ZonePoint point = new ZonePoint();
		point.setId(888);
		point.setMseZoneId(zone.getId());
		point.setMseFloorId(zone.getMseFloorId());
		point.setMseVenueId(zone.getMseVenueId());
		point.setX(23);
		point.setY(40);
		points.add(point);
		zone.setPoints(points);*/
		zoneList.add(zone);
		
		floor.setZoneList(zoneList);
		
		try {
			mobileServerCacheRedisClient.save(floor);
		} catch (CachePersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(Floor.MSE_FLOORID, floorId);
		properties.put(Floor.VENUE_UDID, venueUdId);
		Floor floorFromCache = (Floor)mobileServerCacheRedisClient.get(Floor.class, properties);
		
		System.out.println("Floor From Cache : " + floorFromCache);
		
		Assert.assertEquals(floorFromCache.getMseFloorId(), floorId);
		Assert.assertEquals(floorFromCache.getPoiList().size(), 2);
		Assert.assertEquals(floorFromCache.getZoneList().size(), 1);
		
		List poiListFromCache = floorFromCache.getPoiList();
		for (int i = 0 ; i < poiListFromCache.size() ; i++) {
			PointOfInterest poiFromCache = (PointOfInterest) poiListFromCache.get(i);
			Assert.assertEquals(poiFromCache.getMseFloorId(), floorId);
			if (i == 0) {
				Assert.assertEquals(poiFromCache.getId(), "555");
			} else {
				Assert.assertEquals(poiFromCache.getId(), "556");
			}
		}
		
		List<Zone> zones = floorFromCache.getZoneList();
		for (int i = 0; i < zones.size(); i++) {
			Zone cachezone = zones.get(i);
			Assert.assertEquals(cachezone.getId(), "111");
		}
	}
	
	//TODO: Looking into Mockito !!! - need to add these test cases
	//@Test
	public void testSetPathInfo()
	{
		//Create POI
		String mseUdId = "189";
		String venueId = "88";
		String venueUdId = mseUdId + ":" + venueId;
		String floorId = "100";
		
		Floor floor = new Floor();
		
		//floor.setMseUdId(mseUdId);
		//floor.setMseVenueId(venueId);
		floor.setVenueUdId(venueUdId);
		floor.setMseFloorId(floorId);
		
		String floorPathID = "1999";
		
		FloorPathInfo floorPathInfo = new FloorPathInfo();
		
		//TODO: Add a Proper JSON
		floorPathInfo.setFloorPathInfo("Some JSON String");
		floorPathInfo.setMseFloorId(floorId);
		floorPathInfo.setVenueUdId(venueUdId);
		floorPathInfo.setId(floorPathID);
		
		floor.setFloorPathInfo(floorPathInfo);
		
		try {
			mobileServerCacheRedisClient.save(floor);
		} catch (CachePersistenceException e) {
			Assert.fail("Failed to save the Floor with Floor Path Info", e);
		} 
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(Floor.MSE_FLOORID, floorId);
		properties.put(Floor.VENUE_UDID, venueUdId);
		
		Floor floorFromCache = (Floor) mobileServerCacheRedisClient.get(Floor.class, properties);
		
		Assert.assertNotNull(floorFromCache);
		Assert.assertNotNull(floorFromCache.getFloorPathInfo().getFloorPathInfo());
		
		Assert.assertEquals(floorFromCache.getFloorPathInfo().getId(), floorPathID);
		
	}
}
