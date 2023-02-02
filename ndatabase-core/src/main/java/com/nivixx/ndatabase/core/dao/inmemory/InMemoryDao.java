package com.nivixx.ndatabase.core.dao.inmemory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivixx.ndatabase.api.exception.DatabaseCreationException;
import com.nivixx.ndatabase.api.exception.DuplicateKeyException;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.exception.NEntityNotFoundException;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.core.dao.Dao;
import com.nivixx.ndatabase.core.serialization.Serializer;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryDao<K, V extends NEntity<K>> extends Dao<K, V> {

    private Map<K, byte[]> map;

    public InMemoryDao(String collectionName, String schema, Class<K> keyType, DBLogger dbLogger)  {
        super(collectionName, schema, keyType, dbLogger);
        this.map = Collections.synchronizedMap(new LinkedHashMap<>());
    }

    @Override
    public void insert(V value) {
        K key = value.getId();
        if(map.containsKey(key)) {
            throw new DuplicateKeyException("A value with key " + key + " already exist");
        }
        map.put(key, Serializer.toByteArray(value));
        dbLogger.logInsert(value);
    }

    @Override
    public void upsert(V value) {
        K key = value.getId();
        map.put(key, Serializer.toByteArray(value));
        dbLogger.logUpsert(value);
    }

    @Override
    public void delete(K key) {
        if(!map.containsKey(key)) {
            throw new NEntityNotFoundException("There is no value with the key " + key + " in the database for collection " + collectionName);
        }
        map.remove(key);
        dbLogger.logDelete(key);
    }

    @Override
    public void update(V value) {
        K key = value.getId();
        if(!map.containsKey(key)) {
            throw new NEntityNotFoundException("There is no value with the key " + key + " in the database for collection " + collectionName);
        }
        map.put(key, Serializer.toByteArray(value));
        dbLogger.logUpdate(value);
    }

    @Override
    public void deleteAll() {
        map.clear();
        dbLogger.logDeleteAll();
    }

    @Override
    public Stream<V> streamAllValues(Class<V> classz) throws NDatabaseException {
        return map.values().stream().map(bytes -> Serializer.deserialize(bytes, classz));
    }

    @Override
    public V get(K key, Class<V> classz) throws NDatabaseException {
        V value = Serializer.deserialize(map.get(key), classz);
        dbLogger.logGet(value);
        return value;
    }

    @Override
    public Optional<V> findOne(Predicate<V> predicate, Class<V> classz) throws NDatabaseException {
        return map.values().stream()
                .map(bytes -> Serializer.deserialize(bytes, classz))
                .filter(predicate)
                .findFirst();
    }

    @Override
    public List<V> find(Predicate<V> predicate, Class<V> classz) throws NDatabaseException {
        return map.values().stream()
                .map(bytes -> Serializer.deserialize(bytes, classz))
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @Override
    public void createDatabaseIfNotExist(Class<K> keyType) throws DatabaseCreationException {
        if(map == null) {
            map = new LinkedHashMap<>();
        }
    }

}
