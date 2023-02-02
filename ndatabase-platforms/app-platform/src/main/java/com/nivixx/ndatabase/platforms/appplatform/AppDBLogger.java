package com.nivixx.ndatabase.platforms.appplatform;

import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AppDBLogger extends DBLogger {

    public AppDBLogger(boolean isDebugMode) {
        super(isDebugMode);
    }

    private static final Logger LOGGER = LogManager.getLogger(AppDBLogger.class);

    @Override
    public Consumer<Supplier<String>> consumeDebugMessage() {
        return (msg) -> LOGGER.debug(msg.get());
    }

    @Override
    public Consumer<Supplier<String>> consumeInfoMessage() {
        return (msg) -> LOGGER.info(msg.get());
    }

    @Override
    public BiConsumer<Throwable, String> consumeErrorMessage() {
        return (throwable, msg) -> LOGGER.error(msg, throwable);
    }

    @Override
    public Consumer<Supplier<String>> consumeWarnMessage() {
        return (msg) -> LOGGER.warn(msg.get());
    }
}
