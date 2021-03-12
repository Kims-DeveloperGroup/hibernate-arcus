package com.devookim.hibernatearcus.config;

import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.ArcusClient;
import net.spy.memcached.ArcusClientPool;
import net.spy.memcached.ConnectionFactoryBuilder;

import java.util.Map;

import static com.devookim.hibernatearcus.config.HibernateArcusProperties.*;

@Slf4j
public class ArcusClientConfig {
    public final boolean fallbackEnabled;
    public final boolean initFallbackMode;
    public final int healthCheckIntervalInSec;
    public final int domainDataTTL;
    private final int poolSize;
    private final Map<String, String> properties;
    private final String host;
    private final String serviceCode;

    public ArcusClientConfig(Map<String, String> properties) {
        this.host = properties.getOrDefault(HIBERNATE_CACHE_ARCUS_HOST, "localhost:2181");
        this.serviceCode = properties.getOrDefault(HIBERNATE_CACHE_ARCUS_SERVICE_CODE, "");
        this.poolSize = Integer.parseInt(properties.getOrDefault(HIBERNATE_CACHE_ARCUS_POOL_SIZE, "1"));
        this.domainDataTTL = Integer.parseInt(properties.getOrDefault(HIBERNATE_CACHE_ARCUS_DOMAIN_DATA_TTL, "0"));

        this.fallbackEnabled = Boolean.parseBoolean(properties.getOrDefault(HIBERNATE_CACHE_ARCUS_FALLBACK_ENABLED, "true"));
        this.initFallbackMode = Boolean.parseBoolean(properties.getOrDefault(HIBERNATE_CACHE_ARCUS_INIT_FALLBACK_MODE, "false"));
        this.healthCheckIntervalInSec = Integer.parseInt(properties.getOrDefault(HIBERNATE_CACHE_ARCUS_HEALTH_CHECK_INTERVAL_IN_SEC, "10"));
        this.properties = properties;
    }

    public ArcusClientPool createArcusClientPool() {
        log.info("Creating arcus client pool");
        ConnectionFactoryBuilder cfb = new ConnectionFactoryBuilder();
        cfb.setMaxReconnectDelay(Long.parseLong(properties.getOrDefault(HIBERNATE_CACHE_ARCUS_RECONNECT_INTERVAL_IN_SEC, "10000")));
        cfb.setOpTimeout(Long.parseLong(properties.getOrDefault(HIBERNATE_CACHE_ARCUS_OP_TIMEOUT, "10000")));
        return ArcusClient.createArcusClientPool(
                host,
                serviceCode,
                cfb,
                poolSize
        );
    }
}
