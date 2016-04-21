package com.cisco.cmxmobile.server.clients;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.cacheService.Utils.EncryptionUtil;
import com.cisco.cmxmobile.model.PointOfInterest;
import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;
import com.cisco.cmxmobile.utils.MDCKeys;

@Component
public class POI {

    @Autowired
    public MobileServerCacheService mobileServerCacheService;

    @Value("${map.location}")
    String mapLocation;

    private static final Logger LOGGER = LoggerFactory.getLogger(POI.class);

    public JSONArray getPOIByVenue(String venueUdId, String search) {
        MDC.put(MDCKeys.VENUE_ID, venueUdId);
        LOGGER.debug("Request to get all POI information for '{}'", venueUdId);
        // comment
        List<PointOfInterest> poiList = null;
        if (search == null || (search != null && search.isEmpty())) {
            poiList = mobileServerCacheService.getPointOfInterestByVenue(venueUdId);
        }
        else {
            poiList = mobileServerCacheService.getPointOfInterestByVenue(venueUdId, search);
        }

        JSONArray poiArray = new JSONArray();
        if (poiList != null && !poiList.isEmpty()) {
            try {
                for (int i = 0; i < poiList.size(); i++) {
                    PointOfInterest poi = (PointOfInterest) poiList.get(i);
                    JSONObject poiObject = new JSONObject();
                    poiObject.accumulate("floorid", poi.getMseFloorId());
                    poiObject.accumulate("id", poi.getId());
                    poiObject.accumulate("name", poi.getName());
                    poiObject.accumulate("venueid", poi.getVenueUdId());
                    poiObject.accumulate("imageType", "");

                    JSONArray pointArray = new JSONArray();
                    JSONObject point = new JSONObject();
                    point.accumulate("x", poi.getX());
                    point.accumulate("y", poi.getY());
                    pointArray.put(point);
                    poiObject.accumulate("points", pointArray);

                    poiArray.put(poiObject);
                    LOGGER.debug("Completed initializing POI information '{}' for venue '{}'", poi.getId(), venueUdId);
                }
            }
            catch (Exception e) {
                LOGGER.error("Error creating JSON object for POI", e);
            }
        } else {
            LOGGER.debug("There is no POI information for venue '{}'", venueUdId);            
        }
        LOGGER.debug("Completed request to get all Venue information for '{}'", venueUdId);
        MDC.remove(MDCKeys.VENUE_ID);
        return poiArray;
    }

    public JSONArray getPOIByVenueAndFloor(String venueUdId, String floorId, String search) {
        MDC.put(MDCKeys.VENUE_ID, venueUdId);
        LOGGER.debug("Request to get POI information for '{}' with floor ID '{}'", venueUdId, floorId);
        List<PointOfInterest> poiList = null;
        if (search == null || (search != null && search.isEmpty())) {
            poiList = mobileServerCacheService.getPointOfInterestListByFloor(venueUdId, floorId);
        }
        else {
            poiList = mobileServerCacheService.getPointOfInterestListByFloor(venueUdId, floorId, search);
        }

        JSONArray poiArray = new JSONArray();
        if (poiList != null && !poiList.isEmpty()) {
            try {
                for (int i = 0; i < poiList.size(); i++) {
                    PointOfInterest poi = (PointOfInterest) poiList.get(i);
                    JSONObject poiObject = new JSONObject();
                    poiObject.accumulate("floorid", poi.getMseFloorId());
                    poiObject.accumulate("id", poi.getId());
                    poiObject.accumulate("name", poi.getName());
                    poiObject.accumulate("venueid", poi.getVenueUdId());
                    poiObject.accumulate("imageType", "");

                    JSONArray pointArray = new JSONArray();
                    JSONObject point = new JSONObject();
                    point.accumulate("x", poi.getX());
                    point.accumulate("y", poi.getY());
                    pointArray.put(point);
                    poiObject.accumulate("points", pointArray);

                    poiArray.put(poiObject);
                    LOGGER.debug("Completed initializing POI information '{}' for venue '{}'", poi.getId(), venueUdId);
                }
            }
            catch (Exception e) {
                LOGGER.error("Error creating JSON object for POI", e);
            }
        } else {
            LOGGER.debug("There is no POI information for venue '{}' with floor ID '{}'", venueUdId, floorId);            
        }
        MDC.remove(MDCKeys.VENUE_ID);
        return poiArray;
    }

    public Response getPoiImage(String venueUdId, String floorId, String poiId) {
        return getPoiImageInternal(venueUdId, floorId, poiId);
    }
    
    public Response getPoiImageByPoiId(String venueUdId, String poiId) {
        LOGGER.debug("Request to get Image for POI for '{}' and POI ID", venueUdId, poiId);

        List<PointOfInterest> poiList = mobileServerCacheService.getPointOfInterestByVenue(venueUdId);
        
        if (poiList != null && !poiList.isEmpty()) {
            for (PointOfInterest poi : poiList) {
                if (poi.getId().equals(poiId)) {
                    return getPoiImageInternal(venueUdId, poi.getMseFloorId(), poi.getId());
                }
            }
        } 
        LOGGER.info("No POI image with Venue ID '{}' and POI ID '{}'", venueUdId, poiId);
        return Response.status(Status.NOT_FOUND).build();
    
    }

    private Response getPoiImageInternal(String mseVenueUdId, String floorId, String poiId) {
        MDC.put(MDCKeys.VENUE_ID, mseVenueUdId);
        LOGGER.debug("Request to get POI image for '{}' with floor ID '{}' and POI ID '{}'", mseVenueUdId, floorId, poiId);
        String venueUdidDirName = "";
        
        FileInputStream infile = null;
        try {
            venueUdidDirName = EncryptionUtil.generateMD5(mseVenueUdId);
            File poiDirectory = new File(new File(mapLocation, venueUdidDirName), EncryptionUtil.generateMD5(floorId));

            File imageFile = new File(poiDirectory, poiId + ".jpg");
            LOGGER.debug("POI image file name {} is for '{}' with floor ID '{}' and POI ID '{}'", imageFile, mseVenueUdId, floorId, poiId);
            infile = new FileInputStream(imageFile);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            for (int readNum; (readNum = infile.read(buf)) != -1;) {
                bos.write(buf, 0, readNum);
            }
            byte[] bytes = bos.toByteArray();
            ResponseBuilder response = Response.ok(bytes);
            response.type("image/jpeg");
            LOGGER.debug("Completed request to get POI image for '{}' with floor ID '{}' and POI ID '{}'", mseVenueUdId, floorId, poiId);
            MDC.remove(MDCKeys.VENUE_ID);
            return response.build();
        }
        catch (FileNotFoundException e) {
            LOGGER.error("File not found when attempting to retrieve POI image for '{}' with floor ID '{}' and POI ID '{}'", mseVenueUdId, floorId, poiId);
            MDC.remove(MDCKeys.VENUE_ID);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        catch (Exception ex) {
            LOGGER.error("Error when attempting to retrieve POI image for '{}' with floor ID '{}' and POI ID '{}'", mseVenueUdId, floorId, poiId, ex);
            MDC.remove(MDCKeys.VENUE_ID);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            if (infile != null) {
                try {
                    infile.close();
                }
                catch (IOException e) {
                    LOGGER.info("Failed to close Stream when attempting to retrieve POI image for '{}' with floor ID '{}' and POI ID '{}'", 
                        mseVenueUdId, floorId, poiId, e);
                }
            }
        }
    }
    
}
