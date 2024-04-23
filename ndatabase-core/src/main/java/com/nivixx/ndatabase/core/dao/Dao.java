package com.nivixx.ndatabase.core.dao;

import com.nivixx.ndatabase.api.exception.DatabaseCreationException;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.exception.NDatabaseLoadException;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.api.query.NQuery;
import com.nivixx.ndatabase.core.expressiontree.SingleNodePath;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class Dao<K, V extends NEntity<K>> {

    protected final String collectionName;
    protected final String schema;

    protected final Class<K> keyType;
    protected final Class<V> nEntityType;
    protected final V instantiatedNEntity;
    protected final DBLogger dbLogger;

    protected Dao(String collectionName,
                  String schema,
                  Class<K> keyType,
                  Class<V> nEntityType,
                  V instantiatedNEntity,
                  DBLogger dbLogger) {
        this.collectionName = collectionName;
        this.keyType = keyType;
        this.nEntityType = nEntityType;
        this.instantiatedNEntity = instantiatedNEntity;
        this.dbLogger = dbLogger;
        this.schema = schema;
    }

    public void init() throws DatabaseCreationException {
        // Do nothing by default
    }


    public abstract void insert(V value) throws NDatabaseException;

    public abstract void upsert(V value) throws NDatabaseException;

    public abstract void delete(K key) throws NDatabaseException;

    public abstract void update(V value) throws NDatabaseException;

    public abstract void deleteAll() throws NDatabaseException;

    public abstract Stream<V> streamAllValues(Class<V> classz) throws NDatabaseException;

    public abstract V get(K key, Class<V> classz) throws NDatabaseException;


    public abstract Optional<V> findOne(Predicate<V> predicate, Class<V> classz) throws NDatabaseException;

    public abstract List<V> find(Predicate<V> predicate, Class<V> classz) throws NDatabaseException;

    public abstract void validateConnection() throws NDatabaseLoadException;

    public abstract void createDatabaseIfNotExist(Class<K> keyType) throws DatabaseCreationException;

    public abstract void createIndexes(List<SingleNodePath> singleNodePaths) throws DatabaseCreationException;


    public abstract Optional<V> findOne(NQuery.Predicate expression, Class<V> classz);

    public abstract List<V> find(NQuery.Predicate expression, Class<V> classz);
}
