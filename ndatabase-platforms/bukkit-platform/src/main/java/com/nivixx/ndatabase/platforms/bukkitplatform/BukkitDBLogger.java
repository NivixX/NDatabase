package com.nivixx.ndatabase.platforms.bukkitplatform;

import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;
import org.bukkit.Bukkit;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;

public class BukkitDBLogger implements DBLogger {

    private boolean debugMode;

    public BukkitDBLogger(boolean debugMode) {
        this.debugMode = debugMode;
    }

    @Override
    public Consumer<String> consumeDebugMessage() {
        if(!debugMode) return (msg) -> {};
        return (msg) -> Bukkit.getLogger().info("NDatabase-debug: " + msg);
    }

    @Override
    public Consumer<String> consumeInfoMessage() {
        return (msg) -> Bukkit.getLogger().info(msg);
    }

    @Override
    public BiConsumer<Throwable, String> consumeErrorMessage() {
        return (throwable, msg) -> Bukkit.getLogger().log(Level.SEVERE, msg, throwable);
    }

    @Override
    public Consumer<String> consumeWarnMessage() {
        return (msg) -> Bukkit.getLogger().warning(msg);
    }
}
