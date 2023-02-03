package com.nivixx.ndatabase.api.repository;

import com.nivixx.ndatabase.api.Promise;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.model.NEntity;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Repository<K, V extends NEntity<K>> {

    V get(K key) throws NDatabaseException;
    Promise.AsyncResult<V> getAsync(K key);

    Promise.AsyncResult<Optional<V>> findOneAsync(Predicate<V> predicate);
    Promise.AsyncResult<List<V>> findAsync(Predicate<V> predicate);

    void insert(V value) throws NDatabaseException;

    //TODO GENERIC
    Promise.AsyncEmptyResult insertAsync(V value) throws NDatabaseException;

    void upsert(V value) throws NDatabaseException;
    Promise.AsyncEmptyResult upsertAsync(V value) throws NDatabaseException;

    void update(V value) throws NDatabaseException;
    Promise.AsyncEmptyResult updateAsync(V value) throws NDatabaseException;

    void delete(K key) throws NDatabaseException;
    Promise.AsyncEmptyResult deleteAsync(K key) throws NDatabaseException;

    void delete(V value) throws NDatabaseException;
    Promise.AsyncEmptyResult deleteAsync(V value) throws NDatabaseException;

    void deleteAll() throws NDatabaseException;
    Promise.AsyncEmptyResult deleteAllAsync() throws NDatabaseException;

    Stream<V> streamAllValues() throws NDatabaseException;
    Promise.AsyncResult<Stream<V>> streamAllValuesAsync() throws NDatabaseException;
}
