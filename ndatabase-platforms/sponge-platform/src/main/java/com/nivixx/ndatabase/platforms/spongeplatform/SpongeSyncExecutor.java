package com.nivixx.ndatabase.platforms.spongeplatform;

import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scheduler.TaskExecutorService;
import org.spongepowered.plugin.PluginContainer;

public class SpongeSyncExecutor implements SyncExecutor {

    private final PluginContainer pluginContainer;

    public SpongeSyncExecutor(PluginContainer pluginContainer) {
        this.pluginContainer = pluginContainer;
    }

    @Override
    public void runSync(Runnable runnable) {
        Task syncTask = Task.builder()
                .execute(runnable)
                .plugin(pluginContainer)
                .build();

        Sponge.server().scheduler().submit(syncTask);
    }
}
