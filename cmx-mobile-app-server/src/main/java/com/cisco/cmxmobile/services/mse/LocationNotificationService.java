package com.cisco.cmxmobile.services.mse;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.utils.MDCKeys;

@Component
@Path("/api/cmxmobile/v1/notify/")
public class LocationNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationNotificationService.class);
    
    @Autowired
    private LocationNotificationHandler locationNotificationHandler;

    public enum Event {
        ASSOCIATION("AssociationEvent"), PRESENCE("PresenceEvent"), MOVEMENT("MovementEvent"), CONTAINMENT("ContainmentEvent"), ABSENCE("AbsenceEvent");
        private final String mJsonName;

        private Event(String jsonName) {
            mJsonName = jsonName;
        }

        @Override
        public String toString() {
            return mJsonName;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response receiveNotification(String body, @Context HttpServletRequest req) {
        LOGGER.debug("Received client notification event");
        
        // TODO: authenticate sending MSE
        // get request body
        ObjectMapper mapper = new ObjectMapper();
        // we shouldn't have to do this
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Map<String, Object> inputObject = null;
        try {
            inputObject = mapper.readValue(body, Map.class);
        }
        catch (Exception e) {
            LOGGER.error("Failed to parse notification from server", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        //Handle Events
        if (inputObject.containsKey(Event.MOVEMENT.toString())) {
            return locationNotificationHandler.handleMovementEvent(inputObject, req.getRemoteAddr());
        }
        else if (inputObject.containsKey(Event.ASSOCIATION.toString())) {
            return locationNotificationHandler.handleAssociationEvent(inputObject, req.getRemoteAddr());
        }
        else if (inputObject.containsKey(Event.CONTAINMENT.toString())) {
            return locationNotificationHandler.handleContainmentEvent(inputObject, req.getRemoteAddr());
        }
        else if (inputObject.containsKey(Event.ABSENCE.toString())) {
            LOGGER.trace("An absence event was received but being ignored for this release");
        }
        else {
            // if the input event doesn't match, that's an error
            LOGGER.error("Unexpected event type. Received {}", inputObject.keySet());
            MDC.remove(MDCKeys.DEVICE_MAC_ADDRESS);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // TODO: rate-limiting logic goes here
        MDC.remove(MDCKeys.DEVICE_MAC_ADDRESS);
        return Response.ok().build();
    }
}