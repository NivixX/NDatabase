package com.nivixx.ndatabase.api.repository;

import com.nivixx.ndatabase.api.Promise;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.exception.NEntityNotFoundException;
import com.nivixx.ndatabase.api.exception.DuplicateKeyException;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.api.query.NQuery;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * NDatabase - KeyValue store database
 * Report any issue or contribute here https://github.com/NivixX/NDatabase
 *
 * Repository interface that expose all methods you need
 * to operate your database asynchronously and synchronously
 */
public interface Repository<K, V extends NEntity<K>> {

    /**
     * Get your entity by key from the database.
     *
     * @param key your NEntity key
     * @throws NDatabaseException if a database exception occurred (RuntimeException)
     * @return your entity or {@code null} if not present
     */
    V get(K key) throws NDatabaseException;

    /**
     * Get your entity by key from the database asynchronously.
     *
     * @param key your NEntity key
     * @return {@link Promise.AsyncResult} an async promise which will contain
     * your entity or a throwable if an exception occurred.
     * By recalling this promise, you can consume the operation result either
     * in the same async thread or inside the main thread.
     */
    Promise.AsyncResult<V> getAsync(K key);

    /**
     * Find an entity given your own predicate
     * <pre>{@code
     *     .findOneAsync((nEntity) -> nEntity.getScore() > 20)
     *     // return an entity that have a score higher than 20
     * }</pre>
     * Note that at the current stage of NDatabase, this method is not performant.
     * As value's field are not indexed, this method will stream all your entities
     * and apply your predicate.
     * An index mechanism will be implemented sooner or later
     *
     * @deprecated use "streamAndFindOneAsync" instead
     * or use the indexed version using NQuery
     *
     * @param predicate your predicate that will be applied to your entities
     * @return {@link Promise.AsyncResult} an async promise which will contain
     * your entity (as an Optional) or a throwable if an exception occurred.
     * By recalling this promise, you can consume the operation result either
     * in the same async thread or inside the main thread.
     */
    @Deprecated
    Promise.AsyncResult<Optional<V>> findOneAsync(Predicate<V> predicate);

    /**
     * Find an entity given your own predicate
     * <pre>{@code
     *     .findOneAsync((nEntity) -> nEntity.getScore() > 20)
     *     // return an entity that have a score higher than 20
     * }</pre>
     * Note that at the current stage of NDatabase, this method is not performant.
     * As value's field are not indexed, this method will stream all your entities
     * and apply your predicate.
     * An index mechanism will be implemented sooner or later
     *
     * @param predicate your predicate that will be applied to your entities
     * @return {@link Promise.AsyncResult} an async promise which will contain
     * your entity (as an Optional) or a throwable if an exception occurred.
     * By recalling this promise, you can consume the operation result either
     * in the same async thread or inside the main thread.
     */
    Promise.AsyncResult<Optional<V>> streamAndFindOneAsync(Predicate<V> predicate);

    /**
     * Find all entities given your own predicate
     * <pre>{@code
     *     .findAsync((nEntity) -> nEntity.getScore() > 20)
     *     // return all entities that have a score higher than 20
     * }</pre>
     * Note that at the current stage of NDatabase, this method is not performant.
     * As value's field are not indexed, this method will stream all your entities
     * and apply your predicate.
     * An index mechanism will be implemented sooner or later
     *
     * @deprecated use "streamAndFindOneAsync" instead
     * or use the indexed version using NQuery
     *
     * @param predicate your predicate that will be applied to your entities
     * @return {@link Promise.AsyncResult} an async promise which will contain
     * a list of entities or a throwable if an exception occurred.
     * By recalling this promise, you can consume the operation result either
     * in the same async thread or inside the main thread.
     */
    @Deprecated
    Promise.AsyncResult<List<V>> findAsync(Predicate<V> predicate);

    /**
     * Find all entities given your own predicate
     * <pre>{@code
     *     .findAsync((nEntity) -> nEntity.getScore() > 20)
     *     // return all entities that have a score higher than 20
     * }</pre>
     * Note that at the current stage of NDatabase, this method is not performant.
     * As value's field are not indexed, this method will stream all your entities
     * and apply your predicate.
     * An index mechanism will be implemented sooner or later
     *
     * @param predicate your predicate that will be applied to your entities
     * @return {@link Promise.AsyncResult} an async promise which will contain
     * a list of entities or a throwable if an exception occurred.
     * By recalling this promise, you can consume the operation result either
     * in the same async thread or inside the main thread.
     */
    Promise.AsyncResult<List<V>> streamAndFindAsync(Predicate<V> predicate);

    /**
     * Insert your entity in the database.
     * The operation will fail and throw a {@link DuplicateKeyException} if a value for this entity key already exist in the database.
     *
     * @param value your NEntity data model class
     * @throws NDatabaseException if a database exception occurred (RuntimeException)
     */
    void insert(V value) throws NDatabaseException;

    /**
     * Insert your entity in the database asynchronously.
     * The operation will fail and fill a {@code DuplicateKeyException} inside your Promise if
     * a value for this entity key already exist in the database.
     *
     * @param value your NEntity data model class
     * @return {@link Promise.AsyncEmptyResult} an async promise which will potentially
     * contain a throwable if an exception occurred.
     * By recalling this promise, you can consume the operation result either
     * in the same async thread or inside the main thread.
     */
    Promise.AsyncEmptyResult insertAsync(V value);

    /**
     * Upsert your entity in the database.
     * If the entity doesn't already exist, it will be inserted to the database
     * If the entity already exist, the entity from your database will be updated.
     * This method doesn't throw exception in case of key duplication.
     * Note that the upsert is relatively performant in all database types
     *
     * @param value your NEntity data model class
     */
    void upsert(V value) throws NDatabaseException;

    /**
     * Upsert your entity in the database asynchronously.
     * If the entity doesn't already exist, it will be inserted to the database
     * If the entity already exist, the entity from your database will be updated.
     * No exception will be thrown in case of key duplication.
     * Note that the upsert is relatively performant in all database types
     *
     * @param value your NEntity data model class
     * @return {@link Promise.AsyncEmptyResult} an async promise which will potentially
     * contain a throwable if an exception occurred.
     * By recalling this promise, you can consume the operation result either
     * in the same async thread or inside the main thread.
     */
    Promise.AsyncEmptyResult upsertAsync(V value) throws NDatabaseException;

    /**
     * Update your entity in the database.
     * If the entity doesn't already exist a
     * {@link NEntityNotFoundException} will be thrown
     *
     * @param value your NEntity data model class
     * @throws NDatabaseException if a database exception occurred (RuntimeException)
     */
    void update(V value) throws NDatabaseException;

    /**
     * Update your entity in the database asynchronously.
     * The operation will fail and fill a {@code DuplicateKeyException} inside your Promise if
     * the entity doesn't already exist in the database
     *
     * @param value your NEntity data model class
     * @return {@link Promise.AsyncEmptyResult} an async promise which will potentially
     * contain a throwable if an exception occurred.
     * By recalling this promise, you can consume the operation result either
     * in the same async thread or inside the main thread.
     */
    Promise.AsyncEmptyResult updateAsync(V value);

    /**
     * Delete your entity by key
     * If no entity exist, a {@link NEntityNotFoundException} will be thrown
     *
     * @param key your NEntity key
     * @throws NDatabaseException if a database exception occurred (RuntimeException)
     */
    void delete(K key) throws NDatabaseException;

    /**
     * Delete your entity by key asynchronously
     * The operation will fail and fill a {@code NEntityNotFoundException} inside your Promise if
     * the entity doesn't already exist in the database
     *
     * @param key your NEntity key
     * @return {@link Promise.AsyncEmptyResult} an async promise which will potentially
     * contain a throwable if an exception occurred.
     * By recalling this promise, you can consume the operation result either
     * in the same async thread or inside the main thread.
     */
    Promise.AsyncEmptyResult deleteAsync(K key) throws NDatabaseException;

    /**
     * Delete your entity by entity's key
     * If no entity exist, a {@link NEntityNotFoundException} will be thrown
     *
     * @param value your NEntity (that contain the key)
     * @throws NDatabaseException if a database exception occurred (RuntimeException)
     */
    void delete(V value) throws NDatabaseException;

    /**
     * Delete your entity by entity's key asynchronously
     * The operation will fail and fill a {@code NEntityNotFoundException} inside your Promise if
     * the entity doesn't already exist in the database
     *
     * @param value your NEntity (that contain the key)
     * @return {@link Promise.AsyncEmptyResult} an async promise which will potentially
     * contain a throwable if an exception occurred.
     * By recalling this promise, you can consume the operation result either
     * in the same async thread or inside the main thread.
     */
    Promise.AsyncEmptyResult deleteAsync(V value);

    /**
     * Clear all entities
     * @throws NDatabaseException if a database exception occurred (RuntimeException)
     */
    void deleteAll() throws NDatabaseException;

    /**
     * Clear all entities asynchronously
     *
     * @return {@link Promise.AsyncEmptyResult} an async promise which will potentially
     * contain a throwable if an exception occurred.
     * By recalling this promise, you can consume the operation result either
     * in the same async thread or inside the main thread.
     */
    Promise.AsyncEmptyResult deleteAllAsync();

    /**
     * Retrieve all your entities and stream them.
     * Note that the stream internally use a SplitIterator.
     * So your entities will be streamed by chunks depending on your database type
     * @throws NDatabaseException if a database exception occurred (RuntimeException)
     */
    Stream<V> streamAllValues() throws NDatabaseException;

    /**
     * Retrieve all your entities and stream them asynchronously.
     * Note that the stream internally use a SplitIterator.
     * So your entities will be streamed by chunks depending on your database type
     *
     * @return {@link Promise.AsyncResult} an async promise which will potentially
     * contain a throwable if an exception occurred.
     * By recalling this promise, you can consume the operation result either
     * in the same async thread or inside the main thread.on)
     */
    Promise.AsyncResult<Stream<V>> streamAllValuesAsync();

    /**
     * Retrieve all your entities and compute a top K
     * given a comparator
     * eg: .computeTopAsync(10, (o1, o2) -> o2.getScore() - o1.getScore())
     * return top 10 entries by score
     * return less than 10 entries if the size of the collection is lower
     *
     * @param topMax number of top
     * @param comparator a comparator to sort your entities
     * @return {@link Promise.AsyncResult} an async promise which will potentially
     * contain a throwable if an exception occurred.
     * By recalling this promise, you can consume the operation result either
     * in the same async thread or inside the main thread.on)
     */
    Promise.AsyncResult<List<V>> computeTopAsync(int topMax, Comparator<V> comparator);

    /**
     * Find an entity given a NQuery
     * Read more here : https://github.com/NivixX/NDatabase
     *
     * @param predicate your predicate that will be applied to your entities
     * @return an entity that match your query or empty Optional.
     * Not that if there is more than one result, it won't throw an exception.
     */
    Optional<V> findOne(NQuery.Predicate predicate);

    /**
     * Find an entity given a NQuery
     * Read more here : https://github.com/NivixX/NDatabase
     *
     * @param predicate your predicate that will be applied to your entities
     * @return {@link Promise.AsyncResult} an async promise which will contain
     * your entity (as an Optional) or a throwable if an exception occurred.
     * By recalling this promise, you can consume the operation result either
     * in the same async thread or inside the main thread.
     */
    Promise.AsyncResult<Optional<V>> findOneAsync(NQuery.Predicate predicate);

    /**
     * Find a list of entities given a NQuery
     * Read more here : https://github.com/NivixX/NDatabase
     *
     * @param predicate your predicate that will be applied to your entities
     * @return entity list that match your query or empty Optional.
     * Not that if there is more than one result, it won't throw an exception.
     */
    List<V> find(NQuery.Predicate predicate);

    /**
     * Find a list of entities given a NQuery
     * Read more here : https://github.com/NivixX/NDatabase
     *
     * @param predicate your predicate that will be applied to your entities
     * @return {@link Promise.AsyncResult} an async promise which will contain
     * your entity (as an Optional) or a throwable if an exception occurred.
     * By recalling this promise, you can consume the operation result either
     * in the same async thread or inside the main thread.
     */
    Promise.AsyncResult<List<V>> findAsync(NQuery.Predicate predicate);
}
