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

    private final CompletableFuture<T> valueResultFuture;

    private AtomicReference<DatabaseCallback<T>> databaseCallback;

    private SyncExecutor syncExecutor;

    private DBLogger dbLogger;

    // From where this promise has been called
    private StackTraceElement stackTraceElementCaller;

    public AsyncResultImpl(CompletableFuture<T> valueResultFuture, SyncExecutor syncExecutor, DBLogger dbLogger) {
        this.valueResultFuture = valueResultFuture;
        this.databaseCallback = new AtomicReference<>();
        this.syncExecutor = syncExecutor;
        this.dbLogger = dbLogger;
    }

    @Override
    public CompletableFuture<T> getResultFuture() {
        return valueResultFuture;
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
        handlePromise();
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

    public void handlePromise() {
        Executors.newCachedThreadPool().execute(() -> {
            T value;
            DatabaseCallback<T> bukkitCallback = databaseCallback.get();
            try {
                value = valueResultFuture.get();
                valueResultFuture.handle((t, throwable) -> {
                   throwable.printStackTrace();
                    return null;
                });
                if(bukkitCallback.isAsync()) {
                    bukkitCallback.getCallback().accept(value, null);
                }
                else { // SYNC
                    syncExecutor.runSync(() -> bukkitCallback.getCallback().accept(value, null));
                }

            } catch (Throwable e) {
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

        });
    }
}
