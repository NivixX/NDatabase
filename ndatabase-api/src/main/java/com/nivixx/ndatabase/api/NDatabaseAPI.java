package com.nivixx.ndatabase.api;

import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.api.repository.Repository;

/**
 * NDatabase - KeyValue store database
 * Report any issue or contribute here https://github.com/NivixX/NDatabase
 */
public interface NDatabaseAPI {

    /**
     *
     * This method will return you a repository for your desired entity.
     * Note that calling this method will also create your database schema
     * if it doesn't exist according to your current database type configuration.
     *
     * @param entityType The data model class type which extends {@code NEntity}
     * @param <K> The key type used for your key-value store
     * @param <V> Your entity class which represent your data model
     * @return A fully CRUD usable {@code Repository} with async extension using Async to Sync pipeline
     *
     * @throws NDatabaseException throw an exception if either
     * the creation of your database schema failed or if a reflection issue occurred.
     */
    <K,V extends NEntity<K>> Repository<K,V> getOrCreateRepository(Class<V> entityType) throws NDatabaseException;

}
