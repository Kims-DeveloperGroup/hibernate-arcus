package com.devookim.hibernatearcus.factory;

import net.spy.memcached.ArcusClientPool;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.support.DomainDataStorageAccess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.slf4j.Logger;

public class HibernateArcusStorageAccess implements DomainDataStorageAccess {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(HibernateArcusStorageAccess.class);
    private ArcusClientPool cacheClientPool;
    protected final String CACHE_REGION;
    boolean fallback;
    volatile boolean fallbackMode;

    public HibernateArcusStorageAccess(ArcusClientPool arcusClientPool, String prefix) {
        super();
        this.cacheClientPool = arcusClientPool;
        this.CACHE_REGION = prefix;
        fallback = false;
    }

    protected String generateKey(Object key) {
        return (CACHE_REGION + ":" + key).replace("$", "");
    }

    @Override
    public Object getFromCache(Object key, SharedSessionContractImplementor session) {
        String generatedKey = generateKey(key);
        try {
            Object o = cacheClientPool.get(generatedKey);
            if (o == null) {
                log.debug("cacheMiss for {}", generatedKey);
            } else {
                log.debug("cacheHit for {}", generatedKey);
            }
            return o;
        } catch (Exception e) {
            log.error("key: {}", generatedKey, e);
            if (fallback) {
                return null;
            }
            throw new CacheException(e);
        }
    }

    @Override
    public void putIntoCache(Object key, Object value, SharedSessionContractImplementor session) {
        String generatedKey = generateKey(key);
        try {
            cacheClientPool.set(generatedKey, 0, value).get();
            log.debug("cachePut for key: {} value: {}", generatedKey, value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (fallback) {
                return;
            }
            throw new CacheException(e);
        }
    }

    @Override
    public boolean contains(Object key) {
        if (fallbackMode) {
            return false;
        }
        String generatedKey = generateKey(key);
        try {
            Object result = cacheClientPool.get(generatedKey);
            log.info("containKey for {} contains: {}", generatedKey, result != null);
            return result != null;
        } catch (Exception e) {
            log.error("key: {}", generatedKey, e);
            if (fallback) {
                return false;
            }
            throw new CacheException(e);
        }
    }

    @Override
    public void evictData() {
        if (fallbackMode) {
            return;
        }
        try {
            log.info("cacheEvict for {}", CACHE_REGION);
        } catch (Exception e) {
            log.error("key: {}", CACHE_REGION, e);
            if (fallback) {
                return;
            }
            throw new CacheException(e);
        }
    }

    @Override
    public void evictData(Object key) {
        if (fallbackMode) {
            return;
        }
        String generatedKey = generateKey(key);
        try {
            log.info("cacheEvict for {}", generatedKey);
            cacheClientPool.delete(generatedKey).get();
        } catch (Exception e) {
            log.error("key: {}", generatedKey, e);
            if (fallback) {
                return;
            }
            throw new CacheException(e);
        }
    }

    @Override
    public void release() {
        try {
            this.cacheClientPool = null;
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }
}
