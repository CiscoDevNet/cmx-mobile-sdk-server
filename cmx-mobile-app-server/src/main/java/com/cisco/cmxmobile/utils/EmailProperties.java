package com.cisco.cmxmobile.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EmailProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailProperties.class);
    
    private static EmailProperties instance = new EmailProperties();

    private final Properties props;

    private EmailProperties() {
        props = new Properties();
        InputStream in = null;
        try {
            in = getClass().getResourceAsStream("/config/email.properties");
            props.load(in);
        }
        catch (Exception ex) {
            LOGGER.error("Error attempt to load email.properties", ex);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ioE) {
                    LOGGER.error("Error attempt to close input stream for email.properties", ioE);
                }
            }
        }
    }

    public static EmailProperties getInstance() {
        return instance;
    }
    
    public Properties getProperties() {
        return props;
    }

    public int getKeepAliveTime() {
        return Integer.parseInt(props.getProperty("cmxEmail.keepAliveTime"));
    }

    public int getMaxPoolSize() {
        return Integer.parseInt(props.getProperty("cmxEmail.maxPoolSize"));
    }

    public int getPoolSize() {
        return Integer.parseInt(props.getProperty("cmxEmail.poolSize"));
    }
    
    public String getEmailUsername() {
        return props.getProperty("cmxEmail.userName");
    }
    
    public String getEmailPassword() {
        return props.getProperty("cmxEmail.password");
    }
    
    public String getFeedbackToAddress() {
        return props.getProperty("cmxFeedback.toAddress");
    }

    public String getFeedbackFromAddress() {
        return props.getProperty("cmxFeedback.fromAddress");
    }

    public String getFeedbackSubject() {
        return props.getProperty("cmxFeedback.subject");
    }
}