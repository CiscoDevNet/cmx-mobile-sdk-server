package com.cisco.cmxmobile.dto;

public class FloorInfoDTO {
    
    private String floorId;
    private String mapHierarchyString;
    private String name;
    private String venueid;
    private FloorDimensionDTO dimension;
    
    public String getFloorId() {
        return floorId;
    }
    
    public void setFloorId(String floorId) {
        this.floorId = floorId;
    }
    
    public String getMapHierarchyString() {
        return mapHierarchyString;
    }
    
    public void setMapHierarchyString(String mapHierarchyString) {
        this.mapHierarchyString = mapHierarchyString;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVenueid() {
        return venueid;
    }
    
    public void setVenueid(String venueid) {
        this.venueid = venueid;
    }
    
    public FloorDimensionDTO getDimension() {
        return dimension;
    }
    
    public void setDimension(FloorDimensionDTO dimension) {
        this.dimension = dimension;
    }
}
