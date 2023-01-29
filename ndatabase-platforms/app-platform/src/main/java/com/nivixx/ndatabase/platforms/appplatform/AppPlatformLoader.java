package com.nivixx.ndatabase.platforms.appplatform;

import com.nivixx.ndatabase.core.PlatformLoader;
import com.nivixx.ndatabase.core.config.NDatabaseConfig;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

/**
 * For regular java app
 */
public abstract class AppPlatformLoader extends PlatformLoader {

    @Override
    public DBLogger supplyDbLogger() {
        return new AppDBLogger();
    }

    public abstract SyncExecutor supplySyncExecutor();

    public abstract NDatabaseConfig supplyNDatabaseConfig();

}
