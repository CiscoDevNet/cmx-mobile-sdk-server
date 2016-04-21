package com.cisco.cmxmobile.services.admin;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.server.AdminOperations;

import ch.qos.logback.classic.Level;

@Component
@Path("/api/cmxmobileserver/v1/admin")
public class AdminService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminService.class);

    /**
     * Change logging level
     * 
     * @param loggingLevel
     * @return
     */
    @POST
    @Path("/logging")
    public Response registerClient(@FormParam("loggingLevel") String loggingLevel) {

        LOGGER.info("Request to change admin logging level '{}'", loggingLevel);
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        if (loggingLevel != null) {
            if (loggingLevel.equalsIgnoreCase(AdminOperations.LOGGING_LEVEL_TRACE)) {
                LOGGER.info("Logging level being changed to TRACE");
                root.setLevel(Level.TRACE);               
            } else if (loggingLevel.equalsIgnoreCase(AdminOperations.LOGGING_LEVEL_DEBUG)) {
                LOGGER.info("Logging level being changed to DEBUG");
                root.setLevel(Level.DEBUG);                               
            } else if (loggingLevel.equalsIgnoreCase(AdminOperations.LOGGING_LEVEL_INFO)) {
                LOGGER.info("Logging level being changed to INFO");
                root.setLevel(Level.INFO);               
            } else {
                LOGGER.error("Logging level change requested is not known {}", loggingLevel);               
            }
        }
        LOGGER.info("Completed request to change admin logging level '{}'", loggingLevel);
        return Response.status(200).entity("Success").build();
    }
}