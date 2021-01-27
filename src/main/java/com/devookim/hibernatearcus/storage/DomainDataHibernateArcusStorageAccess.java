package com.devookim.hibernatearcus.storage;

import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.ArcusClientPool;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

@Slf4j
public class DomainDataHibernateArcusStorageAccess extends HibernateArcusStorageAccess {
    public DomainDataHibernateArcusStorageAccess(ArcusClientPool arcusClientPool, String regionName) {
        super(arcusClientPool, regionName);
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
        super.putIntoCache(key, value, session);
        if (value instanceof AbstractReadWriteAccess.SoftLockImpl) {
            log.trace("cacheLock key: {} lock: {}", generateKey(key), value);
        } else {
            log.debug("cachePut key: {} value: {}", generateKey(key), value);
        }
    }
}