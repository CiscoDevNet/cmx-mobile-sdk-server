package com.cisco.cmxmobile.services.clients;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.server.clients.POI;
import com.cisco.cmxmobile.server.stats.MobileAppStats;

@Component
@Path("/api/cmxmobile/v1/pois")
public class POIService {

    @Autowired
    public POI poi;

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public String getErrorMessage(@QueryParam("search") String search) {
        return "Wrong mnethod";
    }

    @GET
    @Path("/info/{venue}")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONArray getPOIByVenue(@PathParam("venue") String venueUdId, @QueryParam("search") String search) {
        MobileAppStats.getInstance().incrementPoiRequestsCount();
        return poi.getPOIByVenue(venueUdId, search);
    }

    @GET
    @Path("/info/{venue}/{floor}")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONArray getPOIByVenueAndFloor(@PathParam("venue") String venueUdId, @PathParam("floor") String floorId, @QueryParam("search") String search) {
        MobileAppStats.getInstance().incrementPoiRequestsCount();
        return poi.getPOIByVenueAndFloor(venueUdId, floorId, search);
    }

    @GET
    @Path("/image/{venueId}/{floorId}/{poiId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPoiImage(@PathParam("venueId") String venueUdId, @PathParam("floorId") String floorId, @PathParam("poiId") String poiId) {
        return poi.getPoiImage(venueUdId, floorId, poiId);
    }
    
    @GET
    @Path("/image/{venueId}/{poiId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPoiImageByPoiId(@PathParam("venueId") String venueUdId, @PathParam("poiId") String poiId) {
        return poi.getPoiImageByPoiId(venueUdId, poiId);
    }
}
