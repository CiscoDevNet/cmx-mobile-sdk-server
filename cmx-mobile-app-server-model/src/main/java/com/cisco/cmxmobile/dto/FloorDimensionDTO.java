package com.cisco.cmxmobile.dto;

public class FloorDimensionDTO {

    private float length;
    private float width;
    private float height;
    private float offsetX;
    private float offsetY;
    private String unit;
    
    public float getLength() {
        return length;
    }
    
    public void setLength(float length) {
        this.length = length;
    }
    
    public float getWidth() {
        return width;
    }
    
    public void setWidth(float width) {
        this.width = width;
    }
    
    public float getHeight() {
        return height;
    }
    
    public void setHeight(float height) {
        this.height = height;
    }
    
    public float getOffsetX() {
        return offsetX;
    }
    
    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }
    
    public float getOffsetY() {
        return offsetY;
    }
    
    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
}
