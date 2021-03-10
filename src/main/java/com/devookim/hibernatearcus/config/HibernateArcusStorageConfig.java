package com.devookim.hibernatearcus.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

public class HibernateArcusStorageConfig {

    public final HashSet<String> evictionRegionGroupOnCacheUpdate;

    public HibernateArcusStorageConfig(Map<String, String> properties) {
        evictionRegionGroupOnCacheUpdate = new HashSet<>();
        Collections.addAll(evictionRegionGroupOnCacheUpdate, properties.getOrDefault(HibernateArcusProperties.HIBERNATE_CACHE_ARCUS_EVICTION_REGION_GROUP_ON_CACHE_UPDATE, "").split(","));
    }
}
