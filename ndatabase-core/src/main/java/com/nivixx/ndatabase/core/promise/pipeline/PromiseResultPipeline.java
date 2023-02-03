package com.nivixx.ndatabase.core.promise.pipeline;

import com.nivixx.ndatabase.api.Promise;
import com.nivixx.ndatabase.core.promise.callback.PromiseCallback;
import com.nivixx.ndatabase.core.promise.callback.PromiseResultCallback;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PromiseResultPipeline<P extends PromiseResultCallback<E>, E> extends PromisePipeline<P,E> implements Promise.AsyncResult<E> {

    public PromiseResultPipeline(CompletableFuture<E> databaseResultFuture,
                                 SyncExecutor syncExecutor,
                                 DBLogger dbLogger) {
        super(databaseResultFuture, syncExecutor, dbLogger);
    }


    @Override
    public CompletableFuture<E> getResultFuture() {
        return databaseResultFuture;
    }

    @Override
    public void thenAsync(Consumer<E> valueConsumer) {
        PromiseResultCallback<E> promiseResultCallback = new PromiseResultCallback<E>(true, valueConsumer);
        PromiseCallback a = promiseResultCallback;
        setCallbackAndHandlePromise((P) a); // TODO
    }

    @Override
    public void thenSync(Consumer<E> valueConsumer) {
        setCallbackAndHandlePromise((P) new PromiseResultCallback<>(false, valueConsumer));
    }

    @Override
    public void thenAsync(BiConsumer<E, Throwable> valueConsumer) {
        setCallbackAndHandlePromise((P) new PromiseResultCallback<>(true, valueConsumer));
    }

    @Override
    public void thenSync(BiConsumer<E, Throwable> valueConsumer) {
        setCallbackAndHandlePromise((P) new PromiseResultCallback<>(true, valueConsumer));
    }

    @Override
    public void handleDatabasePromise() {
        Executors.newCachedThreadPool().execute(() -> {
            E entityResult;
            P promiseCallback = promiseCallbackRef.get();

            try {
                entityResult = databaseResultFuture.get();
                if(promiseCallback.isAsync()) {
                    promiseCallback.getResultCallback().accept(entityResult, null);
                }
                else { // SYNC
                    syncExecutor.runSync(() -> promiseCallback.getResultCallback().accept(entityResult, null));
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
            bukkitCallback.getResultCallback().accept(null, e);
        }
        else { // SYNC
            syncExecutor.runSync(() -> bukkitCallback.getResultCallback().accept(null, e));
        }
    }
}
