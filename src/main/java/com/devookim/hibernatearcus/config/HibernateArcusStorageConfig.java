package com.devookim.hibernatearcus.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

public class HibernateArcusStorageConfig {

    public final boolean enableCacheEvictOnCachePut;
    public final HashSet<String> regionGroupOnCacheEvict;

    public HibernateArcusStorageConfig(Map<String, String> properties) {
        enableCacheEvictOnCachePut = Boolean.parseBoolean(properties.getOrDefault("hibernate.cache.arcus.enableCacheEvictOnCachePut", "false"));
        regionGroupOnCacheEvict = new HashSet<>();
        Collections.addAll(regionGroupOnCacheEvict, properties.getOrDefault("hibernate.cache.arcus.regionGroupOnCacheEvict", "").split(","));
    }
}
