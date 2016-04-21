package com.cisco.cmxmobile.server.clients;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.cacheService.Utils.EncryptionUtil;
import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;
import com.cisco.cmxmobile.cacheService.service.SetupSource;
import com.cisco.cmxmobile.dto.FloorDimensionDTO;
import com.cisco.cmxmobile.dto.FloorInfoDTO;
import com.cisco.cmxmobile.model.CasFloorInfo;
import com.cisco.cmxmobile.model.CasVenue;
import com.cisco.cmxmobile.model.Floor;
import com.cisco.cmxmobile.model.Venue;

@Component
public class MapServiceHelper 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MapServiceHelper.class);

    @Autowired
    private MobileServerCacheService mobileServerCacheService;
    
    public List<FloorInfoDTO> getFloorsByVenue(String venueUdId)
    {
        //First Check if Venue is CASVenue for APPEngage Venue
        
        //Try to Retrieve the APPEngage Venue first
        Venue venue = mobileServerCacheService.getVenue(venueUdId);
        
        if (venue == null) {
            //Get the CasVenue Instead
            CasVenue casVenue = mobileServerCacheService.getCasVenue(venueUdId);
            
            if (casVenue != null && SetupSource.getSource(casVenue.getMseUdid()) == SetupSource.LOCATION_MODULE) {
                // Retrieve the Floors and return
                return getFloorsByVenueFromLocSource(casVenue);
            } 
        } else {
            if (SetupSource.getSource(venue.getMseUdId()) == SetupSource.APP_ENGAGE) {
                //Retrieve the Floors and return
                return getFloorsByVenueFromAppEngageSource(venue);
            } 
        }
        
        LOGGER.error("Unable to find map venue information for '{}'", venueUdId);
        
        //Return an Empty List
        return new ArrayList<FloorInfoDTO>();
    }
    
    public List<FloorInfoDTO> getFloorsByVenue(Venue venue)
    {
        if (venue == null) {
            LOGGER.error("Venue is not specified. Passed value is null returning empty list");
            return new ArrayList<FloorInfoDTO>();
        }
            
        if (SetupSource.getSource(venue.getMseUdId()) == SetupSource.APP_ENGAGE) {
            //Retrieve the Floors and return
            return getFloorsByVenueFromAppEngageSource(venue);
        } 
        
        LOGGER.error("Unable to find map venue information for '{}'", venue.getVenueUdId());
        
        //Return an Empty List
        return new ArrayList<FloorInfoDTO>();
    }
    
    public List<FloorInfoDTO> getFloorsByCasVenue(CasVenue casVenue)
    {
        if (casVenue == null) {
            LOGGER.error("CasVenue is not specified. Passed value is null returning empty list");
            return new ArrayList<FloorInfoDTO>();
        }
            
        if (SetupSource.getSource(casVenue.getMseUdid()) == SetupSource.LOCATION_MODULE) {
            //Retrieve the Floors and return
            return getFloorsByVenueFromLocSource(casVenue);
        } 
        
        LOGGER.error("Unable to find map venue information for '{}'", casVenue.getVenueUdId());
        
        //Return an Empty List
        return new ArrayList<FloorInfoDTO>();
    }

    
    public FloorInfoDTO getFloorInfoDTO(String venueUdId, String floorId) {
        Floor floor = mobileServerCacheService.getFloorByVenueUdid(venueUdId, floorId);

        if (floor == null) {
            // Get the CasVenue Instead
            CasFloorInfo casFloor = mobileServerCacheService.getCasFloorByVenueUdid(venueUdId, floorId);

            if (casFloor != null && SetupSource.getSource(casFloor.getMseUdi()) == SetupSource.LOCATION_MODULE) {
                // Retrieve the Floors and return
                //TODO: Need Optimization
                CasVenue casVenue = mobileServerCacheService.getCasVenue(venueUdId);
                return getFloorInfoDTOFromLocSource(casFloor, casVenue);
            }
        }
        else {
            if (SetupSource.getSource(floor.getMseUdId()) == SetupSource.APP_ENGAGE) {
                // Retrieve the Floors and return
                
                Venue venue = mobileServerCacheService.getVenue(venueUdId);
                return getFloorInfoDTOFromAppEngageSource(floor, venue);
            }
        }
        
        return null;
    }
    
    public File getMapImageFilePath(String mapLocation, String venueUdId, String floorId) 
        throws NoSuchAlgorithmException
    {
        Floor floor = mobileServerCacheService.getFloorByVenueUdid(venueUdId, floorId);

        if (floor == null) {
            // Get the CasVenue Instead
            CasFloorInfo casFloor = mobileServerCacheService.getCasFloorByVenueUdid(venueUdId, floorId);

            if (casFloor != null && SetupSource.getSource(casFloor.getMseUdi()) == SetupSource.LOCATION_MODULE) {
                File venueDirectory = new File(mapLocation, EncryptionUtil.generateMD5(venueUdId));
                return new File(venueDirectory, casFloor.getAesUid() + "." + casFloor.getImageType().substring(casFloor.getImageType().indexOf("/") + 1));
            }
        }
        else {
            if (SetupSource.getSource(floor.getMseUdId()) == SetupSource.APP_ENGAGE) {
                String fileName = floor.getFilename();
                File venueDirectory = new File(mapLocation, EncryptionUtil.generateMD5(venueUdId));
                return new File(venueDirectory, floorId + "_" + fileName);
            }
        }
        
        return null;
    }
    
    private List<FloorInfoDTO> getFloorsByVenueFromAppEngageSource(Venue venue)
    {
        List<FloorInfoDTO> floorInfoList = new ArrayList<FloorInfoDTO>();
        List<Floor> floors = venue.getFloorList();
        if (floors != null && !floors.isEmpty()) {
            for (int i = 0; i < floors.size(); i++) {
                Floor floor = (Floor) floors.get(i);

                floorInfoList.add(this.getFloorInfoDTOFromAppEngageSource(floor, venue));
               
                LOGGER.debug("Completed initializing map floor information '{}' for venue '{}'", 
                    floor.getId(), venue.getVenueUdId());
            }
        }
        LOGGER.debug("Completed getting map floor information for venue '{}'", venue.getVenueUdId());
        
        return floorInfoList;
    }
    
    private List<FloorInfoDTO> getFloorsByVenueFromLocSource(CasVenue casVenue)
    {
        List<FloorInfoDTO> floorInfoList = new ArrayList<FloorInfoDTO>();
        List<CasFloorInfo> floors = casVenue.getCasFloorInfoList();
        if (floors != null && !floors.isEmpty()) {
            for (int i = 0; i < floors.size(); i++) {
                CasFloorInfo floor = (CasFloorInfo) floors.get(i);

                floorInfoList.add(getFloorInfoDTOFromLocSource(floor, casVenue));
               
                LOGGER.debug("Completed initializing map floor information '{}' for venue '{}'", 
                    floor.getAesUid(), casVenue.getVenueUdId());
            }
        }
        LOGGER.debug("Completed getting map floor information for venue '{}'", casVenue.getVenueUdId());
        
        return floorInfoList;
    }
    
    private FloorInfoDTO getFloorInfoDTOFromAppEngageSource(Floor floor, Venue venue)
    {
        /*FloorInfoDTO floorDTO = new FloorInfoDTO();
        floorDTO.setMapHierarchyString("System Campus>" + venue.getName() + ">" + floor.getName());
        floorDTO.setName(floor.getName());
        floorDTO.setFloorId(floor.getMseFloorId());
        floorDTO.setVenueid(floor.getVenueUdId());

        FloorDimensionDTO dimensionDTO = new FloorDimensionDTO();
        dimensionDTO.setLength(floor.getLength());
        dimensionDTO.setWidth(floor.getWidth());
        dimensionDTO.setHeight(10.0f);
        dimensionDTO.setOffsetX(0.0f);
        dimensionDTO.setOffsetY(0.0f);
        dimensionDTO.setUnit("FEET");*/
        
        FloorInfoDTO floorDTO = 
            createFloorInfoDTO(floor.getName(), floor.getMseFloorId(), venue.getName(), venue.getVenueUdId());
            
        FloorDimensionDTO floorDimensionDTO =
            createFloorDimensionDTO(floor.getLength(), floor.getWidth(), 10f);
        
        floorDTO.setDimension(floorDimensionDTO);
        
        return floorDTO;
    }
    
    private FloorInfoDTO getFloorInfoDTOFromLocSource(CasFloorInfo casFloor, CasVenue casVenue)
    {
        FloorInfoDTO floorDTO = 
            createFloorInfoDTO(casFloor.getFloorName(), casFloor.getAesUid(), casVenue.getVenueName(), casVenue.getVenueUdId());
            
        FloorDimensionDTO floorDimensionDTO =
            createFloorDimensionDTO(casFloor.getLength(), casFloor.getWidth(), casFloor.getHeight());
            
        floorDTO.setDimension(floorDimensionDTO);
        
        return floorDTO;
    }

    
    private FloorInfoDTO createFloorInfoDTO(String floorName, String floorId, String venueName, String venueUdid)
    {
        FloorInfoDTO floorDTO = new FloorInfoDTO();
        floorDTO.setMapHierarchyString("System Campus>" + venueName + ">" + floorName);
        floorDTO.setName(floorName);
        floorDTO.setFloorId(floorId);
        floorDTO.setVenueid(venueUdid); 
        return floorDTO;
    }
    
    private FloorDimensionDTO createFloorDimensionDTO(float length, float width, float height)
    {
        FloorDimensionDTO dimensionDTO = new FloorDimensionDTO();
        dimensionDTO.setLength(length);
        dimensionDTO.setWidth(width);
        dimensionDTO.setHeight(height);
        dimensionDTO.setOffsetX(0.0f);
        dimensionDTO.setOffsetY(0.0f);
        dimensionDTO.setUnit("FEET");
        return dimensionDTO;
    }
    
    
}
