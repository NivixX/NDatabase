package com.nivixx.ndatabase.platforms.bukkitplatform;

import com.nivixx.ndatabase.core.Injector;
import com.nivixx.ndatabase.core.PlatformLoader;
import com.nivixx.ndatabase.core.config.DatabaseType;
import com.nivixx.ndatabase.core.config.MysqlConfig;
import com.nivixx.ndatabase.core.config.NDatabaseConfig;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * For Spigot/Bukkit based servers
 */
public class BukkitPlaformLoader extends PlatformLoader {

    @Override
    public DBLogger supplyDbLogger(NDatabaseConfig nDatabaseConfig) {
        return new BukkitDBLogger(nDatabaseConfig.isDebugMode());
    }

    @Override
    public SyncExecutor supplySyncExecutor() {
        return new BukkitSyncExecutor();
    }

    @Override
    public NDatabaseConfig supplyNDatabaseConfig() {
        NDatabasePlugin instance = NDatabasePlugin.getInstance();
        FileConfiguration config = instance.getConfig();
        ConfigurationSection mysql = config.getConfigurationSection("database.mysql");
        MysqlConfig mysqlConfig = new MysqlConfig();
        mysqlConfig.setHost(mysql.getString("host"));
        mysqlConfig.setClassName(mysql.getString("driver-class-name"));
        mysqlConfig.setMinimumIdleConnection(mysql.getInt("minimum-idle-connection"));
        mysqlConfig.setMaximumPoolSize(mysql.getInt("maximum-pool-size"));
        mysqlConfig.setPort(mysql.getInt("port"));
        mysqlConfig.setDatabaseName(mysql.getString("database-name"));
        mysqlConfig.setUser(mysql.getString("user"));
        mysqlConfig.setPass(mysql.getString("pass"));
        boolean debug = config.getBoolean("debug-mode", false);

        BukkitNDatabaseConfig bukkitNDatabaseConfig = new BukkitNDatabaseConfig();
        bukkitNDatabaseConfig.setDebugMode(debug);
        bukkitNDatabaseConfig.setIdleThreadPoolSize(config.getInt("idle-thread-pool-size",3));
        bukkitNDatabaseConfig.setDatabaseType(DatabaseType.valueOf(config.getString("database-type")));
        bukkitNDatabaseConfig.setMysqlConfig(mysqlConfig);

        return bukkitNDatabaseConfig;
    }
}
