package com.nivixx.ndatabase.platforms.bukkitplatform;

import com.nivixx.ndatabase.core.Injector;
import com.nivixx.ndatabase.core.PlatformLoader;
import com.nivixx.ndatabase.core.config.DatabaseType;
import com.nivixx.ndatabase.core.config.MysqlConfig;
import com.nivixx.ndatabase.core.config.NDatabaseConfig;
import com.nivixx.ndatabase.core.config.SqliteConfig;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * For Spigot/Bukkit based servers
 */
public class BukkitPlaformLoader extends PlatformLoader {

    @Override
    public DBLogger supplyDbLogger(boolean isDebugMode) {
        return new BukkitDBLogger(isDebugMode);
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
        mysqlConfig.setHost(mysql.getString("host",""));
        mysqlConfig.setClassName(mysql.getString("driver-class-name",""));
        mysqlConfig.setMinimumIdleConnection(mysql.getInt("minimum-idle-connection",1));
        mysqlConfig.setMaximumPoolSize(mysql.getInt("maximum-pool-size", 3));
        mysqlConfig.setPort(mysql.getInt("port", 3306));
        mysqlConfig.setDatabaseName(mysql.getString("database-name","ndatabase"));
        mysqlConfig.setUser(mysql.getString("user","user"));
        mysqlConfig.setPass(mysql.getString("pass","pass"));

        ConfigurationSection sqlite = config.getConfigurationSection("database.sqlite");
        SqliteConfig sqliteConfig = new SqliteConfig();
        String fileName = sqlite.getString("file-name", "ndatabase.sqlite");
        sqliteConfig.setFileFullPath(NDatabasePlugin.getInstance().getDataFolder() + "/" + fileName);

        boolean debug = config.getBoolean("debug-mode", false);

        BukkitNDatabaseConfig bukkitNDatabaseConfig = new BukkitNDatabaseConfig();
        bukkitNDatabaseConfig.setDebugMode(debug);
        bukkitNDatabaseConfig.setIdleThreadPoolSize(config.getInt("idle-thread-pool-size",3));
        bukkitNDatabaseConfig.setDatabaseType(DatabaseType.valueOf(config.getString("database-type")));
        bukkitNDatabaseConfig.setMysqlConfig(mysqlConfig);
        bukkitNDatabaseConfig.setSqliteConfig(sqliteConfig);

        return bukkitNDatabaseConfig;
    }
}
