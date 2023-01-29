package com.nivixx.ndatabase.core;

import com.google.inject.Inject;
import com.nivixx.ndatabase.api.exception.DatabaseCreationException;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.api.repository.Repository;
import com.nivixx.ndatabase.core.dao.Dao;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RepositoryManager<K,V extends NEntity<K>> {

    private Map<Class<V>, Repository<K,V>> repositoryCache;

    private DatabaseTypeResolver databaseTypeResolver;

    @Inject
    public RepositoryManager(DatabaseTypeResolver databaseTypeResolver) {
        this.repositoryCache = new ConcurrentHashMap<>();
        this.databaseTypeResolver = databaseTypeResolver;
    }

    public Repository<K,V> getOrCreateDao(Class<V> entityType) throws NDatabaseException {
        if(repositoryCache.containsKey(entityType)) {
            return repositoryCache.get(entityType);
        }

        // Create an instance of this type
        V nEntity = createEntityInstance(entityType);
        V cast = entityType.cast(nEntity);

        ParameterizedTypeImpl genericSuperclass = (ParameterizedTypeImpl) cast.getClass().getGenericSuperclass();
        Type actualTypeArgument = genericSuperclass.getActualTypeArguments()[0];
        Class<K> keyType = (Class<K>) actualTypeArgument;
        //((ParameterizedTypeImpl) ((TestEntity) nEntity).getClass().getGenericSuperclass()).actualTypeArguments
        K key = nEntity.getId();

        // Find configured database type (MYSQL, IN_MEMORY, ...)
        Dao<K,V> dao = databaseTypeResolver.getDaoForConfiguredDatabase(nEntity, keyType);

        // Create the database/schema structure if doesn't exist
        dao.createDatabaseIfNotExist(keyType);

        // Init repository
        DBLogger dbLogger = Injector.resolveInstance(DBLogger.class);
        SyncExecutor syncExecutor = Injector.resolveInstance(SyncExecutor.class);
        Repository<K,V> repository = new RepositoryImpl<>(dao, entityType, syncExecutor, dbLogger);
        repositoryCache.put(entityType, repository);


        return repository;
    }

    public V createEntityInstance(Class<V> entityClass) throws DatabaseCreationException {
        try {
            return entityClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new DatabaseCreationException(
                    String.format("could not instantiate NEntity class '%s'." +
                                    " /!\\ Don't forget you have to create a default empty constructor for your entity object",
                            entityClass.getCanonicalName()), e);
        }
    }


}
