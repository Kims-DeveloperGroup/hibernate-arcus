package com.devookim.hibernatearcus.client;

import com.devookim.hibernatearcus.config.ArcusClientConfig;
import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.ArcusClientPool;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class HibernateArcusClientFactory {
    public final boolean fallbackEnabled;
    private final ArcusClientPool clientPool;
    private final AtomicBoolean fallbackMode;

    public HibernateArcusClientFactory(ArcusClientConfig clientConfig) {
        this.clientPool = clientConfig.createArcusClientPool();
        fallbackEnabled = clientConfig.fallbackEnabled;
        fallbackMode = new AtomicBoolean(clientConfig.initFallbackMode);
        healthCheckArcusCluster();
    }

    public void shutdown() {
        clientPool.shutdown();
        log.info("ArcusClient shutdown");
    }

    public ArcusClientPool getClientPool() {
        return clientPool;
    }

    public void setFallbackMode(boolean onAndOff) {
        if(fallbackMode.compareAndSet(!onAndOff, onAndOff)) {
            log.info("Fallback mode changed: {}", onAndOff);
        }
    }

    public boolean isFallbackModeOn() {
        return fallbackMode.get();
    }

    public void healthCheckArcusCluster() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            log.trace("ping...");
            Map<SocketAddress, Map<String, String>> stats = clientPool.getStats();
            if(stats.size() == 0 && fallbackEnabled) {
                setFallbackMode(true);
                return;
            }

            Collection<Map<String, String>> nodes = stats.values();
            for (Map<String, String> node: nodes) {
                if (node.containsKey("zk_connected")
                        && Boolean.parseBoolean(node.get("zk_connected"))) {
                    setFallbackMode(false);
                    return;
                }
            }

            if (fallbackEnabled) {
                setFallbackMode(true);
            }
        }, 10, 10, TimeUnit.SECONDS);
    }
}