package com.nivixx.ndatabase.platforms.appplatform;

import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
public class AppSyncExecutor implements SyncExecutor {
    @Override
    public void runSync(Runnable runnable) {
        runnable.run();
    }
}
