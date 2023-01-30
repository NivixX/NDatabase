package com.nivixx.ndatabase.core;

import com.nivixx.ndatabase.api.annotation.NTable;
import com.nivixx.ndatabase.api.exception.DatabaseCreationException;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.core.config.NDatabaseConfig;
import com.nivixx.ndatabase.core.dao.Dao;
import com.nivixx.ndatabase.core.dao.inmemory.InMemoryDao;
import com.nivixx.ndatabase.core.dao.mysql.MysqlConnectionPool;
import com.nivixx.ndatabase.core.dao.mysql.MysqlDao;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.lang.annotation.Annotation;

public class DatabaseTypeResolver {

    public <K,V extends NEntity<K>> Dao<K,V> getDaoForConfiguredDatabase(V nEntity, Class<K> keyType) {

        DBLogger dbLogger = Injector.resolveInstance(DBLogger.class);
        NDatabaseConfig nDatabaseConfig = Injector.resolveInstance(NDatabaseConfig.class);
        NTable nTable = extractNTable(nEntity);

        switch (nDatabaseConfig.getDatabaseType()) {
            case IN_MEMORY:
                return new InMemoryDao<>(nTable.name(), nTable.schema(), keyType, dbLogger);
            case MYSQL:
                MysqlConnectionPool jdbcConnectionPool = Injector.resolveInstance(MysqlConnectionPool.class);
                return new MysqlDao<>(nTable.name(), nTable.schema(), keyType, jdbcConnectionPool, dbLogger);
            default:
                throw new DatabaseCreationException("Database type has not been provided in the config");
        }
    }

    private <K,V extends NEntity<K>> NTable extractNTable(V nEntity) {
        Annotation[] annotations = nEntity.getClass().getAnnotations();
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> type = annotation.annotationType();
            if(annotation instanceof NTable) {
                return (NTable) annotation;
            }
        }
        String msg = String.format("Could not get table name for entity '%s'. You have to annotate your class with @NTable and specify a name", nEntity.getClass().getCanonicalName());
        throw new DatabaseCreationException(msg);
    }
}
