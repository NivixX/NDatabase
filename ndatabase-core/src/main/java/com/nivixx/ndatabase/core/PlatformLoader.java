package com.nivixx.ndatabase.core;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.nivixx.ndatabase.api.NDatabase;
import com.nivixx.ndatabase.api.NDatabaseAPI;
import com.nivixx.ndatabase.api.exception.NDatabaseLoadException;
import com.nivixx.ndatabase.core.config.DatabaseType;
import com.nivixx.ndatabase.core.config.NDatabaseConfig;
import com.nivixx.ndatabase.core.dao.mysql.MysqlConnectionPool;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.lang.reflect.Method;

public abstract class PlatformLoader extends AbstractModule  {

    @Override
    protected void configure() {
        bind(SyncExecutor.class).toInstance(supplySyncExecutor());

        NDatabaseConfig nDatabaseConfig = supplyNDatabaseConfig();
        nDatabaseConfig.verifyConfig();
        if(nDatabaseConfig.getDatabaseType() == DatabaseType.MYSQL) {
            bind(MysqlConnectionPool.class).toInstance(new MysqlConnectionPool(nDatabaseConfig.getMysqlConfig()));
        }
        bind(NDatabaseConfig.class).toInstance(nDatabaseConfig);

        bind(DBLogger.class).toInstance(supplyDbLogger(nDatabaseConfig));
        bind(NDatabaseAPI.class).to(NDatabaseAPIImpl.class);
    }

    public void load() throws NDatabaseLoadException {
        try {
            loadConfigAndDepencies();
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

    protected abstract DBLogger supplyDbLogger(NDatabaseConfig nDatabaseConfig);
    protected abstract SyncExecutor supplySyncExecutor();
    protected abstract NDatabaseConfig supplyNDatabaseConfig();
}
