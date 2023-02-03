package com.nivixx.ndatabase.core.promise.pipeline;

import com.nivixx.ndatabase.core.promise.callback.PromiseCallback;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public abstract class PromisePipeline<P extends PromiseCallback, E>  {

    protected final CompletableFuture<E> databaseResultFuture;
    protected final SyncExecutor syncExecutor;
    protected final DBLogger dbLogger;

    protected AtomicReference<P> promiseCallbackRef;

    // TODO From where this promise has been called TODO
    private StackTraceElement stackTraceElementCaller;

    public PromisePipeline(CompletableFuture<E> databaseResultFuture,
                           SyncExecutor syncExecutor,
                           DBLogger dbLogger) {

        this.databaseResultFuture = databaseResultFuture;
        this.syncExecutor = syncExecutor;
        this.dbLogger = dbLogger;
        this.promiseCallbackRef = new AtomicReference<>();
    }
    protected void setCallbackAndHandlePromise(P promiseCallback) {
        if(this.promiseCallbackRef.get() != null) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[1];
            dbLogger.logWarn(String.format("%s\nAsync database result promise callback called twice," +
                    " the async result as already been consumed", stackTraceElement.toString()));
            return;
        }
        this.promiseCallbackRef.set(promiseCallback);
        handleDatabasePromise();
    }

    protected abstract void handleDatabasePromise();

    protected abstract void handleDatabaseException(P promiseCallback, Throwable e);
}
