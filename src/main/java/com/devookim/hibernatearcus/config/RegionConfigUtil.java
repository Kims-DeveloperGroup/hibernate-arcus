package com.devookim.hibernatearcus.config;

import org.hibernate.cache.cfg.spi.DomainDataRegionConfig;
import org.hibernate.cache.spi.access.AccessType;

public class RegionConfigUtil {
    public static boolean isEntityCaching(DomainDataRegionConfig regionConfig) {
        return regionConfig.getEntityCaching().size() > 0;
    }

    public static AccessType getAccessTypeOfEntityCaching(DomainDataRegionConfig regionConfig) {
        return isEntityCaching(regionConfig) ? regionConfig.getEntityCaching().get(0).getAccessType() : null;
    }

    public static String getEntityClassName(DomainDataRegionConfig regionConfig) {
        return isEntityCaching(regionConfig) ? regionConfig.getEntityCaching().get(0).getNavigableRole().getNavigableName() : "";
    }
}
