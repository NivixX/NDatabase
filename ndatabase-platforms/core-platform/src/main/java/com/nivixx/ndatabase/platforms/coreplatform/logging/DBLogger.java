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
        consumeInfoMessage().accept("BLABLABLA");
    }
    default void logUpsert(Object value) {
        consumeDebugMessage().accept("BLABLABLA");
    }
    default void logUpdate(Object value) {
        consumeDebugMessage().accept("BLABLABLA");
    }
    default void logDelete(Object key) {
        consumeDebugMessage().accept("BLABLABLA");
    }
    default void logDeleteAll() {
        consumeInfoMessage().accept("BLABLABLA");
    }
    default void logGet(Object value) {
        consumeDebugMessage().accept("BLABLABLA");
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
