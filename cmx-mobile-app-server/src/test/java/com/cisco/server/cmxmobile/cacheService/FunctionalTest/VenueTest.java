package com.cisco.server.cmxmobile.cacheService.FunctionalTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cisco.cmxmobile.cacheService.client.MobileServerCacheClient;
import com.cisco.cmxmobile.model.Floor;
import com.cisco.cmxmobile.model.FloorPathInfo;
import com.cisco.cmxmobile.model.PointOfInterest;
import com.cisco.cmxmobile.model.Venue;
import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;
import com.cisco.cmxmobile.model.Zone;

@ContextConfiguration(locations={"classpath:spring/application-context.xml"})
public class VenueTest extends AbstractTestNGSpringContextTests
{
	//TODO: Fix this - DataProvider
	private static String MSE_UID = "999999MSEUDID";	
	private static String VENUE_ID = "10999";
	private static String VENUE_UD_ID = MSE_UID + ":" + VENUE_ID;
	private static String FLOOR_ID = "10057";
	private static String POI_ID = "1";
	private static String POI_2_ID = "2";
	private static String ZONE_1_ID = "11";
	private static String ZONE_2_ID = "12";
	private static String ZONE_POINT_1_ID = "21";
	private static String ZONE_POINT_2_ID = "22";
	//Test
	@Autowired
	private MobileServerCacheService mobileServerCacheService;
	
	@Autowired
	private MobileServerCacheClient mobileServerCacheClient;
	
	//@BeforeClass
	public void setUp() {
		// code that will be invoked when this test is instantiated
		
		//Create the Venue
		Venue venue = new Venue();
		venue.setMseVenueId(VENUE_ID);
		venue.setMseUdId(MSE_UID);
		venue.setVenueUdId(VENUE_UD_ID);
		
		//Create the Floor
		List<Floor> floorList = new ArrayList<Floor>();

		Floor floor = new Floor();
		floor.setMseFloorId(FLOOR_ID);
		floor.setVenueUdId(VENUE_UD_ID);
		floor.setMseVenueId(VENUE_ID);
		floor.setMseUdId(MSE_UID);
		
		FloorPathInfo floorPathInfo = new FloorPathInfo();
		floorPathInfo.setId("10020");
		floorPathInfo.setMseFloorId(FLOOR_ID);
		floorPathInfo.setMseUdId(MSE_UID);
		floorPathInfo.setMseVenueId(VENUE_ID);
		floorPathInfo.setVenueUdId(VENUE_UD_ID);
		floor.setFloorPathInfo(floorPathInfo);
		
		floorList.add(floor);
		
		//Add Floor to Venue
		venue.setFloorList(floorList);
		
		//Create the POIS
		List<PointOfInterest> poiList = new ArrayList<PointOfInterest>();
		
		PointOfInterest poi = new PointOfInterest();
		poi.setName("Conference Room - Miami Beach");
		poi.setDescription("Some Conference Room in the center of the 4th Floor");
		poi.setId(POI_ID);
		poi.setVenueUdId(floor.getVenueUdId());
		poi.setMseFloorId(floor.getMseFloorId());
		
		poiList.add(poi);
		
		PointOfInterest poi2 = new PointOfInterest();
		poi2.setId(POI_2_ID);
		poi2.setName("Conference Room - Santa Cruz");
		poi2.setDescription("Some Conference Room in the corner of the 4th Floor");
		poi.setVenueUdId(floor.getVenueUdId());
		poi2.setMseFloorId(floor.getMseFloorId());
		poiList.add(poi2);
		
		//Add POI List to Floor
		floor.setPoiList(poiList);
		
		//Create Zones for floor
		List<Zone> zoneList = new ArrayList<Zone>();
		
		Zone zone = new Zone();
		zone.setId(ZONE_1_ID);
		zone.setName("Entry ZOne");
		zone.setVenueUdId(floor.getVenueUdId());
		zone.setMseFloorId(floor.getMseFloorId());
			
		//TODO : Fix this
		/*ZonePoint point = new ZonePoint();
		point.setId(ZONE_POINT_1_ID);
		point.setZoneId(zone.getZoneId());
		point.setFloorId(zone.getFloorId());
		point.setVenueId(zone.getFloorId());
		point.setX(23);
		point.setY(40);
		points.add(point);
		zoneList.add(zone);
		zone.setPoints(points); */
		zoneList.add(zone);
		floor.setZoneList(zoneList);
		
		try {
			List<Venue> venueList = new ArrayList<Venue>();
			venueList.add(venue);
			mobileServerCacheService.saveVenue(venueList);			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	//@Test
	public void testVenuePresence()
	{
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(Venue.VENUE_UDID, VENUE_UD_ID);
		//mobileServerCacheClient.get(Venue.class, properties);
		
		Venue venueFromCache = (Venue)mobileServerCacheClient.get(Venue.class, properties);
		
		Assert.assertNotNull(venueFromCache);
	}
	
	//@Test
	public void testVenueAbsence()
	{
		int VENUE_ID_NOT_IN_CACHE = 6661212;
				
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(Venue.VENUE_UDID, Integer.toString(VENUE_ID_NOT_IN_CACHE));
		mobileServerCacheClient.get(Venue.class, properties);
		
		Venue venueFromCache = (Venue)mobileServerCacheClient.get(Venue.class, properties);
		
		Assert.assertNull(venueFromCache);
	}
	
	//@Test
	public void testFloorCountOnVenue()
	{
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(Venue.VENUE_UDID, VENUE_UD_ID);
		mobileServerCacheClient.get(Venue.class, properties);
		
		Venue venueFromCache = (Venue)mobileServerCacheClient.get(Venue.class, properties);
		
		Assert.assertNotNull(venueFromCache);
		
		if (venueFromCache != null) {
			Assert.assertTrue(venueFromCache.getFloorList().size() == 1);
		}
	}

	//@Test
	public void testPoiCountOnVenue()
	{
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(Venue.VENUE_UDID, VENUE_UD_ID);
		mobileServerCacheClient.get(Venue.class, properties);
		
		Venue venueFromCache = (Venue)mobileServerCacheClient.get(Venue.class, properties);
		
		Assert.assertNotNull(venueFromCache);
		
		List<PointOfInterest> poiListFromCache = new ArrayList<PointOfInterest>();
		if (venueFromCache != null) {
			for (Floor floor : venueFromCache.getFloorList()) {
				Assert.assertNotNull(floor.getPoiList());
				poiListFromCache.addAll(floor.getPoiList());
			}
		}
		
		Assert.assertTrue(poiListFromCache.size() == 2);
	}
	
	//@Test
	public void testPoiCountOnVenueByServiceCall()
	{
		List<PointOfInterest> poiListFromCache = mobileServerCacheService.getPointOfInterestByVenue(MSE_UID, VENUE_ID);
		
		Assert.assertNotNull(poiListFromCache);
		
		Assert.assertTrue(poiListFromCache.size() == 2);
	}
	
	//@Test
	public void testMseUdidVenueUdidMapping()
	{
		Assert.assertEquals(mobileServerCacheService.getVenueUdid(MSE_UID, FLOOR_ID), VENUE_UD_ID) ;
	}
	
	//@Test
	public void testGetAllVenues()
	{
		List<Venue> venueList = mobileServerCacheService.getAllVenues();
		List<Venue> venueListToTest = new ArrayList<Venue>();
		for (Venue venue : venueList) {
			if (venue.getVenueUdId().equals(VENUE_UD_ID)) {
				venueListToTest.add(venue);
			}
		}
		
		Assert.assertEquals(venueListToTest.size(), 1);
	}
}
