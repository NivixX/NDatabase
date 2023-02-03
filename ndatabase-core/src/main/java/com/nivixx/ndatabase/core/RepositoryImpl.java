package com.nivixx.ndatabase.core;

import com.nivixx.ndatabase.api.Promise;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.api.repository.Repository;
import com.nivixx.ndatabase.core.dao.Dao;
import com.nivixx.ndatabase.core.promise.pipeline.PromiseEmptyResultPipeline;
import com.nivixx.ndatabase.core.promise.pipeline.PromiseResultPipeline;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RepositoryImpl<K, V extends NEntity<K>> implements Repository<K, V> {

    private final Dao<K,V> dao;
    private final Class<V> classz;
    private final SyncExecutor syncExecutor;
    private final DBLogger dbLogger;

    public RepositoryImpl(Dao<K, V> dao, Class<V> classz, SyncExecutor syncExecutor, DBLogger dbLogger) {
        this.dao = dao;
        this.classz = classz;
        this.syncExecutor = syncExecutor;
        this.dbLogger = dbLogger;
    }

    @Override
    public V get(K key) throws NDatabaseException {
        return dao.get(key, classz);
    }

    @Override
    public Promise.AsyncResult<V> getAsync(K key) {
        CompletableFuture<V> future = CompletableFuture.supplyAsync(() -> dao.get(key, classz));
        return new PromiseResultPipeline<>(future, syncExecutor, dbLogger);
    }

    @Override
    public Promise.AsyncResult<Optional<V>> findOneAsync(Predicate<V> predicate) {
        CompletableFuture<Optional<V>> future = CompletableFuture.supplyAsync(() -> dao.findOne(predicate, classz));
        return new PromiseResultPipeline<>(future, syncExecutor, dbLogger);
    }


    @Override
    public Promise.AsyncResult<List<V>> findAsync(Predicate<V> predicate) {
        CompletableFuture<List<V>> future = CompletableFuture.supplyAsync(() -> dao.find(predicate, classz));
        return new PromiseResultPipeline<>(future, syncExecutor, dbLogger);
    }

    @Override
    public void insert(V value) throws NDatabaseException {
        dao.insert(value);
    }

    @Override
    public Promise.AsyncEmptyResult insertAsync(V value) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> dao.insert(value));
        return new PromiseEmptyResultPipeline(future, syncExecutor, dbLogger);
    }

    @Override
    public void upsert(V value) throws NDatabaseException {
        dao.upsert(value);
    }

    @Override
    public Promise.AsyncEmptyResult upsertAsync(V value) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> dao.upsert(value));
        return new PromiseEmptyResultPipeline(future, syncExecutor, dbLogger);
    }

    @Override
    public void update(V value) throws NDatabaseException {
        dao.update(value);
    }

    @Override
    public Promise.AsyncEmptyResult updateAsync(V value) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> dao.update(value));
        return new PromiseEmptyResultPipeline(future, syncExecutor, dbLogger);
    }

    @Override
    public void delete(K key) throws NDatabaseException {
        dao.delete(key);
    }

    @Override
    public Promise.AsyncEmptyResult deleteAsync(K key)  {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> dao.delete(key));
        return new PromiseEmptyResultPipeline(future, syncExecutor, dbLogger);
    }

    @Override
    public void delete(V value) throws NDatabaseException {
        delete(value.getId());
    }

    @Override
    public Promise.AsyncEmptyResult deleteAsync(V value)  {
        return deleteAsync(value.getId());
    }

    @Override
    public void deleteAll() throws NDatabaseException {
        dao.deleteAll();
    }

    @Override
    public Promise.AsyncEmptyResult deleteAllAsync() throws NDatabaseException {
        CompletableFuture<Void> future = CompletableFuture.runAsync(dao::deleteAll);
        return new PromiseEmptyResultPipeline(future, syncExecutor, dbLogger);
    }

    @Override
    public Stream<V> streamAllValues() throws NDatabaseException {
        return dao.streamAllValues(classz);
    }

    @Override
    public Promise.AsyncResult<Stream<V>> streamAllValuesAsync() throws NDatabaseException {
        CompletableFuture<Stream<V>> future = CompletableFuture.supplyAsync(() -> dao.streamAllValues(classz));
        return new PromiseResultPipeline<>(future, syncExecutor, dbLogger);
    }
}
