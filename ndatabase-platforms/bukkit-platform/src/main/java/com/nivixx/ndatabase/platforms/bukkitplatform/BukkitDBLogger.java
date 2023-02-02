package com.nivixx.ndatabase.platforms.bukkitplatform;

import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;
import org.bukkit.Bukkit;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

public class BukkitDBLogger extends DBLogger {

    public BukkitDBLogger(boolean isDebugMode) {
        super(isDebugMode);
    }

    @Override
    public Consumer<Supplier<String>> consumeDebugMessage() {
        if(!isDebugMode) return (msg) -> {};
        return (msg) -> Bukkit.getLogger().info("NDatabase-debug: " + msg.get());
    }

    @Override
    public Consumer<Supplier<String>> consumeInfoMessage() {
        return (msg) -> Bukkit.getLogger().info(msg.get());
    }

    @Override
    public BiConsumer<Throwable, String> consumeErrorMessage() {
        return (throwable, msg) -> Bukkit.getLogger().log(Level.SEVERE, msg, throwable);
    }

    @Override
    public Consumer<Supplier<String>> consumeWarnMessage() {
        return (msg) -> Bukkit.getLogger().warning(msg.get());
    }
}
