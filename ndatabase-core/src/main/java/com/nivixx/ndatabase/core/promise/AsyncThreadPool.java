package com.nivixx.ndatabase.core.promise;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncThreadPool {

    private final int minIdleThread;
    private final ExecutorService executorService;

    public AsyncThreadPool(int minIdleThread) {
        this.minIdleThread = minIdleThread;
        this.executorService = Executors.newScheduledThreadPool(minIdleThread);
    }

    public int getMinIdleThread() {
        return minIdleThread;
    }

    public ExecutorService getExecutor() {
        return executorService;
    }
}
