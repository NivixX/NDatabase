package com.nivixx.ndatabase.platforms.bukkitplatform;

import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import org.bukkit.Bukkit;

public class BukkitSyncExecutor implements SyncExecutor {
    @Override
    public void runSync(Runnable runnable) {
        Bukkit.getScheduler().runTask(NDatabasePlugin.getInstance(), runnable);
    }
}
