package com.devookim.hibernatearcus.factory;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.cache.internal.DefaultCacheKeysFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;

import javax.persistence.Id;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
public class HibernateArcusCacheKeysFactory extends DefaultCacheKeysFactory {

    private static class CollectionKey implements Serializable {
        private final String role;
        private final String keyId;

        public CollectionKey(String role, String keyId) {
            this.role = role;
            this.keyId = keyId;
        }

        @Override
        public String toString() {
            return role + "#id" + "[" + keyId + "]";
        }
    }

    public static class EntityKey implements Serializable {
        private final String entityName;
        private final Object id;

        public EntityKey(String entityName, Object id) {
            this.entityName = entityName;
            this.id = id;
        }

        @Override
        public String toString() {
            String[] splitByDot = entityName.split("\\.");
            return splitByDot[splitByDot.length - 1] + "#" + id;
        }
    }

    private static class NaturalIdKey implements Serializable {
        private final String entityName;
        private final Object[] naturalIds;

        public NaturalIdKey(String entityName, Object[] naturalIds) {
            this.entityName = entityName;
            this.naturalIds = naturalIds;
        }

        @Override
        public String toString() {
            String[] splitByDot = entityName.split("\\.");
            return splitByDot[splitByDot.length - 1] + "#NaturalId" + Arrays.toString(naturalIds).replace(" ", "");
        }
    }

    @Override
    public Object createCollectionKey(Object id, CollectionPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {
        try {
            Optional<Field> annotatedWithId = Arrays.stream(id.getClass().getDeclaredFields())
                    .filter(field -> field.getAnnotation(Id.class) != null)
                    .findFirst();
            if (annotatedWithId.isPresent()) {
                annotatedWithId.get().setAccessible(true);
                Object keyValue = annotatedWithId.get().get(id);
                return new CollectionKey(persister.getRole(), keyValue.toString());
            }
        } catch (IllegalAccessException e) {
            log.error("Error occurred in createCollectionKey.", e);
        }
        return super.createCollectionKey(id, persister, factory, tenantIdentifier);
    }

    @Override
    public Object createEntityKey(Object id, EntityPersister persister, SessionFactoryImplementor factory, String tenantIdentifier) {
        return new EntityKey(persister.getRootEntityName(), id);
    }

    @Override
    public Object createNaturalIdKey(Object[] naturalIdValues, EntityPersister persister, SharedSessionContractImplementor session) {
        return new NaturalIdKey(persister.getRootEntityName(), naturalIdValues);
    }
}