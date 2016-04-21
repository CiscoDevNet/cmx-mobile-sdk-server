package com.cisco.cmxmobile.security;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import com.cisco.cmxmobile.services.clients.ClientService;

public class MobileAppPreAuthFilter extends AbstractPreAuthenticatedProcessingFilter {

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        // TODO Auto-generated method stub
        Cookie[] requestCookies = request.getCookies();
        if (requestCookies != null) {
            for (int i = 0; i < requestCookies.length; i++) {
                Cookie c = requestCookies[i];
                if (ClientService.CLIENT_AUTHENTICATION_COOKIE_NAME.equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.substring(path.lastIndexOf("/") + 1);
    }

}
