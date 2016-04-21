package com.cisco.cmxmobile.services.clients;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.graph.Node;
import com.cisco.cmxmobile.server.clients.Route;
import com.cisco.cmxmobile.server.stats.MobileAppStats;

@Component
@Path("/api/cmxmobile/v1/routes/clients")
public class RouteService {

    @Autowired
    private Route route;

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public String getErrorMessage() {
        return "No Routes available";
    }

    @GET
    @Path("/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Node> getRouteByDeviceId(@PathParam("deviceId") String deviceId, @Context UriInfo uriInfo, @QueryParam("destx") Double destX, @QueryParam("desty") Double destY) {
        MobileAppStats.getInstance().incrementRouteRequestsCount();
        return route.getRouteByDeviceId(deviceId, uriInfo, destX, destY);
    }

    @GET
    @Path("/routebypoints/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Node> getRouteByPoints(@PathParam("deviceId") String deviceId, @QueryParam("startx") double startX, @QueryParam("starty") double startY, @QueryParam("destx") double destX, @QueryParam("desty") double destY) {
        MobileAppStats.getInstance().incrementRouteRequestsCount();
        return route.getRouteByPoints(deviceId, startX, startY, destX, destY);
    }
}
