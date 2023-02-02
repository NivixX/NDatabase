package com.nivixx.ndatabase.platforms.spongeplatform;

import com.google.inject.Inject;
import com.nivixx.ndatabase.core.PlatformLoader;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.IOException;
import java.nio.file.Path;

@Plugin("NDatabase")
public class NDatabasePlugin {

    @Inject
    private static Logger logger;

    private static NDatabasePlugin nDatabasePlugin;

    private PluginManager pluginManager = Sponge.pluginManager();

    @Listener
    public void onServerStart(final StartedEngineEvent<Server> event) {
        // TODO WIP
        /*
        PlatformLoader plaformLoader = new SpongePlaformLoader();
        nDatabasePlugin = this;

        Path potentialFile = getConfigPath();
        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().path(potentialFile)
                        .build();
        ConfigurationNode rootNode;
        try {
            rootNode = loader.load();
        } catch(IOException e) {
            // handle error
        }

        PluginContainer myOtherPlugin = pluginManager.plugin("NDatabase").orElse(null);
        Sponge.configManager().pluginConfig(myOtherPlugin).
        loader = HoconConfigurationLoader.builder().setPath(path).build();
        rootNode = loader.load();
        try {
            plaformLoader.load();
        } catch (Throwable e) {
            throw new IllegalStateException("Could not init NDatabase bukkit plugin.", e);
        }
        logger.info("Successfully running NDatabase");

         */
    }

    public Logger getLogger() {
        return logger;
    }

    public static NDatabasePlugin getInstance() {
        return nDatabasePlugin;
    }
}
