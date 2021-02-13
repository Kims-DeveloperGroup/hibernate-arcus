package com.devookim.hibernatearcus.client;

import com.devookim.hibernatearcus.config.ArcusClientConfig;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HibernateArcusClientFactoryTest {

    @Test
    public void testHealthCheckArcusCluster_whenFallbackModeIsOn_andWhenArcusClusterIsAvailable_thenFallbackModeTurnsOffAfterHealthCheck() throws InterruptedException {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("hibernate.cache.arcus.host", "localhost:12181");
        properties.put("hibernate.cache.arcus.serviceCode", "test");
        properties.put("hibernate.cache.arcus.initFallbackMode", "true");
        properties.put("hibernate.cache.arcus.healthCheckIntervalInSec", "1");
        HibernateArcusClientFactory sut = new HibernateArcusClientFactory(new ArcusClientConfig(properties));
        assertTrue(sut.isFallbackModeOn());
        
        Thread.sleep(1500);

        assertFalse(sut.isFallbackModeOn());
    }

    @Test
    public void testHealthCheckArcusCluster_whenFallbackModeIsOn_andWhenArcusClusterIsAvailable_butFallbackEnabledIsFalse_thenFallbackModeShouldStayOn() throws InterruptedException {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("hibernate.cache.arcus.host", "localhost:12181");
        properties.put("hibernate.cache.arcus.serviceCode", "test");
        properties.put("hibernate.cache.arcus.initFallbackMode", "true");
        properties.put("hibernate.cache.arcus.fallbackEnabled", "false");

        properties.put("hibernate.cache.arcus.healthCheckIntervalInSec", "1");
        HibernateArcusClientFactory sut = new HibernateArcusClientFactory(new ArcusClientConfig(properties));
        assertTrue(sut.isFallbackModeOn());

        Thread.sleep(1500);

        assertTrue(sut.isFallbackModeOn());
    }
}