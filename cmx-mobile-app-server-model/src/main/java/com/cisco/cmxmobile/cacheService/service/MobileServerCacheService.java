package com.cisco.cmxmobile.cacheService.service;

import java.lang.reflect.Field;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cisco.cmxmobile.cacheService.Utils.CacheServiceUtils;
import com.cisco.cmxmobile.cacheService.client.CachePersistenceException;
import com.cisco.cmxmobile.cacheService.client.MobileServerCacheClient;
import com.cisco.cmxmobile.cacheService.client.MobileServerCacheException;
import com.cisco.cmxmobile.model.AssociationMap;
import com.cisco.cmxmobile.model.BleBeacon;
import com.cisco.cmxmobile.model.CasFloorInfo;
import com.cisco.cmxmobile.model.CasVenue;
import com.cisco.cmxmobile.model.Floor;
import com.cisco.cmxmobile.model.IDtoMACMapping;
import com.cisco.cmxmobile.model.UsertoMACMapping;
import com.cisco.cmxmobile.model.PendingAssociationNotification;
import com.cisco.cmxmobile.model.PointOfInterest;
import com.cisco.cmxmobile.model.Venue;
import com.cisco.cmxmobile.model.WirelessClient;
import com.cisco.cmxmobile.model.Zone;

public class MobileServerCacheService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MobileServerCacheService.class);

    @Autowired
    private MobileServerCacheClient mobileServerCacheClient;
    
    @Autowired
    private CampaignCacheService campaignCacheService;
    
    public List<WirelessClient> getAllWirelessClients() {
        return mobileServerCacheClient.getObjectsByClass(WirelessClient.class);
    }

    public long getAllWirelessClientsCount() {
        return mobileServerCacheClient.getCountByClass(WirelessClient.class);
    }

    public WirelessClient getWirelessClient(String macAddress) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(WirelessClient.MAC_ADDRESS, CacheServiceUtils.formatHexTo12Digits(macAddress));
        return mobileServerCacheClient.get(WirelessClient.class, properties);
    }

    /**
     * Gets a wireless client, given its push token/unique ID
     * 
     * @param id
     *            the unique ID
     * @return the matching wireless client, or null if no such client exists
     */
    public WirelessClient getWirelessClientByUniqueID(String id) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(IDtoMACMapping.ID, id);
        IDtoMACMapping mapping = mobileServerCacheClient.get(IDtoMACMapping.class, properties);
        if (mapping != null) {
            String clientMAC = mapping.getMacAddress();
            return getWirelessClient(clientMAC);
        }

        return null;
    }

    public List<WirelessClient> getWirelessClientsByUser(String user) {
        LOGGER.trace("Get all the wireless clients for user '{}'", user);
        List<WirelessClient> wirelessClientList = new ArrayList<WirelessClient>();
        WirelessClient client = null;
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(UsertoMACMapping.USER_ID, user);
        UsertoMACMapping mapping = mobileServerCacheClient.get(UsertoMACMapping.class, properties);
        if (mapping != null && mapping.getMacAddressList() != null && mapping.getMacAddressList().size() > 0) {
            LOGGER.trace("Found wireless clients for user '{}'", user);
            List<String> macAddressList = mapping.getMacAddressList();
            List<String> updateMacAddressList = new ArrayList<String>();
            boolean updateMacList = false;
            for (String macAddress : macAddressList) {
                LOGGER.trace("Found wireless client '{}' for user '{}'", macAddress, user);
                client = getWirelessClient(macAddress);
                if (client != null) {
                    updateMacAddressList.add(macAddress);
                    wirelessClientList.add(client);
                } else {
                    updateMacList = true;
                }
            }
            if (updateMacList) {
                try {
                    mobileServerCacheClient.save(mapping);
                }
                catch (CachePersistenceException e) {
                    LOGGER.error("Error trying to save clients by user list", e);
                }
            }
            return wirelessClientList;
        }
        return null;
    }

    public WirelessClient getWirelessClientByUniqueID(UUID id) {
        return getWirelessClientByUniqueID(id.toString());
    }

    public long getAllPendingAssociationsCount() {
        return mobileServerCacheClient.getCountByClass(PendingAssociationNotification.class);
    }

    public PendingAssociationNotification getPendingAssociation(String apMAC, String clientIP) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(PendingAssociationNotification.AP_MAC, CacheServiceUtils.formatHexTo12Digits(apMAC));
        properties.put(PendingAssociationNotification.CLIENT_IP, clientIP);
        return mobileServerCacheClient.get(PendingAssociationNotification.class, properties);
    }

    public PendingAssociationNotification getPendingAssociationByClientMac(String clientMac) {
        Field clientMacField = null;
        try {
            clientMacField = PendingAssociationNotification.class.getDeclaredField("clientMAC");
        } catch (Exception ex) {
            LOGGER.error("Failed to clientMAC field: {}", ex);
            return null;
        }
        return mobileServerCacheClient.getObjectByHashValue(PendingAssociationNotification.class, clientMacField, CacheServiceUtils.formatHexTo12Digits(clientMac));
    }

    public UsertoMACMapping getUsertoMACMapping(String user) {
        LOGGER.trace("Get the User to MAC mapping for user '{}'", user);
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(UsertoMACMapping.USER_ID, user);
        return mobileServerCacheClient.get(UsertoMACMapping.class, properties);
    }

    // TODO: time these out... one hour? Ten minutes?
    public void addOrUpdatePendingAssociationNotification(PendingAssociationNotification pan, int expireTimeInSeconds) throws MobileServerCacheException {
        try {
            mobileServerCacheClient.save(pan, expireTimeInSeconds);
        }
        catch (CachePersistenceException e) {
            throw new MobileServerCacheException(e);
        }
    }

    public void addOrUpdateWirelessClient(WirelessClient wirelessClient, int expireTimeInSeconds, boolean isTestClient) throws MobileServerCacheException {
        // TODO: put wireless client validation somewhere else
        final String wcMAC = wirelessClient.getMacAddress();
        if (wcMAC == null || wcMAC.length() == 0) {
            throw new IllegalArgumentException();
        }
        IDtoMACMapping mapping = new IDtoMACMapping();
        mapping.setId(wirelessClient.getUniqueID());
        if (isTestClient) {
            mapping.setMacAddress(wcMAC);            
        } else {
            mapping.setMacAddress(CacheServiceUtils.formatHexTo12Digits(wcMAC));
        }
        UsertoMACMapping userMapping = null;
        if (wirelessClient.getUserId() != null && wirelessClient.getUserId().length() > 0) {
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(UsertoMACMapping.USER_ID, wirelessClient.getUserId());
            userMapping = mobileServerCacheClient.get(UsertoMACMapping.class, properties);
            if (userMapping == null) {
                userMapping = new UsertoMACMapping();
                userMapping.setUserId(wirelessClient.getUserId());
            }
            if (isTestClient) {
                userMapping.addMacAddress(wcMAC);
            } else {
                userMapping.addMacAddress(CacheServiceUtils.formatHexTo12Digits(wcMAC));                
            }
            userMapping.setVenueUdId(wirelessClient.getVenueUdId());
            userMapping.setFloorId(wirelessClient.getFloorId());
            userMapping.setX(wirelessClient.getX());
            userMapping.setY(wirelessClient.getY());
        }
        try {
            mobileServerCacheClient.save(mapping, expireTimeInSeconds);
            mobileServerCacheClient.save(wirelessClient, expireTimeInSeconds);
            if (wirelessClient.getUserId() != null && wirelessClient.getUserId().length() > 0) {
                mobileServerCacheClient.save(userMapping, expireTimeInSeconds);                
            }
        }
        catch (CachePersistenceException e) {
            throw new MobileServerCacheException(e);
        }
    }

    // API for Venue
    public void saveVenue(List<Venue> venueList) {
        // Create a MSE ID -> VenueUdid Mapping

        // Safe Assumption : All Venues will be saved from same MSE
        // TODO: Is this assumption valid
        AssociationMap associationMap = new AssociationMap(venueList.get(0).getMseUdId());

        for (Venue venue : venueList) {

            List<Floor> floorList = venue.getFloorList();
            
            if (floorList != null) { 
                for (Floor floor : floorList) {
                    associationMap.addAssociation(floor.getMseFloorId(), floor.getVenueUdId());

                    //Update the BLEBeacon Zone Information
                    try {
                        floor.updateBLEBeaconZone();
                    }
                    catch (JsonParseException e) {
                        LOGGER.error("Failed to update BLE Beacon Information for floor {} at Venue {}", 
                            floor.getMseFloorId(), floor.getVenueUdId(), e);
                    }
                    catch (JsonMappingException e) {
                        LOGGER.error("Failed to update BLE Beacon Information for floor {} at Venue {}", 
                                floor.getMseFloorId(), floor.getVenueUdId(), e);
                    }
                    catch (IOException e) {
                        LOGGER.error("Failed to update BLE Beacon Information for floor {} at Venue {}", 
                                floor.getMseFloorId(), floor.getVenueUdId(), e);
                    }
                }
            }
            
            // Save the Venue
            save(venue);
        }

        // Save the Association MAp
        save(associationMap);
    }
    
    /**
     * Deletes all the Venues for the MSE based on mseUdid
     * 
     * @param mseUdid Unique Identifier of the MSE
     * @return List of Venue deleted - The List contains only the VenueUdIds 
     */
    public List<Venue> deleteAllVenues(String mseUdid) {
        List<String> venueUdidListToDelete = getVenueUdidList(mseUdid);
        
        List<Venue> venueListDeleted = new ArrayList<Venue>();
        
        for (String venueUdId : venueUdidListToDelete) {
            Venue venueToDelete = getVenue(venueUdId);
            if (venueToDelete != null) {
                delete(venueToDelete);
                venueListDeleted.add(venueToDelete);
            }
        }

        // Delete the Association Map for this MSE
        delete(new AssociationMap(mseUdid));
        
        return venueListDeleted;
    }
    
    // API for Venue
    public void saveCasVenue(List<CasVenue> casVenueList) {
        AssociationMap associationMap = new AssociationMap(casVenueList.get(0).getMseUdid());

        for (CasVenue casVenue : casVenueList) {

            List<CasFloorInfo> floorList = casVenue.getCasFloorInfoList();

            for (CasFloorInfo floor : floorList) {
                associationMap.addAssociation(floor.getAesUid(), floor.getVenueUdid());
            }

            // Save the Venue
            save(casVenue);

        }

        // Save the Association MAp
        save(associationMap);

    }
    
    public void deleteCasVenue(List<CasVenue> casVenueList) {
        for (CasVenue casVenue : casVenueList) {
            // Save the Venue
            delete(casVenue);
        }
    }
    
    /**
     * Deletes all the Cas Venues for the MSE based on mseUdid
     * 
     * @param mseUdid Unique Identifier of the MSE
     * @return List of CasVenue deleted - The List contains only the VenueUdIds 
     */
    public List<String> deleteAllCasVenues(String mseUdid) {
        List<String> venueUdidListToDelete = getVenueUdidList(mseUdid);

        for (String venueUdId : venueUdidListToDelete) {
            CasVenue venueToDelete = getCasVenue(venueUdId);
            if (venueToDelete != null) {
                delete(venueToDelete);
            }
        }

        // Delete the Association Map for this MSE
        delete(new AssociationMap(mseUdid));
        
        return venueUdidListToDelete;
    }
    
    public List<String> getVenueUdidList(String mseUdid) 
    {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(AssociationMap.ID, mseUdid);
        AssociationMap associationMap = mobileServerCacheClient.get(AssociationMap.class, properties);
        
        List<String> uniqueVenueUdidList = new ArrayList<String>();
        if (associationMap != null) {
            Iterator<String> iter = associationMap.values().iterator();
            while (iter.hasNext()) {
                String venueUdid = iter.next();
                if (!uniqueVenueUdidList.contains(venueUdid)) {
                    uniqueVenueUdidList.add(venueUdid);
                }
            }
        }
        
        return uniqueVenueUdidList;
    }

    public String getVenueUdid(String mseUdid, String floorId) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(AssociationMap.ID, mseUdid);
        AssociationMap associationMap = mobileServerCacheClient.get(AssociationMap.class, properties);
        if (associationMap != null) {
            return associationMap.getAssociatedEntity(floorId);
        }
        return null;
    }

    public Venue getVenue(String venueUdId) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(Venue.VENUE_UDID, venueUdId);
        return mobileServerCacheClient.get(Venue.class, properties);
    }
    
    public CasVenue getCasVenue(String venueUdId) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(CasVenue.CAS_VENUE_UDID, venueUdId);
        return mobileServerCacheClient.get(CasVenue.class, properties);
    }

    public List<Venue> getAllVenues() {
        return mobileServerCacheClient.getObjectsByClass(Venue.class);
    }
    
    public List<CasVenue> getAllCasVenues() {
        return mobileServerCacheClient.getObjectsByClass(CasVenue.class);
    }

    // API for Zones

    public List<Zone> getZonesByFloor(String venueUdId, String floorId) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(Floor.VENUE_UDID, venueUdId);
        properties.put(Floor.MSE_FLOORID, floorId);
        Floor floor = mobileServerCacheClient.get(Floor.class, properties);
        if (floor != null) {
            return floor.getZoneList();
        }
        return null;
    }
    
    public Zone getZoneByFloor(String venueUdId, String floorId, String zoneId) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(Zone.VENUE_UDID, venueUdId);
        properties.put(Zone.MSE_FLOORID, floorId);
        properties.put(Zone.ID, zoneId);
        return mobileServerCacheClient.get(Zone.class, properties);
    }


    //
    // API for Point of Interest
    //

    public PointOfInterest getPointOfInterest(String venueUdId, String floorId, String poiId) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(PointOfInterest.VENUE_UDID, venueUdId);
        properties.put(PointOfInterest.MSE_FLOORID, floorId);
        properties.put(PointOfInterest.ID, poiId);

        return mobileServerCacheClient.get(PointOfInterest.class, properties);
    }

    public List<PointOfInterest> getPointOfInterestListByFloor(String venueUdId, String floorId, String searchString) {
        List<PointOfInterest> poiList = getPointOfInterestListByFloor(venueUdId, floorId);

        List<PointOfInterest> filteredPoiList = new ArrayList<PointOfInterest>();
        for (PointOfInterest poi : poiList) {
            if (poi.getName().contains(searchString)) {
                filteredPoiList.add(poi);
            }
        }

        return poiList;
    }

    public Floor getFloorByVenueUdid(String venueUdId, String floorId) {
        if (venueUdId == null || floorId == null) {
            return null;
        }
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(Floor.VENUE_UDID, venueUdId);
        properties.put(Floor.MSE_FLOORID, floorId);

        return mobileServerCacheClient.get(Floor.class, properties);
    }
    
    public CasFloorInfo getCasFloorByVenueUdid(String venueUdId, String floorId) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(CasFloorInfo.CAS_VENUE_UDID, venueUdId);
        properties.put(CasFloorInfo.CAS_FLOOR_ID, floorId);

        return mobileServerCacheClient.get(CasFloorInfo.class, properties);
    }

    public List<PointOfInterest> getPointOfInterestListByFloor(String venueUdId, String floorId) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(Floor.VENUE_UDID, venueUdId);
        properties.put(Floor.MSE_FLOORID, floorId);

        // Get all the POIs for the Floor
        // [Map<String, List<String>>]
        // Key:CLASS:VENUE:FloorID
        // Return List of POI IDs
        Floor floor = mobileServerCacheClient.get(Floor.class, properties);

        return floor != null ? floor.getPoiList() : new ArrayList<PointOfInterest>();
    }

    public List<PointOfInterest> getPointOfInterestByVenue(String venueUdId, String searchString) {
        // /logger.trace("Search String : " + searchString);

        List<PointOfInterest> poiList = getPointOfInterestByVenue(venueUdId);

        List<PointOfInterest> filteredPoiList = new ArrayList<PointOfInterest>();

        LOGGER.trace("POI List Size : " + poiList.size());
        for (PointOfInterest poi : poiList) {
            LOGGER.trace("POI Name : " + poi.getName() + " - Search String xx : " + searchString + " - Contains : " + poi.getName().toLowerCase().contains(searchString.trim().toLowerCase()));
            String poiName = poi.getName().toLowerCase();
            String myString = searchString.toLowerCase();
            LOGGER.trace("Test : " + poiName + " : " + myString + " : " + poiName.contains(myString));
            if (poiName.toLowerCase().contains(searchString.trim().toLowerCase())) {
                filteredPoiList.add(poi);
            }
        }
        //
        return filteredPoiList;
    }

    public List<PointOfInterest> getPointOfInterestByVenue(String venueUdId) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(Floor.VENUE_UDID, venueUdId);
        Venue venue = mobileServerCacheClient.get(Venue.class, properties);

        List<PointOfInterest> poiListByVenue = new ArrayList<PointOfInterest>();
        if (venue != null) {
            List<Floor> floorList = venue.getFloorList();
            for (Floor floor : floorList) {
                if (floor.getPoiList() != null) {
                    poiListByVenue.addAll(floor.getPoiList());
                }
                else {
                    LOGGER.info("Venue ID", venueUdId, "does not have any floors");
                }
            }
        }
        return poiListByVenue;
    }

    public void addOrUpdateVenue(List<Venue> venues) {
        for (Venue venue : venues) {
            try {
                if (mobileServerCacheClient.exists(venue)) {
                    // Remove the old one once delete is implemented
                    LOGGER.info("Venue", venue, "exists");
                }
                mobileServerCacheClient.save(venue);
            }
            catch (CachePersistenceException e) {
                // TODO Auto-generated catch block
                LOGGER.error("CachePersistenceException", e);
            }
            catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                LOGGER.error("IllegalAccessException", e);
            }
        }
    }
    
    
    //
    // API For BleBeacon
    //
    public List<BleBeacon> getBleBeaonListByFloor(String venueUdid, String floorId)
    {
        Floor floor = this.getFloorByVenueUdid(venueUdid, floorId);
        
        if (floor != null && floor.getBleBeaconList() != null) {
            return floor.getBleBeaconList();
        }
        
        return new ArrayList<BleBeacon>();
    }
    
    public List<BleBeacon> getBleBeaonListByVenue(String venueUdid)
    {
        List<BleBeacon> bleBeaconList = new ArrayList<BleBeacon>();
        
        Venue venue = this.getVenue(venueUdid);
        
        if (venue != null && venue.getFloorList() != null) {
            List<Floor> floorList = venue.getFloorList();

            for (Floor floor : floorList) {
                if (floor.getBleBeaconList() != null) {
                    bleBeaconList.addAll(floor.getBleBeaconList());
                }
            }
        }
        
        return bleBeaconList;
    }
    
    
    /*
     * public static void addOrUpdateFloor(List<Floor> floors) { for (Floor
     * floor : floors) { if (MobileServerCacheRedisClient.exists(floor)) {
     * //Remove the old one once delete is implemented } else { //Update the
     * Venue List } MobileServerCacheRedisClient.save(floor); } }
     */

    public void addOrUpdatePointOfInterest(List<PointOfInterest> pointOfInterests) {
        //Need to add code
    }

    public void removeVenue(int venue) {
        //Need to add code
    }

    public void removeFloor(int floorId) {
        //Need to add code
    }

    public void removePointOfInterest(List<PointOfInterest> pointOfInterests) {
        //Need to add code
    }
    
    //
    // API To Get Campaign Service
    //
    public CampaignCacheService getCampaignCacheService()
    {
    	return campaignCacheService;
    }

    //
    // Generic
    //

    public void save(Object object) {
        try {
            mobileServerCacheClient.save(object);
        }
        catch (CachePersistenceException e) {
            LOGGER.error("Failed to Save Object '{}' of type ", object.getClass(), e);
        }
    }
    
    public void delete(Object object) {
        try {
            mobileServerCacheClient.delete(object);
        }
        catch (CachePersistenceException e) {
            LOGGER.error("Failed to Delete Object '{}' of type ", object.getClass(), e);
        }
    }
    
}
