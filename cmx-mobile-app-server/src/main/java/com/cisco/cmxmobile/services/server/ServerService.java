package com.cisco.cmxmobile.services.server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.info.ServerInfo;
import com.cisco.cmxmobile.model.ServerStats;
import com.cisco.cmxmobile.server.stats.ConnectAndEngageStats;
import com.cisco.cmxmobile.server.stats.ContextAwareServiceStats;
import com.cisco.cmxmobile.server.stats.MobileAppStats;
import com.cisco.cmxmobile.server.stats.MobilePushNotificationStats;

@Component
@Path("/api/cmxmobileserver/v1/server")
public class ServerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerService.class);

    @GET
    @Path("/started")
    public Response getAllInfo() {

        LOGGER.info("Request to check if started");
        LOGGER.info("{}", (new ServerInfo().getStartupInfo()));
        LOGGER.info("Completed request for started check");
        ConnectAndEngageStats.getInstance();
        ContextAwareServiceStats.getInstance();
        MobileAppStats.getInstance();
        MobilePushNotificationStats.getInstance();
        return Response.status(200).entity("Success").build();
    }

    @GET
    @Path("/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public ServerStats getStats() {
        LOGGER.info("Request for server stats");
        ServerStats stats = new ServerStats();
        stats.setConnectAndEngageServerStats(ConnectAndEngageStats.getInstance().getConnectAndEngageServerStats());
        stats.setContextAwareServiceServerStats(ContextAwareServiceStats.getInstance().getContextAwareServiceServerStats());
        stats.setMobileAppServerStats(MobileAppStats.getInstance().getMobileAppServerStats());
        stats.setMobilePushNotificationServerStats(MobilePushNotificationStats.getInstance().getMobilePushNotificationServiceStats());
        return stats;
    }

    @GET
    @Path("/resetStats")
    public Response resetStats() {
        LOGGER.info("Request to reset server stats");
        ConnectAndEngageStats.getInstance().resetAllStats();
        ContextAwareServiceStats.getInstance().resetAllStats();
        MobileAppStats.getInstance().resetAllStats();
        MobilePushNotificationStats.getInstance().resetAllStats();
        return Response.status(200).entity("Success").build();
    }
}