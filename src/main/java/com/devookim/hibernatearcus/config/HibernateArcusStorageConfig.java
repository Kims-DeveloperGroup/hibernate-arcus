package com.devookim.hibernatearcus.config;

import java.util.Map;

public class HibernateArcusStorageConfig {

    public final boolean cacheEvictOnCachePut;

    public HibernateArcusStorageConfig(Map<String, String> properties) {
        cacheEvictOnCachePut = Boolean.parseBoolean(properties.getOrDefault("hibernate.cache.arcus.cacheEvictOnCachePut", "false"));
    }
}
