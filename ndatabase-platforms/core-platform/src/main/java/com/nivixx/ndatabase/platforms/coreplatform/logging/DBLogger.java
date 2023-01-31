package com.nivixx.ndatabase.platforms.coreplatform.logging;

import com.nivixx.ndatabase.api.model.NEntity;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface DBLogger {

    Consumer<String> consumeDebugMessage();
    Consumer<String> consumeInfoMessage();
    BiConsumer<Throwable, String> consumeErrorMessage();
    Consumer<String> consumeWarnMessage();

    default void logInsert(Object value) {
        consumeInfoMessage().accept(String.format("inserted entity %s", value));
    }
    default void logUpsert(Object value) {
        consumeInfoMessage().accept(String.format("upserted entity %s", value));
    }
    default void logUpdate(Object value) {
        consumeInfoMessage().accept(String.format("updated entity %s", value));
    }
    default void logDelete(Object key) {
        consumeInfoMessage().accept(String.format("deleted entity with key %s", key));
    }
    default void logDeleteAll() {
        consumeInfoMessage().accept(String.format("deleted all %s", "<collection name>")); // TODO entity type
    }
    default void logGet(Object value) {
        consumeInfoMessage().accept(String.format("get entity %s", value));
    }
    default void logError(Throwable throwable, String message) {
        consumeErrorMessage().accept(throwable, message);
    }
    default void logWarn(String message) {
        consumeWarnMessage().accept(message);
    }
    default void logDebug(String message) {
        consumeDebugMessage().accept(message);
    }

}
