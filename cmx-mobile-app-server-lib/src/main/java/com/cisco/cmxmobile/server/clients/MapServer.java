package com.cisco.cmxmobile.server.clients;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.dto.FloorInfoDTO;
import com.cisco.cmxmobile.utils.MDCKeys;

@Component
public class MapServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapServer.class);
    
    @Autowired
    private MapServiceHelper mapServiceHelper;

    @Value("${map.location}")
    String mapLocation;

    public List<FloorInfoDTO> getVenueMapInfo(String venueUdId) {
        MDC.put(MDCKeys.VENUE_ID, venueUdId);
        LOGGER.debug("Request to get all map venue information for '{}'", venueUdId);
        try {
            return mapServiceHelper.getFloorsByVenue(venueUdId);
        }
        catch (Exception e) {
            LOGGER.error("Failed to get map venue information for venue '{}'", venueUdId, e);
        }
        LOGGER.debug("Completed request to get all map venue information for '{}'", venueUdId);
        MDC.remove(MDCKeys.VENUE_ID);
        return new ArrayList<FloorInfoDTO>();
    }

    public FloorInfoDTO getMapInfo(String mseVenueUdId, String floorId) {
        MDC.put(MDCKeys.VENUE_ID, mseVenueUdId);
        LOGGER.debug("Request to get map venue information for '{}' with floor ID '{}'", mseVenueUdId, floorId);
        FloorInfoDTO floorDTO = new FloorInfoDTO();
        try {
            return mapServiceHelper.getFloorInfoDTO(mseVenueUdId, floorId);
            
        }
        catch (Exception e) {
            LOGGER.error("Failed to get map venue information for venue '{}' with floor ID '{}'", mseVenueUdId, floorId, e);
        }
        LOGGER.debug("Completed request to get map venue information for '{}' with floor ID '{}'", mseVenueUdId, floorId);
        MDC.remove(MDCKeys.VENUE_ID);
        return floorDTO;
    }

    public Response getMapImage(String mseVenueUdId, String floorId) {
        MDC.put(MDCKeys.VENUE_ID, mseVenueUdId);
        LOGGER.debug("Request to get map venue image for '{}' with floor ID '{}'", mseVenueUdId, floorId);
        try {
            File imageFile = mapServiceHelper.getMapImageFilePath(mapLocation, mseVenueUdId, floorId);
            
            if (!imageFile.exists()) {
                LOGGER.error("Image File {} does not exist", imageFile.getAbsoluteFile());
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            
            LOGGER.debug("Map venu image file name {} is for '{}' with floor ID '{}'", imageFile, mseVenueUdId, floorId);
            FileInputStream infile = new FileInputStream(imageFile);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            for (int readNum; (readNum = infile.read(buf)) != -1;) {
                bos.write(buf, 0, readNum);
            }
            byte[] bytes = bos.toByteArray();
            ResponseBuilder response = Response.ok(bytes);
            response.type("image/gif");
            
            //TODO: Check This
            String fileName = imageFile.getName();
            
            if (fileName != null && fileName.length() > 0) {
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                if (extension != null && extension.equalsIgnoreCase("png")) {
                    response.type("image/png");
                }
                else if (extension != null && extension.equalsIgnoreCase("jpg")) {
                    response.type("image/jpeg");
                }
            }
            LOGGER.debug("Completed request to get map venue image for '{}' with floor ID '{}'", mseVenueUdId, floorId);
            MDC.remove(MDCKeys.VENUE_ID);
            return response.build();
        }
        catch (Exception ex) {
            MDC.remove(MDCKeys.VENUE_ID);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
