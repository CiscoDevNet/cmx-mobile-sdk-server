package com.cisco.cmxmobile.server.clients;

import java.util.List;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;
import com.cisco.cmxmobile.dto.VenueDTO;
import com.cisco.cmxmobile.utils.MDCKeys;

@Component
public class Venue {

    private static final Logger LOGGER = LoggerFactory.getLogger(Venue.class);

    @Autowired
    public MobileServerCacheService mobileServerCacheService;
    
    @Autowired
    private VenueServiceHelper venueServiceHelper;

    public List<VenueDTO> getVenueInfo() {
        LOGGER.debug("Request to get all venue information");
        
        List<VenueDTO> venueDTOList = venueServiceHelper.getVenueDTOList();
        
        LOGGER.debug("Completed request to get all venue information");
        return venueDTOList;
    }
    
    public VenueDTO getVenueInfo(@PathParam("venueId") String venueUdId) 
    {
        MDC.put(MDCKeys.VENUE_ID, venueUdId);
        LOGGER.debug("Request to get venue information for", venueUdId);
        
        VenueDTO venueDto = venueServiceHelper.getVenueDTO(venueUdId);
        
        LOGGER.debug("Completed request to get venue information for", venueUdId);
        return venueDto;
    }
    
    public Response getMapImage(String mseVenueUdId) {
        MDC.put(MDCKeys.VENUE_ID, mseVenueUdId);
        LOGGER.debug("Request to get map venue image for '{}'", mseVenueUdId);
        try {
            //TODO: Read venue image file from file store
            ResponseBuilder response = Response.ok();
            response.type("image/gif");
            LOGGER.debug("Completed request to get map venue image for '{}' with floor ID '{}'", mseVenueUdId);
            MDC.remove(MDCKeys.VENUE_ID);
            return response.build();
        }
        catch (Exception ex) {
            MDC.remove(MDCKeys.VENUE_ID);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
