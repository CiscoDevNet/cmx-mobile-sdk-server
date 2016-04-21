package com.cisco.cmxmobileserver.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CmxSdkProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmxSdkProperties.class);
    
    private static CmxSdkProperties instance = new CmxSdkProperties();

    private final Properties props;

    private CmxSdkProperties() {
        props = new Properties();
        InputStream in = null;
        try {
            in = getClass().getResourceAsStream("/config/settings.properties");
            props.load(in);
        }
        catch (Exception ex) {
            LOGGER.error("Error attempt to load settings.properties", ex);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ioE) {
                    LOGGER.error("Error attempt to cloase input stream for settings.properties", ioE);
                }
            }
        }
    }

    public static CmxSdkProperties getInstance() {
        return instance;
    }

    public String getWebappPort() {
        return props.getProperty("webapp.port");
    }

    public String getWebapContextPath() {
        return props.getProperty("webapp.contextPath");
    }
}