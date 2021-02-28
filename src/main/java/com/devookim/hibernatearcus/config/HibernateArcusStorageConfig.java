package com.devookim.hibernatearcus.config;

import java.util.Map;

public class HibernateArcusStorageConfig {

    public final boolean enableCacheEvictOnCachePut;

    public HibernateArcusStorageConfig(Map<String, String> properties) {
        enableCacheEvictOnCachePut = Boolean.parseBoolean(properties.getOrDefault("hibernate.cache.arcus.enableCacheEvictOnCachePut", "false"));
    }
}
