package com.nivixx.ndatabase.platforms.spongeplatform;

import com.nivixx.ndatabase.api.NDatabase;
import com.nivixx.ndatabase.core.PlatformLoader;
import com.nivixx.ndatabase.core.config.NDatabaseConfig;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigRoot;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Note that we don't rely on Google Guice to avoid conflict with NDatabase
 * as it also use Google Guice
 */
@Plugin("ndatabase")
public class NDatabasePlugin {

    private static PluginContainer instance;

    public static PluginContainer getInstance() {
        if (instance == null) {
            instance = Sponge.pluginManager().plugin("ndatabase")
                    .orElseThrow(() ->
                            new IllegalStateException("NDatabase Sponge Plugin instance not found"));
        }
        return instance;
    }

    public static Logger getLogger() {
        return getInstance().logger();
    }

    public static ConfigRoot configRoot() {
        return Sponge.configManager().pluginConfig(getInstance());
    }

    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event) {
        loadConfig();
        loadPlatform();
    }

    private void loadPlatform() {

        try {

            PlatformLoader platformLoader = new SpongePlatformLoader(getInstance());
            NDatabaseConfig nDatabaseConfig = platformLoader.supplyNDatabaseConfig();

            getInstance().logger().info(
                    "\n\n" +
                            "  _   _   ____        _        _                    \n" +
                            " | \\ | | |  _ \\  __ _| |_ __ _| |__   __ _ ___  ___ \n" +
                            " |  \\| | | | | |/ _` | __/ _` | '_ \\ / _` / __|/ _ \\\n" +
                            " | |\\  | | |_| | (_| | || (_| | |_) | (_| \\__ \\  __/\n" +
                            " |_| \\_| |____/ \\__,_|\\__\\__,_|_.__/ \\__,_|___/\\___|\n" +
                            "                                      v. " + getInstance().metadata().version() + "\n" +
                            "Database type: " + nDatabaseConfig.getDatabaseType() + "     \n" +
                            " - If you need support, use the GitHub page \n" +
                            " - https://github.com/NivixX/NDatabase/ \n" +
                            " - Don't hesitate to drop a star and contribute to the project :)\n"
            );

            platformLoader.load();
            Objects.requireNonNull(NDatabase.api(), "NDatabase instance is null after platform load.");
            getInstance().logger().info("NDatabase platform (Sponge) has been loaded with success and the API is usable.");
        } catch (Throwable e) {
            throw new IllegalStateException("Could not initialize NDatabase Sponge plugin.", e);
        }
    }

    private void loadConfig() {
        try {
            // Get the configuration root for this plugin
            Path configPath = configRoot().configPath();

            // Initialize HOCON loader for the configuration path
            HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                    .path(configPath)
                    .build();

            // Check if the config file exists, and if not, copy the default from resources
            if (Files.notExists(configPath)) {
                Files.createDirectories(configPath.getParent());
                try (InputStream in = getClass().getResourceAsStream("/ndatabase.conf")) {
                    if (in != null) {
                        Files.copy(in, configPath, StandardCopyOption.REPLACE_EXISTING);
                        getLogger().info("Default configuration file created at " + configPath);
                    } else {
                        getLogger().warn("Default configuration file (ndatabase.conf) not found in resources.");
                    }
                }
            }

            // Load the configuration after ensuring it exists
            ConfigurationNode config = loader.load();

        } catch (IOException  e) {
            getLogger().error("Failed to load configuration", e);
        }
    }

}