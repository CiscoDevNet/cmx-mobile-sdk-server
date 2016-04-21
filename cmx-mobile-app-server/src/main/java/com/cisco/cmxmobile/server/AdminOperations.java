package com.cisco.cmxmobile.server;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import com.cisco.cmxmobile.utils.CmxProperties;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class AdminOperations {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminOperations.class);
    
    public static final String LOGGING_LEVEL_TRACE = "TRACE";
    
    public static final String LOGGING_LEVEL_DEBUG = "DEBUG";
    
    public static final String LOGGING_LEVEL_INFO = "INFO";
    
    public static final String LOGGING_BASE_URL = "/api/cmxmobile/v1/admin/logging";
    
    public static final String SERVER_BASE_URL = "https://localhost:";

    public void run(String[] args) {

        int cmdIndex;
        for (cmdIndex = 0; cmdIndex < args.length; cmdIndex++) {
            if (args[cmdIndex].equalsIgnoreCase("logginglevel")) {
                cmdIndex++;
                if (args[cmdIndex].equalsIgnoreCase(LOGGING_LEVEL_TRACE) || args[cmdIndex].equalsIgnoreCase(LOGGING_LEVEL_DEBUG) || args[cmdIndex].equalsIgnoreCase(LOGGING_LEVEL_INFO)) {
                    LOGGER.info("Starting to change logging level to '{}'", args[cmdIndex]);
                    changeLoggingConfiguration(args[cmdIndex]);
                    LOGGER.info("Completed changing logging level to '{}'", args[cmdIndex]);
                } else {
                    LOGGER.error("Logging level is not correct '{}'", args[cmdIndex]);
                }
            }
        }
    }
    
    public void changeLoggingConfiguration(String loggingLevel) {
        MultivaluedMapImpl formData = new MultivaluedMapImpl();
        formData.add("loggingLevel", loggingLevel);
        try {
            String url = SERVER_BASE_URL + CmxProperties.getInstance().getWebappPort() + "/" + CmxProperties.getInstance().getWebapContextPath() + LOGGING_BASE_URL;
            LOGGER.trace("URL for changing logging level '{}'", url);
            postFormData(url, formData);
        }
        catch (KeyManagementException e) {
            // TODO Auto-generated catch block
            LOGGER.error("KeyManagementException", e);
        }
        catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            LOGGER.error("NoSuchAlgorithmException", e);
        }
    }
    
    private ClientResponse postFormData(String url, MultivaluedMapImpl formData) throws KeyManagementException, NoSuchAlgorithmException {
        HostnameVerifier hv = getHostnameVerifier();
        ClientConfig config = new DefaultClientConfig();
        SSLContext ctx = getSSLContext();
        config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(hv, ctx));
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        Client client = Client.create(config);

        // POST the request
        WebResource clientResource = client.resource(url);
        return clientResource.type(MediaType.APPLICATION_FORM_URLENCODED_VALUE).post(ClientResponse.class, formData);
        
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
        final SSLContext sslContext = SSLContext.getInstance("SSL");

        // set up a TrustManager that trusts everything
        sslContext.init(null, new TrustManager[] {
            new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
        
                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        //Empty method
                    }
        
                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        //Empty method
                    }
                }
        }, new SecureRandom());

        return sslContext;
    }

    public static void main(String[] args) {
        AdminOperations adminOperations = new AdminOperations();
        adminOperations.run(args);
    }

}
