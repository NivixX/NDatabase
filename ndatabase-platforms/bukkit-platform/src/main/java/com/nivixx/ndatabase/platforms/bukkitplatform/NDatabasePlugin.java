package com.nivixx.ndatabase.platforms.bukkitplatform;

import com.nivixx.ndatabase.core.PlatformLoader;
import org.bukkit.plugin.java.JavaPlugin;

public class NDatabasePlugin extends JavaPlugin {

    private static NDatabasePlugin instance;

    public static NDatabasePlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        saveResource("config.yml", false);
        PlatformLoader plaformLoader = new BukkitPlaformLoader();
        this.instance = this;
        try {
            plaformLoader.load();
        } catch (Throwable e) {
            throw new IllegalStateException("Could not init NDatabase bukkit plugin.", e);
        }
    }
}
