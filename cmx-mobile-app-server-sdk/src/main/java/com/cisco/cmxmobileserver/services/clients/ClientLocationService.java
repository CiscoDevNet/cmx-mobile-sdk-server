package com.cisco.cmxmobileserver.services.clients;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;
import com.cisco.cmxmobile.dto.WirelessClientDTO;
import com.cisco.cmxmobile.model.WirelessClient;

@Component
@Path("/api/cmxmobileserver/v1/clients")
public class ClientLocationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientLocationService.class);

    @Autowired
    private MobileServerCacheService mobileServerCacheService;

    @GET
    @Path("/location/{macAddress}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClientByMAC(@PathParam("macAddress") String macAddress) {
        LOGGER.trace("Request to retrieve client current location for MAC address '{}'", macAddress);

        WirelessClient macClientLocation = mobileServerCacheService.getWirelessClient(macAddress);
        if (macClientLocation == null) {
            LOGGER.trace("Unable to find client MAC address '{}'", macAddress);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        WirelessClientDTO clientObject = new WirelessClientDTO(macClientLocation);
        LOGGER.trace("Returning JSON object with MAC Address '{}' : {}", macAddress, clientObject);
        return Response.ok(clientObject, MediaType.APPLICATION_JSON).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllClients() {
        LOGGER.trace("Request to retrieve all wireless clients");

        List<WirelessClient> wirelessClients = mobileServerCacheService.getAllWirelessClients();
        if (wirelessClients == null) {
            LOGGER.trace("Unable to find any wireless clients");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<WirelessClientDTO> clientObjects = new ArrayList<WirelessClientDTO>();
        for (WirelessClient macClientLocation : wirelessClients) {
            WirelessClientDTO clientObject = new WirelessClientDTO(macClientLocation);
            clientObjects.add(clientObject);
        }
        LOGGER.trace("Returning JSON object with all the wireless clients");
        return Response.ok(clientObjects, MediaType.APPLICATION_JSON).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
    }
    
    @GET
    @Path("/location/floor/{venueId}/{floorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllClientsByFloor(@PathParam("venueId") String venueId, @PathParam("floorId") String floorId) {
        LOGGER.trace("Request to retrieve all wireless clients");

        List<WirelessClient> wirelessClients = mobileServerCacheService.getAllWirelessClients();
        if (wirelessClients == null) {
            LOGGER.trace("Unable to find any wireless clients");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<WirelessClientDTO> clientObjects = new ArrayList<WirelessClientDTO>();
        for (WirelessClient macClientLocation : wirelessClients) {
            if (macClientLocation.getVenueUdId().equals(venueId) && macClientLocation.getFloorId().equals(floorId)) {
                WirelessClientDTO clientObject = new WirelessClientDTO(macClientLocation);
                clientObjects.add(clientObject);
            }
        }
        LOGGER.trace("Returning JSON object with all the wireless clients");
        return Response.ok(clientObjects, MediaType.APPLICATION_JSON).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
    }

}