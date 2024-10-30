package com.nivixx.ndatabase.platforms.spongeplatform;

import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SpongeDBLogger extends DBLogger {

    private final Logger logger;

    public SpongeDBLogger(boolean isDebugMode) {
        super(isDebugMode);
        this.logger = NDatabasePlugin.getLogger();
    }

    @Override
    public Consumer<Supplier<String>> consumeDebugMessage() {
        if (!isDebugMode) return (msg) -> {
            logger.debug(msg.get());
        };
        // Actually debug level are not logged by default, so we wanna
        // log debug entries as info in sponge
        return (msg) -> logger.info("NDatabase-debug: " + msg.get());
    }

    @Override
    public Consumer<Supplier<String>> consumeInfoMessage() {
        return (msg) -> logger.info(msg.get());
    }

    @Override
    public BiConsumer<Throwable, String> consumeErrorMessage() {
        return (throwable, msg) -> logger.error(msg, throwable);
    }

    @Override
    public Consumer<Supplier<String>> consumeWarnMessage() {
        return (msg) -> logger.warn(msg.get());
    }
}
