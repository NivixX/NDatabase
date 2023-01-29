package com.nivixx.ndatabase.api;

import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.api.repository.Repository;

public interface NDatabaseAPI {

    <K,V extends NEntity<K>> Repository<K,V> getOrCreateRepository(Class<V> entityType) throws NDatabaseException;

}
