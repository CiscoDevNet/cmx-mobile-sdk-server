package com.cisco.cmxmobileserver.info;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Version {

    private static final Logger LOGGER = LoggerFactory.getLogger(Version.class);
    
    private static Version instance = new Version();

    private final Properties props;

    private Version() {
        props = new Properties();
        InputStream in = null;
        try {
            in = getClass().getResourceAsStream("/config/version.properties");
            props.load(in);
        }
        catch (Exception ex) {
            LOGGER.error("Error attempt to load version.properties", ex);
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

    public static Version getInstance() {
        return instance;
    }
    
    public String getVersionNumber() {
        return new StringBuffer().append(props.getProperty("major.number")).append('.').append(props.getProperty("minor.number")).append('.').append(props.getProperty("incremental.number")).append('.').append(props.getProperty("build.number")).toString();
    }
}