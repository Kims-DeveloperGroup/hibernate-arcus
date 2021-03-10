package com.devookim.hibernatearcus.client;

import com.devookim.hibernatearcus.config.ArcusClientConfig;
import org.junit.Test;

import java.util.HashMap;

import static com.devookim.hibernatearcus.config.HibernateArcusProperties.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HibernateArcusClientFactoryTest {

    @Test
    public void testHealthCheckArcusCluster_whenFallbackModeIsOn_andWhenArcusClusterIsAvailable_thenFallbackModeTurnsOffAfterHealthCheck() throws InterruptedException {
        HashMap<String, String> properties = new HashMap<>();
        properties.put(HIBERNATE_CACHE_ARCUS_HOST, "localhost:12181");
        properties.put(HIBERNATE_CACHE_ARCUS_SERVICE_CODE, "test");
        properties.put(HIBERNATE_CACHE_ARCUS_INIT_FALLBACK_MODE, "true");
        properties.put(HIBERNATE_CACHE_ARCUS_HEALTH_CHECK_INTERVAL_IN_SEC, "1");
        HibernateArcusClientFactory sut = new HibernateArcusClientFactory(new ArcusClientConfig(properties));
        assertTrue(sut.isFallbackModeOn());

        Thread.sleep(1500);

        assertFalse(sut.isFallbackModeOn());
    }

    @Test
    public void testHealthCheckArcusCluster_whenFallbackModeIsOn_andWhenArcusClusterIsAvailable_butFallbackEnabledIsFalse_thenFallbackModeShouldStayOn() throws InterruptedException {
        HashMap<String, String> properties = new HashMap<>();
        properties.put(HIBERNATE_CACHE_ARCUS_HOST, "localhost:12181");
        properties.put(HIBERNATE_CACHE_ARCUS_SERVICE_CODE, "test");
        properties.put(HIBERNATE_CACHE_ARCUS_INIT_FALLBACK_MODE, "true");
        properties.put(HIBERNATE_CACHE_ARCUS_FALLBACK_ENABLED, "false");

        properties.put(HIBERNATE_CACHE_ARCUS_HEALTH_CHECK_INTERVAL_IN_SEC, "1");
        HibernateArcusClientFactory sut = new HibernateArcusClientFactory(new ArcusClientConfig(properties));
        assertTrue(sut.isFallbackModeOn());

        Thread.sleep(1500);

        assertTrue(sut.isFallbackModeOn());
    }
}
