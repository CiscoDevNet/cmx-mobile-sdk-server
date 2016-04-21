package com.cisco.cmxmobile.services.mse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.info.Version;

@Component
@Path("/api/v1/server")
public class ServerMetricsService 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerMetricsService.class);
    
    @GET
    @Path("/mobileAppServerVersion")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServerVersion()
    {
        try {
            String versionNumber = Version.getInstance().getVersionNumber();
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("mobileAppServerVersion", versionNumber);
            return Response.ok(jsonObject.toString()).build();
        } catch (JSONException e) {
            LOGGER.error("Failed to get Mobile Application Server version", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            LOGGER.error("Failed to get Mobile Application Server version", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }        
    }
}
