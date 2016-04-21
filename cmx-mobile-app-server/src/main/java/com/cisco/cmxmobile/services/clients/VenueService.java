package com.cisco.cmxmobile.services.clients;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.server.clients.Venue;
import com.cisco.cmxmobile.server.stats.MobileAppStats;
import com.cisco.cmxmobile.dto.VenueDTO;

@Component
@Path("/api/cmxmobile/v1/venues")
public class VenueService {

    @Autowired
    private Venue venue;

    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    public List<VenueDTO> getVenueInfo() {
        MobileAppStats.getInstance().incrementVenueRequestsCount();
        return venue.getVenueInfo();
    }
    
    @GET
    @Path("/info/{venueId}")
    @Produces(MediaType.APPLICATION_JSON)
    public VenueDTO getVenueInfo(@PathParam("venueId") String venueUdId) 
    {
        MobileAppStats.getInstance().incrementVenueRequestsCount();
        return venue.getVenueInfo(venueUdId);
    }
    
    @GET
    @Path("/image/{venueId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMapImage(@PathParam("venueId") String mseVenueUdId) {
        return venue.getMapImage(mseVenueUdId);
    }
}
