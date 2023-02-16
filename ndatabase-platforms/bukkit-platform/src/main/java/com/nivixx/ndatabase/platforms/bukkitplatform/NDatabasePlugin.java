package com.nivixx.ndatabase.platforms.bukkitplatform;

import com.nivixx.ndatabase.core.PlatformLoader;
import com.nivixx.ndatabase.core.config.NDatabaseConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class NDatabasePlugin extends JavaPlugin {

    private static NDatabasePlugin instance;

    public static NDatabasePlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        this.instance = this;
        saveResource("config.yml", false);
        PlatformLoader plaformLoader = new BukkitPlaformLoader();
        NDatabaseConfig nDatabaseConfig = plaformLoader.supplyNDatabaseConfig();
        Bukkit.getLogger().info(
                "\n\n" +
                        "  _   _   ____        _        _                    \n" +
                        " | \\ | | |  _ \\  __ _| |_ __ _| |__   __ _ ___  ___ \n" +
                        " |  \\| | | | | |/ _` | __/ _` | '_ \\ / _` / __|/ _ \\\n" +
                        " | |\\  | | |_| | (_| | || (_| | |_) | (_| \\__ \\  __/\n" +
                        " |_| \\_| |____/ \\__,_|\\__\\__,_|_.__/ \\__,_|___/\\___|\n" +
                        "                                      v. " + getDescription().getVersion() + "\n" +
                        "Database type: " + nDatabaseConfig.getDatabaseType() + "     \n" +
                        " - If you need support, use the github page \n" +
                        " - https://github.com/NivixX/NDatabase/ \n" +
                        " - Don't hesitate to drop a star and contribute to the project :)\n"
        );
        try {
            plaformLoader.load();
        } catch (Throwable e) {
            throw new IllegalStateException("Could not init NDatabase bukkit plugin.", e);
        }
    }
}
