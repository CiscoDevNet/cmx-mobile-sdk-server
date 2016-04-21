package com.cisco.cmxmobile.dto;

public class BleBeaconDTO 
{
    private String uuid;
    private int major;
    private int minor;
    private int mfgId;
    private int calRssi;
    private long floorId;
    private float xCord;
    private float yCord;
    private float zCord;
    private String bleBeaconName;
    private int bleBeaconType;
    private String message;
    
    private String zoneId;
    
    //That is what Apple Calls it
    private String regionIdentifier;
    
    public String getUuid() {
        return uuid;
    }
    
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public int getMajor() {
        return major;
    }
    
    public void setMajor(int major) {
        this.major = major;
    }
    
    public int getMinor() {
        return minor;
    }
    
    public void setMinor(int minor) {
        this.minor = minor;
    }
    
    public int getMfgId() {
        return mfgId;
    }
    
    public void setMfgId(int mfgId) {
        this.mfgId = mfgId;
    }
    
    public int getCalRssi() {
        return calRssi;
    }
    
    public void setCalRssi(int calRssi) {
        this.calRssi = calRssi;
    }
    
    public long getFloorId() {
        return floorId;
    }
    
    public void setFloorId(long floorId) {
        this.floorId = floorId;
    }
    
    public float getxCord() {
        return xCord;
    }
    
    public void setxCord(float xCord) {
        this.xCord = xCord;
    }
    
    public float getyCord() {
        return yCord;
    }
    
    public void setyCord(float yCord) {
        this.yCord = yCord;
    }
    
    public float getzCord() {
        return zCord;
    }
    
    public void setzCord(float zCord) {
        this.zCord = zCord;
    }
    
    public String getBleBeaconName() {
        return bleBeaconName;
    }
    
    public void setBleBeaconName(String bleBeaconName) {
        this.bleBeaconName = bleBeaconName;
    }
    
    public int getBleBeaconType() {
        return bleBeaconType;
    }
    
    public void setBleBeaconType(int bleBeaconType) {
        this.bleBeaconType = bleBeaconType;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public String getRegionIdentifier() {
        return regionIdentifier;
    }

    public void setRegionIdentifier(String regionIdentifier) {
        this.regionIdentifier = regionIdentifier;
    }
}
