package com.nivixx.ndatabase.api;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * NDatabase - KeyValue store database
 * Report any issue or contribute here https://github.com/NivixX/NDatabase
 *
 * Promise interface to handle the Async to Sync mechanism
 * You can easily do async call to generate a CompletableFuture
 * and handle this future task to schedule callback in the main thread
 */
public interface Promise {

    /**
     * Async pipeline in the case your database operation return a result
     * @param <E> Your Entity class
     */
    interface AsyncResult<E> {

        /**
         * @return future task, handle it as your needs
         */
        CompletableFuture<E> getResultFuture();

        /**
         * retrieve data async and consume it in the same async thread once it's available.
         * This method doesn't handle exception, so if an Exception occurred during
         * the data retrieving, your consumer will be ignored and won't be called.
         * <pre>{@code
         *     .thenAsync((entity) -> {
         *         // handle entity
         *         // will not be called if an exception occurred
         *     })
         * }</pre>
         * Note that if an exception occurred NDatabase with handle and log a warning message for you
         * telling that you didn't handle the exception and the code line will be specified.
         *
         * @param valueConsumer your entity value consumer.
         *                      Will be called only if there is no exception.
         *                      E will be null if no result was found
         */
        void thenAsync(Consumer<E> valueConsumer);

        /**
         * retrieve data async and consume it in the main thread once it's available.
         * This method doesn't handle exception, so if an Exception occurred during
         * the data retrieving, your consumer will be ignored and won't be called.
         * <pre>{@code
         *     .thenSync((entity) -> {
         *         // handle entity
         *         // will not be called if an exception occurred
         *     })
         * }</pre>
         * Note that if an exception occurred NDatabase with handle and log a warning message for you
         * telling that you didn't handle the exception and the code line will be specified.
         *
         * @param valueConsumer your entity value consumer.
         *                      Will be called only if there is no exception.
         *                      E will be null if no result was found
         */
        void thenSync(Consumer<E> valueConsumer);

        /**
         * retrieve data async and consume it in the same async thread once it's available.
         * This method handle exception if an exception occurred.
         * In case where the task ended exceptionally, E value will be null and Throwable
         * will contain your exception.
         * <pre>{@code
         *     .thenAsync((entity, throwable) -> {
         *         if(throwable != null) {
         *             // Handle exception
         *             return;
         *         }
         *         // handle entity
         *     })
         * }</pre>
         * @param valueConsumer your entity, exception value consumer.
         *                      E will be null if no result was found
         */
        void thenAsync(BiConsumer<E, Throwable> valueConsumer);

        /**
         * retrieve data async and consume it in the main thread once it's available.
         * This method handle exception if an exception occurred.
         * In case where the task ended exceptionally, E value will be null and Throwable
         * will contain your exception.
         * <pre>{@code
         *     .thenSync((entity, throwable) -> {
         *         if(throwable != null) {
         *             // Handle exception
         *             return;
         *         }
         *         // handle entity
         *     })
         * }</pre>
         * @param valueConsumer your entity, exception value consumer.
         *                      E will be null if no result was found
         */
        void thenSync(BiConsumer<E, Throwable> valueConsumer);
    }

    /**
     * Async pipeline in the case your database operation doesn't return a result
     */
    interface AsyncEmptyResult {

        /**
         * @return future task, handle it as your needs
         */
        CompletableFuture<Void> getResultFuture();

        /**
         * process your database operation async and run a callback in the same async thread when finished
         * This method doesn't handle exception, so if an Exception occurred during
         * the operation, your callback will be ignored and won't be called.
         * <pre>{@code
         *     .thenAsync(() -> {
         *         // execute callback
         *         // will not be called if an exception occurred
         *     })
         * }</pre>
         * Note that if an exception occurred NDatabase with handle and log a warning message for you
         * telling that you didn't handle the exception and the code line will be specified.
         *
         * @param callback that will be called only if there is no exception.
         */
        void thenAsync(Runnable callback);

        /**
         * process your database operation async and run a callback in the main thread when finished
         * This method doesn't handle exception, so if an Exception occurred during
         * the operation, your callback will be ignored and won't be called.
         * <pre>{@code
         *     .thenAsync(() -> {
         *         // execute callback
         *         // will not be called if an exception occurred
         *     })
         * }</pre>
         * Note that if an exception occurred NDatabase with handle and log a warning message for you
         * telling that you didn't handle the exception and the code line will be specified.
         *
         * @param callback that will be called only if there is no exception.
         */
        void thenSync(Runnable callback);

        /**
         * process your database operation async and run a callback in the same async thread when finished
         * This method handle exception if an exception occurred.
         * <pre>{@code
         *     .thenAsync((throwable) -> {
         *         if(throwable != null) {
         *             // handle exception
         *             return;
         *         }
         *         // execute callback
         *     })
         * }</pre>
         * @param throwableConsumer throwable consumer.
         *                          will be null if no exception occurred
         */
        void thenAsync(Consumer<Throwable> throwableConsumer);

        /**
         * process your database operation async and run a callback in the main thread when finished
         * This method handle exception if an exception occurred.
         * <pre>{@code
         *     .thenSync((throwable) -> {
         *         if(throwable != null) {
         *             // handle exception
         *             return;
         *         }
         *         // execute callback
         *     })
         * }</pre>
         * @param throwableConsumer throwable consumer.
         *                          will be null if no exception occurred
         */
        void thenSync(Consumer<Throwable> throwableConsumer);
    }

}
