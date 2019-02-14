package com.revolut.backend;

import lombok.Getter;

import java.net.URI;
import java.util.Properties;

@Getter
public class ConfigurationProperties {

    private static final ConfigurationProperties INSTANCE = new ConfigurationProperties();

    public static ConfigurationProperties configuration() {
        return INSTANCE;
    }

    private URI webServerPublicAddress;
    private int webServerPort;
    private int webServerThreads;
    private String prevalayerUserFolder;

    private ConfigurationProperties() {
        Properties properties = new java.util.Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("configuration.properties"));
            loadProperties(properties);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to load configuration", e);
        }
    }

    private void loadProperties(Properties properties) throws Exception {
        webServerPublicAddress = new URI(properties.getProperty("webServer.publicAddress"));
        webServerPort = Integer.parseInt(properties.getProperty("webServer.port"));
        webServerThreads = Integer.parseInt(properties.getProperty("webServer.threads"));
        prevalayerUserFolder = properties.getProperty("prevalayer.userFolder");
    }
}
