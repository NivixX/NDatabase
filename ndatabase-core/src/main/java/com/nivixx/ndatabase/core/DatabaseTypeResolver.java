package com.nivixx.ndatabase.core;

import com.nivixx.ndatabase.api.annotation.NTable;
import com.nivixx.ndatabase.api.exception.DatabaseCreationException;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.core.config.NDatabaseConfig;
import com.nivixx.ndatabase.core.dao.Dao;
import com.nivixx.ndatabase.core.dao.inmemory.InMemoryDao;
import com.nivixx.ndatabase.core.dao.mongodb.MongodbConnection;
import com.nivixx.ndatabase.core.dao.mongodb.MongodbDao;
import com.nivixx.ndatabase.core.dao.mysql.HikariConnectionPool;
import com.nivixx.ndatabase.core.dao.mysql.MysqlDao;
import com.nivixx.ndatabase.core.dao.sqlite.SqliteConnectionPool;
import com.nivixx.ndatabase.core.dao.sqlite.SqliteDao;
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
                HikariConnectionPool hikariConnectionPool = Injector.resolveInstance(HikariConnectionPool.class);
                return new MysqlDao<>(
                        nTable.name(),
                        nTable.schema(),
                        keyType,
                        hikariConnectionPool,
                        dbLogger);
            case SQLITE:
                SqliteConnectionPool sqliteConnectionPool = Injector.resolveInstance(SqliteConnectionPool.class);
                return new SqliteDao<>(
                        nTable.name(),
                        nTable.schema(),
                        keyType,
                        sqliteConnectionPool,
                        dbLogger);
            case MONGODB:
                MongodbConnection mongodbConnection = Injector.resolveInstance(MongodbConnection.class);
                return new MongodbDao<>(
                        nTable.name(),
                        nTable.schema(),
                        keyType,
                        mongodbConnection,
                        dbLogger
                );
            default:
                throw new DatabaseCreationException("Database type has not been provided in the config");
        }
    }

    private <K,V extends NEntity<K>> NTable extractNTable(V nEntity) {
        Annotation[] annotations = nEntity.getClass().getAnnotations();
        for (Annotation annotation : annotations) {
            if(annotation instanceof NTable) {
                return (NTable) annotation;
            }
        }
        String msg = String.format("Could not get table name for entity '%s'. You have to annotate your class with @NTable and specify a name", nEntity.getClass().getCanonicalName());
        throw new DatabaseCreationException(msg);
    }
}
