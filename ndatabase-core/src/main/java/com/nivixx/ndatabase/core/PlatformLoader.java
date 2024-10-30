package com.nivixx.ndatabase.core;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.nivixx.ndatabase.api.NDatabase;
import com.nivixx.ndatabase.api.NDatabaseAPI;
import com.nivixx.ndatabase.api.exception.NDatabaseLoadException;
import com.nivixx.ndatabase.core.config.NDatabaseConfig;
import com.nivixx.ndatabase.core.promise.AsyncThreadPool;
import com.nivixx.ndatabase.dbms.api.DatabaseConnection;
import com.nivixx.ndatabase.dbms.mariadb.HikariMariaConnectionPool;
import com.nivixx.ndatabase.dbms.mongodb.MongodbConnection;
import com.nivixx.ndatabase.dbms.mysql.HikariConnectionPool;
import com.nivixx.ndatabase.dbms.sqlite.SqliteConnectionPool;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;
import net.byteflux.libby.LibraryManager;

import java.lang.reflect.Method;

public abstract class PlatformLoader extends AbstractModule {

    @Override
    protected void configure() {
        LibraryManager libraryManager = supplyLibraryManager();
        libraryManager.addMavenCentral();

        bind(SyncExecutor.class).toInstance(supplySyncExecutor());

        NDatabaseConfig nDatabaseConfig = supplyNDatabaseConfig();
        nDatabaseConfig.verifyConfig();
        DBLogger dbLogger = supplyDbLogger(nDatabaseConfig.isDebugMode());
        bind(DBLogger.class).toInstance(dbLogger);
        switch (nDatabaseConfig.getDatabaseType()) {
            case MYSQL:
                HikariConnectionPool hikariConnectionPool =
                        new HikariConnectionPool(nDatabaseConfig.getMysqlConfig(), dbLogger, libraryManager);
                bind(HikariConnectionPool.class).toInstance(hikariConnectionPool);
                bind(DatabaseConnection.class).toInstance(hikariConnectionPool);
                break;
            case MARIADB:
                HikariMariaConnectionPool hikariConnectionPoolMaria = new HikariMariaConnectionPool(nDatabaseConfig.getMariaDBConfig());
                bind(HikariMariaConnectionPool.class).toInstance(hikariConnectionPoolMaria);
                bind(DatabaseConnection.class).toInstance(hikariConnectionPoolMaria);
                break;
            case SQLITE:
                SqliteConnectionPool sqliteConnectionPool = new SqliteConnectionPool(nDatabaseConfig.getSqliteConfig(), dbLogger, libraryManager);
                bind(SqliteConnectionPool.class).toInstance(sqliteConnectionPool);
                bind(DatabaseConnection.class).toInstance(sqliteConnectionPool);
                break;
            case MONGODB:
                MongodbConnection mongodbConnection = new MongodbConnection(nDatabaseConfig.getMongoDBConfig(), dbLogger, libraryManager);
                bind(MongodbConnection.class).toInstance(mongodbConnection);
                bind(DatabaseConnection.class).toInstance(mongodbConnection);
                break;
            default:
                bind(DatabaseConnection.class).toInstance(() -> {});
                break;
        }
        bind(NDatabaseConfig.class).toInstance(nDatabaseConfig);

        bind(AsyncThreadPool.class).toInstance(new AsyncThreadPool(nDatabaseConfig.getIdleThreadPoolSize()));
        bind(NDatabaseAPI.class).toInstance(new NDatabaseAPIImpl());
    }

    public void load() throws NDatabaseLoadException {
        try {
            loadConfigAndDependencies();
            com.nivixx.ndatabase.core.Injector.resolveInstance(DatabaseConnection.class).connect();
        } catch (Throwable t) {
            throw new NDatabaseLoadException("Failed to load NDatabase, error during configuration or loading", t);
        }
    }

    private void loadConfigAndDependencies() throws Throwable {
        // Init dependency injection system
        Injector injector = Guice.createInjector(this);
        com.nivixx.ndatabase.core.Injector.set(injector);

        // init the API instance
        // The usage of the lib is by calling NDatabase.api()
        Class<NDatabase> clazz = NDatabase.class;
        Method method = clazz.getDeclaredMethod("set", NDatabaseAPI.class);
        method.setAccessible(true);

        NDatabaseAPI instance = injector.getInstance(NDatabaseAPI.class);

        method.invoke(null, instance);
    }

    public abstract DBLogger supplyDbLogger(boolean isDebugMode);
    public abstract SyncExecutor supplySyncExecutor();
    public abstract NDatabaseConfig supplyNDatabaseConfig();
    public abstract LibraryManager supplyLibraryManager();
}
