package com.devookim.hibernatearcus.config;

import net.spy.memcached.ArcusClient;
import net.spy.memcached.ArcusClientPool;
import net.spy.memcached.ConnectionFactoryBuilder;

public class ArcusClientConfig {
    private final int poolSize;
    String host;
    String serviceCode;

    public ArcusClientConfig(String host, String serviceCode, int poolSize) {
        this.host = host;
        this.serviceCode = serviceCode;
        this.poolSize = poolSize;
    }

    public ArcusClientPool getArcusClientPool() {
        return createArcusClientPool();
    }

    private ArcusClientPool createArcusClientPool() {
        return ArcusClient.createArcusClientPool(
                host,
                serviceCode,
                new ConnectionFactoryBuilder(),
                poolSize
        );
    }
}