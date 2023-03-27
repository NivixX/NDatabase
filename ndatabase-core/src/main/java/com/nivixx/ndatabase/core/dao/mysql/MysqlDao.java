package com.nivixx.ndatabase.core.dao.mysql;

import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.core.dao.jdbc.JdbcDao;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

public class MysqlDao<K, V extends NEntity<K>> extends JdbcDao<K,V> {

    public MysqlDao(String collectionName,
                    String schema,
                    Class<K> keyType,
                    Class<V> nEntityType,
                    V instantiatedNEntity,
                    HikariConnectionPool hikariConnectionPool,
                    DBLogger dbLogger) {
        super(collectionName, schema, keyType, nEntityType, instantiatedNEntity, hikariConnectionPool,  dbLogger);
    }

    // Override here if mysql implementation need more specifications
}
