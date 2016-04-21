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

import com.cisco.cmxmobile.dto.FloorInfoDTO;
import com.cisco.cmxmobile.server.clients.MapServer;
import com.cisco.cmxmobile.server.stats.MobileAppStats;

@Component
@Path("/api/cmxmobile/v1/maps")
public class MapService {

    @Autowired
    private MapServer mapServer;

    @GET
    @Path("/info/{venueId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FloorInfoDTO> getVenueMapInfo(@PathParam("venueId") String venueUdId) {
        MobileAppStats.getInstance().incrementMapRequestsCount();
        return mapServer.getVenueMapInfo(venueUdId);
    }

    @GET
    @Path("/info/{venueId}/{floorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public FloorInfoDTO getMapInfo(@PathParam("venueId") String mseVenueUdId, @PathParam("floorId") String floorId) {
        MobileAppStats.getInstance().incrementMapRequestsCount();
        return mapServer.getMapInfo(mseVenueUdId, floorId);
    }

    @GET
    @Path("/image/{venueId}/{floorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMapImage(@PathParam("venueId") String mseVenueUdId, @PathParam("floorId") String floorId) {
        return mapServer.getMapImage(mseVenueUdId, floorId);
    }
}
