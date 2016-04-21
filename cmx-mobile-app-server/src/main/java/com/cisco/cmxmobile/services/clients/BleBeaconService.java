package com.cisco.cmxmobile.services.clients;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.server.clients.BleBeaconServer;


@Component
@Path("/api/cmxmobile/v1/blebeacons")
public class BleBeaconService 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BleBeaconService.class);
    
    @Autowired
    public BleBeaconServer bleBeaconServer;

    /**
     *  API : /api/cmxmobile/v1/blebeacons/info/:venueId/
     */
    @GET
    @Path("/info/{venueId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBleBeaconsInfo(@PathParam("venueId") String venueUdId)
    {
        try {
            return Response.ok().entity(bleBeaconServer.getBleBeaconsInfoListByVenue(venueUdId)).build();
        } catch (Exception e) {
            LOGGER.error("Failed to Retrive BleBeacons List", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to Retrive BleBeacons List" + e.getLocalizedMessage()).build();
        } 
    }
    
    /**
     *  API : /api/cmxmobile/v1/blebeacons/info/:venueId/:floorId/
     */
    @GET
    @Path("/info/{venueId}/{floorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBleBeaconsInfo(@PathParam("venueId") String venueUdId, 
                                      @PathParam("floorId") String floorId)
    {
        try {
            return Response.ok().entity(bleBeaconServer.getBleBeaconsInfoListByFloor(venueUdId, floorId)).build();
        } catch (Exception e) {
            LOGGER.error("Failed to Retrive BleBeacons List", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to Retrive BleBeacons List" + e.getLocalizedMessage()).build();
        } 
    }
    

}