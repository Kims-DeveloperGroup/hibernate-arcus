package com.devookim.hibernatearcus.factory;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.cache.internal.DefaultCacheKeysFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.collection.CollectionPersister;

@Slf4j
public class HibernateArcusCacheKeysFactory extends DefaultCacheKeysFactory {

    @Override
    public Object createCollectionKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {
        return super.createCollectionKey(id, persister, factory, tenantIdentifier);
    }
}