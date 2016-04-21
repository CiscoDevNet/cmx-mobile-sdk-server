package com.cisco.cmxmobile.cacheService.client;

import static org.testng.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

import org.testng.annotations.Test;

import com.cisco.cmxmobile.model.Floor;
import com.cisco.cmxmobile.model.PointOfInterest;
import com.cisco.cmxmobile.model.Venue;

@ContextConfiguration(locations={"classpath:spring/application-context.xml"})
public class FloorDeleteTest extends VenueTestBase
{
    private Floor deletedFloorTestSubject;
    
    @Override
    public void postSetup()   
    {        
        //Get the Venue from the Cache
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(Venue.VENUE_UDID, venue.getVenueUdId());
        Venue venueFromCache = mobileServerCacheClient.get(Venue.class, properties);
        
        assertNotNull(venueFromCache);
        
        //Pick a Random Floor
        List<Floor> floorList = venueFromCache.getFloorList();
        
        deletedFloorTestSubject = floorList.get(new Random().nextInt(floorList.size()));
        
        assertNotNull(deletedFloorTestSubject);
        
        try {
            mobileServerCacheClient.delete(deletedFloorTestSubject);
        }
        catch (CachePersistenceException e) {
            fail("Failed to Delete Floor", e);
        }
    }
    
    //@Test
    public void test_Num_Floors_After_Delete()
    {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(Venue.VENUE_UDID, venue.getVenueUdId());
            
        Venue venueFromCache = mobileServerCacheClient.get(Venue.class, properties);
        Assert.notNull(venueFromCache);

        List<Floor> floorListFromCache = venueFromCache.getFloorList();

        Assert.notNull(floorListFromCache);
        
        assertEquals(floorListFromCache.size(), venue.getFloorList().size() - 1);
    }

    //@Test
    public void test_Query_Deleted_Floor_By_Venue()
    {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(Venue.VENUE_UDID, venue.getVenueUdId());
            
        Venue venueFromCache = mobileServerCacheClient.get(Venue.class, properties);
        assertNotNull(venueFromCache);
        
        List<Floor> floorList = venueFromCache.getFloorList();
        assertNotNull(floorList);
        
        boolean floorFound = false;
        for (Floor floor : floorList) {
            if (floor.getMseFloorId().equals(deletedFloorTestSubject.getMseFloorId())) {
                floorFound = true;
            }
        }
        
        assertFalse(floorFound);
    }
    
    //@Test
    public void test_Query_Deleted_Floor_By_Floor()
    {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(Floor.VENUE_UDID, deletedFloorTestSubject.getVenueUdId());
        properties.put(Floor.MSE_FLOORID, deletedFloorTestSubject.getMseFloorId());
        Floor floorFromCache = mobileServerCacheClient.get(Floor.class, properties);

        assertNull(floorFromCache);
    }
    
    //@Test
    public void test_Query_Deleted_PointOfInterest_By_PointOfInterest()
    {
        Map<String, String> properties = new HashMap<String, String>();
        
        PointOfInterest poiTestSubject = deletedFloorTestSubject.getPoiList().get(0);
        
        properties.put(PointOfInterest.VENUE_UDID, poiTestSubject.getVenueUdId());
        properties.put(PointOfInterest.MSE_FLOORID, poiTestSubject.getMseFloorId());
        properties.put(PointOfInterest.ID, poiTestSubject.getId());
        PointOfInterest pointOfInterestFromCache = mobileServerCacheClient.get(PointOfInterest.class, properties);

        assertNull(pointOfInterestFromCache);
    }
    
    //@Test
    public void test_Query_Deleted_Floor_By_Class()
    {
        List<Floor> floorList = mobileServerCacheClient.getObjectsByClass(Floor.class);
        
        boolean floorFound = false;
        for (Floor floor : floorList) {
            if (floor.getMseFloorId().equals(deletedFloorTestSubject.getMseFloorId())) {
                floorFound = true;
            }
        }
        
        assertFalse(floorFound);
    }
 }
