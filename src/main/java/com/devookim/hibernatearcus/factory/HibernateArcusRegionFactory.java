package com.devookim.hibernatearcus.factory;

import com.devookim.hibernatearcus.client.HibernateArcusClientFactory;
import com.devookim.hibernatearcus.config.ArcusClientConfig;
import com.devookim.hibernatearcus.storage.DomainDataHibernateArcusStorageAccess;
import com.devookim.hibernatearcus.storage.HibernateArcusStorageAccess;
import com.devookim.hibernatearcus.storage.QueryCacheHibernateArcusStorageAccess;
import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.ArcusClientPool;
import org.hibernate.boot.registry.selector.spi.StrategySelector;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.cfg.spi.DomainDataRegionBuildingContext;
import org.hibernate.cache.cfg.spi.DomainDataRegionConfig;
import org.hibernate.cache.spi.CacheKeysFactory;
import org.hibernate.cache.spi.DomainDataRegion;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.support.DomainDataRegionImpl;
import org.hibernate.cache.spi.support.DomainDataStorageAccess;
import org.hibernate.cache.spi.support.RegionFactoryTemplate;
import org.hibernate.cache.spi.support.StorageAccess;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Map;

@Slf4j
public class HibernateArcusRegionFactory extends RegionFactoryTemplate {
    
    private HibernateArcusClientFactory hibernateArcusClientFactory;
    private CacheKeysFactory cacheKeysFactory;
    protected boolean fallback;

    @Autowired
    private ArcusClientConfig arcusClientConfig;

    @PostConstruct
    public void postConstruct() {
        log.debug("HibernateArcusRegionFactory is initialized");
    }

    @Override
    protected CacheKeysFactory getImplicitCacheKeysFactory() {
        return cacheKeysFactory;
    }

    @Override
    protected void prepareForUse(SessionFactoryOptions settings, Map properties) throws CacheException {
        if (arcusClientConfig == null) {
            arcusClientConfig = new ArcusClientConfig(
                    properties.getOrDefault("hibernate.cache.arcus.host", "localhost:2181").toString(),
                    properties.getOrDefault("hibernate.cache.arcus.serviceCode", "").toString(),
                    Integer.parseInt(properties.getOrDefault("hibernate.cache.arcus.poolSize", 1).toString())
            );
        }
        this.hibernateArcusClientFactory = new HibernateArcusClientFactory(this.arcusClientConfig);

        StrategySelector selector = settings.getServiceRegistry().getService(StrategySelector.class);
        cacheKeysFactory = selector.resolveDefaultableStrategy(CacheKeysFactory.class,
                properties.get(Environment.CACHE_KEYS_FACTORY), new HibernateArcusCacheKeysFactory());
    }

    @Override
    protected void releaseFromUse() {
        hibernateArcusClientFactory.shutdown();
    }

    @Override
    public boolean isMinimalPutsEnabledByDefault() {
        return true;
    }

    @Override
    public AccessType getDefaultAccessType() {
        return AccessType.TRANSACTIONAL;
    }

    @Override
    public long nextTimestamp() {
        long currentTimeStamp = System.currentTimeMillis() << 12;
        try {
            return hibernateArcusClientFactory.nextTimeStamp(currentTimeStamp, "TIME_STAMP");
        } catch (Exception e) {
            if (fallback) {
                return super.nextTimestamp();
            }
            throw e;
        }
    }

    @Override
    public DomainDataRegion buildDomainDataRegion(
            DomainDataRegionConfig regionConfig,
            DomainDataRegionBuildingContext buildingContext) {
        verifyStarted();
        return new DomainDataRegionImpl(
                regionConfig,
                this,
                createDomainDataStorageAccess( regionConfig, buildingContext ),
                getImplicitCacheKeysFactory(),
                buildingContext
        );
    }

    @Override
    protected DomainDataStorageAccess createDomainDataStorageAccess(DomainDataRegionConfig regionConfig, DomainDataRegionBuildingContext buildingContext) {
        ArcusClientPool cacheClientPool = getCache(qualify(regionConfig.getRegionName()));
        return new DomainDataHibernateArcusStorageAccess(cacheClientPool,  qualify(regionConfig.getRegionName()));
    }

    @Override
    public String qualify(String regionName) {
        return super.qualify(regionName).replace("#", "_");
    }

    @Override
    protected StorageAccess createQueryResultsRegionStorageAccess(String regionName, SessionFactoryImplementor sessionFactory) {
        ArcusClientPool cacheClientPool = getCache(qualify(regionName));
        return new QueryCacheHibernateArcusStorageAccess(cacheClientPool, qualify(regionName));
    }

    @Override
    protected StorageAccess createTimestampsRegionStorageAccess(String regionName, SessionFactoryImplementor sessionFactory) {
        ArcusClientPool cacheClientPool = getCache(qualify(regionName));
        return new HibernateArcusStorageAccess(cacheClientPool, qualify(regionName));
    }

    protected ArcusClientPool getCache(String regionName) {
        log.debug("getCache region: {}", regionName);
        return hibernateArcusClientFactory.getClient();
    }
}
