package com.cisco.cmxmobile.model;

import java.util.List;

/*
	"id":3201,
         "endDate":"05\/31\/2014",
         "endTime":"18:00",
         "frequencyUnit":"ONCE",
         "frequencyValue":0,
         "userFrequencyValue":0,
         "userFrequencyUnit":"DAYS",
         "userFrequencyUnitValue":0,
         "userTimeSpendValue":0,
         "guestType":"ANY",
         "startDate":"11\/01\/2013",
         "startTime":"08:00",
         "days":[
            "MONDAY",
            "WEDNESDAY",
            "FRIDAY"
         ]
 */
public class CampaignRule 
{
    String id;
    String endDate;
    String endTime;
    String frequencyUnit;
    String frequencyValue;
    String userFrequencyValue;
    String userFrequencyUnit;
    String userFrequencyUnitValue;
    String userTimeSpendValue;
    String guestType;
    String startDate;
    String startTime;
    List<String> days;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getEndDate() {
        return endDate;
    }
    
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    
    public String getEndTime() {
        return endTime;
    }
    
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    
    public String getFrequencyUnit() {
        return frequencyUnit;
    }
    
    public void setFrequencyUnit(String frequencyUnit) {
        this.frequencyUnit = frequencyUnit;
    }
    
    public String getFrequencyValue() {
        return frequencyValue;
    }
    
    public void setFrequencyValue(String frequencyValue) {
        this.frequencyValue = frequencyValue;
    }
    
    public String getUserFrequencyValue() {
        return userFrequencyValue;
    }
    
    public void setUserFrequencyValue(String userFrequencyValue) {
        this.userFrequencyValue = userFrequencyValue;
    }
    
    public String getUserFrequencyUnit() {
        return userFrequencyUnit;
    }
    
    public void setUserFrequencyUnit(String userFrequencyUnit) {
        this.userFrequencyUnit = userFrequencyUnit;
    }
    
    public String getUserFrequencyUnitValue() {
        return userFrequencyUnitValue;
    }
    
    public void setUserFrequencyUnitValue(String userFrequencyUnitValue) {
        this.userFrequencyUnitValue = userFrequencyUnitValue;
    }
    
    public String getUserTimeSpendValue() {
        return userTimeSpendValue;
    }
    
    public void setUserTimeSpendValue(String userTimeSpendValue) {
        this.userTimeSpendValue = userTimeSpendValue;
    }
    
    public String getGuestType() {
        return guestType;
    }
    
    public void setGuestType(String guestType) {
        this.guestType = guestType;
    }
    
    public String getStartDate() {
        return startDate;
    }
    
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    
    public String getStartTime() {
        return startTime;
    }
    
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    
    public List<String> getDays() {
        return days;
    }
    
    public void setDays(List<String> days) {
        this.days = days;
    }
}