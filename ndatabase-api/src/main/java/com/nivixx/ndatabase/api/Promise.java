package com.nivixx.ndatabase.api;

import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.model.NEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Promise {

    interface AsyncResult<T> {

        CompletableFuture<T> getResultFuture();

        void thenAsync(Consumer<T> valueConsumer);
        void thenSync(Consumer<T> valueConsumer);

        void thenAsync(BiConsumer<T, Throwable> valueConsumer);
        void thenSync(BiConsumer<T, Throwable> valueConsumer);
    }

    interface ResultOptionalFromAsync<T> {

        CompletableFuture<T> getResultFuture();

        void thenAsync(Consumer<T> valueConsumer);
        void thenSync(Consumer<T> valueConsumer);

        void thenAsync(BiConsumer<T, NDatabaseException> valueConsumer);
        void thenSync(BiConsumer<T, NDatabaseException> valueConsumer);
    }

    interface ResultListFromAsync<K, V extends NEntity<K>> {

        CompletableFuture<List<V>> getResultFuture();

        void thenAsync(Consumer<List<V>> valueConsumer);
        void thenSync(Consumer<List<V>> valueConsumer);

        void thenAsync(BiConsumer<List<V>, NDatabaseException> valueConsumer);
        void thenSync(BiConsumer<List<V>, NDatabaseException> valueConsumer);
    }

    interface ResultFromSync<V extends NEntity<?>> {

        V getResult();

        void then(Consumer<V> valueConsumer);
        void thenAsync(Consumer<V> valueConsumer);
        void thenSync(Consumer<V> valueConsumer);

        void then(BiConsumer<V, NDatabaseException> valueConsumer);
        void thenAsync(BiConsumer<V, NDatabaseException> valueConsumer);
        void thenSync(BiConsumer<V, NDatabaseException> valueConsumer);
    }

    interface NoResultFromAsync {

        CompletableFuture<Void> getResultFuture();

        void thenAsync(Runnable valueConsumer);
        void thenSync(Runnable valueConsumer);
    }

    interface NoResultFromSync {


        void then(Runnable valueConsumer);
        void thenAsync(Runnable valueConsumer);
        void thenSync(Runnable valueConsumer);
    }
}
