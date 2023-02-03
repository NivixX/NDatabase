package com.nivixx.ndatabase.core.promise.callback;

import java.util.function.Consumer;

public class PromiseEmptyResultCallback extends PromiseCallback {

    private final Consumer<Throwable> emptyResultCallback;

    public PromiseEmptyResultCallback(boolean isAsync, Runnable callback) {
        super(isAsync, false);
        this.emptyResultCallback = (t) -> callback.run();
    }

    public PromiseEmptyResultCallback(boolean isAsync, Consumer<Throwable> callback) {
        super(isAsync, true);
        this.emptyResultCallback = callback;
    }

    public Consumer<Throwable> getEmptyResultCallback() {
        return emptyResultCallback;
    }
}