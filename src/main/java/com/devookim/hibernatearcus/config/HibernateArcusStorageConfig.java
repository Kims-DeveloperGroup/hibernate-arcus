package com.devookim.hibernatearcus.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import static com.devookim.hibernatearcus.config.HibernateArcusProperties.HIBERNATE_CACHE_ARCUS_DOMAIN_DATA_TTL;
import static com.devookim.hibernatearcus.config.HibernateArcusProperties.HIBERNATE_CACHE_ARCUS_EVICTION_REGION_GROUP_ON_CACHE_UPDATE;

public class HibernateArcusStorageConfig {

    public final HashSet<String> evictionRegionGroupOnCacheUpdate;
    public final int domainDataTTL;

    public HibernateArcusStorageConfig(Map<String, String> properties) {
        evictionRegionGroupOnCacheUpdate = new HashSet<>();
        domainDataTTL = Integer.parseInt(properties.getOrDefault(HIBERNATE_CACHE_ARCUS_DOMAIN_DATA_TTL, "0"));
        Collections.addAll(evictionRegionGroupOnCacheUpdate, properties.getOrDefault(HIBERNATE_CACHE_ARCUS_EVICTION_REGION_GROUP_ON_CACHE_UPDATE, "").split(","));
    }
}
