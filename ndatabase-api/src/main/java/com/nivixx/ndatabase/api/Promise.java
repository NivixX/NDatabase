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

}
