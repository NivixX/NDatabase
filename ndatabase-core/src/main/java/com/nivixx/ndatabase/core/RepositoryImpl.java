package com.nivixx.ndatabase.core;

import com.nivixx.ndatabase.api.Promise;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.api.query.NQuery;
import com.nivixx.ndatabase.api.repository.Repository;
import com.nivixx.ndatabase.core.promise.AsyncThreadPool;
import com.nivixx.ndatabase.core.promise.pipeline.PromiseEmptyResultPipeline;
import com.nivixx.ndatabase.core.promise.pipeline.PromiseResultPipeline;
import com.nivixx.ndatabase.dbms.api.Dao;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RepositoryImpl<K, V extends NEntity<K>> implements Repository<K, V> {

    private final Dao<K,V> dao;
    private final Class<V> classz;
    private final SyncExecutor syncExecutor;
    private final AsyncThreadPool asyncThreadPool;
    private final DBLogger dbLogger;

    public RepositoryImpl(Dao<K, V> dao,
                          Class<V> classz,
                          SyncExecutor syncExecutor,
                          AsyncThreadPool asyncThreadPool,
                          DBLogger dbLogger) {
        this.dao = dao;
        this.classz = classz;
        this.syncExecutor = syncExecutor;
        this.asyncThreadPool = asyncThreadPool;
        this.dbLogger = dbLogger;
    }

    @Override
    public V get(K key) throws NDatabaseException {
        return dao.get(key, classz);
    }

    @Override
    public Promise.AsyncResult<V> getAsync(K key) {
        CompletableFuture<V> future = CompletableFuture.supplyAsync(() -> dao.get(key, classz));
        return new PromiseResultPipeline<>(future, syncExecutor, asyncThreadPool, dbLogger);
    }

    @Override
    public Promise.AsyncResult<Optional<V>> findOneAsync(Predicate<V> predicate) {
        CompletableFuture<Optional<V>> future = CompletableFuture.supplyAsync(() -> dao.findOne(predicate, classz));
        return new PromiseResultPipeline<>(future, syncExecutor, asyncThreadPool, dbLogger);
    }

    @Override
    public Promise.AsyncResult<Optional<V>> streamAndFindOneAsync(Predicate<V> predicate) {
        return findOneAsync(predicate);
    }

    @Override
    public Promise.AsyncResult<List<V>> findAsync(Predicate<V> predicate) {
        CompletableFuture<List<V>> future = CompletableFuture.supplyAsync(() -> dao.find(predicate, classz));
        return new PromiseResultPipeline<>(future, syncExecutor, asyncThreadPool, dbLogger);
    }

    @Override
    public Promise.AsyncResult<List<V>> streamAndFindAsync(Predicate<V> predicate) {
        return findAsync(predicate);
    }

    @Override
    public void insert(V value) throws NDatabaseException {
        dao.insert(value);
    }

    @Override
    public Promise.AsyncEmptyResult insertAsync(V value) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> dao.insert(value));
        return new PromiseEmptyResultPipeline<>(future, syncExecutor, asyncThreadPool, dbLogger);
    }

    @Override
    public void upsert(V value) throws NDatabaseException {
        dao.upsert(value);
    }

    @Override
    public Promise.AsyncEmptyResult upsertAsync(V value) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> dao.upsert(value));
        return new PromiseEmptyResultPipeline<>(future, syncExecutor, asyncThreadPool, dbLogger);
    }

    @Override
    public void update(V value) throws NDatabaseException {
        dao.update(value);
    }

    @Override
    public Promise.AsyncEmptyResult updateAsync(V value) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> dao.update(value));
        return new PromiseEmptyResultPipeline<>(future, syncExecutor, asyncThreadPool, dbLogger);
    }

    @Override
    public void delete(K key) throws NDatabaseException {
        dao.delete(key);
    }

    @Override
    public Promise.AsyncEmptyResult deleteAsync(K key)  {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> dao.delete(key));
        return new PromiseEmptyResultPipeline<>(future, syncExecutor, asyncThreadPool, dbLogger);
    }

    @Override
    public void delete(V value) throws NDatabaseException {
        delete(value.getKey());
    }

    @Override
    public Promise.AsyncEmptyResult deleteAsync(V value)  {
        return deleteAsync(value.getKey());
    }

    @Override
    public void deleteAll() throws NDatabaseException {
        dao.deleteAll();
    }

    @Override
    public Promise.AsyncEmptyResult deleteAllAsync() throws NDatabaseException {
        CompletableFuture<Void> future = CompletableFuture.runAsync(dao::deleteAll);
        return new PromiseEmptyResultPipeline<>(future, syncExecutor, asyncThreadPool, dbLogger);
    }

    @Override
    public Stream<V> streamAllValues() throws NDatabaseException {
        return dao.streamAllValues(classz);
    }

    @Override
    public Promise.AsyncResult<Stream<V>> streamAllValuesAsync() throws NDatabaseException {
        CompletableFuture<Stream<V>> future = CompletableFuture.supplyAsync(() -> dao.streamAllValues(classz));
        return new PromiseResultPipeline<>(future, syncExecutor, asyncThreadPool, dbLogger);
    }

    @Override
    public Promise.AsyncResult<List<V>> computeTopAsync(int topMax, Comparator<V> comparator) {
        CompletableFuture<List<V>> future = CompletableFuture.supplyAsync(() ->
                dao.streamAllValues(classz)
                        .sorted(comparator)
                        .limit(topMax)
                        .collect(Collectors.toList()));
        return new PromiseResultPipeline<>(future, syncExecutor, asyncThreadPool, dbLogger);
    }

    @Override
    public Optional<V> findOne(NQuery.Predicate predicate) {
        return dao.findOne(predicate, classz);
    }

    @Override
    public Promise.AsyncResult<Optional<V>> findOneAsync(NQuery.Predicate predicate) {
        CompletableFuture<Optional<V>> future = CompletableFuture.supplyAsync(() -> dao.findOne(predicate, classz));
        return new PromiseResultPipeline<>(future, syncExecutor, asyncThreadPool, dbLogger);
    }

    @Override
    public List<V> find(NQuery.Predicate predicate) {
        return dao.find(predicate, classz);
    }

    @Override
    public Promise.AsyncResult<List<V>> findAsync(NQuery.Predicate predicate) {
        CompletableFuture<List<V>> future = CompletableFuture.supplyAsync(() -> dao.find(predicate, classz));
        return new PromiseResultPipeline<>(future, syncExecutor, asyncThreadPool, dbLogger);
    }
}
