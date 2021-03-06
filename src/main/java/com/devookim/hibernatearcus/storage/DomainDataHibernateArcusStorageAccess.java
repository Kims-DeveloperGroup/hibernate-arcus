package com.devookim.hibernatearcus.storage;

import com.devookim.hibernatearcus.client.HibernateArcusClientFactory;
import com.devookim.hibernatearcus.config.HibernateArcusStorageConfig;
import com.devookim.hibernatearcus.factory.HibernateArcusCacheKeysFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cache.cfg.spi.DomainDataRegionConfig;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.time.Duration;
import java.util.HashMap;

@Slf4j
public class DomainDataHibernateArcusStorageAccess extends HibernateArcusStorageAccess {
    private static final HashMap<String, DomainDataHibernateArcusStorageAccess> domainDataStorageAccesses = new HashMap<>();

    private final HibernateArcusStorageConfig storageAccessConfig;
    public final String entityClassName;
    private final boolean isEntityCachingStorage;
    private final AccessType accessType;
    private final Cache<AbstractReadWriteAccess.SoftLockImpl, Object> readWriteAccessLocks;

    public DomainDataHibernateArcusStorageAccess(HibernateArcusClientFactory arcusClientFactory,
                                                 String regionName,
                                                 HibernateArcusStorageConfig storageAccessConfig,
                                                 DomainDataRegionConfig regionConfig) {
        super(arcusClientFactory, regionName);
        this.storageAccessConfig = storageAccessConfig;
        isEntityCachingStorage = regionConfig.getEntityCaching().size() > 0;
        this.entityClassName = isEntityCachingStorage ? regionConfig.getEntityCaching().get(0).getNavigableRole().getNavigableName() : "";
        this.accessType = isEntityCachingStorage ? regionConfig.getEntityCaching().get(0).getAccessType() : null;
        domainDataStorageAccesses.put(regionName, this);
        readWriteAccessLocks = CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(10))
                .concurrencyLevel(1000)
                .build();

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
                && storageAccessConfig.enableCacheEvictOnCachePut
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
        if (storageAccessConfig.regionGroupOnCacheEvict.contains(super.CACHE_REGION) && storageAccessConfig.enableCacheEvictOnCachePut) {
            String id = key.toString().split("#")[1];
            log.debug("regionGroupOnCacheEvict contains region: {}, id: {}", CACHE_REGION, id);
            domainDataStorageAccesses.forEach((s, storageAccess) -> {
                storageAccess.evictDataOnRegionGroupCacheEvict(new HibernateArcusCacheKeysFactory.EntityKey(storageAccess.entityClassName, id));
            });
        } else {
            super.evictData(key);
        }
    }

    public void evictDataOnRegionGroupCacheEvict(Object key) {
        if (storageAccessConfig.regionGroupOnCacheEvict.contains(super.CACHE_REGION)) {
            log.debug("cacheEvict {} by regionGroupOnCacheEvict", key);
            super.evictData(key);
        }
    }
}
