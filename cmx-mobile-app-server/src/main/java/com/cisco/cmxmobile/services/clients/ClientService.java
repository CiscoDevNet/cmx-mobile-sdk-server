package com.cisco.cmxmobile.services.clients;

import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Resource;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.cacheService.client.MobileServerCacheException;
import com.cisco.cmxmobile.model.DeviceType;
import com.cisco.cmxmobile.model.PendingAssociationNotification;
import com.cisco.cmxmobile.model.UsertoMACMapping;
import com.cisco.cmxmobile.model.WirelessClient;
import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;
import com.cisco.cmxmobile.utils.EmailProperties;
import com.cisco.cmxmobile.utils.MDCKeys;
import com.cisco.cmxmobile.server.stats.MobileAppStats;
import com.cisco.cmxmobile.services.mail.SendEmail;
import com.cisco.cmxmobile.services.mse.LocationNotificationHandler;

@Component
@Path("/api/cmxmobile/v1/clients")
public class ClientService {

    @Value("${location.clientExpireTimeInSeconds}")
    int clientExpireTimeInSeconds;

    @Context
    UriInfo uriInfo;

    @Autowired
    private LocationNotificationHandler locationNotificationHandler;

    public static final String CLIENT_AUTHENTICATION_COOKIE_NAME = "cmxMobileApplicationCookie";

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientService.class);
    
    private static int currentTestClientCount = 0;

    private static final String EMAIL_LINE_FEED_CHAR = "\n";

    @Autowired
    private MobileServerCacheService mobileServerCacheService;

    @Resource
    private List<WirelessClient> testClientDevices;

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public String getErrorMessage(@QueryParam("search") String search) {
        return "Wrong method";
    }

    @GET
    @Path("/location/{deviceId}")
    /*
     * @PreAuthorize( "hasRole('mobileAppUser')")
     */
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClientsByMAC(@PathParam("deviceId") String deviceId) {
        MDC.put(MDCKeys.DEVICE_ID, deviceId);
        MobileAppStats.getInstance().incrementLocationRequestsCount();
        LOGGER.trace("Request to retrieve client current location for device ID '{}'", deviceId);

        WirelessClient macClientLocation = mobileServerCacheService.getWirelessClientByUniqueID(deviceId);
        if (macClientLocation == null) {
            // Forbidden better than File Not Found - it doesn't leak
            // information
            LOGGER.trace("Unable to determine client MAC address from device ID '{}'", deviceId);
            MDC.remove(MDCKeys.DEVICE_ID);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        JSONObject clientObject = new JSONObject();
        try {
            clientObject.accumulate("deviceId", macClientLocation.getUniqueID());
            clientObject.accumulate("lastLocationUpdateTime", macClientLocation.getLastLocationUpdateTime());
            clientObject.accumulate("venueId", macClientLocation.getVenueUdId());
            clientObject.accumulate("floorId", macClientLocation.getFloorId());
            try {
                if (!macClientLocation.getZoneId().isEmpty()) {
                    clientObject.accumulate("zoneId", macClientLocation.getZoneId());
                    clientObject.accumulate("zoneName", macClientLocation.getZoneName()); 
                    clientObject.accumulate("zonePoints", macClientLocation.getZonePoints());
                }
            } catch (Exception ex) {
                LOGGER.error("Error during zone object creation for device ID '{}'", deviceId, ex.getLocalizedMessage());
            }
            JSONObject locationObject = new JSONObject();
            locationObject.accumulate("x", macClientLocation.getX());
            locationObject.accumulate("y", macClientLocation.getY());
            clientObject.accumulate("mapCoordinate", locationObject);
            
            JSONObject geoCoordinateObject = new JSONObject();
            geoCoordinateObject.accumulate("latitude", macClientLocation.getLatitude());
            geoCoordinateObject.accumulate("longitude", macClientLocation.getLongitude());
            clientObject.accumulate("geoCoordinate", geoCoordinateObject);
            
            LOGGER.trace("Completed setting current location for device ID '{}' with MAC Address '{}'", deviceId, macClientLocation.getMacAddress());
        }
        catch (Exception e) {
            LOGGER.error("Error during location object creation for device ID '{}'", deviceId, e.getLocalizedMessage());
        }
        LOGGER.trace("Returning JSON object with device ID '{}' : {}", deviceId, clientObject);
        MDC.remove(MDCKeys.DEVICE_ID);
        if (macClientLocation.getFloorId() == null || macClientLocation.getFloorId().length() <= 0 || macClientLocation.getVenueUdId() == null || macClientLocation.getVenueUdId().length() <= 0) {
            return Response.status(Response.Status.NOT_FOUND).entity(clientObject).build();            
        }
        return Response.status(Response.Status.OK).entity(clientObject).build();
    }

    /**
     * Registers a client. For iOS clients, there needs to be an association
     * event sent from the MSE to this
     * 
     * @param registrationId
     * @param apMAC
     * @param clientIP
     * @param device
     * @param clientMAC
     * @return
     */
    @POST
    @Path("/register")
    public Response registerClient(@FormParam("pushNotificationRegistrationId") String registrationId, @FormParam("apMACAddress") String apMAC, @FormParam("clientIPAddress") String clientIP, @FormParam("clientType") String device, @FormParam("clientMACAddress") String clientMAC, @FormParam("userId") String userId) {

        MobileAppStats.getInstance().incrementRegisterRequestsCount();
        LOGGER.info("Client registration requested with registration ID '{}' AP MAC Address '{}' Client IP Address '{}' Client Type '{}' Client MAC Address '{}' User ID '{}'", registrationId != null ? registrationId : "None", apMAC != null ? apMAC : "None", clientIP != null ? clientIP : "None", device, clientMAC != null ? clientMAC : "None", userId != null ? userId : "None");
        if (registrationId == null || registrationId.length() <= 0) {
            LOGGER.info("Client does not have a registration ID. Push notifications will not work for this device: AP MAC Address '{}' Client IP Address '{}' Client Type '{}' Client MAC Address '{}'  User ID '{}'", apMAC != null ? apMAC : "None", clientIP != null ? clientIP : "None", device, clientMAC != null ? clientMAC : "None", userId != null ? userId : "None");            
        }
        
        String actualClientMAC = null;
        String deviceLower = device.toLowerCase();
        if (!testClientDevices.isEmpty()) {
            actualClientMAC = testClientDevices.get(currentTestClientCount).getMacAddress() + System.currentTimeMillis();
            ++currentTestClientCount;
            if (currentTestClientCount >= testClientDevices.size()) {
                currentTestClientCount = 0;
            }
        } else if (DeviceType.ANDROID.toString().equals(deviceLower)) {
            // get clientMAC from POST
            actualClientMAC = clientMAC.toLowerCase();
            LOGGER.info("Android device sent actual MAC address '{}'", actualClientMAC);
        }  else if (DeviceType.IOS6.toString().equals(deviceLower)) {
            // get client MAc from IOS 6 device
            actualClientMAC = clientMAC.toLowerCase();
            LOGGER.info("IOS 6 device sent actual MAC address '{}'", actualClientMAC);
        } else if (DeviceType.IOS.toString().equals(deviceLower)) {
            // get clientMAC from PendingAssociationNotification for IOS 7
            // Last byte of the AP MAC Address has been set to zero since the broadcast
            // AP MAC address can fall into any range of that last byte
            int endIndex = apMAC.length() - 1;
            String newApMAC = apMAC.substring(0, endIndex) + '0';
            LOGGER.debug("IOS lookup using base AP MAC '{}'", newApMAC);
            PendingAssociationNotification pan = mobileServerCacheService.getPendingAssociation(newApMAC, clientIP);
            if (pan == null) {
                LOGGER.info("iOS device has no pending association nofication. Respond indicating the registration was accepted but not completed");
                return Response.status(Response.Status.CONFLICT).entity("iOS device has no pending association nofication. Please join a Wi-Fi network.").build();
            } else {
                actualClientMAC = pan.getClientMAC();
                LOGGER.info("iOS device pending assocation notification found. Using the MAC address '{}'", actualClientMAC);
            }
        } else {
            // no device type? Fail.
            LOGGER.error("Device has attempted to register with invalid device type. Respond with bad message");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        
        // check input validity
        // check for existing clientID.
        WirelessClient existingClient = null;

        // look up client from AP MAC & client IP address

        // client found - make new client, delete the pending association
        // notification
        // check for duplicate MAC address
        existingClient = mobileServerCacheService.getWirelessClient(actualClientMAC);
        WirelessClient newClient = null;
        if (existingClient == null) {
            // LOGGER.error("Duplicate MAC Address detected through device registration! MAC '{}' Respond with conflict message",
            // actualClientMAC);
            // TODO: same concern about leaking too much information
            // return Response.status(Response.Status.CONFLICT).build();
            // }
            LOGGER.info("Client is new and cache will be updated: '{}'", actualClientMAC);
            newClient = new WirelessClient();
            newClient.setMacAddress(actualClientMAC.toLowerCase());
            newClient.setPushNotificationRegistrationId(registrationId);
            newClient.setDeviceType(DeviceType.fromString(device));
            newClient.setUserId(userId);
            UsertoMACMapping userToMacMapping = mobileServerCacheService.getUsertoMACMapping(userId);
            if (userToMacMapping != null) {
                newClient.setVenueUdId(userToMacMapping.getVenueUdId());
                newClient.setFloorId(userToMacMapping.getFloorId());
                newClient.setX(userToMacMapping.getX());
                newClient.setY(userToMacMapping.getY());
            }
            try {
                LOGGER.info("Updating cache for new client: '{}'", actualClientMAC);
                mobileServerCacheService.addOrUpdateWirelessClient(newClient, clientExpireTimeInSeconds, !testClientDevices.isEmpty());
            }
            catch (MobileServerCacheException e) {
                LOGGER.error("Error attempting to register new client '{}': {}", newClient, e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            locationNotificationHandler.addNewRegisteredDevice(clientMAC);
        }
        else {
            LOGGER.info("Client already exists in the cache: '{}'", actualClientMAC);
            newClient = existingClient;
            // When a registration is done again. The push notification registration ID may have changed
            // If this changed then updated the cache entry for the client.
            newClient.setPushNotificationRegistrationId(registrationId);
            newClient.generateNewAuthenticationToken();
            newClient.setUserId(userId);
            try {
                LOGGER.info("Updating cache for new client: '{}'", actualClientMAC);
                mobileServerCacheService.addOrUpdateWirelessClient(newClient, clientExpireTimeInSeconds, !testClientDevices.isEmpty());
            }
            catch (MobileServerCacheException e) {
                LOGGER.error("Error attempting to register new client but continuing '{}'", actualClientMAC, e);
            }
        }
        // TODO: decide on a good expiration period... which may not be
        // necessary, since our client is probably not a browser.
        // TODO: expire some time in the future... for now, this is badly
        // behaved
        // make cookie String
        String cookieString;
        try {
            cookieString = generateRememberMeCookie(newClient);
        }
        catch (NoSuchAlgorithmException e) {
            LOGGER.error("No MD5 algorithm available - whaaaaaaaaat!?");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("We won't be able to remember you!").build();
        }
        LOGGER.info("Generated cookie for client completed: '{}'", clientMAC);
        URI newLocation = uriInfo.getBaseUriBuilder().path("/api/cmxmobile/v1/clients").path("location").path(newClient.getUsername()).build();
        LOGGER.info("Device registration has completed successfully. Respond with registration completion message");
        URL baseUrl = null;
        try {
            baseUrl = new URL(uriInfo.getBaseUri().toString());
        } catch (Exception ex) {
            LOGGER.error("Problem get base URL for registration request");
        }
        return Response.created(newLocation).cookie(new NewCookie(CLIENT_AUTHENTICATION_COOKIE_NAME, cookieString, baseUrl.getPath(), null, null, -1, false)).build();
    }

    private String generateRememberMeCookie(WirelessClient newClient) throws NoSuchAlgorithmException {
        return newClient.getPassword();
    }

    @POST
    @Path("/feedback/location/{deviceId}")
    public Response getClientFeedback(@PathParam("deviceId") String deviceId, @FormParam("rating") String rating, @FormParam("comment") String comment) {
        WirelessClient client = mobileServerCacheService.getWirelessClientByUniqueID(deviceId);
        if (client == null) {
            // Forbidden better than File Not Found - it doesn't leak
            // information
            LOGGER.trace("Unable to determine client MAC address from device ID '{}'", deviceId);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        LOGGER.info("Rating and comment is", rating, comment);
        SendEmail sendMail = new SendEmail(EmailProperties.getInstance().getFeedbackToAddress(), EmailProperties.getInstance().getFeedbackFromAddress());
        StringBuffer mailBody = new StringBuffer("Rating is ").append(rating).append(EMAIL_LINE_FEED_CHAR);
        mailBody.append("Current Location:").append(EMAIL_LINE_FEED_CHAR);
        mailBody.append("MAC Address: ").append(client.getMacAddress()).append(EMAIL_LINE_FEED_CHAR);
        mailBody.append("Venue UDID: ").append(client.getVenueUdId()).append(EMAIL_LINE_FEED_CHAR);
        mailBody.append("Floor ID: ").append(client.getFloorId()).append(EMAIL_LINE_FEED_CHAR);
        mailBody.append("Zone ID: ").append(client.getZoneId()).append(EMAIL_LINE_FEED_CHAR);
        mailBody.append("Map Location : (").append(client.getX()).append(",").append(client.getY()).append(")").append(EMAIL_LINE_FEED_CHAR);
        mailBody.append("GPS Location : Latitude - ").append(client.getLatitude()).append(" Longitude - ").append(client.getLongitude()).append(EMAIL_LINE_FEED_CHAR);
        mailBody.append("Last Update Time : ").append(DateFormat.getInstance().format(new Date(client.getLastLocationUpdateTime()))).append(EMAIL_LINE_FEED_CHAR);
        mailBody.append("Last Calculation Time : ").append(DateFormat.getInstance().format(new Date(client.getLastLocationCalculationTime()))).append(EMAIL_LINE_FEED_CHAR);
        mailBody.append("Comment is : ").append(comment);
        
        sendMail.sendMail(EmailProperties.getInstance().getFeedbackSubject(), mailBody.toString());
        return Response.ok().build();
    }

    // TODO: think of a better verb+URI with which to opt-out...
    // maybe @delete on /location/deviceId?
    @DELETE
    @Path("/optOut/{deviceID}")
    public Response optOutClient(@PathParam("deviceID") String deviceID) {
        // TODO: authentication check

        // get client
        // WirelessClient macClientLocation =
        // mobileServerCacheService.getWirelessClient(deviceID);
        // delete client
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    }
}
