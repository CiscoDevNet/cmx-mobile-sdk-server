package com.cisco.cmxmobile.model;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.cisco.cmxmobile.cacheService.Utils.DateTimeUtils;
import com.cisco.cmxmobile.cacheService.Utils.EncryptionUtil;
import com.cisco.cmxmobile.cacheService.client.annotations.Key;
import com.cisco.cmxmobile.cacheService.client.annotations.MaskLogEntry;

public class WirelessClient implements UserDetails {
    /**
     * Auto-generated. Do no change.
     */
    private static final long serialVersionUID = -8830121039316507776L;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WirelessClient.class);

    private static final int MAXIMUM_ZONES = 5;

    @Key(index = 1)
    private String macAddress;

    private String venueUdId;

    private String floorId;

    private String mPushNotificationRegistrationId;

    @MaskLogEntry
    private String authenticationToken;

    private float xCoordinate;

    private float yCoordinate;
    
    private double latitude;

    private double longitude;

    private UUID uniqueID;

    HashMap<String, String> zonesMap;

    DeviceType mDeviceType;
    
    private long lastLocationUpdateTime;
    
    private long lastLocationCalculationTime;
    
    //New Id to store zone for push notification
    private String zoneId; 
    
    private String zoneName;
    
    private String zonePoints;
    
    /**
     * MSEs do not support a z coordinate (yet)
     */
    private float zCoordinate;
    
    private boolean hasVenueChanged;
    
    private String userId;

    // TODO: Query Attributes : Can be an enum !!!
    public static final String MAC_ADDRESS = "macAddress";

    public static final String PUSH_NOTIFICATION_REGISTRATION_ID = "pushNotificationRegistrationID";

    public WirelessClient() {
        // TODO: I don't know if this will work with how we retrieve
        // WirelessClients from the cache...
        this.uniqueID = UUID.randomUUID();
        SecureRandom secureRandom = EncryptionUtil.getInstance().getSecureRandom();
        byte[] bytes = new byte[40];
        secureRandom.nextBytes(bytes);
        authenticationToken = Base64.encodeBase64URLSafeString(bytes);
        hasVenueChanged = false;
    }

    public WirelessClient(String macAddress, String venueUdId, String floorId) {
        this.macAddress = macAddress;
        this.venueUdId = venueUdId;
        this.floorId = floorId;
        this.uniqueID = UUID.randomUUID();
        SecureRandom secureRandom = EncryptionUtil.getInstance().getSecureRandom();
        byte[] bytes = new byte[40];
        secureRandom.nextBytes(bytes);
        authenticationToken = Base64.encodeBase64URLSafeString(bytes);
    }

    public void generateNewAuthenticationToken() {
        SecureRandom secureRandom = EncryptionUtil.getInstance().getSecureRandom();
        byte[] bytes = new byte[40];
        secureRandom.nextBytes(bytes);
        authenticationToken = Base64.encodeBase64URLSafeString(bytes);
    }

    // TODO: when backing up to disk, do not back up the location
    // data (x/y/z/venue)!
    public float getX() {
        return xCoordinate;
    }

    public void setX(float x) {
        xCoordinate = x;
    }

    public float getY() {
        return yCoordinate;
    }

    public void setY(float y) {
        yCoordinate = y;
    }

    public float getZ() {
        return zCoordinate;
    }

    public void setZ(float z) {
        zCoordinate = z;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double lat) {
        latitude = lat;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double lon) {
        longitude = lon;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getVenueUdId() {
        return venueUdId;
    }

    public void setVenueUdId(String venueUdId) {
        //Flag to indicate if Venue has changed since last Event
        if (this.venueUdId == null) {
            hasVenueChanged = true;
        } else {
            hasVenueChanged = !this.venueUdId.equalsIgnoreCase(venueUdId);
        }
        this.venueUdId = venueUdId;
    }

    public String getFloorId() {
        return floorId;
    }

    public void setFloorId(String floorId) {
        this.floorId = floorId;
    }

    public void setPushNotificationRegistrationId(String registrationId) {
        mPushNotificationRegistrationId = registrationId;
    }

    public String getPushNotificationRegistrationId() {
        return mPushNotificationRegistrationId;
    }

    public void setUniqueID(UUID id) {
        uniqueID = id;
    }

    public UUID getUniqueID() {
        return uniqueID;
    }

    public DeviceType getDeviceType() {
        return mDeviceType;
    }

    public void setDeviceType(DeviceType mDeviceType) {
        this.mDeviceType = mDeviceType;
    }

    public HashMap<String, String> getZoneList() {
        return (zonesMap == null) ? null : (HashMap<String, String>) zonesMap.clone();

    }

    // This method store zone for which push notification sent to client
    /**
     * This method store last 5 zones for which push notification sent. If a new
     * zone need to save, it will remove oldest zone and add new zone
     * 
     * @param zoneId
     *            - This is mseId + venueId + floorId + zoneId
     * @param time
     *            - Time when push notification sent
     */
    public void setNotificationZone(String zoneId, long time) {
        if (zonesMap == null) {
            zonesMap = new HashMap<String, String>();
        }
        if (zonesMap.size() == MAXIMUM_ZONES) {
            Iterator iterator = zonesMap.keySet().iterator();
            String key = "";
            long lastTime = 0;
            while (iterator.hasNext()) {
                String tempKey = (String) iterator.next();
                long storeTime = DateTimeUtils.getTimeDiffInMinutes(Long.valueOf(zonesMap.get(tempKey)));
                if (storeTime > lastTime) {
                    lastTime = storeTime;
                    key = tempKey;
                }
            }
            if (key.length() > 0) {
                zonesMap.remove(key);
            }
        }

        zonesMap.put(zoneId, Long.toString(time));
    }

    /**
     * Check status to send push notification. It require 1. If no push
     * notification message send for any zone 2. If Notification sent for same
     * zone, check the expire time which is 5 hour
     * 
     * @param zoneId
     * @param minutesBetweenSendingNofications
     * @return boolean
     */
    public boolean requirePushNotification(String zoneId, int minutesBetweenSendingNofications) {
        boolean require = true;
        if (mPushNotificationRegistrationId == null) {
            return false;
        }
        if (zonesMap == null) {
            setNotificationZone(zoneId, System.currentTimeMillis());
            return true;
        }

        if (zonesMap.containsKey(zoneId)) {
            long storeTime = DateTimeUtils.getTimeDiffInMinutes(Long.valueOf(zonesMap.get(zoneId)));
            LOGGER.trace("{} minutes since last notification sent for zone {}", storeTime, zoneId);
            if (storeTime > minutesBetweenSendingNofications) {
                LOGGER.trace("Last notification for zone {} is greater then configured delay {}", zoneId, minutesBetweenSendingNofications);
                zonesMap.remove(zoneId);
                require = true;
            }
            else {
                LOGGER.trace("Last notification for zone {} is still less then configured delay {}", zoneId, minutesBetweenSendingNofications);
                require = false;
            }
        }
        else {
            require = true;
        }
        
        if (require) {
            setNotificationZone(zoneId, System.currentTimeMillis());
        }
        
        return require;
    }
    
    public boolean requirePresenceNotificaton() 
    {
        return hasVenueChanged;
    }
    
    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public long getLastLocationUpdateTime() {
        return lastLocationUpdateTime;
    }

    public void setLastLocationUpdateTime(long lastLocationUpdateTime) {
        this.lastLocationUpdateTime = lastLocationUpdateTime;
    }

    public long getLastLocationCalculationTime() {
        return lastLocationCalculationTime;
    }

    public void setLastLocationCalculationTime(long lastLocationCalculationTime) {
        this.lastLocationCalculationTime = lastLocationCalculationTime;
    }

    public void setLastLocationCalculationTime(String lastLocationCalculationTime) {
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        SimpleDateFormat dataFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        dataFormatter.setTimeZone(gmt);
        Date convertedDate = null;
        try {
            convertedDate = dataFormatter.parse(lastLocationCalculationTime);
            this.lastLocationCalculationTime = convertedDate.getTime();
        } catch (ParseException ex) {
            this.lastLocationCalculationTime = 0;
        }
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getZonePoints() {
        return zonePoints;
    }

    public void setZonePoints(String zonePoints) {
        this.zonePoints = zonePoints;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int hashCode() {
        return super.hashCode();
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof WirelessClient) {
            WirelessClient other = (WirelessClient) o;
            return (this.macAddress.equals(other.getMacAddress()) && this.mPushNotificationRegistrationId.equals(other.getPushNotificationRegistrationId()) && this.uniqueID.equals(other.getUniqueID()));
            // TODO: need to think about whether or not the password/auth token
            // field is important for equals
        }
        return false;
    }

    // =============================================
    // Spring Security stuff / interface UserDetails
    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        HashSet<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_MOBILE_APP_USER"));

        return authorities;
    }

    @Override
    public String getPassword() {
        // need to auto-generate the cookie/authentication token when this
        // object
        // gets created
        return authenticationToken;
    }

    @Override
    public String getUsername() {
        return getUniqueID().toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        // Accounts only expire when the user opts-out
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        // TODO: need logic (somewhere) to determine if the user made too many
        // login attempts
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Accounts (and their passwords) only expire when the user opts-out
        return false;
    }

    @Override
    public boolean isEnabled() {
        // If there's a WirelessClient in the system, it's enabled
        // TODO: need to determine if the user made too many login attempts
        return true;
    }
    // End Spring Security stuff / interface UserDetails
    // =================================================
}
