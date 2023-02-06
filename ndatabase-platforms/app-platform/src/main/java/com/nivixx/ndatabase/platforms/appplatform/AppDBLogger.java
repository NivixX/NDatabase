package com.nivixx.ndatabase.platforms.appplatform;

import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AppDBLogger extends DBLogger {

    public AppDBLogger(boolean isDebugMode) {
        super(isDebugMode);
    }


    @Override
    public Consumer<Supplier<String>> consumeDebugMessage() {
        return (msg) -> System.out.println(msg.get());
    }

    @Override
    public Consumer<Supplier<String>> consumeInfoMessage() {
        return (msg) -> System.out.println(msg.get());
    }

    @Override
    public BiConsumer<Throwable, String> consumeErrorMessage() {
        return (throwable, msg) -> {
            System.out.println(msg);
            throwable.printStackTrace();
        };
    }

    @Override
    public Consumer<Supplier<String>> consumeWarnMessage() {
        return (msg) -> System.out.println(msg.get());
    }
}
