package com.cisco.cmxmobile.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.cisco.cmxmobile.cacheService.Utils.EncryptionUtil;
import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;
import com.cisco.cmxmobile.info.Version;
import com.cisco.cmxmobile.model.DeviceType;
import com.cisco.cmxmobile.model.Floor;
import com.cisco.cmxmobile.model.PendingAssociationNotification;
import com.cisco.cmxmobile.model.PointOfInterest;
import com.cisco.cmxmobile.model.ServerStats;
import com.cisco.cmxmobile.model.Venue;
import com.cisco.cmxmobile.model.WirelessClient;
import com.cisco.cmxmobile.model.Zone;
import com.cisco.cmxmobile.pushNotification.apple.ApplePushNotification;
import com.cisco.cmxmobile.server.rest.MobileServerRestClient;
import com.cisco.cmxmobile.server.rest.RestClientException;
import com.cisco.cmxmobile.services.mse.LocationNotificationHandler;

public class ServerOperations {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerOperations.class);
    
    private final DateFormat dateFormat = new SimpleDateFormat("dd MMM YYYY HH:mm:ss.SS");

    public MobileServerCacheService mobileServerCacheService;
    
    public LocationNotificationHandler locationNotificationHandler;
    
    public MobileServerRestClient restClient;

    public void run(String[] args) {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] {"spring/application-context.xml"});
        mobileServerCacheService = (MobileServerCacheService) ctx.getBean("mobileServerCacheService");
        locationNotificationHandler = (LocationNotificationHandler) ctx.getBean(LocationNotificationHandler.class);
        restClient = (MobileServerRestClient) ctx.getBean(MobileServerRestClient.class);
        
        if (args.length <= 0) {
            displayUsage();
            return;
        }
        int cmdIndex;
        for (cmdIndex = 0; cmdIndex < args.length; cmdIndex++) {
            if (args[cmdIndex].equalsIgnoreCase("getServerConfig")) {
                getServerConfig();
            } else if (args[cmdIndex].equalsIgnoreCase("getServerStats")) {
                ++cmdIndex;
                if (cmdIndex < args.length && args[cmdIndex].equalsIgnoreCase("-reset")) {
                    resetServerStats();
                }
                getServerStats();
            } else if (args[cmdIndex].equalsIgnoreCase("getLocationByMac")) {
                ++cmdIndex;
                if (cmdIndex >= args.length) {
                    LOGGER.error("Missing MAC Address of client");
                    return;
                }
                getLocationByMac(args[cmdIndex]);
                return;
            } else if (args[cmdIndex].equalsIgnoreCase("monitorLocationByMac")) {
                ++cmdIndex;
                if (cmdIndex >= args.length) {
                    LOGGER.error("Missing MAC Address of client");
                    return;
                }
                monitorLocationByMac(args[cmdIndex]);
                return;
            } else if (args[cmdIndex].equalsIgnoreCase("sendPushNotificationByMac")) {
                ++cmdIndex;
                if (cmdIndex >= args.length) {
                    LOGGER.error("Missing MAC Address of client");
                    return;
                }
                String macAddress = args[cmdIndex];
                ++cmdIndex;
                if (cmdIndex >= args.length) {
                    LOGGER.error("Missing message to send to client");
                    return;
                }
                String message = args[cmdIndex];
                sendPushNotificationByMac(macAddress, message);
                return;
            } else if (args[cmdIndex].equalsIgnoreCase("testPushNotificationByMac")) {
                ++cmdIndex;
                if (cmdIndex >= args.length) {
                    LOGGER.error("Missing MAC Address of client");
                    return;
                }
                String macAddress = args[cmdIndex];
                testPushNotificationByMac(macAddress);
                return;
            } else {
                displayUsage();
                return;
            }
        }
    }
    
    private void displayUsage() {
        LOGGER.error("Usage: cmd OPTION");
        LOGGER.error("");
        LOGGER.error("Mandatory options.");
        LOGGER.error("   getServerConfig                                 Get server configuration");
        LOGGER.error("   getServerStats [-reset]                         Get server statistics. Option to reset stats");
        LOGGER.error("   getLocationByMac MACADDRESS                     Get the location of a device by MAC Address");
        LOGGER.error("   moniotrLocationByMac MACADDRESS                 Monitor the location of a device by MAC Address");
        LOGGER.error("   sendPushNotificationByMac MACADDRESS MESSAGE    Send a push notification to a device base upon the MAC Address");
    }
    
    private void monitorLocationByMac(String macAddress) {
        WirelessClient client = mobileServerCacheService.getWirelessClient(macAddress);
        WirelessClient previousClient = null;
        if (client == null) {
            LOGGER.error("Wireless Client {} Not Found", macAddress);
            return;
        }
        while (true) {
            if (previousClient == null || client == null || previousClient.getLastLocationUpdateTime() != client.getLastLocationUpdateTime()) {
                logClientLocation(client);
            }
            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException ex) {
                LOGGER.debug("Sleep interrupted for monitoring client");
            }
            previousClient = client;
            client = mobileServerCacheService.getWirelessClient(macAddress);
        }
    }

    private void getLocationByMac(String macAddress) {
        PendingAssociationNotification pendingClient = mobileServerCacheService.getPendingAssociationByClientMac(macAddress);
        if (pendingClient != null) {
            logClientPendingAssociation(pendingClient);
        }
        WirelessClient client = mobileServerCacheService.getWirelessClient(macAddress);
        if (client == null) {
            if (pendingClient == null) {
                LOGGER.error("Wireless Client {} Not Found", macAddress);
            } else {
                LOGGER.error("No Wireless Client Location");                
            }
            return;
        }
        logClientLocation(client);
    }

    private void logClientPendingAssociation(PendingAssociationNotification pendingClient) {
        if (pendingClient != null) {
            LOGGER.info("------- Wireless Client Pending Association -------");
            LOGGER.info("Last Updated Time: {}", dateFormat.format(new Date(pendingClient.getLastUpdateTime())));
            LOGGER.info("MAC Address: {}", pendingClient.getClientMAC());
            LOGGER.info("AP MAC Address: {}", pendingClient.getApMAC());
            LOGGER.info("Client IP Address: {}", pendingClient.getClientIP());
        }
    }

    private void logClientLocation(WirelessClient client) {
        LOGGER.info("------- Wireless Client Location -------");
        if (client == null) {
            LOGGER.error("Wireless Client Not Found");
        } else {
            LOGGER.info("Last Updated Time: {}", dateFormat.format(new Date(client.getLastLocationUpdateTime())));
            LOGGER.info("MAC Address: {}", client.getMacAddress());
            LOGGER.info("Device Type: {}", client.getDeviceType());
            Venue venue = mobileServerCacheService.getVenue(client.getVenueUdId());
            if (venue == null) {
                LOGGER.info("Venue Name: Unkown");
            } else {
                LOGGER.info("Venue Name: {}", venue.getName());
            }
            LOGGER.info("Venue ID: {}", client.getVenueUdId());
            if (venue == null) {
                LOGGER.info("Floor Name: Unkown");
            } else {
                Floor floor = venue.getFloorById(client.getFloorId());
                if (floor == null) {
                    LOGGER.info("Floor Name: Unkown");                    
                } else {
                    LOGGER.info("Floor Name: {}", floor.getName());
                }
            }
            LOGGER.info("Floor ID: {}", client.getFloorId());
            LOGGER.info("Zone Name: {}", client.getZoneName());
            LOGGER.info("Zone ID: {}", client.getZoneId());
            LOGGER.info("X: {}", client.getX());
            LOGGER.info("Y: {}", client.getY());
            LOGGER.info("Latitude: {}", client.getLatitude());
            LOGGER.info("Longitude: {}", client.getLongitude());
        }
    }
    
    private void sendPushNotificationByMac(String macAddress, String message) {
        LOGGER.info("------- Wireless Client Push Notification -------");
        WirelessClient client = mobileServerCacheService.getWirelessClient(macAddress);
        if (client == null) {
            LOGGER.error("Wireless Client {} Not Found", macAddress);
        } else {
            LOGGER.trace("Sending push notification for the device");
            Venue venue = mobileServerCacheService.getVenue(client.getVenueUdId());
            if (venue == null) {
                LOGGER.error("No Venue found for wireless client '{}'", macAddress);
                return;
            }
            LOGGER.info("Starting to send push notification to client: {} with message: {}", macAddress, message);
            locationNotificationHandler.sendPushNotification(client, venue, message);
            LOGGER.info("Completed sending push notification to client: {} with message: {}", macAddress, message);
        }
    }

    private void testPushNotificationByMac(String macAddress) {
        LOGGER.info("------- Wireless Client Push Notification -------");
        WirelessClient client = mobileServerCacheService.getWirelessClient(macAddress);
        if (client == null) {
            LOGGER.error("Wireless Client {} Not Found", macAddress);
        } else {
            if (client.getDeviceType().equals(DeviceType.ANDROID)) {
                LOGGER.trace("Sending test notification for an Android device");
                sendPushNotificationByMac(macAddress, new Date() + ": Test Message");
            }  else if (client.getDeviceType().equals(DeviceType.IOS) || client.getDeviceType().equals(DeviceType.IOS6)) {
                LOGGER.trace("Sending test notification for an iOS device");
                Venue venue = mobileServerCacheService.getVenue(client.getVenueUdId());
                if (venue == null) {
                    LOGGER.error("No Venue found for wireless client '{}'", macAddress);
                    return;
                }
                LOGGER.info("Starting to test push notification for client: {}", macAddress);
                String s = null;
                try {
                	
                    File file = new File(locationNotificationHandler.getKeyFileLocation(), venue.getApplePushNotificationFile());
                    Process p = Runtime.getRuntime().exec(locationNotificationHandler.getKeyFileLocation() + "/../bin/testPushNotificationByMac.sh runAppleTest " + file + " " + EncryptionUtil.decrypt(venue.getApplePushNotificationKey(), ApplePushNotification.PASSWORD_DECREPTION_KEY) + " " + client.getPushNotificationRegistrationId());
                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    BufferedReader stdError = new BufferedReader(new 
                            InputStreamReader(p.getErrorStream()));
                   // read the output from the command
                    while ((s = stdInput.readLine()) != null) {
                        LOGGER.info(s);
                    }
                    // read any errors from the attempted command
                    LOGGER.info("Here is the standard error of the command (if any):\n");
                    while ((s = stdError.readLine()) != null) {
                        LOGGER.info(s);
                    }
                   
                    LOGGER.info("Completed testing push notification for client: {}", macAddress);
                } catch (Exception ex) {
                    LOGGER.error("Error running test command '{}'", ex);
                }
            }
        }
    }

    private void getServerConfig() {
        getServerInfo();
        getVenueInfo();
    }
    
    private void resetServerStats() {
        try {
            restClient.resetServerStats();
        } catch (RestClientException ex) {
            LOGGER.error("Failure reseting server stats");
        }
    }

    private void getServerStats() {
        getWirelessClientStats();
        try {
            ServerStats stats = restClient.getServerStats();
            stats.logStats();
        } catch (RestClientException ex) {
            LOGGER.error("Failure getting server stats");
        }
    }

    private void getWirelessClientStats() {
        LOGGER.info("------- Wireless Client Information --------");
        LOGGER.info("Tracked Associated Clients                 : {}", mobileServerCacheService.getAllPendingAssociationsCount());        
        LOGGER.info("Tracked Clients                            : {}", mobileServerCacheService.getAllWirelessClientsCount());        
    }
    
    private void getVenueInfo() {
        int poiCountForVenue = 0;
        int poiCountTotal = 0;
        int floorCountTotal = 0;
        List<Venue> venueList = mobileServerCacheService.getAllVenues();
        if (venueList != null) {
            for (Venue venueInfo : venueList) {
                poiCountForVenue = 0;
                LOGGER.info("------- Venue Information -------");
                LOGGER.info("Venue Name: {}", venueInfo.getName());
                LOGGER.info("Venue Description: {}", venueInfo.getDescription());
                LOGGER.info("Venue ID: {}", venueInfo.getVenueUdId());
                LOGGER.info("MSE IP Address: {}", venueInfo.getMseIP());
                LOGGER.info("Created Date: {}", dateFormat.format(new Date(venueInfo.getCreatedDate())));
                LOGGER.info("Last Update Date: {}", dateFormat.format(new Date(venueInfo.getUpdatedDate())));
                LOGGER.info("MSE UDI ID: {}", venueInfo.getMseUdId());
                LOGGER.info("Latitude: {}", venueInfo.getLat());
                LOGGER.info("Longitude: {}", venueInfo.getLon());
                LOGGER.info("Mobile Push Notification Message: {}", venueInfo.getPushNotificationMessage());
                List<Floor> floorList = venueInfo.getFloorList();
                if (floorList != null) {
                    for (Floor floorInfo : floorList) {
                        LOGGER.info("   ------- Floor Information -------");
                        LOGGER.info("   Floor Name: {}", floorInfo.getName());
                        LOGGER.info("   Floor Description: {}", floorInfo.getDescription());
                        LOGGER.info("   Floor ID: {}", floorInfo.getId());
                        LOGGER.info("   Created Date: {}", dateFormat.format(new Date(floorInfo.getCreatedDate())));
                        LOGGER.info("   Last Update Date: {}", dateFormat.format(new Date(floorInfo.getUpdatedDate())));
                        List<PointOfInterest> pointOfInterestList = floorInfo.getPoiList();
                        if (pointOfInterestList != null) {
                            for (PointOfInterest pointOfInterestInfo : pointOfInterestList) {
                                LOGGER.info("      ------- Point Of Interest Information -------");
                                LOGGER.info("      POI Name: {}", pointOfInterestInfo.getName());
                                LOGGER.info("      POI ID: {}", pointOfInterestInfo.getId());
                                LOGGER.info("      POI Description: {}", pointOfInterestInfo.getDescription());
                                LOGGER.info("      POI Position X: {}, Y: {}", pointOfInterestInfo.getX(), pointOfInterestInfo.getY());
                            }
                            poiCountForVenue += pointOfInterestList.size();
                            poiCountTotal += pointOfInterestList.size();
                            LOGGER.info("   POI Count For Floor: {}", pointOfInterestList.size());
                        } else {
                            LOGGER.info("      ------- NO Point Of Interest Information -------"); 
                        }
                        List<Zone> zoneList = floorInfo.getZoneList();
                        if (zoneList != null) {
                            for (Zone zoneInfo : zoneList) {
                                LOGGER.info("      ------- Zone Information -------");
                                LOGGER.info("      Zone Name: {}", zoneInfo.getName());
                                LOGGER.info("      Zone ID: {}", zoneInfo.getId());
                                LOGGER.info("      Zone Points: {}", zoneInfo.getPoints());
                                LOGGER.info("      Zone Mobile Push Notification Message: {}", zoneInfo.getPushNotificationMessage());
                            }
                            poiCountForVenue += pointOfInterestList.size();
                            poiCountTotal += pointOfInterestList.size();
                            LOGGER.info("   POI Count For Floor: {}", pointOfInterestList.size());
                        } else {
                            LOGGER.info("      ------- NO Zone Information -------"); 
                        }
                    }
                    floorCountTotal += floorList.size();
                    LOGGER.info("Floor Count For Venue: {}", floorList.size());
                    LOGGER.info("POI Count For Venue: {}", poiCountForVenue);
                } else {
                    LOGGER.info("   ------- NO Floor Information -------");                    
                }
            }
            LOGGER.info("------- Total Venue Information -------");
            LOGGER.info("Venue Count: {}", venueList.size());
            LOGGER.info("Floor Count Total: {}", floorCountTotal);
            LOGGER.info("POI Count Total: {}", poiCountTotal);
        } else {
            LOGGER.info("------- NO Venue Information -------");            
        }
    }
    
    private void getServerInfo() {
        LOGGER.info("Server Version: {}", Version.getInstance().getVersionNumber());
    }
    
    public static void main(String[] args) {
        ServerOperations operations = new ServerOperations();
        operations.run(args);
    }

}
