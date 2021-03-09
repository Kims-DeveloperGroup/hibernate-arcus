package com.devookim.hibernatearcus.storage;

import com.devookim.hibernatearcus.client.HibernateArcusClientFactory;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.support.DomainDataStorageAccess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.slf4j.Logger;

public class HibernateArcusStorageAccess implements DomainDataStorageAccess {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(HibernateArcusStorageAccess.class);
    private HibernateArcusClientFactory arcusClientFactory;
    protected final String CACHE_REGION;

    public HibernateArcusStorageAccess(HibernateArcusClientFactory arcusClientFactory, String prefix) {
        super();
        this.arcusClientFactory = arcusClientFactory;
        this.CACHE_REGION = prefix;
    }

    protected String generateKey(Object key) {
        return (CACHE_REGION + ":" + key).replace("$", "");
    }

    @Override
    public Object getFromCache(Object key, SharedSessionContractImplementor session) {
        return getFromCache(key);
    }

    protected Object getFromCache(Object key) {
        if (arcusClientFactory.isFallbackModeOn()) {
            log.info("Fallback is on key: {}", key);
            return null;
        }

        String generatedKey = generateKey(key);
        try {
            Object o = arcusClientFactory.getClientPool().get(generatedKey);
            log.trace("get key:{} value: {}", generatedKey, o);
            return o;
        } catch (Exception e) {
            log.error("fallbackEnabled: {} key: {} errorMsg: {}", arcusClientFactory.fallbackEnabled, generatedKey, e.getMessage());
            if (arcusClientFactory.fallbackEnabled) {
                return null;
            }
            throw new CacheException(e);
        }
    }

    @Override
    public void putIntoCache(Object key, Object value, SharedSessionContractImplementor session) {
        if (arcusClientFactory.isFallbackModeOn()) {
            log.info("Fallback is on key: {}", key);
            return;
        }

        String generatedKey = generateKey(key);
        try {
            arcusClientFactory.getClientPool().set(generatedKey, 0, value).get();
            log.trace("put key:{} value: {}", generatedKey, value);
        } catch (Exception e) {
            log.error("fallbackEnabled: {} key: {} errorMsg: {}", arcusClientFactory.fallbackEnabled, generatedKey, e.getMessage());
            if (arcusClientFactory.fallbackEnabled) {
                return;
            }
            throw new CacheException(e);
        }
    }

    @Override
    public boolean contains(Object key) {
        if (arcusClientFactory.isFallbackModeOn()) {
            log.info("Fallback is on key: {}", key);
            return false;
        }

        String generatedKey = generateKey(key);
        try {
            Object result = arcusClientFactory.getClientPool().get(generatedKey);
            log.trace("containKey for {} contains: {}", generatedKey, result != null);
            return result != null;
        } catch (Exception e) {
            log.error("fallbackEnabled: {} key: {} errorMsg: {}", arcusClientFactory.fallbackEnabled, generatedKey, e.getMessage());
            if (arcusClientFactory.fallbackEnabled) {
                return false;
            }
            throw new CacheException(e);
        }
    }

    @Override
    public void evictData() {
        if (arcusClientFactory.isFallbackModeOn()) {
            log.info("Fallback is on");
            return;
        }
        try {
            log.info("cacheEvict for {}", CACHE_REGION);
        } catch (Exception e) {
            log.error("fallbackEnabled: {} region: {} errorMsg: {}", arcusClientFactory.fallbackEnabled, CACHE_REGION, e.getMessage());
            if (arcusClientFactory.fallbackEnabled) {
                return;
            }
            throw new CacheException(e);
        }
    }

    @Override
    public void evictData(Object key) {
        if (arcusClientFactory.isFallbackModeOn()) {
            log.info("Fallback is on key: {}", key);
            return;
        }
        String generatedKey = generateKey(key);
        try {
            log.info("cacheEvict for {}", generatedKey);
            arcusClientFactory.getClientPool().delete(generatedKey).get();
        } catch (Exception e) {
            log.error("fallbackEnabled: {} key: {} errorMsg: {}", arcusClientFactory.fallbackEnabled, generatedKey, e.getMessage());
            if (arcusClientFactory.fallbackEnabled) {
                return;
            }
            throw new CacheException(e);
        }
    }
}
