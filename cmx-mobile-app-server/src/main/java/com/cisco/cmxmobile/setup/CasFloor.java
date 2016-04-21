package com.cisco.cmxmobile.setup;

import java.util.List;

public class CasFloor {
    private String aesUid;

    private String name;

    private int level;

    private String isOutdoor;

    private int width;

    private int length;

    private int height;

    private int offsetX;

    private int offsetY;

    private List<String> textHierarchy;

    private List<String> idHierarchy;

    private String imageName;

    private String imageType;

    private String imageExists;

    private long imageLastModified;

    public String getAesUid() {
        return aesUid;
    }

    public void setAesUid(String aesUid) {
        this.aesUid = aesUid;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getIsOutdoor() {
        return isOutdoor;
    }

    public void setIsOutdoor(String isOutdoor) {
        this.isOutdoor = isOutdoor;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public List<String> getTextHierarchy() {
        return textHierarchy;
    }

    public void setTextHierarchy(List<String> textHierarchy) {
        this.textHierarchy = textHierarchy;
    }

    public List<String> getIdHierarchy() {
        return idHierarchy;
    }

    public void setIdHierarchy(List<String> idHierarchy) {
        this.idHierarchy = idHierarchy;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public String getImageExists() {
        return imageExists;
    }

    public void setImageExists(String imageExists) {
        this.imageExists = imageExists;
    }

    public long getImageLastModified() {
        return imageLastModified;
    }

    public void setImageLastModified(long imageLastModified) {
        this.imageLastModified = imageLastModified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "CasFloor [aesUid=" + aesUid + ", name=" + name + ", level=" + level + ", isOutdoor=" + isOutdoor + ", width=" + width + ", length=" + length + ", height=" + height + ", offsetX=" + offsetX + ", offsetY=" + offsetY + ", textHierarchy=" + textHierarchy + ", idHierarchy=" + idHierarchy
                + ", imageName=" + imageName + ", imageType=" + imageType + ", imageExists=" + imageExists + ", imageLastModified=" + imageLastModified + "]";
    }

}
