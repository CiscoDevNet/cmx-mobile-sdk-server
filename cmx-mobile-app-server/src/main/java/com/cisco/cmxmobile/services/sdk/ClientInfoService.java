package com.cisco.cmxmobile.services.sdk;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.codehaus.jettison.json.JSONObject;

import com.cisco.cmxmobile.model.WirelessClient;
import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;

@Component
@Path("/api/cmxmobileserver/v1/clients")
public class ClientInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientInfoService.class);

    @Autowired
    private MobileServerCacheService mobileServerCacheService;

    @GET
    @Path("isRegistered/{macAddress}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isDeviceRegistered(@PathParam("macAddress") String macAddress) {
        WirelessClient client = mobileServerCacheService.getWirelessClient(macAddress);
        JSONObject clientObject = new JSONObject();
        try {
            clientObject.accumulate("isRegistered", "false");
            if (client != null) {
                clientObject.accumulate("isRegistered", "true");
            }
        } catch (Exception e) {
            LOGGER.error("ERROR during check");
        }
        return Response.status(Response.Status.OK).entity(clientObject).build();
    }
}