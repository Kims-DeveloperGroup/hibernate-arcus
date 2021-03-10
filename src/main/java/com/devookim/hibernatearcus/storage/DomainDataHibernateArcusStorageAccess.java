package com.devookim.hibernatearcus.storage;

import com.devookim.hibernatearcus.client.HibernateArcusClientFactory;
import com.devookim.hibernatearcus.config.HibernateArcusStorageConfig;
import com.devookim.hibernatearcus.config.RegionConfigUtil;
import com.devookim.hibernatearcus.factory.HibernateArcusCacheKeysFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cache.cfg.spi.DomainDataRegionConfig;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.util.HashMap;

@Slf4j
public class DomainDataHibernateArcusStorageAccess extends HibernateArcusStorageAccess {
    protected static final HashMap<String, DomainDataHibernateArcusStorageAccess> domainDataStorageAccesses = new HashMap<>();

    private final HibernateArcusStorageConfig storageAccessConfig;
    public final String entityClassName;
    private final AccessType accessType;

    public DomainDataHibernateArcusStorageAccess(HibernateArcusClientFactory arcusClientFactory,
                                                 String regionName,
                                                 HibernateArcusStorageConfig storageAccessConfig,
                                                 DomainDataRegionConfig regionConfig) {
        super(arcusClientFactory, regionName);
        this.storageAccessConfig = storageAccessConfig;
        this.entityClassName = RegionConfigUtil.getEntityClassName(regionConfig);
        this.accessType = RegionConfigUtil.getAccessTypeOfEntityCaching(regionConfig);
        domainDataStorageAccesses.put(regionName, this);
    }

    @Override
    public Object getFromCache(Object key, SharedSessionContractImplementor session) {
        Object o = super.getFromCache(key, session);
        if (o == null) {
            log.debug("cacheMiss key: {}", generateKey(key));
        } else if (o instanceof AbstractReadWriteAccess.SoftLockImpl){
            log.trace("cache is locked: key: {} lock: {}", key, o);
        } else {
            log.debug("cacheHit key: {} value: {}", generateKey(key), o);
        }
        return o;
    }

    @Override
    public void putIntoCache(Object key, Object value, SharedSessionContractImplementor session) {
        if (accessType != AccessType.READ_WRITE
                && storageAccessConfig.cacheEvictionRegionGroupOnCacheUpdate.contains(CACHE_REGION)
                && contains(key)) {
            log.debug("enableCacheEvictOnCachePut enabled. key: {}", key);
            evictData(key);
        }

        super.putIntoCache(key, value, session);
        if (value instanceof AbstractReadWriteAccess.SoftLockImpl) {
            log.trace("cacheLock key: {} lock: {}", generateKey(key), value);
        } else {
            log.debug("cachePut key: {} value: {}", generateKey(key), value);
        }
    }

    @Override
    public void evictData(Object key) {
        if (storageAccessConfig.cacheEvictionRegionGroupOnCacheUpdate.contains(CACHE_REGION)) {
            String id = key.toString().split("#")[1];
            log.debug("regionGroupOnCacheEvict contains region: {}, id: {}", CACHE_REGION, id);
            domainDataStorageAccesses.forEach((region, storageAccess) ->
                    storageAccess.evictDataOnRegionGroupCacheEvict(new HibernateArcusCacheKeysFactory.EntityKey(storageAccess.entityClassName, id)));
        } else {
            super.evictData(key);
        }
    }

    public void evictDataOnRegionGroupCacheEvict(Object key) {
        if (storageAccessConfig.cacheEvictionRegionGroupOnCacheUpdate.contains(CACHE_REGION)) {
            log.debug("cacheEvict {} by regionGroupOnCacheEvict", key);
            super.evictData(key);
        }
    }
}
