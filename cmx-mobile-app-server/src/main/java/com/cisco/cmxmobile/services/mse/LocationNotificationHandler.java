package com.cisco.cmxmobile.services.mse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aes.notification.datamodel.AssociationEvent;
import com.aes.notification.datamodel.ContainmentEvent;
import com.aes.notification.datamodel.MovementEvent;
import com.aes.rest.datamodel.location.MapCoordinate;
import com.cisco.cmxmobile.cacheService.client.MobileServerCacheException;
import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;
import com.cisco.cmxmobile.model.DeviceType;
import com.cisco.cmxmobile.model.Floor;
import com.cisco.cmxmobile.model.PendingAssociationNotification;
import com.cisco.cmxmobile.model.Venue;
import com.cisco.cmxmobile.model.WirelessClient;
import com.cisco.cmxmobile.model.Zone;
import com.cisco.cmxmobile.pushNotification.PushNotificationService;
import com.cisco.cmxmobile.pushNotification.android.AndroidPushNotification;
import com.cisco.cmxmobile.pushNotification.apple.ApplePushNotification;
import com.cisco.cmxmobile.server.mse.AuthTokenCache;
import com.cisco.cmxmobile.server.stats.ContextAwareServiceStats;
import com.cisco.cmxmobile.services.mse.LocationNotificationService.Event;
import com.cisco.cmxmobile.utils.MDCKeys;
import com.vividsolutions.jts.geom.GeometryFactory;

@Component
public class LocationNotificationHandler 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationNotificationHandler.class);
    
    private static final String EVENT_KEY_MSE_UDI = "mseUdi";
    
    private static final String EVENT_KEY_FLOOR_REF_ID = "floorRefId";
    
    private static final String EVENT_KEY_TIME_STAMP = "timestamp";
    
    private static final String EVENT_KEY_GEO_COORDINATE = "geoCoordinate";
    
    private static final String EVENT_KEY_LATITUDE = "latitude";

    private static final String EVENT_KEY_LONGITUDE = "longitude";
    
    private static final String EVENT_KEY_USER_ID = "userId";
    
    private static final String EVENT_CLIENT_AUTH_INFO = "clientAuthInfo";
    
    private static final String CMX_LOCATIION_EVENT_SUBSCRIPTION_NAME = "CMX_Location_Event";
 
    private static List<String> newRegisteredDevices = new ArrayList<String>();

    @Autowired
    private MobileServerCacheService mMobileServerCacheService;
    
    @Autowired
    private PushNotificationService mNotificationService;

    @Value("${map.location}")
    String mapLocation;
    
    @Value("${location.trackAllClients}")
    boolean trackAllClients;
    
    @Value("${location.clientExpireTimeInSeconds}")
    int clientExpireTimeInSeconds;

    @Value("${location.associationExpireTimeInSeconds}")
    int associationExpireTimeInSeconds;

    @Value("${location.updateDistance}")
    int updateDistance;

    @Value("${location.zoneTimeoutResponseSeconds}")
    int zoneTimeoutResponseSeconds;
    
    @Value("${location.minutesBetweenSendingNotification}")
    int minutesBetweenSendingNotification;
    
    @Value("${location.zoneCalcEachMovementEvent}")
    boolean zoneCalcEachMovementEvent;
    
    @Value("${location.filterRegisteredDevices}")
    boolean filterRegisteredDevices;

    @Value("${authCache.ignoreNotificationEventAuth}")
    boolean ignoreNotificationEventAuth;
    
    @Value("${location.sendPresenceNotificationOnEachAssociationEvent}")
    boolean sendPresenceNotificationOnEachAssociationEvent;
    
    //Reference the Cache
    @Autowired
    private AuthTokenCache authTokenCache;
    
    @Resource
    private List<WirelessClient> testClientDevices;

    GeometryFactory gmFactory = null;
    
    public void addNewRegisteredDevice(String macAddress) {
        newRegisteredDevices.add(macAddress);
    }

    public void getNewRegisteredDevice(JSONArray macAddressArray) {
        int numberNewRegisteredDevices = newRegisteredDevices.size();
        if (numberNewRegisteredDevices > 0) {
            synchronized (this) {
                for (int i = 0; i < numberNewRegisteredDevices; i++) {
                    macAddressArray.put(newRegisteredDevices.get(0));
                    newRegisteredDevices.remove(0);
                }
            }
        }
    }
    
    public String getKeyFileLocation() {
    	return mapLocation;
    }

    public Response handleMovementEvent(Map<String, Object> inputObject, String remoteAddress) 
    {
        Map<String, Object> dataInputObject = (Map<String, Object>) inputObject.get(Event.MOVEMENT.toString());
        if (!authKeyMatches(dataInputObject)) {
            ContextAwareServiceStats.getInstance().incrementUnauthorizedEventsCount(remoteAddress, (String) dataInputObject.get(EVENT_KEY_MSE_UDI));
            return Response.status(Status.UNAUTHORIZED).build();
        }

        ContextAwareServiceStats.getInstance().incrementLocationEventsCount();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        MovementEvent me = mapper.convertValue(inputObject.get(Event.MOVEMENT.toString()), MovementEvent.class);
        
        String deviceId = me.getDeviceId();
       
        MDC.put(MDCKeys.DEVICE_MAC_ADDRESS, deviceId);
        LOGGER.trace("The event received from the server is a movement event for client '{}'", deviceId);
        WirelessClient client = null;
        try {
            client = mMobileServerCacheService.getWirelessClient(deviceId.toLowerCase());
        } catch (Exception e) {
            LOGGER.error("Exception saving a movement event for client: ", e);
            MDC.remove(MDCKeys.DEVICE_MAC_ADDRESS);
        }
        
        if (trackAllClients && client == null) {
            client = new WirelessClient();
            client.setMacAddress(deviceId.toLowerCase());
            client.setPushNotificationRegistrationId("None");
            client.setDeviceType(DeviceType.ANDROID);
        }
        String userId = (String) dataInputObject.get(EVENT_KEY_USER_ID);
        LOGGER.trace("Checking movement event for user '{}'", userId);
        if (!testClientDevices.isEmpty() && client == null && userId != null) {
            LOGGER.trace("The event received from the server is a movement event for user '{}'", userId);
            List<WirelessClient> clients = null;
            try {
                clients = mMobileServerCacheService.getWirelessClientsByUser(userId);
            } catch (Exception e) {
                LOGGER.error("Exception saving a movement event for client: ", e);
                MDC.remove(MDCKeys.DEVICE_MAC_ADDRESS);
            }
            if (clients != null) {
                for (WirelessClient userClient : clients) {
                    userClient.setUserId(userId);
                    processMovementEvent(userClient, me, inputObject);            
                }
            }
            return Response.ok().build();            
        }
        
        if (client != null) {
            return processMovementEvent(client, me, inputObject);
        } else {
            LOGGER.trace("Client does not exist and the movement event will not be updated '{}'", deviceId);
        }
        return Response.ok().build();
    }
    
    public Response processMovementEvent(WirelessClient client, MovementEvent me, Map<String, Object> inputObject) {
        String deviceId = me.getDeviceId();
        LOGGER.trace("Client found with device Id '{}'", deviceId);
        Map<String, Object> data = (Map<String, Object>) inputObject.get(Event.MOVEMENT.toString());
        String mseUdId = (String) data.get(EVENT_KEY_MSE_UDI);
        
        //Get the Floor ID - sometimes it is cast as Long
        String floorId =  "";
        Object floorObject = data.get(EVENT_KEY_FLOOR_REF_ID);
        if (floorObject != null) {
            if (floorObject instanceof Integer) {
                floorId = Integer.toString(((Integer) floorObject));
            } else if (floorObject instanceof Long) {
                floorId = Long.toString(((Long) floorObject));
            } else if (floorObject instanceof String) {
                floorId = (String) floorObject;
            }
        }
        
        String venueUdId = "";
        String floorName = "";
        Floor floor = null;
        if (mseUdId != null) {
            venueUdId = mMobileServerCacheService.getVenueUdid(mseUdId, floorId);
            floor = mMobileServerCacheService.getFloorByVenueUdid(venueUdId, floorId);
            if (floor == null) {
                if (trackAllClients) {
                    venueUdId = mseUdId;
                    LOGGER.trace("Floor Notified by Location server is not available for VenueUdId but the following will be used {}" , venueUdId);
                } else {
                    LOGGER.trace("Floor Notified by Location server is not available for VenueUdId {}" , venueUdId);
                }
            } else {
                floorName = floor.getName();
                LOGGER.trace("Got Floor and VenueUdId '{}', '{}'", floorName, venueUdId);
            }
        }
        if (venueUdId != null && !venueUdId.isEmpty()) {
            client.setFloorId(floorId);
            client.setVenueUdId(venueUdId);
            MapCoordinate coordinates = me.getLocationCoordinate();
            Object timeStampObject = data.get(EVENT_KEY_TIME_STAMP);
            if (timeStampObject != null) {
                if (timeStampObject instanceof String) {
                    client.setLastLocationCalculationTime((String) timeStampObject);
                } else {
                    client.setLastLocationCalculationTime(0);
                }
            }
            Object geoCoordinate = data.get(EVENT_KEY_GEO_COORDINATE);
            if (geoCoordinate != null) {
                if (geoCoordinate instanceof Map) {
                    Map geoCoordinateMap = (Map) geoCoordinate;
                    Object latitude = geoCoordinateMap.get(EVENT_KEY_LATITUDE);
                    if (latitude != null) {
                        if (latitude instanceof Double) {
                            client.setLatitude((Double) latitude);
                        } else {
                            client.setLatitude(0);
                        }
                    }
                    Object longitude = geoCoordinateMap.get(EVENT_KEY_LONGITUDE);
                    if (longitude != null) {
                        if (longitude instanceof Double) {
                            client.setLongitude((Double) longitude);
                        } else {
                            client.setLongitude(0);
                        }
                    }
                } else {
                    client.setLongitude(0);
                    client.setLatitude(0);
                }
            }
            client.setX(coordinates.getX());
            client.setY(coordinates.getY()); 
            client.setLastLocationUpdateTime(System.currentTimeMillis());
            LOGGER.trace("Client | VenueUdId | Floor Name | X | Y : {} | {} | {} | {} | {}", client.getMacAddress(), venueUdId, floorName, client.getX(), client.getY());

            Venue venue = mMobileServerCacheService.getVenue(venueUdId);

            //Send the Presence Notification before zone notification if necessary
            if (venue != null && client.requirePresenceNotificaton()) {
                LOGGER.trace("Venue has changed for device {}. New Venue ID : {}", deviceId, venue.getVenueUdId());
                sendPresenceNotification(client, venue);
            }
            
            try {
                if (floor != null && zoneCalcEachMovementEvent) {
                    Zone zone = getPushNotificationZone(floor, me.getLocationCoordinate());
                    if (zone != null && (client.getZoneId() == null || !client.getZoneId().equalsIgnoreCase(zone.getId()))) {
                        client.setZoneId(zone.getId());
                        client.setZoneName(zone.getName());
                        client.setZonePoints(zone.getPoints());
                        if (venue == null) {
                            LOGGER.error("No Venue found for containment event with device id '{}'", deviceId);
                        } else {
                            //Send the Zone notification now if necessary
                            String zoneKey = zone.getVenueUdId() + ":" + zone.getMseFloorId() + ":" + zone.getId();
                            LOGGER.trace("Checking if push notification is required for movement of device {} in zone {}", deviceId, zoneKey);
                            if (client.requirePushNotification(zoneKey, minutesBetweenSendingNotification)) {
                                sendPushNotification(client, zone, venue);
                            }
                        }
                    }
                }
                mMobileServerCacheService.addOrUpdateWirelessClient(client, clientExpireTimeInSeconds, !testClientDevices.isEmpty());
                JSONObject responseData = new JSONObject();

                JSONObject parameters = new JSONObject();
                parameters.accumulate("notifications_enabled", "true");
                parameters.accumulate("update_distance", updateDistance);
                parameters.accumulate("zone_timeout", zoneTimeoutResponseSeconds);
                
                if (filterRegisteredDevices) {
                    JSONArray subscriptionNameArray = new JSONArray();
                    subscriptionNameArray.put(CMX_LOCATIION_EVENT_SUBSCRIPTION_NAME);
                    parameters.accumulate("subscription_names", subscriptionNameArray);
                    JSONArray macAddressArray = new JSONArray();
                    macAddressArray.put(client.getMacAddress());
                    getNewRegisteredDevice(macAddressArray);
                    parameters.accumulate("mac_address_list", macAddressArray);
                }

                responseData.accumulate("configuration", parameters);
                LOGGER.trace("Completed update of movement event for client '{}'. Respond object is {}", client.getMacAddress(), responseData);
                
                MDC.remove(MDCKeys.DEVICE_MAC_ADDRESS);
                return Response.ok().entity(responseData).build();
            }
            catch (MobileServerCacheException e) {
                LOGGER.error("Exception saving a movement event for client: ", e);
                MDC.remove(MDCKeys.DEVICE_MAC_ADDRESS);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            catch (Exception e) {
                LOGGER.error("Exception in movement event", e);
            }
        }
        else {
            LOGGER.trace("Cound not find a venue for client: '{}'", client.getMacAddress());
        }
        return Response.ok().build();
    }
    
    public Response handleContainmentEvent(Map<String, Object> inputObject, String remoteAddress)
    {
        Map<String, Object> dataInputObject = (Map<String, Object>) inputObject.get(Event.CONTAINMENT.toString());
        if (!authKeyMatches(dataInputObject)) {
            ContextAwareServiceStats.getInstance().incrementUnauthorizedEventsCount(remoteAddress, (String) dataInputObject.get(EVENT_KEY_MSE_UDI));
            return Response.status(Status.UNAUTHORIZED).build();
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        ContainmentEvent containmentEvent = mapper.convertValue(inputObject.get(Event.CONTAINMENT.toString()), ContainmentEvent.class);
        String deviceId = containmentEvent.getDeviceId();
        MDC.put(MDCKeys.DEVICE_MAC_ADDRESS, deviceId);
        LOGGER.trace("The event received from the server is a containment event for client '{}'", deviceId);
        WirelessClient client = mMobileServerCacheService.getWirelessClient(deviceId);
        // Check for valid client to send notification.
        if (client != null) {
            Map<String, Object> data = (Map<String, Object>) inputObject.get(Event.CONTAINMENT.toString());
            String mseUdId = (String) data.get("mseUdi");
            String floorId = Long.toString((Long) data.get("floorRefId"));
            if (mseUdId != null && floorId != null) {
                String venueUdId = mMobileServerCacheService.getVenueUdid(mseUdId, floorId);
                if (venueUdId.isEmpty()) {
                    LOGGER.error("No VenueUdId found for containment event with dvice id '{}'", deviceId);
                    MDC.remove(MDCKeys.DEVICE_MAC_ADDRESS);
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }
                Venue venue = mMobileServerCacheService.getVenue(venueUdId);
                if (venue == null) {
                    LOGGER.error("No Venue found for containment event with dvice id '{}'", deviceId);
                    MDC.remove(MDCKeys.DEVICE_MAC_ADDRESS);
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }
                Floor floor = mMobileServerCacheService.getFloorByVenueUdid(venueUdId, floorId);
                
                LOGGER.trace("The containment event is for server '{}' Floor ID '{}' Venue ID '{}'", mseUdId, floorId, venueUdId);
                Zone zone = getPushNotificationZone(floor, containmentEvent.getLocationCoordinate());
                if (zone != null) {
                    LOGGER.trace("The containment event is for zone '{}'", zone.getId());
                    String zoneKey = zone.getVenueUdId() + ":" + zone.getMseFloorId() + ":" + zone.getId();
                    LOGGER.trace("Checking if push notification is required for containment of device {} in zone {}", deviceId, zoneKey);
                    if (client.requirePushNotification(zoneKey, minutesBetweenSendingNotification)) {
                        sendPushNotification(client, zone, venue);
                    } else {
                        LOGGER.trace("Push notification not required for the current event");
                    }
                    try {
                        JSONObject responseData = new JSONObject();

                        JSONObject parameters = new JSONObject();
                        parameters.accumulate("notifications_enabled", "true");
                        parameters.accumulate("update_distance", updateDistance);
                        parameters.accumulate("zone_timeout", zoneTimeoutResponseSeconds);

                        if (filterRegisteredDevices) {
                            JSONArray subscriptionNameArray = new JSONArray();
                            subscriptionNameArray.put(CMX_LOCATIION_EVENT_SUBSCRIPTION_NAME);
                            parameters.accumulate("subscription_names", subscriptionNameArray);
                            JSONArray macAddressArray = new JSONArray();
                            macAddressArray.put(client.getMacAddress());
                            getNewRegisteredDevice(macAddressArray);
                            parameters.accumulate("mac_address_list", macAddressArray);
                        }
                        responseData.accumulate("configuration", parameters);
                        LOGGER.trace("Completed update of containment event for client '{}'.", client.getMacAddress());
                        return Response.ok().entity(responseData).build();
                    } catch (JSONException ex) {
                        LOGGER.trace("Error trying to build response object for containment event {}", ex);
                    }
                } else {
                    LOGGER.trace("Containment event does not match a defined zone for push notifications");
                }
            } else {
                LOGGER.trace("Containment event does not have an MSE UDID or Floor ID"); 
            }
        } else {
            LOGGER.trace("Client does not exist and the containment event is ignored for '{}'", deviceId);
        }
        
        return Response.ok().build();
    }
    
    public Response handleAssociationEvent(Map<String, Object> inputObject, String remoteAddress)
    {
        Map<String, Object> dataInputObject = (Map<String, Object>) inputObject.get(Event.ASSOCIATION.toString());
        if (!authKeyMatches(dataInputObject)) {
            ContextAwareServiceStats.getInstance().incrementUnauthorizedEventsCount(remoteAddress, (String) dataInputObject.get(EVENT_KEY_MSE_UDI));
            return Response.status(Status.UNAUTHORIZED).build();
        }
        
        ContextAwareServiceStats.getInstance().incrementAassociationEventsCount();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        AssociationEvent ae = mapper.convertValue(inputObject.get(Event.ASSOCIATION.toString()), AssociationEvent.class);
        String deviceId = ae.getDeviceId();
        MDC.put(MDCKeys.DEVICE_MAC_ADDRESS, deviceId);
        String responseBody = "AP MAC Address = " + ae.getApMacAddress();
        LOGGER.trace("The event received from the server is an assocation event for client '{}'", deviceId);
        WirelessClient client = null;
        try {
            client = mMobileServerCacheService.getWirelessClient(deviceId.toLowerCase());
            
            if ((client != null) && (client.getDeviceType() == DeviceType.IOS)) {
                //Dont worry about return response type - we are doing best effort
                savePendingNotification(ae, deviceId, responseBody);
            }
        } catch (Exception e) {
            LOGGER.error("Exception getting a client for association event: ", e);
        }
        if (client == null) {
            Response response = savePendingNotification(ae, deviceId, responseBody);
            if (response != null) {
                return response;
            }
        } else {
            Map<String, Object> data = (Map<String, Object>) inputObject.get(Event.ASSOCIATION.toString());
            String mseUdId = (String) data.get(EVENT_KEY_MSE_UDI);
            
            //Get the Floor ID - sometimes it is cast as Long
            String floorId =  "";
            Object floorObject = data.get(EVENT_KEY_FLOOR_REF_ID);
            if (floorObject != null) {
                if (floorObject instanceof Integer) {
                    floorId = Integer.toString(((Integer) floorObject));
                } else if (floorObject instanceof Long) {
                    floorId = Long.toString(((Long) floorObject));
                } else if (floorObject instanceof String) {
                    floorId = (String) floorObject;
                }
            }
            String venueUdId = "";
            String floorName = "";
            Floor floor = null;
            if (mseUdId != null) {
                venueUdId = mMobileServerCacheService.getVenueUdid(mseUdId, floorId);
                floor = mMobileServerCacheService.getFloorByVenueUdid(venueUdId, floorId);
                if (floor == null) {
                    LOGGER.trace("Floor Notified by Location server is not available for VenueUdId {}" , venueUdId);
                } else {
                    floorName = floor.getName();
                    LOGGER.trace("Got Floor and VenueUdId '{}', '{}'", floorName, venueUdId);
                }
            }
            Object geoCoordinate = data.get(EVENT_KEY_GEO_COORDINATE);
            if (geoCoordinate != null) {
                if (geoCoordinate instanceof Map) {
                    Map geoCoordinateMap = (Map) geoCoordinate;
                    Object latitude = geoCoordinateMap.get(EVENT_KEY_LATITUDE);
                    if (latitude != null) {
                        if (latitude instanceof Double) {
                            client.setLatitude((Double) latitude);
                        } else {
                            client.setLatitude(0);
                        }
                    }
                    Object longitude = geoCoordinateMap.get(EVENT_KEY_LONGITUDE);
                    if (longitude != null) {
                        if (longitude instanceof Double) {
                            client.setLongitude((Double) longitude);
                        } else {
                            client.setLongitude(0);
                        }
                    }
                } else {
                    client.setLongitude(0);
                    client.setLatitude(0);
                }
            }
            client.setFloorId(floorId);
            client.setVenueUdId(venueUdId);
            MapCoordinate coordinates = ae.getLocationCoordinate(); 
            client.setX(coordinates.getX());
            client.setY(coordinates.getY()); 
            client.setLastLocationUpdateTime(System.currentTimeMillis());
            LOGGER.trace("Client | VenueUdId | Floor Name | X | Y : {} | {} | {} | {} | {}", client.getMacAddress(), venueUdId, floorName, client.getX(), client.getY());
            
            //Send the Presence Notification before zone notification if necessary
            Venue venue = mMobileServerCacheService.getVenue(venueUdId);
            if (venue != null && (client.requirePresenceNotificaton() || sendPresenceNotificationOnEachAssociationEvent)) {
                LOGGER.trace("Venue has changed for device {}. New Venue ID : {}", deviceId, venue.getVenueUdId());
                sendPresenceNotification(client, venue);
            }
            
            try {
                if (floor != null) {
                    Zone zone = getPushNotificationZone(floor, ae.getLocationCoordinate());
                    if (zone != null && (client.getZoneId() == null || !client.getZoneId().equalsIgnoreCase(zone.getId()))) {
                        client.setZoneId(zone.getId());
                        client.setZoneName(zone.getName());
                        client.setZonePoints(zone.getPoints());
                        if (venue == null) {
                            LOGGER.error("No Venue found for containment event with dvice id '{}'", deviceId);
                        } else {                            
                            //Send the Zone notification after presence notification
                            String zoneKey = zone.getVenueUdId() + ":" + zone.getMseFloorId() + ":" + zone.getId();
                            LOGGER.trace("Checking if push notification is required for association of device {} in zone {}", deviceId, zoneKey);
                            if (client.requirePushNotification(zoneKey, minutesBetweenSendingNotification)) {
                                sendPushNotification(client, zone, venue);
                            }
                        }
                    }
                }
                mMobileServerCacheService.addOrUpdateWirelessClient(client, clientExpireTimeInSeconds, !testClientDevices.isEmpty());
                JSONObject responseData = new JSONObject();

                JSONObject parameters = new JSONObject();
                parameters.accumulate("notifications_enabled", "true");
                parameters.accumulate("update_distance", updateDistance);
                parameters.accumulate("zone_timeout", zoneTimeoutResponseSeconds);

                if (filterRegisteredDevices) {
                    JSONArray subscriptionNameArray = new JSONArray();
                    subscriptionNameArray.put(CMX_LOCATIION_EVENT_SUBSCRIPTION_NAME);
                    parameters.accumulate("subscription_names", subscriptionNameArray);
                    JSONArray macAddressArray = new JSONArray();
                    macAddressArray.put(client.getMacAddress());
                    getNewRegisteredDevice(macAddressArray);
                    parameters.accumulate("mac_address_list", macAddressArray);
                }

                responseData.accumulate("configuration", parameters);
                LOGGER.trace("Completed update of assocation event for client '{}'. Respond object is {}", client.getMacAddress(), responseData);
                return Response.ok().entity(responseData).build();
            }
            catch (MobileServerCacheException e) {
                LOGGER.error("Exception saving a assocation event for client: ", e);
            }
            catch (Exception e) {
                LOGGER.error("Exception in assocation event", e);
            }
        }
        
        return Response.ok(responseBody).build();

    }

    private Response savePendingNotification(AssociationEvent ae, String deviceId, String responseBody) {
        LOGGER.trace("The event is a new association notification for '{}'", deviceId);
        PendingAssociationNotification pan = new PendingAssociationNotification();
        
        //
        // The base radio AP MAC address is the one set in the association event. However the client
        // device will see a radio MAC address which can be any one of the last bytes of the MAC address.
        // Just set the last byte to a zero and use this to do the lookup for the AP MAC address later
        //
        String apMac = ae.getApMacAddress();
        int endIndex = apMac.length() - 1;
        String newApMAC = apMac.substring(0, endIndex) + '0';
        pan.setApMAC(newApMAC);
        //
        // Check if IP Address is not null. There are cases when no IP address is set.
        // In that case the event has no use and can be ignored
        //
        if (ae.getIpAddress().size() <= 0 || ae.getIpAddress().get(0) == null) {
            LOGGER.debug("Association event for client: {} does not have an IP address and will be ignored", deviceId);
            return Response.ok(responseBody).build();
        }
        //
        // An IP Address list is sent. The list is random and can contain an IPv4 address as the first element
        // or an IPv6 address as the first element. The IPv4 address is determined by checking for a period character
        //
        for (int i = 0; i < ae.getIpAddress().size(); ++i) {
            if (ae.getIpAddress().get(i).indexOf('.') >= 0) {
                pan.setClientIP(ae.getIpAddress().get(i));
                break;
            }
        }
        pan.setClientMAC(deviceId);
        pan.setLastUpdateTime(System.currentTimeMillis());
        try {
            mMobileServerCacheService.addOrUpdatePendingAssociationNotification(pan, associationExpireTimeInSeconds);
        }
        catch (MobileServerCacheException e) {
            LOGGER.error("Exception saving an association notification", e);
            MDC.remove(MDCKeys.DEVICE_MAC_ADDRESS);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        
        return null;
    }
    
    public Zone getPushNotificationZone(Floor floor, MapCoordinate mapCoordinate) {
        if (gmFactory == null) {
            gmFactory = new GeometryFactory();
        }
        Zone zone = null;
        try {
            zone = floor.getZoneByLocation(gmFactory, mapCoordinate);
        }
        catch (JsonParseException e) {
            LOGGER.error("JSON Parsing Error : {}",  e.getLocalizedMessage());
        }
        catch (JsonMappingException e) {
            LOGGER.error("JSON Mapping Error : {}", e.getLocalizedMessage());
        }
        catch (IOException e) {
            LOGGER.error("IO Error : {}", e.getLocalizedMessage());
        }
        catch (Exception e) {
            LOGGER.error("Exception : {}", e.getLocalizedMessage());
        }

        return zone;
    }

    private void sendPresenceNotification(WirelessClient client, Venue venue) {
        sendPushNotification(client, venue, venue.getPushNotificationMessage());
    }
    
    private void sendPushNotification(WirelessClient client, Zone zone, Venue venue) {
        sendPushNotification(client, venue, zone.getPushNotificationMessage());
    }

    public void sendPushNotification(WirelessClient client, Venue venue, String message) {
        if (client.getPushNotificationRegistrationId() == null || client.getPushNotificationRegistrationId().length() <= 0) {
            LOGGER.trace("Push notification ID not set for the device: {}", client.getMacAddress());
            return;
        }
        if (message == null || message.length() <= 0) {
            LOGGER.trace("Venue {} does not have a push notification message for the device: {}", venue.getName(), client.getMacAddress());
            return;            
        }
        LOGGER.trace("Sending push notification for the device: {}", client.getMacAddress());
        if (client.getDeviceType().equals(DeviceType.ANDROID)) {
            LOGGER.trace("Sending push notification for an Android device");
            mNotificationService.runTask(new AndroidPushNotification(client, message, venue));
        }  else if (client.getDeviceType().equals(DeviceType.IOS) || client.getDeviceType().equals(DeviceType.IOS6)) {
            // TODO: Need to read password for p12 file
            // Push notification file is per MSE, so it is place
            // at map location
            LOGGER.trace("Sending push notification for an iOS device");
            File file = new File(mapLocation, venue.getApplePushNotificationFile());
            boolean isProductionBuild = venue.getAppleProductionServer();
            mNotificationService.runTask(new ApplePushNotification(client, message, venue, file, venue.getApplePushNotificationKey(), isProductionBuild));
        } else {
            LOGGER.trace("Unable to determined device type to send push notification");
        }
    }
    
    private boolean authKeyMatches(Map<String, Object> data)
    {
        if (ignoreNotificationEventAuth) {
            return true;
        }
        
        String authTokenFromCache = "";
        String floorId =  "";
        Object floorObject = data.get(EVENT_KEY_FLOOR_REF_ID);
        if (floorObject != null) {
            if (floorObject instanceof Integer) {
                floorId = Integer.toString(((Integer) floorObject));
            } else if (floorObject instanceof Long) {
                floorId = Long.toString(((Long) floorObject));
            } else if (floorObject instanceof String) {
                floorId = (String) floorObject;
            }
        }
        try {
            authTokenFromCache = authTokenCache.getAuthToken((String) data.get(EVENT_KEY_MSE_UDI), floorId); 
        } catch (Exception e) {
            LOGGER.debug("No Auth Token for MSEUDI {} and Floor {}", (String) data.get(EVENT_KEY_MSE_UDI), floorId);
            LOGGER.debug("Check if MSE has been added to server. Server is not authorized and may be need to be synched again");
        }
        
        String eventClientAuthInfo = (String) data.get(EVENT_CLIENT_AUTH_INFO);
        
        if (authTokenFromCache != null && authTokenFromCache.equalsIgnoreCase(eventClientAuthInfo)) {
            return true;
        }
        
        LOGGER.debug("Unauthorized Event : MSEUDI {} and Floor {}", (String) data.get(EVENT_KEY_MSE_UDI), floorId);        
        return false;
    }
}
