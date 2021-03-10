package com.devookim.hibernatearcus.storage;

import com.devookim.hibernatearcus.client.HibernateArcusClientFactory;
import com.devookim.hibernatearcus.config.HibernateArcusStorageConfig;
import com.devookim.hibernatearcus.factory.HibernateArcusCacheKeysFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cache.cfg.spi.DomainDataRegionConfig;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.resource.transaction.spi.TransactionStatus;

@Slf4j
public class ReadWriteAccessDomainDataStorageAccess extends DomainDataHibernateArcusStorageAccess {
    private final HibernateArcusStorageConfig storageAccessConfig;

    public ReadWriteAccessDomainDataStorageAccess(HibernateArcusClientFactory arcusClientFactory, String regionName, HibernateArcusStorageConfig storageAccessConfig, DomainDataRegionConfig regionConfig) {
        super(arcusClientFactory, regionName, storageAccessConfig, regionConfig);
        this.storageAccessConfig = storageAccessConfig;
    }

    @Override
    public void putIntoCache(Object key, Object value, SharedSessionContractImplementor session) {
        if (storageAccessConfig.cacheEvictionRegionGroupOnCacheUpdate.contains(CACHE_REGION)
                && value instanceof SoftLock
                && isTransactionActive(session)) {
            log.debug("enableCacheEvictOnCachePut enabled. key: {}", key);
            evictData(key);
        }
        super.putIntoCache(key, value, session);
    }

    @Override
    public void evictData(Object key) {
        if (storageAccessConfig.cacheEvictionRegionGroupOnCacheUpdate.contains(CACHE_REGION)) {
            String id = key.toString().split("#")[1];
            log.debug("regionGroupOnCacheEvict contains region: {}, id: {}", CACHE_REGION, id);
            domainDataStorageAccesses.forEach((region, storageAccess) -> {
                if (!region.equals(CACHE_REGION)) {
                    storageAccess.evictDataOnRegionGroupCacheEvict(new HibernateArcusCacheKeysFactory.EntityKey(storageAccess.entityClassName, id));
                }
            });
        } else {
            super.evictData(key);
        }
    }

    @Override
    public void evictDataOnRegionGroupCacheEvict(Object key) {
        if (getFromCache(key) instanceof SoftLock) {
            return;
        }
        super.evictDataOnRegionGroupCacheEvict(key);
    }

    private boolean isTransactionActive(SharedSessionContractImplementor session) {
        return session.getTransaction() != null
                && session.getTransaction().getStatus().equals(TransactionStatus.ACTIVE);
    }
}
