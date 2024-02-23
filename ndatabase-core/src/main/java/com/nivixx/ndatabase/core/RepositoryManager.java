package com.nivixx.ndatabase.core;

import com.google.inject.Inject;
import com.nivixx.ndatabase.api.annotation.Indexed;
import com.nivixx.ndatabase.api.exception.DatabaseCreationException;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.core.expressiontree.SingleNodePath;
import com.nivixx.ndatabase.api.repository.Repository;
import com.nivixx.ndatabase.core.dao.Dao;
import com.nivixx.ndatabase.core.promise.AsyncThreadPool;
import com.nivixx.ndatabase.core.reflection.NReflectionUtil;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RepositoryManager<K,V extends NEntity<K>> {

    private final Map<Class<V>, Repository<K,V>> repositoryCache;

    private final DatabaseTypeResolver databaseTypeResolver;

    @Inject
    public RepositoryManager(DatabaseTypeResolver databaseTypeResolver) {
        this.repositoryCache = new ConcurrentHashMap<>();
        this.databaseTypeResolver = databaseTypeResolver;
    }

    public Repository<K,V> getOrCreateRepository(Class<V> entityType) throws NDatabaseException {
        if(repositoryCache.containsKey(entityType)) {
            return repositoryCache.get(entityType);
        }

        // Create an instance of this type and resolve the key type
        V nEntity = createEntityInstance(entityType);
        Class<K> keyType = resolveKeyFromEntity(nEntity);

        // Find configured database type (MYSQL, MongoDB, ...)
        Dao<K,V> dao = databaseTypeResolver.getDaoForConfiguredDatabase(nEntity, keyType, entityType);

        // Create the database/schema structure if doesn't exist
        dao.createDatabaseIfNotExist(keyType);

        // Index the key-value store
        List<SingleNodePath> indexSingleNodePathList = new ArrayList<>();
        try {
            NReflectionUtil.resolveIndexedFieldsFromEntity(indexSingleNodePathList, new SingleNodePath(), nEntity);
        } catch (Exception e) {
            throw new DatabaseCreationException("Failed to resolve nEntity index paths ", e);
        }
        dao.createIndexes(indexSingleNodePathList);

        // Init repository
        DBLogger dbLogger = Injector.resolveInstance(DBLogger.class);
        SyncExecutor syncExecutor = Injector.resolveInstance(SyncExecutor.class);
        AsyncThreadPool asyncThreadPool = Injector.resolveInstance(AsyncThreadPool.class);
        Repository<K,V> repository = new RepositoryImpl<>(dao, entityType, syncExecutor, asyncThreadPool, dbLogger);
        repositoryCache.put(entityType, repository);

        return repository;
    }

    private V createEntityInstance(Class<V> entityClass) throws DatabaseCreationException {
        try {
            return entityClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new DatabaseCreationException(
                    String.format("could not instantiate NEntity class '%s'." +
                                    " /!\\ Don't forget you have to create a default empty constructor for your entity object",
                            entityClass.getCanonicalName()), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<K> resolveKeyFromEntity(V nEntity) {
        try {
            ParameterizedType genericSuperclass = (ParameterizedType) nEntity.getClass().getGenericSuperclass();
            Type actualTypeArgument = genericSuperclass.getActualTypeArguments()[0];
            return (Class<K>) actualTypeArgument;
        } catch (Exception e) {
            throw new DatabaseCreationException(
                    String.format("could not resolve the key type for NEntity class '%s'." +
                                    " Did you properly used a supported key type ?",
                            nEntity.getClass().getCanonicalName()), e);
        }
    }

}
