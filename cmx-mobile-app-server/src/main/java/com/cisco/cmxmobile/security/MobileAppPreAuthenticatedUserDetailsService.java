package com.cisco.cmxmobile.security;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.cisco.cmxmobile.model.WirelessClient;
import com.cisco.cmxmobile.cacheService.service.MobileServerCacheService;
import com.cisco.cmxmobile.utils.MDCKeys;

public class MobileAppPreAuthenticatedUserDetailsService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MobileAppPreAuthenticatedUserDetailsService.class);
    
    @Autowired
    MobileServerCacheService mMobileServerCacheService;

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) {
        MDC.put(MDCKeys.DEVICE_ID, token.getName());
        LOGGER.trace("Loading user detail information for device ID '{}'", token.getName());
        WirelessClient client = mMobileServerCacheService.getWirelessClientByUniqueID(token.getName());
        if (client == null) {
            LOGGER.error("Unable to find client with device ID '{}'", token.getName());
            throw new UsernameNotFoundException(token.getName());
        }
        if (token.getCredentials() == null || (token.getCredentials() != null && !token.getCredentials().equals(client.getPassword()))) {
            /*
             * TODO: this is dirty... we should authentication/credential-
             * checking at some other point in the framework
             */
            LOGGER.error("Failed to authenticate client with device ID '{}'", token.getName());
            throw new UsernameNotFoundException("Bad credentials");
        }
        Collection<GrantedAuthority> authorities = client.getAuthorities();
        LOGGER.trace("Completed loading user detail information for device ID '{}'", token.getName());
        return new User(token.getName(), client.getPassword(), true, true, true, true, authorities);
    }
}
