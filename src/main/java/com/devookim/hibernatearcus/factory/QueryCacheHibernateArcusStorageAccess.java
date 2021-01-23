package com.devookim.hibernatearcus.factory;

import net.spy.memcached.ArcusClientPool;
import org.hibernate.cache.spi.QueryKey;

import java.util.Map;

public class QueryCacheHibernateArcusStorageAccess extends HibernateArcusStorageAccess {
    public QueryCacheHibernateArcusStorageAccess(ArcusClientPool arcusClientPool, String prefix) {
        super(arcusClientPool, prefix);
    }

    @Override
    protected String generateKey(Object key) {
        QueryKey queryKey = (QueryKey) key;
        Map namedParameters = queryKey.getNamedParameters();
        return super.CACHE_REGION + ":" + queryKey.hashCode() + "#" + namedParameters.values();
    }
}