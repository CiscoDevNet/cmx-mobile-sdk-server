/**
 * This is a runnable class which send push notification message to device
 */

package com.cisco.cmxmobile.pushNotification.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.net.InetSocketAddress;
import java.net.Proxy;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.cmxmobile.model.Ssid;
import com.cisco.cmxmobile.model.Venue;
import com.cisco.cmxmobile.model.WirelessClient;
import com.cisco.cmxmobile.utils.CmxProperties;

public class AndroidPushNotification implements Runnable {

    public static Proxy androidProxy = null;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AndroidPushNotification.class);

    WirelessClient mClient;

    Sender mSender;

    String pushMessage;
    
    Venue mVenue;

    static {
        if (CmxProperties.getInstance().getProxyHost() != null && CmxProperties.getInstance().getProxyPort() != null) {
            androidProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(CmxProperties.getInstance().getProxyHost(), Integer.parseInt(CmxProperties.getInstance().getProxyPort())));
        }
    };

    public AndroidPushNotification(WirelessClient client, String message, Venue venue) {
        mClient = client;
        pushMessage = message;
        mSender = new Sender(venue.getAndroidPushNotificationKey());
        mVenue = venue;
    }

    @Override
    public void run() {
        String registrationId = mClient.getPushNotificationRegistrationId();
        List<String> devices = new ArrayList<String>();
        devices.add(registrationId);
        JSONObject networkObject = new JSONObject();
        JSONArray ssidObject =  new JSONArray();
        if (mVenue == null) {
            try {
                for (Ssid ssid : mVenue.getSsidList()) {
                    JSONObject networkObjectDetails = new JSONObject();
                    networkObjectDetails.accumulate("ssid", ssid.getSsid());
                    networkObjectDetails.accumulate("password", ssid.getPassword());
                    ssidObject.put(networkObjectDetails);
                }
                networkObject.accumulate("preferredNetwork", ssidObject);
            } catch (Exception e) {
                LOGGER.error("Error will");
            }
        }
        Message message;
        if (mVenue != null && mVenue.getSsidList().size() > 0) {
            message = new Message.Builder().addData("message", pushMessage).addData("preferredNetwork", networkObject.toString()).build();
        } else {
            message = new Message.Builder().addData("message", pushMessage).build();
        }
        MulticastResult multiResult = null;
        try {
            multiResult = mSender.send(message, devices, 1);
            List<Result> results = multiResult.getResults();
            Result result = results.get(0);
            String messageId = result.getMessageId();
            if (messageId != null) {
                String canonicalRegId = result.getCanonicalRegistrationId();
                if (canonicalRegId != null) {
                    mClient.setPushNotificationRegistrationId(canonicalRegId);
                }
            }
            else {
                String errorCode = result.getErrorCodeName();
                if (errorCode.equals(Constants.ERROR_NOT_REGISTERED)) {
                    mClient.setPushNotificationRegistrationId(null);
                }
            }
        }
        catch (IOException e) {
            LOGGER.error("Error will sending android push notification: {}", e.getLocalizedMessage());
        }
    }

}
