package com.cisco.cmxmobile.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.cmxmobile.model.WirelessClient;

public class WirelessClientDTO {

    private static final Logger LOGGER = LoggerFactory.getLogger(WirelessClientDTO.class);
    
    private String deviceId;
    private String macAddress;
    private long lastLocationCalculationTime;
    private long lastLocationUpdateTime;
    private String venueId;
    private String floorId;
    private String zoneId;
    private String zoneName;
    private String zonePoints;
    private MapLocationDTO mapCoordinate;
    private GeoLocationDTO geoCoordinate;
    
    public WirelessClientDTO(WirelessClient wirelessClient) {
        this.macAddress = wirelessClient.getMacAddress();
        this.lastLocationCalculationTime = wirelessClient.getLastLocationCalculationTime();
        this.lastLocationUpdateTime = wirelessClient.getLastLocationUpdateTime();
        this.venueId = wirelessClient.getVenueUdId();
        this.floorId = wirelessClient.getFloorId();
        try {
            if (!wirelessClient.getZoneId().isEmpty()) {
                this.zoneId = wirelessClient.getZoneId();
                this.zoneName = wirelessClient.getZoneName();
                this.zonePoints = wirelessClient.getZonePoints();
            }
        } catch (Exception ex) {
            LOGGER.error("Error during zone object creation for device ID '{}'", macAddress, ex.getLocalizedMessage());
        }
        MapLocationDTO mapLocation = new MapLocationDTO();
        mapLocation.setX(wirelessClient.getX());
        mapLocation.setY(wirelessClient.getY());
        this.mapCoordinate = mapLocation;

        GeoLocationDTO geoLocation = new GeoLocationDTO();
        geoLocation.setLatitude(wirelessClient.getLatitude());
        geoLocation.setLongitude(wirelessClient.getLongitude());
        this.geoCoordinate = geoLocation;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public GeoLocationDTO getGeoCoordinate() {
        return geoCoordinate;
    }

    public void setGeoCoordinate(GeoLocationDTO geoCoordinate) {
        this.geoCoordinate = geoCoordinate;
    }

    
    public String getDeviceID() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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

    public long getLastLocationCalculationTime() {
        return lastLocationCalculationTime;
    }

    public void setLastLocationCalculationTime(long lastLocationCalculationTime) {
        this.lastLocationCalculationTime = lastLocationCalculationTime;
    }

    public long getLastLocationUpdateTime() {
        return lastLocationUpdateTime;
    }

    public void setLastLocationUpdateTime(long lastLocationUpdateTime) {
        this.lastLocationUpdateTime = lastLocationUpdateTime;
    }

    public String getVenueId() {
        return venueId;
    }
    
    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }
    
    public String getFloorId() {
        return floorId;
    }
    
    public void setFloorId(String floorId) {
        this.floorId = floorId;
    }
    
    public MapLocationDTO getMapCoordinate() {
        return mapCoordinate;
    }
    
    public void setMapCoordinate(MapLocationDTO mapCoordinate) {
        this.mapCoordinate = mapCoordinate;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }
}
