package com.nivixx.ndatabase.core.dao.inmemory;

import com.nivixx.ndatabase.api.exception.*;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.core.dao.Dao;
import com.nivixx.ndatabase.core.serialization.BytesNEntityEncoder;
import com.nivixx.ndatabase.core.serialization.NEntityEncoder;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryDao<K, V extends NEntity<K>> extends Dao<K, V> {

    private Map<K, byte[]> map;
    private final NEntityEncoder<V, byte[]> byteObjectSerializer;

    public InMemoryDao(String collectionName, String schema, Class<K> keyType, DBLogger dbLogger)  {
        super(collectionName, schema, keyType, dbLogger);
        this.map = Collections.synchronizedMap(new LinkedHashMap<>());
        this.byteObjectSerializer = new BytesNEntityEncoder<>();
    }

    @Override
    public void insert(V value) {
        K key = value.getKey();
        if(map.containsKey(key)) {
            throw new DuplicateKeyException("A value with key " + key + " already exist");
        }
        map.put(key, byteObjectSerializer.encode(value));
        dbLogger.logInsert(value);
    }

    @Override
    public void upsert(V value) {
        K key = value.getKey();
        map.put(key, byteObjectSerializer.encode(value));
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
        K key = value.getKey();
        if(!map.containsKey(key)) {
            throw new NEntityNotFoundException("There is no value with the key " + key + " in the database for collection " + collectionName);
        }
        map.put(key, byteObjectSerializer.encode(value));
        dbLogger.logUpdate(value);
    }

    @Override
    public void deleteAll() {
        map.clear();
        dbLogger.logDeleteAll();
    }

    @Override
    public Stream<V> streamAllValues(Class<V> classz) throws NDatabaseException {
        return map.values().stream().map(bytes -> byteObjectSerializer.decode(bytes, classz));
    }

    @Override
    public V get(K key, Class<V> classz) throws NDatabaseException {
        V value = byteObjectSerializer.decode(map.get(key), classz);
        dbLogger.logGet(value);
        return value;
    }

    @Override
    public Optional<V> findOne(Predicate<V> predicate, Class<V> classz) throws NDatabaseException {
        return map.values().stream()
                .map(bytes -> byteObjectSerializer.decode(bytes, classz))
                .filter(predicate)
                .findFirst();
    }

    @Override
    public List<V> find(Predicate<V> predicate, Class<V> classz) throws NDatabaseException {
        return map.values().stream()
                .map(bytes -> byteObjectSerializer.decode(bytes, classz))
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @Override
    public void validateConnection() throws NDatabaseLoadException {
        if(map == null) {
            throw new NDatabaseLoadException("in memory Map is null");
        }
    }

    @Override
    public void createDatabaseIfNotExist(Class<K> keyType) throws DatabaseCreationException {
        if(map == null) {
            map = new LinkedHashMap<>();
        }
    }

}
