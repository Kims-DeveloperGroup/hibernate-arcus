package com.devookim.hibernatearcus.client;

import com.devookim.hibernatearcus.config.ArcusClientConfig;
import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.ArcusClientPool;
import net.spy.memcached.ConnectionObserver;

import java.net.SocketAddress;

@Slf4j
public class HibernateArcusClientFactory implements ConnectionObserver {
    private final ArcusClientPool clientPool;

    public HibernateArcusClientFactory(ArcusClientConfig clientConfig) {
        this.clientPool = clientConfig.getArcusClientPool();
    }

    public void shutdown() {
        log.info("ArcusClient shutdown");
    }

    public Long nextTimeStamp(long currentTimeStamp, String TIMESTAMP_KEY) {
        long timeStamp = (long) clientPool.get(TIMESTAMP_KEY);
        if (timeStamp < currentTimeStamp) {
            clientPool.set(TIMESTAMP_KEY, -1, currentTimeStamp);
        }
        return Math.max(timeStamp, currentTimeStamp);
    }

    public ArcusClientPool getClient() {
        return clientPool;
    }

    @Override
    public void connectionEstablished(SocketAddress sa, int reconnectCount) {
        log.info("Arcus client connection established. addr: {} reconnect: {}", sa, reconnectCount);
    }

    @Override
    public void connectionLost(SocketAddress sa) {
        log.info("Arcus client connection is lost. addr: {}", sa);

    }
}