package com.nivixx.ndatabase.core.promise.callback;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PromiseResultCallback<E> extends PromiseCallback {

    private final BiConsumer<E, Throwable> resultCallback;

    public PromiseResultCallback(boolean isAsync, Consumer<E> callback) {
        super(isAsync, false);
        this.resultCallback = (e, t) -> callback.accept(e);
    }

    public PromiseResultCallback(boolean isAsync, BiConsumer<E, Throwable> callback) {
        super(isAsync, true);
        this.resultCallback = callback;
    }

    public BiConsumer<E, Throwable> getResultCallback() {
        return resultCallback;
    }
}