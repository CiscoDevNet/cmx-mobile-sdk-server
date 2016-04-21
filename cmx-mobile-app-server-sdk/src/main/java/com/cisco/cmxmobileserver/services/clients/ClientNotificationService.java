package com.cisco.cmxmobileserver.services.clients;

import java.io.File;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;
import com.cisco.cmxmobile.model.DeviceType;
import com.cisco.cmxmobile.model.Venue;
import com.cisco.cmxmobile.model.WirelessClient;
import com.cisco.cmxmobile.pushNotification.PushNotificationService;
import com.cisco.cmxmobile.pushNotification.android.AndroidPushNotification;
import com.cisco.cmxmobile.pushNotification.apple.ApplePushNotification;

@Component
@Path("/api/cmxmobileserver/v1/client/notification")
public class ClientNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientNotificationService.class);

    @Value("${map.location}")
    String mapLocation;
    
    @Autowired
    private PushNotificationService mNotificationService;

    @Autowired
    private MobileServerCacheService mobileServerCacheService;

    @POST
    @Path("/user/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response notificationByUserId(@PathParam("userId") String userId, @FormParam("message") String message) {
        LOGGER.trace("Request to send push notification to user '{}' with message '{}'", userId, message);

        List<WirelessClient> clients = null;
        try {
            clients = mobileServerCacheService.getWirelessClientsByUser(userId);
        } catch (Exception e) {
            LOGGER.error("Exception trying to get wireless client by user for notification: ", e);
        }
        if (clients != null) {
            for (WirelessClient userClient : clients) {
                Venue venue = mobileServerCacheService.getVenue(userClient.getVenueUdId());
                if (userClient.getPushNotificationRegistrationId() == null || userClient.getPushNotificationRegistrationId().length() <= 0) {
                    LOGGER.trace("Push notification ID not set for the device: {}", userClient.getMacAddress());
                }
                if (message == null || message.length() <= 0) {
                    LOGGER.trace("Do not have a push notification message for the device: {}", userClient.getMacAddress());
                }
                LOGGER.trace("Sending push notification for the device: {}", userClient.getMacAddress());
                if (userClient.getDeviceType().equals(DeviceType.ANDROID)) {
                    LOGGER.trace("Sending push notification for an Android device");
                    mNotificationService.runTask(new AndroidPushNotification(userClient, message, venue));
                }  else if (userClient.getDeviceType().equals(DeviceType.IOS) || userClient.getDeviceType().equals(DeviceType.IOS6)) {
                    // TODO: Need to read password for p12 file
                    // Push notification file is per MSE, so it is place
                    // at map location
                    LOGGER.trace("Sending push notification for an iOS device");
                    File file = new File(mapLocation, venue.getApplePushNotificationFile());
                    boolean isProductionBuild = venue.getAppleProductionServer();
                    mNotificationService.runTask(new ApplePushNotification(userClient, message, venue, file, venue.getApplePushNotificationKey(), isProductionBuild));
                } else {
                    LOGGER.trace("Unable to determined device type to send push notification");
                }
            }
        }
        LOGGER.trace("Completed sending push notification for user '{}' with message '{}'", userId, message);
        return Response.ok().build();
    }

    @POST
    @Path("/device/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response notificationByDeviceId(@PathParam("deviceId") String deviceId, @FormParam("message") String message) {
        LOGGER.trace("Request to send push notification to device '{}' with message '{}'", deviceId, message);

        WirelessClient client = null;
        try {
            client = mobileServerCacheService.getWirelessClientByUniqueID(deviceId);
        } catch (Exception e) {
            LOGGER.error("Exception trying to get wireless client by user for notification: ", e);
        }
        if (client != null) {
            Venue venue = mobileServerCacheService.getVenue(client.getVenueUdId());
            if (client.getPushNotificationRegistrationId() == null || client.getPushNotificationRegistrationId().length() <= 0) {
                LOGGER.trace("Push notification ID not set for the device: {}", client.getMacAddress());
            }
            if (message == null || message.length() <= 0) {
                LOGGER.trace("Do not have a push notification message for the device: {}", client.getMacAddress());
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
        LOGGER.trace("Completed sending push notification for device '{}' with message '{}'", deviceId, message);
        return Response.ok().build();
    }
}