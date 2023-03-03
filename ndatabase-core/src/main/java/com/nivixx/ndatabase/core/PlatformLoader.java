package com.nivixx.ndatabase.core;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.nivixx.ndatabase.api.NDatabase;
import com.nivixx.ndatabase.api.NDatabaseAPI;
import com.nivixx.ndatabase.api.exception.NDatabaseLoadException;
import com.nivixx.ndatabase.core.config.DatabaseType;
import com.nivixx.ndatabase.core.config.NDatabaseConfig;
import com.nivixx.ndatabase.core.dao.DatabaseConnection;
import com.nivixx.ndatabase.core.dao.mariadb.HikariMariaConnectionPool;
import com.nivixx.ndatabase.core.dao.mongodb.MongodbConnection;
import com.nivixx.ndatabase.core.dao.mysql.HikariConnectionPool;
import com.nivixx.ndatabase.core.dao.sqlite.SqliteConnectionPool;
import com.nivixx.ndatabase.core.promise.AsyncThreadPool;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.lang.reflect.Method;

public abstract class PlatformLoader extends AbstractModule  {

    @Override
    protected void configure() {
        bind(SyncExecutor.class).toInstance(supplySyncExecutor());

        NDatabaseConfig nDatabaseConfig = supplyNDatabaseConfig();
        nDatabaseConfig.verifyConfig();
        DBLogger dbLogger = supplyDbLogger(nDatabaseConfig.isDebugMode());
        bind(DBLogger.class).toInstance(dbLogger);
        switch (nDatabaseConfig.getDatabaseType()) {
            case MYSQL:
                HikariConnectionPool hikariConnectionPool = new HikariConnectionPool(nDatabaseConfig.getMysqlConfig());
                bind(HikariConnectionPool.class).toInstance(hikariConnectionPool);
                bind(DatabaseConnection.class).toInstance(hikariConnectionPool);
                break;
            case MARIADB:
                HikariMariaConnectionPool hikariConnectionPoolMaria = new HikariMariaConnectionPool(nDatabaseConfig.getMariaDBConfig());
                bind(HikariMariaConnectionPool.class).toInstance(hikariConnectionPoolMaria);
                bind(DatabaseConnection.class).toInstance(hikariConnectionPoolMaria);
                break;
            case SQLITE:
                SqliteConnectionPool sqliteConnectionPool = new SqliteConnectionPool(nDatabaseConfig.getSqliteConfig(), dbLogger);
                bind(SqliteConnectionPool.class).toInstance(sqliteConnectionPool);
                bind(DatabaseConnection.class).toInstance(sqliteConnectionPool);
                break;
            case MONGODB:
                MongodbConnection mongodbConnection = new MongodbConnection(nDatabaseConfig.getMongoDBConfig());
                bind(MongodbConnection.class).toInstance(mongodbConnection);
                bind(DatabaseConnection.class).toInstance(mongodbConnection);
                break;
            default:
                bind(DatabaseConnection.class).toInstance(() -> {});
                break;
        }
        bind(NDatabaseConfig.class).toInstance(nDatabaseConfig);

        bind(AsyncThreadPool.class).toInstance(new AsyncThreadPool(nDatabaseConfig.getIdleThreadPoolSize()));
        bind(NDatabaseAPI.class).to(NDatabaseAPIImpl.class);
    }

    public void load() throws NDatabaseLoadException {
        try {
            loadConfigAndDepencies();
            com.nivixx.ndatabase.core.Injector.resolveInstance(DatabaseConnection.class).connect();
        } catch (Throwable t) {
            throw new NDatabaseLoadException("Failed to load NDatabase, error during configuration or loading", t);
        }
    }

    private void loadConfigAndDepencies() throws Throwable{
        // Init dependency injection system
        Injector injector = Guice.createInjector(this);
        com.nivixx.ndatabase.core.Injector.set(injector);

        // init the API instance
        // The usage of the lib is by calling NDatabase.api()
        Class<NDatabase> clazz = NDatabase.class;
        Method method = clazz.getDeclaredMethod("set", NDatabaseAPI.class);
        method.setAccessible(true);
        method.invoke(null, injector.getInstance(NDatabaseAPI.class));
    }

    public abstract DBLogger supplyDbLogger(boolean isDebugMode);
    public abstract SyncExecutor supplySyncExecutor();
    public abstract NDatabaseConfig supplyNDatabaseConfig();
}
