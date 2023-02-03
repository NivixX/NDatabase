package com.nivixx.ndatabase.api;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Promise {

    interface AsyncResult<E> {

        CompletableFuture<E> getResultFuture();

        void thenAsync(Consumer<E> valueConsumer);
        void thenSync(Consumer<E> valueConsumer);

        void thenAsync(BiConsumer<E, Throwable> valueConsumer);
        void thenSync(BiConsumer<E, Throwable> valueConsumer);
    }

    interface AsyncEmptyResult { //TODO void

        CompletableFuture<Void> getResultFuture();

        void thenAsync(Runnable callback);
        void thenSync(Runnable callback);

        void thenAsync(Consumer<Throwable> throwableConsumer);
        void thenSync(Consumer<Throwable> throwableConsumer);
    }

}
