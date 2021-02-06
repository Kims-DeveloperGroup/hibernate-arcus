package com.devookim.hibernatearcus.factory;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.cache.internal.DefaultCacheKeysFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.collection.CollectionPersister;

import java.io.Serializable;
import java.lang.reflect.Field;

@Slf4j
public class HibernateArcusCacheKeysFactory extends DefaultCacheKeysFactory {

    static class CollectionKey implements Serializable {
        private final String role;
        private final String keyName;
        private final String keyId;

        public CollectionKey(String role, String keyName, String keyId) {
            this.role = role;
            this.keyName = keyName;
            this.keyId = keyId;
        }

        @Override
        public String toString() {
            return role + "#" + keyName +"[" + keyId + "]";
        }
    }

    @Override
    public Object createCollectionKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {
        try {
            String keyName = persister.getKeyType().getName()
                    .replace("component", "")
                    .replace("\"", "")
                    .replace("[","")
                    .replace("]", "");
            Field keyField = id.getClass().getDeclaredField(keyName);
            keyField.setAccessible(true);
            Object keyValue = keyField.get(id);
            return new CollectionKey(persister.getRole(), keyName, keyValue.toString());
        } catch (IllegalAccessException | NoSuchFieldException ignored) {
            return super.createCollectionKey(id, persister, factory, tenantIdentifier);
        }
    }
}