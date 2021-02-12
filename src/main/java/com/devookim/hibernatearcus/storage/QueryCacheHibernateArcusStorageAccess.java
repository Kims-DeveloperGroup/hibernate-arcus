package com.devookim.hibernatearcus.storage;

import com.devookim.hibernatearcus.client.HibernateArcusClientFactory;
import org.hibernate.cache.spi.QueryKey;

public class QueryCacheHibernateArcusStorageAccess extends HibernateArcusStorageAccess {
    public QueryCacheHibernateArcusStorageAccess(HibernateArcusClientFactory hibernateArcusClientFactory, String prefix) {
        super(hibernateArcusClientFactory, prefix);
    }

    @Override
    protected String generateKey(Object key) {
        QueryKey queryKey = (QueryKey) key;
        return super.CACHE_REGION + ":" + queryKey.hashCode() + "#" + queryKey.getNamedParameters().values();
    }
}