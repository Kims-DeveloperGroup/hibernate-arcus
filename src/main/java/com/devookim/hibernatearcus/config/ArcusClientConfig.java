package com.devookim.hibernatearcus.config;

import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.*;

import java.util.Map;

@Slf4j
public class ArcusClientConfig {
    public final boolean fallbackEnabled;
    public final boolean initFallbackMode;
    private final int poolSize;
    private final Map<String, String> properties;
    private final String host;
    private final String serviceCode;

    public ArcusClientConfig(Map<String, String> properties) {
        this.host = properties.getOrDefault("hibernate.cache.arcus.host", "localhost:2181");
        this.serviceCode = properties.getOrDefault("hibernate.cache.arcus.serviceCode", "");
        this.poolSize = Integer.parseInt(properties.getOrDefault("hibernate.cache.arcus.poolSize", "1"));
        this.fallbackEnabled = Boolean.parseBoolean(properties.getOrDefault("hibernate.cache.arcus.fallbackEnabled", "true"));
        this.initFallbackMode = Boolean.parseBoolean(properties.getOrDefault("hibernate.cache.arcus.initFallbackMode", "false"));
        this.properties = properties;
    }

    public ArcusClientPool createArcusClientPool() {
        log.info("Creating arcus client pool");
        ConnectionFactoryBuilder cfb = new ConnectionFactoryBuilder();
        cfb.setMaxReconnectDelay(Long.parseLong(properties.getOrDefault("hibernate.cache.arcus.reconnectIntervalInSec", "10000")));
        cfb.setOpTimeout(Long.parseLong(properties.getOrDefault("hibernate.cache.arcus.opTimeout", "10000")));
        cfb.setTimeoutExceptionThreshold(Integer.parseInt(properties.getOrDefault("hibernate.cache.arcus", "3")));
        return ArcusClient.createArcusClientPool(
                host,
                serviceCode,
                cfb,
                poolSize
        );
    }
}