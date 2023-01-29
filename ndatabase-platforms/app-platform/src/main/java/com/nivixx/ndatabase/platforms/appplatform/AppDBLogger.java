package com.nivixx.ndatabase.platforms.appplatform;

import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AppDBLogger implements DBLogger {

    private static final Logger LOGGER = LogManager.getLogger(AppDBLogger.class);

    @Override
    public Consumer<String> consumeDebugMessage() {
        return LOGGER::debug;
    }

    @Override
    public Consumer<String> consumeInfoMessage() {
        return LOGGER::info;
    }

    @Override
    public BiConsumer<Throwable, String> consumeErrorMessage() {
        return (throwable, msg) -> LOGGER.error(msg, throwable);
    }

    @Override
    public Consumer<String> consumeWarnMessage() {
        return LOGGER::warn;
    }
}
