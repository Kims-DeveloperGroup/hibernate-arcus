package com.devookim.hibernatearcus.client;

import com.devookim.hibernatearcus.config.ArcusClientConfig;
import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.ArcusClientPool;
import net.spy.memcached.ConnectionObserver;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class HibernateArcusClientFactory implements ConnectionObserver {
    public final boolean fallbackEnabled;
    private final ArcusClientPool clientPool;
    private final ArcusClientConfig clientConfig;
    private final AtomicBoolean fallbackMode;

    public HibernateArcusClientFactory(ArcusClientConfig clientConfig) {
        this.clientPool = clientConfig.createArcusClientPool(this);
        this.clientConfig = clientConfig;
        fallbackEnabled = clientConfig.fallbackEnabled;
        fallbackMode = new AtomicBoolean(clientConfig.initFallbackMode);
    }

    public void shutdown() {
        clientPool.shutdown();
        log.info("ArcusClient shutdown");
    }

    public ArcusClientPool getClientPool() {
        return clientPool;
    }

    public boolean isFallbackModeOn() {
        return fallbackMode.get();
    }

    @Override
    public void connectionEstablished(SocketAddress sa, int reconnectCount) {
        log.info("Arcus client connection established. addr: {} reconnect: {}", sa, reconnectCount);
        fallbackMode.set(false);
    }

    @Override
    public void connectionLost(SocketAddress sa) {
        log.error("Arcus client connection is lost. addr: {}", sa);
        if (clientConfig.fallbackEnabled) {
            log.info("fallback mode on.");
            fallbackMode.set(true);
        }
    }
}