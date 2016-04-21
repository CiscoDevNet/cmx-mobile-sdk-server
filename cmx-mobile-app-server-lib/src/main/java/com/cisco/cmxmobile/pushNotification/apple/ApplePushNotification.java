package com.cisco.cmxmobile.pushNotification.apple;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javapns.Push;
import javapns.communication.ProxyManager;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import javapns.notification.ResponsePacket;

import com.cisco.cmxmobile.cacheService.Utils.EncryptionUtil;
import com.cisco.cmxmobile.model.Ssid;
import com.cisco.cmxmobile.model.Venue;
import com.cisco.cmxmobile.model.WirelessClient;
import com.cisco.cmxmobile.utils.CmxProperties;

public class ApplePushNotification implements Runnable {

    // Never change, otherwise need to change this key in BBX project also
    public static final String PASSWORD_DECREPTION_KEY = "SuperSecretBadlyNamedKey";

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplePushNotification.class);
    
    WirelessClient mClient;

    File mFileName;

    String mPassword;

    String mMessage;

    boolean mProducationBuild;
    
    Venue mVenue;

    static {
        if (CmxProperties.getInstance().getProxyHost() != null && CmxProperties.getInstance().getProxyPort() != null) {
            ProxyManager.setProxy(CmxProperties.getInstance().getProxyHost(), CmxProperties.getInstance().getProxyPort());
        }
    };

    public ApplePushNotification(WirelessClient client, String message, Venue venue, File certificateFilePath, String password, boolean productionBuild) {
        mClient = client;
        mFileName = certificateFilePath;
        mPassword = password;
        mMessage = message;
        mProducationBuild = productionBuild;
        mVenue = venue;
        try {
            BasicConfigurator.configure();
        } catch (Exception e) {
            LOGGER.error("Error attempt to initialize loj4j for ApplePushNotification {}", e);
        }
    }

    @Override
    public void run() {
        try {
            String password = EncryptionUtil.decrypt(mPassword, PASSWORD_DECREPTION_KEY);
            PushNotificationPayload payload = PushNotificationPayload.complex();
            payload.addAlert(mMessage);
            if (mVenue != null && mVenue.getSsidList().size() > 0) {
                ArrayList<Map<String, String>> networkList = new ArrayList<Map<String, String>>();
                for (Ssid ssid : mVenue.getSsidList()) {
                    Map<String, String> ssidDetailsList = new HashMap<String, String>();
                    ssidDetailsList.put("ssid", ssid.getSsid());
                    ssidDetailsList.put("password", ssid.getPassword());
                    networkList.add(ssidDetailsList);
                }
                payload.addCustomDictionary("preferredNetwork", networkList);
            }
            
            LOGGER.trace("Starting to send iOS push notification for device: {} with token '{}'", mClient.getMacAddress(), mClient.getPushNotificationRegistrationId());
            List<PushedNotification> notifications = Push.payload(payload, mFileName, password, mProducationBuild, mClient.getPushNotificationRegistrationId());
            for (PushedNotification notification : notifications) {
                if (notification.isSuccessful()) {
                    /* Apple accepted the notification and should deliver it */  
                    LOGGER.trace("Push notification sent successfully for device: {} with token '{}'",  mClient.getMacAddress(), notification.getDevice().getToken());
                }
                else {
                    //String invalidToken = notification.getDevice().getToken();

                    /* Find out more about what the problem was */
                    LOGGER.error("Error sending notification for device: {} with message: {}", mClient.getMacAddress(), notification.getException().getMessage());
                    LOGGER.error("Error sending notification for device", notification.getException());

                    /*
                     * If the problem was an error-response packet returned by
                     * Apple, get it
                     */
                    ResponsePacket theErrorResponse = notification.getResponse();
                    if (theErrorResponse != null) {
                        LOGGER.error("Error sending iOS push notification for device: {}: {}", mClient.getMacAddress(), theErrorResponse.getMessage());
                    }
                }
            }
            LOGGER.trace("Completed handling iOS push notification for device: {}", mClient.getMacAddress());
        }
        catch (CommunicationException e) {
            LOGGER.error("Communication Exception during push notificaiton for client : Reason for fail : {}", e.getLocalizedMessage());
        }
        catch (KeystoreException e) {
            LOGGER.error("Keystore Exception during push notificaiton for client : Reason for fail : {}", e.getLocalizedMessage());
        }
        catch (Exception e) {
            LOGGER.error("Exception during push notificaiton for client : Reason for fail : {}",  e.getLocalizedMessage());
        }
    }
}
