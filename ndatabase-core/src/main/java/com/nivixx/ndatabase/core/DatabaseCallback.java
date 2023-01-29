package com.nivixx.ndatabase.core;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DatabaseCallback<T> {
    private boolean isAsync;
    private boolean isProvidedExceptionHandler;
    private BiConsumer<T, Throwable> callback;

    public DatabaseCallback(boolean isAsync, Consumer<T> callback) {
        this.isAsync = isAsync;
        this.callback = (e, t) -> callback.accept(e);
        this.isProvidedExceptionHandler = false;
    }

    public DatabaseCallback(boolean isAsync, BiConsumer<T, Throwable> callback) {
        this.isAsync = isAsync;
        this.callback = callback;
        this.isProvidedExceptionHandler = true;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public boolean isProvidedExceptionHandler() {
        return isProvidedExceptionHandler;
    }

    public BiConsumer<T, Throwable> getCallback() {
        return callback;
    }
}