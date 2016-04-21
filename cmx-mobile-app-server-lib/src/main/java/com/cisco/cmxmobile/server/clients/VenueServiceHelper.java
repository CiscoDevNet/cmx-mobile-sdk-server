package com.cisco.cmxmobile.server.clients;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;
import com.cisco.cmxmobile.cacheService.service.SetupSource;
import com.cisco.cmxmobile.dto.NetworkInfoDTO;
import com.cisco.cmxmobile.dto.VenueDTO;
import com.cisco.cmxmobile.model.CasVenue;
import com.cisco.cmxmobile.model.Ssid;
import com.cisco.cmxmobile.model.Venue;

@Component
public class VenueServiceHelper 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MapServiceHelper.class);

    @Autowired
    private MobileServerCacheService mobileServerCacheService;
    
    @Autowired
    private MapServiceHelper mapServiceHelper;
    

    @Value("${default.locationUpdateInterval}")
    String defaultLocationUpdateInterval;
    
    @Value("${default.wifiConnectionMode}")
    String defaultWifiConnectionMode;

    public List<VenueDTO> getVenueDTOList() {
        List<VenueDTO> venueDTOList = new ArrayList<VenueDTO>();
        
        venueDTOList.addAll(getVenueDTOListFromAppEngageSource());
        venueDTOList.addAll(getVenueDTOListFromLocationSource());

        return venueDTOList;
    }
    
    private List<VenueDTO> getVenueDTOListFromAppEngageSource()
    {
        List<Venue> venueList = mobileServerCacheService.getAllVenues();
        List<VenueDTO> venueDTOList = new ArrayList<VenueDTO>();
        if (venueList != null && !venueList.isEmpty()) {
            for (int i = 0; i < venueList.size(); i++) {
                Venue venue = (Venue) venueList.get(i);
                if (SetupSource.getSource(venue.getMseUdId()) == SetupSource.APP_ENGAGE) {
                    try {
                        venueDTOList.add(getVenueDTOFromAppEngageSource(venue));
                        LOGGER.debug("Completed initializing venue information for venue", venue.getVenueUdId());
                    } catch (Exception e) {
                        LOGGER.error("Error attempting to get venue information for", venue.getVenueUdId());
                    }
                }    
            }
        }
        
        return venueDTOList;
    }

    private List<VenueDTO> getVenueDTOListFromLocationSource()
    {
        List<CasVenue> venueList = mobileServerCacheService.getAllCasVenues();
        List<VenueDTO> venueDTOList = new ArrayList<VenueDTO>();
        if (venueList != null && !venueList.isEmpty()) {
            for (int i = 0; i < venueList.size(); i++) {
                CasVenue casVenue = (CasVenue) venueList.get(i);
                if (SetupSource.getSource(casVenue.getMseUdid()) == SetupSource.LOCATION_MODULE) {
                    try {
                        venueDTOList.add(getVenueDTOFromLocationSource(casVenue));
                        LOGGER.debug("Completed initializing venue information for venue", casVenue.getVenueUdId());
                    } catch (Exception e) {
                        LOGGER.error("Error attempting to get venue information for", casVenue.getVenueUdId());
                    }
                }
            }
        }
        return venueDTOList;
    }
    
    public VenueDTO getVenueDTO(String venueUdid)
    {
        //Try to Retrieve the APPEngage Venue first
        Venue venue = mobileServerCacheService.getVenue(venueUdid);
        
        if (venue == null) {
            //Get the CasVenue Instead
            CasVenue casVenue = mobileServerCacheService.getCasVenue(venueUdid);
            
            if (casVenue != null && SetupSource.getSource(casVenue.getMseUdid()) == SetupSource.LOCATION_MODULE) {
                // Retrieve the Floors and return
                return getVenueDTOFromLocationSource(casVenue);
            } 
        } else {
            if (SetupSource.getSource(venue.getMseUdId()) == SetupSource.APP_ENGAGE) {
                //Retrieve the Floors and return
                return getVenueDTOFromAppEngageSource(venue);
            } 
        }
        
        LOGGER.error("Unable to find map venue information for '{}'", venueUdid);
        
        //Return an Empty List
        return null;
    }

    private VenueDTO getVenueDTOFromAppEngageSource(Venue venue)
    {
        VenueDTO venueDto = new VenueDTO();
        venueDto.setVenueId(venue.getVenueUdId());
        venueDto.setName(venue.getName());
        venueDto.setStreetAddress(venue.getAddress());
        venueDto.setLocationUpdateInterval(venue.getLocationUpdateInterval());
        //TODO: Need to read image type from venue obejct
        venueDto.setImageType("none");
        venueDto.setWifiConnectionMode(venue.getWifiConnectionMode());
        
        List<NetworkInfoDTO> networkInfo =  new ArrayList<NetworkInfoDTO>();
        if (venue.getSsidList() != null && !venue.getSsidList().isEmpty()) {
            for (Ssid ssid : venue.getSsidList()) {
                NetworkInfoDTO info = new NetworkInfoDTO();
                info.setSsid(ssid.getSsid());
                info.setPassword(ssid.getPassword());
                networkInfo.add(info);
            }
        }
        
        //Added preferred network to venue 
        venueDto.setPreferredNetwork(networkInfo);
        
        //Add the Floors
        venueDto.setFloors(mapServiceHelper.getFloorsByVenue(venue));
        
        return venueDto;
    }
    
    private VenueDTO getVenueDTOFromLocationSource(CasVenue casVenue)
    {
        VenueDTO venueDto = new VenueDTO();
        venueDto.setVenueId(casVenue.getVenueUdId());
        venueDto.setName(casVenue.getVenueName());
        venueDto.setStreetAddress("");
        
        //Setting 5 seconds = 5
        venueDto.setLocationUpdateInterval(new Integer(defaultLocationUpdateInterval)); 
        
        //TODO: Need to read image type from venue obejct
        venueDto.setImageType("none");
        
        //Setting File auto
        venueDto.setWifiConnectionMode(defaultWifiConnectionMode);
        
        //Added preferred network to venue 
        venueDto.setPreferredNetwork(new ArrayList<NetworkInfoDTO>());
        
        //Add the Floors
        venueDto.setFloors(mapServiceHelper.getFloorsByCasVenue(casVenue));
        
        return venueDto;
    }
}
