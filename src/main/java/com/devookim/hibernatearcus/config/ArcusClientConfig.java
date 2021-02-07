package com.devookim.hibernatearcus.config;

import net.spy.memcached.ArcusClient;
import net.spy.memcached.ArcusClientPool;
import net.spy.memcached.ConnectionFactoryBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

public class ArcusClientConfig {
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