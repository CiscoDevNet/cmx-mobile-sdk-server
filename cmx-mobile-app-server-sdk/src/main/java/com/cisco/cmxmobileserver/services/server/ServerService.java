package com.cisco.cmxmobileserver.services.server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobileserver.info.ServerInfo;

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
        return Response.status(200).entity("Success").build();
    }
}