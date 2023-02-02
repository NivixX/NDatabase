package com.nivixx.ndatabase.platforms.appplatform;

import com.nivixx.ndatabase.core.Injector;
import com.nivixx.ndatabase.core.PlatformLoader;
import com.nivixx.ndatabase.core.config.NDatabaseConfig;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

/**
 * For regular java app
 */
public abstract class AppPlatformLoader extends PlatformLoader {

    @Override
    public DBLogger supplyDbLogger(NDatabaseConfig nDatabaseConfig) {
        return new AppDBLogger(nDatabaseConfig.isDebugMode());
    }

    public abstract SyncExecutor supplySyncExecutor();

    public abstract NDatabaseConfig supplyNDatabaseConfig();

}
