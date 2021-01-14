package com.devookim.hibernatearcus.factory;

import com.devookim.hibernatearcus.client.HibernateArcusClientFactory;
import com.devookim.hibernatearcus.config.ArcusClientConfig;
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
import org.hibernate.cache.spi.support.*;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import javax.annotation.PostConstruct;
import java.util.Map;

@Slf4j
public class HibernateArcusRegionFactory extends RegionFactoryTemplate {
    
    private HibernateArcusClientFactory hibernateArcusClientFactory;
    private CacheKeysFactory cacheKeysFactory;
    protected boolean fallback;

    @PostConstruct
    public void postConstruct() {
        log.debug("HibernateArcusRegionFactory is initialized");
    }

    @Override
    protected CacheKeysFactory getImplicitCacheKeysFactory() {
        return cacheKeysFactory;
    }

    @Override
    protected void prepareForUse(SessionFactoryOptions settings, @SuppressWarnings("rawtypes") Map properties) throws CacheException {
        this.hibernateArcusClientFactory = createArcus(properties);

        fallback = true;

        StrategySelector selector = settings.getServiceRegistry().getService(StrategySelector.class);
        cacheKeysFactory = selector.resolveDefaultableStrategy(CacheKeysFactory.class, 
                properties.get(Environment.CACHE_KEYS_FACTORY), new HibernateArcusCacheKeysFactory());
    }

    protected HibernateArcusClientFactory createArcus(Map<Object, Object> properties) {
        return new HibernateArcusClientFactory(new ArcusClientConfig("", "service-code", 1));
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
        ArcusClientPool cacheClientPool = getCache(qualifyName(regionConfig.getRegionName()));
        return new HibernateArcusStorageAccess(cacheClientPool, regionConfig.getRegionName());
    }

    private String qualifyName(String name) {
        return RegionNameQualifier.INSTANCE.qualify(name, getOptions());
    }

    @Override
    protected StorageAccess createQueryResultsRegionStorageAccess(String regionName, SessionFactoryImplementor sessionFactory) {
        ArcusClientPool cacheClientPool = getCache(qualifyName(regionName));
        return new HibernateArcusStorageAccess(cacheClientPool,regionName);
    }

    @Override
    protected StorageAccess createTimestampsRegionStorageAccess(String regionName, SessionFactoryImplementor sessionFactory) {
        ArcusClientPool cacheClientPool = getCache(qualifyName(regionName));
        return new HibernateArcusStorageAccess(cacheClientPool, regionName);
    }

    protected ArcusClientPool getCache(String regionName) {
        log.debug("getCache region: {}", regionName);
        return hibernateArcusClientFactory.getClient();
    }
}
