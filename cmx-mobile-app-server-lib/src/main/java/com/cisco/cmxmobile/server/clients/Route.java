package com.cisco.cmxmobile.server.clients;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jettison.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.model.Floor;
import com.cisco.cmxmobile.model.PointOfInterest;
import com.cisco.cmxmobile.model.WirelessClient;
import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;
import com.cisco.cmxmobile.graph.Edge;
import com.cisco.cmxmobile.graph.GraphUtil;
import com.cisco.cmxmobile.graph.Node;
import com.cisco.cmxmobile.utils.MDCKeys;

import edu.uci.ics.jung.graph.Graph;

@Component
public class Route {

    private static final Logger LOGGER = LoggerFactory.getLogger(Route.class);

    @Autowired
    private MobileServerCacheService mMobileServerCacheService;

    public List<Node> getRouteByDeviceId(String deviceId, UriInfo uriInfo, Double destX, Double destY) {
        MDC.put(MDCKeys.DEVICE_ID, deviceId);
        LOGGER.debug("Request to get route for '{}' with destination X '{}' and destination Y '{}'", deviceId, destX, destY);
        List<Node> routePath = null;
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        WirelessClient client = mMobileServerCacheService.getWirelessClientByUniqueID(deviceId);
        if (client != null && !client.getFloorId().isEmpty()) {
            Floor floor = mMobileServerCacheService.getFloorByVenueUdid(client.getVenueUdId(), client.getFloorId());
            double targetX = 0, targetY = 0;
            if (floor != null && queryParams.containsKey("destpoi")) {
                String poiId = queryParams.getFirst("destpoi");
                LOGGER.debug("Attempting to get route for '{}' to destination POI '{}'", deviceId, poiId);
                PointOfInterest poi = mMobileServerCacheService.getPointOfInterest(client.getVenueUdId(), floor.getMseFloorId(), poiId);
                if (poi != null) {
                    targetX = poi.getX();
                    targetY = poi.getY();
                } else {
                    LOGGER.debug("Unable to get route for '{}' to destination POI '{}' since POI does not exist", deviceId, poiId);
                }
            }
            else if (floor != null) {
                targetX = destX;
                targetY = destY;
            }

            if (targetX != 0 && targetY != 0) {
                routePath = getRouteByFloor(floor.getFloorPathInfo().getFloorPathInfo(), client.getX(), client.getY(), targetX, targetY);
            }
        } else {
            if (client == null) {
                LOGGER.error("Unable to find client with device ID '{}'", deviceId);
            } else {
                LOGGER.error("Device does not currently have a floor ID '{}'", deviceId);
            }
        }
        LOGGER.debug("Completed request to get route for '{}' with destination X '{}' and destination Y '{}'", deviceId, destX, destY);
        MDC.remove(MDCKeys.DEVICE_ID);
        return routePath;
    }

    public List<Node> getRouteByPoints(String deviceId, double startX, double startY, double destX, double destY) {
        MDC.put(MDCKeys.DEVICE_ID, deviceId);
        LOGGER.debug("Request to get route for '{}' swith start X '{}' and start Y '{}' with destination X '{}' and destination Y '{}'", deviceId, startX, startY, destX, destY);
        WirelessClient client = mMobileServerCacheService.getWirelessClient(deviceId);
        Floor floor = mMobileServerCacheService.getFloorByVenueUdid(client.getVenueUdId(), client.getFloorId());
        LOGGER.debug("Completed request to get route for '{}' swith start X '{}' and start Y '{}' with destination X '{}' and destination Y '{}'", deviceId, startX, startY, destX, destY);
        MDC.remove(MDCKeys.DEVICE_ID);
        return getRouteByFloor(floor.getFloorPathInfo().getFloorPathInfo(), startX, startY, destX, destY);
    }

    private List<Node> getRouteByFloor(String floorPathInfo, double startX, double startY, double destX, double destY) {
        List<Node> routePath = new ArrayList<Node>();
        try {
            Graph<Node, Edge> floorGraph = GraphUtil.getGraphFromJSON(floorPathInfo);
            Node sourceNode = new Node("source");
            sourceNode.setX(startX);
            sourceNode.setY(startY);
            Node targetNode = new Node("target");
            targetNode.setX(destX);
            targetNode.setY(destY);
            routePath = GraphUtil.getShortestPath(floorGraph, sourceNode, targetNode);
        }
        catch (JSONException e) {
            LOGGER.error("Error during route calculation : {}", e.getLocalizedMessage());
        }
        return routePath;
    }
}
