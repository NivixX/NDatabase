package com.nivixx.ndatabase.core.promise;

import com.nivixx.ndatabase.api.Promise;
import com.nivixx.ndatabase.core.DatabaseCallback;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AsyncResultImpl<T> implements Promise.AsyncResult<T> {

    private final CompletableFuture<T> databaseResultFuture;
    private final AtomicReference<DatabaseCallback<T>> databaseCallback;
    private final SyncExecutor syncExecutor;
    private final DBLogger dbLogger;

    // From where this promise has been called
    private StackTraceElement stackTraceElementCaller;

    public AsyncResultImpl(CompletableFuture<T> valueResultFuture, SyncExecutor syncExecutor, DBLogger dbLogger) {
        this.databaseResultFuture = valueResultFuture;
        this.databaseCallback = new AtomicReference<>();
        this.syncExecutor = syncExecutor;
        this.dbLogger = dbLogger;
    }

    @Override
    public CompletableFuture<T> getResultFuture() {
        return databaseResultFuture;
    }

    @Override
    public void thenAsync(Consumer<T> valueConsumer) {
        setCallbackAndHandlePromise(new DatabaseCallback<>(true, valueConsumer));
    }

    private void setCallbackAndHandlePromise(DatabaseCallback<T> bukkitCallback) {
        if(this.databaseCallback.get() != null) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[1];
            dbLogger.logWarn(String.format("%s\nAsync database result promise callback called twice," +
                    " the async result as already been consumed", stackTraceElement.toString()));
            return;
        }
        this.databaseCallback.set(bukkitCallback);
        handleDatabasePromise();
    }

    @Override
    public void thenSync(Consumer<T> valueConsumer) {
        setCallbackAndHandlePromise(new DatabaseCallback<>(false, valueConsumer));
    }

    @Override
    public void thenAsync(BiConsumer<T, Throwable> valueConsumer) {
        setCallbackAndHandlePromise(new DatabaseCallback<>(true, valueConsumer));
    }

    @Override
    public void thenSync(BiConsumer<T, Throwable> valueConsumer) {
        setCallbackAndHandlePromise(new DatabaseCallback<>(true, valueConsumer));
    }

    public void handleDatabasePromise() {
        Executors.newCachedThreadPool().execute(() -> {
            T entityResult;
            DatabaseCallback<T> bukkitCallback = databaseCallback.get();

            try {
                entityResult = databaseResultFuture.get();
                if(bukkitCallback.isAsync()) {
                    bukkitCallback.getCallback().accept(entityResult, null);
                }
                else { // SYNC
                    syncExecutor.runSync(() -> bukkitCallback.getCallback().accept(entityResult, null));
                }
            } catch (Throwable e) {
                handleDatabaseException(bukkitCallback, e);
            }
        });
    }

    private void handleDatabaseException(DatabaseCallback<T> bukkitCallback, Throwable e) {
        if(!bukkitCallback.isProvidedExceptionHandler()) {
            dbLogger.logWarn(
                    String.format("Async database result promise ended with an" +
                            " exception and you didn't handled the exception, error message: '%s'." +
                            "If you want to handle the exception, you can use the " +
                            "then or thenAsync((entity, throwable) -> ) method.", e.getMessage()));
            return;
        }
        if(bukkitCallback.isAsync()) {
            bukkitCallback.getCallback().accept(null, e);
        }
        else { // SYNC
            syncExecutor.runSync(() -> bukkitCallback.getCallback().accept(null, e));
        }
    }
}
