package com.cisco.cmxmobile.server.rest;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.UriBuilder;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.model.ServerStats;
import com.cisco.cmxmobile.utils.CmxProperties;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

@Component
public class MobileServerRestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MobileServerRestClient.class);
    
    public static final String SERVER_BASE_URL = "https://localhost:";
    
    public static final String SERVER_STATS_URL = "/api/cmxmobileserver/v1/server/stats";
    
    public static final String RESET_SERVER_STATS_URL = "/api/cmxmobileserver/v1/server/resetStats";
    
    public ServerStats getServerStats() throws RestClientException {
        String url = SERVER_BASE_URL + CmxProperties.getInstance().getWebappPort() + "/" + CmxProperties.getInstance().getWebapContextPath() + SERVER_STATS_URL;
        URI serverStatsUrl = UriBuilder.fromUri(url).build();
        try {
            return getObjectFromServer(serverStatsUrl, ServerStats.class);
        } catch (KeyManagementException e) {
            LOGGER.error("Failed to get server stats", e);
            throw new RestClientException(e);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Failed to get server stats", e);
            throw new RestClientException(e);
        } catch (ClientHandlerException e) {
            LOGGER.error("Unable to establish connection with server. Verify server is running.", e);            
            throw new RestClientException(e);            
        }
    }
    
    public void resetServerStats() throws RestClientException {
        String url = SERVER_BASE_URL + CmxProperties.getInstance().getWebappPort() + "/" + CmxProperties.getInstance().getWebapContextPath() + RESET_SERVER_STATS_URL;
        URI resetStatsUrl = UriBuilder.fromUri(url).build();
        try {
            ClientResponse response = getClientResponse(resetStatsUrl);
            if (response.getClientResponseStatus() != ClientResponse.Status.OK) {
                LOGGER.error("Failed to reset server stats. Status code returned: {}", response.getClientResponseStatus());
            }
        } catch (KeyManagementException e) {
            LOGGER.error("Failed to get server stats", e);
            throw new RestClientException(e);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Failed to get server stats", e);
            throw new RestClientException(e);
        } catch (ClientHandlerException e) {
            LOGGER.error("Unable to establish connection with server. Verify server is running.");            
            throw new RestClientException(e);            
        }
    }

    public <T extends Object> T getObjectFromServer(URI uri, final Class<T> clazz) throws KeyManagementException, NoSuchAlgorithmException, ClientHandlerException {
        ClientResponse response = getClientResponse(uri);

        if (response.getClientResponseStatus() == ClientResponse.Status.OK) {
            return response.getEntity(clazz);
        }
        else {
            return null;
        }
    }

    private ClientResponse getClientResponse(URI uri) throws KeyManagementException, NoSuchAlgorithmException, ClientHandlerException {
        HostnameVerifier hv = getHostnameVerifier();
        ClientConfig config = new DefaultClientConfig();
        SSLContext ctx = this.getSSLContext();
        config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(hv, ctx));
        config.getClasses().add(ObjectMapper.class);
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        Client client = Client.create(config);

        WebResource webResource = client.resource(uri);

        return webResource.accept("application/json").get(ClientResponse.class);
    }

    private HostnameVerifier getHostnameVerifier() {
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }
        };

        return hostnameVerifier;
    }

    private SSLContext getSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        final SSLContext sslContext = SSLContext.getInstance("TLS");

        // set up a TrustManager that trusts everything
        sslContext.init(null, new TrustManager[] {new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                // Do nothing
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                // Do nothing
            }
        } }, new SecureRandom());

        return sslContext;
    }  
}
