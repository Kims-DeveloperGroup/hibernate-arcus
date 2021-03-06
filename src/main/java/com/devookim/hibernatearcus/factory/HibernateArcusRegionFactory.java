package com.devookim.hibernatearcus.factory;

import com.devookim.hibernatearcus.client.HibernateArcusClientFactory;
import com.devookim.hibernatearcus.config.ArcusClientConfig;
import com.devookim.hibernatearcus.config.HibernateArcusStorageConfig;
import com.devookim.hibernatearcus.storage.DomainDataHibernateArcusStorageAccess;
import com.devookim.hibernatearcus.storage.HibernateArcusStorageAccess;
import com.devookim.hibernatearcus.storage.QueryCacheHibernateArcusStorageAccess;
import com.devookim.hibernatearcus.storage.ReadWriteAccessDomainDataStorageAccess;
import lombok.extern.slf4j.Slf4j;
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

import javax.annotation.PostConstruct;
import java.util.Map;

@Slf4j
public class HibernateArcusRegionFactory extends RegionFactoryTemplate {
    
    private HibernateArcusClientFactory hibernateArcusClientFactory;
    private CacheKeysFactory cacheKeysFactory;
    private HibernateArcusStorageConfig storageConfig;

    @PostConstruct
    public void postConstruct() {
        log.info("HibernateArcusRegionFactory is initialized");
    }

    @Override
    protected CacheKeysFactory getImplicitCacheKeysFactory() {
        return cacheKeysFactory;
    }

    @Override
    protected void prepareForUse(SessionFactoryOptions settings, Map properties) throws CacheException {

        ArcusClientConfig arcusClientConfig = new ArcusClientConfig(properties);
        storageConfig = new HibernateArcusStorageConfig(properties);
        this.hibernateArcusClientFactory = new HibernateArcusClientFactory(arcusClientConfig);
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
        if (regionConfig.getEntityCaching().size() > 0 && regionConfig.getEntityCaching().get(0).getAccessType().equals(AccessType.READ_WRITE)) {
            return new ReadWriteAccessDomainDataStorageAccess(getClientFactory(qualify(regionConfig.getRegionName())),
                    qualify(regionConfig.getRegionName()),
                    storageConfig,
                    regionConfig);
        }
        return new DomainDataHibernateArcusStorageAccess(getClientFactory(qualify(regionConfig.getRegionName())),
                qualify(regionConfig.getRegionName()),
                storageConfig,
                regionConfig);
    }

    @Override
    public String qualify(String regionName) {
        return super.qualify(regionName).replace("#", "_");
    }

    @Override
    protected StorageAccess createQueryResultsRegionStorageAccess(String regionName, SessionFactoryImplementor sessionFactory) {
        return new QueryCacheHibernateArcusStorageAccess(getClientFactory(qualify(regionName)), qualify(regionName));
    }

    @Override
    protected StorageAccess createTimestampsRegionStorageAccess(String regionName, SessionFactoryImplementor sessionFactory) {
        return new HibernateArcusStorageAccess(getClientFactory(qualify(regionName)), qualify(regionName));
    }

    protected HibernateArcusClientFactory getClientFactory(String regionName) {
        log.debug("getCache region: {}", regionName);
        return hibernateArcusClientFactory;
    }
}
