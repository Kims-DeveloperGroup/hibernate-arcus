package com.devookim.hibernatearcus.storage;

import com.devookim.hibernatearcus.client.HibernateArcusClientFactory;
import com.devookim.hibernatearcus.config.HibernateArcusStorageConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.hibernate.cache.cfg.spi.DomainDataRegionConfig;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.time.Duration;

public class ReadWriteAccessDomainDataStorageAccess extends DomainDataHibernateArcusStorageAccess {
    private final Cache<AbstractReadWriteAccess.SoftLockImpl, Object> readWriteAccessLocks;
    private HibernateArcusStorageConfig storageAccessConfig;

    public ReadWriteAccessDomainDataStorageAccess(HibernateArcusClientFactory arcusClientFactory, String regionName, HibernateArcusStorageConfig storageAccessConfig, DomainDataRegionConfig regionConfig) {
        super(arcusClientFactory, regionName, storageAccessConfig, regionConfig);
        this.storageAccessConfig = storageAccessConfig;
        readWriteAccessLocks = CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(10))
                .maximumSize(10000)
                .concurrencyLevel(1000)
                .build();
    }

    @Override
    public void putIntoCache(Object key, Object value, SharedSessionContractImplementor session) {
        if (storageAccessConfig.enableCacheEvictOnCachePut
                && value instanceof AbstractReadWriteAccess.SoftLockImpl
                && readWriteAccessLocks.getIfPresent(value) == null) {
            evictData(key);
            readWriteAccessLocks.put((AbstractReadWriteAccess.SoftLockImpl) value, key);
        }
        super.putIntoCache(key, value, session);
    }
}
