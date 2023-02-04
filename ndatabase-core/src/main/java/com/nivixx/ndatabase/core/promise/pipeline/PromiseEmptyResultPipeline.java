package com.nivixx.ndatabase.core.promise.pipeline;

import com.nivixx.ndatabase.api.Promise;
import com.nivixx.ndatabase.core.promise.AsyncThreadPool;
import com.nivixx.ndatabase.core.promise.callback.PromiseEmptyResultCallback;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class PromiseEmptyResultPipeline<P extends PromiseEmptyResultCallback, E> extends PromisePipeline<P,E> implements Promise.AsyncEmptyResult {

    public PromiseEmptyResultPipeline(CompletableFuture<E> databaseResultFuture,
                                      SyncExecutor syncExecutor,
                                      AsyncThreadPool asyncThreadPool,
                                      DBLogger dbLogger) {
        super(databaseResultFuture, syncExecutor, asyncThreadPool, dbLogger);
    }

    public CompletableFuture<Void> getResultFuture() {
        return (CompletableFuture<Void>) databaseResultFuture;
    }


    @Override
    public void thenAsync(Runnable callback) {
        setCallbackAndHandlePromise((P) new PromiseEmptyResultCallback(true, callback));
    }

    @Override
    public void thenSync(Runnable callback) {
        setCallbackAndHandlePromise((P) new PromiseEmptyResultCallback(false, callback));
    }

    @Override
    public void thenAsync(Consumer<Throwable> throwableConsumer) {
        setCallbackAndHandlePromise((P) new PromiseEmptyResultCallback(true, throwableConsumer));
    }

    @Override
    public void thenSync(Consumer<Throwable> throwableConsumer) {
        setCallbackAndHandlePromise((P) new PromiseEmptyResultCallback(false, throwableConsumer));
    }


    @Override
    public void handleDatabasePromise() {
        asyncThreadPool.getExecutor().execute(() -> {
            P promiseCallback = promiseCallbackRef.get();

            try {
                databaseResultFuture.get();
                if(promiseCallback.isAsync()) {
                    promiseCallback.getEmptyResultCallback().accept(null);
                }
                else { // SYNC
                    syncExecutor.runSync(() -> promiseCallback.getEmptyResultCallback().accept(null));
                }
            } catch (Throwable e) {
                handleDatabaseException(promiseCallback, e);
            }
        });
    }

    @Override
    protected void handleDatabaseException(P bukkitCallback, Throwable e) {
        if(bukkitCallback.isProvidedExceptionHandler()) {
            dbLogger.logWarn(
                    String.format("Async database result promise ended with an" +
                            " exception and you didn't handled the exception, error message: '%s'." +
                            "If you want to handle the exception, you can use the " +
                            "then or thenAsync((entity, throwable) -> ) method.", e.getMessage()));
            return;
        }
        if(bukkitCallback.isAsync()) {
            bukkitCallback.getEmptyResultCallback().accept(e);
        }
        else { // SYNC
            syncExecutor.runSync(() -> bukkitCallback.getEmptyResultCallback().accept(e));
        }
    }
}
