package com.cisco.cmxmobile.cacheService.client;

import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.cisco.cmxmobile.model.Floor;
import com.cisco.cmxmobile.model.FloorPathInfo;
import com.cisco.cmxmobile.model.PointOfInterest;
import com.cisco.cmxmobile.model.Venue;
import com.cisco.cmxmobile.model.Zone;

@ContextConfiguration(locations={"classpath:spring/application-context.xml"})
public class VenueTestBase extends AbstractTestNGSpringContextTests
{
    @Autowired
    protected MobileServerCacheClient mobileServerCacheClient;
        
    //TODO: Fix this - DataProvider - can be from JSON File
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
    
    protected Venue venue;
    
    //@BeforeClass
    public void setUp()   
    {
        // code that will be invoked when this test is instantiated
        
        //Create the Venue
        venue = new Venue();
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
        zoneList.add(zone);
        floor.setZoneList(zoneList);
        
        /*List<Venue> venueList = new ArrayList<Venue>();
        venueList.add(venue);*/
        
        try {
            mobileServerCacheClient.save(venue);
            
            postSetup();
        }
        catch (CachePersistenceException e) {
            fail("Failed to Setup VenueTestBase", e);
        }          
    }
    
    //@AfterClass
    public void tearDown()
    {
        try {
            mobileServerCacheClient.delete(venue);
        }
        catch (CachePersistenceException e) {
            fail("Failed to TearDown VenueTestBase", e);
        }
    }
    
    
    public void postSetup() {
        //Subclass implements if needed -
        //This is called after initial Setup is complete
    }
}
