package com.cisco.cmxmobile.setup;

import java.util.List;

public class CasSetup {
    private String locationName;

    private double latitude;

    private double longitude;

    private String mseUdi;

    private long appId;

    private List<String> emailAddresses;

    private String additionalInfo;

    private int trackedElementsLimit;

    private String serverName;

    private String streetAddress;

    private List<CasFloor> floors;

    private String phoneNumber;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getMseUdi() {
        return mseUdi;
    }

    public void setMseUdi(String mseUdi) {
        this.mseUdi = mseUdi;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public List<String> getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(List<String> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public int getTrackedElementsLimit() {
        return trackedElementsLimit;
    }

    public void setTrackedElementsLimit(int trackedElementsLimit) {
        this.trackedElementsLimit = trackedElementsLimit;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public List<CasFloor> getFloors() {
        return floors;
    }

    public void setFloors(List<CasFloor> floors) {
        this.floors = floors;
    }

    @Override
    public String toString() {
        return "CasSetup [locationName=" + locationName + ", latitude=" + latitude + ", longitude=" + longitude + ", mseUdi=" + mseUdi + ", appId=" + appId + ", emailAddresses=" + emailAddresses + ", additionalInfo=" + additionalInfo + ", trackedElementsLimit=" + trackedElementsLimit
                + ", serverName=" + serverName + ", streetAddress=" + streetAddress + ", floors=" + floors + "]";
    }

}
