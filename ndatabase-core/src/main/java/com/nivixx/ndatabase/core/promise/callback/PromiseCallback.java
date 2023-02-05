package com.nivixx.ndatabase.core.promise.callback;

public abstract class PromiseCallback {

    protected final boolean isAsync;
    protected final boolean isProvidedExceptionHandler;

    public PromiseCallback(boolean isAsync, boolean isProvidedExceptionHandler) {
        this.isAsync = isAsync;
        this.isProvidedExceptionHandler = isProvidedExceptionHandler;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public boolean isProvidedExceptionHandler() {
        return isProvidedExceptionHandler;
    }
}