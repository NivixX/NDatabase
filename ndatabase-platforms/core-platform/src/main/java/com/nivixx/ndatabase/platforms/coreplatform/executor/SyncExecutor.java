package com.nivixx.ndatabase.platforms.coreplatform.executor;

public interface SyncExecutor {

    /**
     * Run a task on the main thread and is safe to mute game state
     */
    void runSync(Runnable runnable);
}
