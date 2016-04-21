package com.cisco.cmxmobile.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class MobileAppUserAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException arg2) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, arg2.getLocalizedMessage());
        /*
         * TODO: this should really redirect to some sort of form-login page so
         * that we can do the location challenge... this now leaves us with two
         * sensitivities of requests to handle: /.../clients/location/ - needs
         * the location challenge /.../clients/optOut/ - doesn't need the
         * location challenge, still needs an authentication/authorization token
         * from the client
         */
    }

}
