package com.nivixx.ndatabase.core.dao;

import com.nivixx.ndatabase.api.exception.DatabaseCreationException;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * CRUD Low level DAO (working with serializable object)
 */
public abstract class Dao<K, V extends NEntity<K>> {

    protected String collectionName;
    protected String schema;

    protected Class<K> keyType;
    protected DBLogger dbLogger;

    public Dao(String collectionName, String schema, Class<K> keyType, DBLogger dbLogger) {
        this.collectionName = collectionName;
        this.keyType = keyType;
        this.dbLogger = dbLogger;
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

    public abstract void createDatabaseIfNotExist(Class<K> keyType) throws DatabaseCreationException;

}
