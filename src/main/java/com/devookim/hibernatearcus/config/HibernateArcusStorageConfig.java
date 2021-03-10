package com.devookim.hibernatearcus.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

public class HibernateArcusStorageConfig {

    public final HashSet<String> cacheEvictionRegionGroupOnCacheUpdate;

    public HibernateArcusStorageConfig(Map<String, String> properties) {
        cacheEvictionRegionGroupOnCacheUpdate = new HashSet<>();
        Collections.addAll(cacheEvictionRegionGroupOnCacheUpdate, properties.getOrDefault("hibernate.cache.arcus.regionGroupOnCacheEvict", "").split(","));
    }
}
