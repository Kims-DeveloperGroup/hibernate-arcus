package com.devookim.hibernatearcus.config;

import net.spy.memcached.ArcusClient;
import net.spy.memcached.ArcusClientPool;
import net.spy.memcached.ConnectionFactoryBuilder;

public class ArcusClientConfig {
    public final boolean fallbackEnabled;
    public final boolean initFallbackMode;
    private final int poolSize;
    private final String host;
    private final String serviceCode;

    public ArcusClientConfig(String host,
                             String serviceCode,
                             int poolSize) {
        this.host = host;
        this.serviceCode = serviceCode;
        this.poolSize = poolSize;
    }

    public ArcusClientPool createArcusClientPool(ConnectionObserver connectionObserver) {
        ConnectionFactoryBuilder cfb = new ConnectionFactoryBuilder();
        cfb.setInitialObservers(Collections.singleton(connectionObserver));
        cfb.setOpTimeout(Long.parseLong(properties.getOrDefault("hibernate.cache.arcus.opTimeout", "1000")));
        cfb.setTimeoutExceptionThreshold(Integer.parseInt(properties.getOrDefault("hibernate.cache.arcus", "3")));
        return ArcusClient.createArcusClientPool(
                host,
                serviceCode,
                new ConnectionFactoryBuilder(),
                poolSize
        );
    }
}