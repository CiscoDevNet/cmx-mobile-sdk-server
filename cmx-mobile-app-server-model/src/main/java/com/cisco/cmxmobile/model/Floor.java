package com.cisco.cmxmobile.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.aes.rest.datamodel.location.MapCoordinate;
import com.cisco.cmxmobile.cacheService.client.annotations.Key;
import com.cisco.cmxmobile.cacheService.client.annotations.PersistableStrategy;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Floor {
    // TOD0: Parent Keys need to be refactored out
    // Parent Keys
    @Key(index = 1)
    private String venueUdId;

    // Keys
    @Key(index = 2)
    private String mseFloorId;

    private String mseUdId;

    private String mseVenueId;

    private long id;

    private String name;

    private String description;

    private String tags;

    private String filename;

    private long campusid;

    private long venueid;

    private float length;

    private float width;

    private String categories;

    private long createdDate;

    private long updatedDate;

    private String mseIP;
    
    private String imageWidth;
    
    private String imageHeight;

    private List<PointOfInterest> poiList;

    private List<Zone> zoneList;
    
    @PersistableStrategy(AsJson = true)
    private List<BleBeacon> bleBeaconList;

    private FloorPathInfo floorPathInfo;

    public static final String VENUE_UDID = "venueUdId";

    public static final String MSE_FLOORID = "mseFloorId";

    public String getVenueUdId() {
        return venueUdId;
    }

    public void setVenueUdId(String venueUdId) {
        this.venueUdId = venueUdId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getCampusid() {
        return campusid;
    }

    public void setCampusid(long campusid) {
        this.campusid = campusid;
    }

    public long getVenueid() {
        return venueid;
    }

    public void setVenueid(long venueid) {
        this.venueid = venueid;
    }

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

    public String getMseFloorId() {
        return mseFloorId;
    }

    public void setMseFloorId(String mseFloorId) {
        this.mseFloorId = mseFloorId;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public long getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(long updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getMseIP() {
        return mseIP;
    }

    public void setMseIP(String mseIP) {
        this.mseIP = mseIP;
    }

    public List<PointOfInterest> getPoiList() {
        return poiList;
    }

    public void setPoiList(List<PointOfInterest> poiList) {
        this.poiList = poiList;
    }

    public List<Zone> getZoneList() {
        return zoneList;
    }

    public void setZoneList(List<Zone> zoneList) {
        this.zoneList = zoneList;
    }

    public String getMseUdId() {
        return mseUdId;
    }

    public void setMseUdId(String mseUdId) {
        this.mseUdId = mseUdId;
    }

    public String getMseVenueId() {
        return mseVenueId;
    }

    public void setMseVenueId(String mseVenueId) {
        this.mseVenueId = mseVenueId;
    }

    public FloorPathInfo getFloorPathInfo() {
        return floorPathInfo;
    }

    public void setFloorPathInfo(FloorPathInfo floorPathInfo) {
        this.floorPathInfo = floorPathInfo;
    }
    
    public String getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(String imageWidth) {
        this.imageWidth = imageWidth;
    }

    public String getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(String imageHeight) {
        this.imageHeight = imageHeight;
    }

    public List<BleBeacon> getBleBeaconList() {
        return bleBeaconList;
    }

    public void setBleBeaconList(List<BleBeacon> bleBeaconList) {
        this.bleBeaconList = bleBeaconList;
    }
    
    public void updateBLEBeaconZone() throws JsonParseException, JsonMappingException, IOException
    {
        //Guard Clause
        if (this.bleBeaconList ==  null) {
            return;
        }

        GeometryFactory gm = new GeometryFactory();
        MapCoordinate mapCoordinate = new MapCoordinate();
        
        for (BleBeacon bleBeacon : this.bleBeaconList) {
            mapCoordinate.setX(bleBeacon.getxCord());
            mapCoordinate.setY(bleBeacon.getyCord());
            
            Zone zone = getZoneByLocation(gm, mapCoordinate);
            
            if (zone != null) {
                bleBeacon.setZoneId(zone.getId());
            }
        }
    }

    public Zone getZoneByLocation(GeometryFactory gf, MapCoordinate location) throws JsonParseException, JsonMappingException, IOException {
        if (this.zoneList == null || this.zoneList.size() == 0) {
            return null;
        }

        Zone locationZone = null;
        if (location != null) {
            Coordinate coord = new Coordinate(location.getX(), location.getY());
            Point mylocation = gf.createPoint(coord);
            for (Zone zone : zoneList) {
                List<Coordinate> coordList = new ArrayList<Coordinate>();
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Double>> pointList = mapper.readValue(zone.getPoints(), new TypeReference<List<Map<String, Double>>>() {
                });
                if (!pointList.isEmpty()) {
                    for (Map<String, Double> point : pointList) {
                        coordList.add(new Coordinate(point.get("x"), point.get("y")));
                    }
                    coordList.add(coordList.get(0));
                }
                if (!coordList.isEmpty()) {
                    Coordinate[] tempCoordArr = coordList.toArray(new Coordinate[pointList.size()]);
                    Polygon currZone = gf.createPolygon(new LinearRing(new CoordinateArraySequence(tempCoordArr), gf), null);
                    if (mylocation.within(currZone)) {
                        locationZone = zone;
                        break;
                    }
                }
            }
        }

        return locationZone;
    }

 
}