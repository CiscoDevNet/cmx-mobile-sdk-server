package com.cisco.cmxmobile.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CmxProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmxProperties.class);
    
    private static CmxProperties instance = new CmxProperties();

    private final Properties props;

    private CmxProperties() {
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

    public static CmxProperties getInstance() {
        return instance;
    }

    public String getWebappPort() {
        return props.getProperty("webapp.port");
    }

    public String getWebapContextPath() {
        return props.getProperty("webapp.contextPath");
    }
    
    public String getProxyHost() {
        return props.getProperty("webapp.proxyHost");
    }

    public String getProxyPort() {
        return props.getProperty("webapp.proxyPort");
    }

    public int getPushNotificationKeepAliveTime() {
        return Integer.parseInt(props.getProperty("pushNotification.keepAliveTime"));
    }

    public int getPushNotificationMaxPoolSize() {
        return Integer.parseInt(props.getProperty("pushNotification.maxPoolSize"));
    }

    public int getPushNotificationPoolSize() {
        return Integer.parseInt(props.getProperty("pushNotification.poolSize"));
    }
}