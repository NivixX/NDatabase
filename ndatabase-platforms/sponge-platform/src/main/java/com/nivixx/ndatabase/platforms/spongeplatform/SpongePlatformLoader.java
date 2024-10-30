package com.nivixx.ndatabase.platforms.spongeplatform;

import com.nivixx.ndatabase.core.PlatformLoader;
import com.nivixx.ndatabase.core.config.*;
import com.nivixx.ndatabase.dbms.mariadb.MariaDBConfig;
import com.nivixx.ndatabase.dbms.mongodb.MongoDBConfig;
import com.nivixx.ndatabase.dbms.mysql.MysqlConfig;
import com.nivixx.ndatabase.dbms.sqlite.SqliteConfig;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;
import net.byteflux.libby.LibraryManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigRoot;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.plugin.PluginContainer;

import java.io.IOException;
import java.nio.file.Path;

public class SpongePlatformLoader extends PlatformLoader {

    private final ConfigurationNode config;

    public SpongePlatformLoader(PluginContainer pluginContainer) throws IOException {
        // Load the config file on initialization
        ConfigRoot configRoot = Sponge.configManager().pluginConfig(pluginContainer);
        Path configPath = configRoot.configPath();
        HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .path(configPath)
                .build();
        this.config = loader.load();
    }

    @Override
    public DBLogger supplyDbLogger(boolean isDebugMode) {
        return new SpongeDBLogger(isDebugMode);
    }

    @Override
    public SyncExecutor supplySyncExecutor() {
        return new SpongeSyncExecutor(NDatabasePlugin.getInstance());
    }

    @Override
    public NDatabaseConfig supplyNDatabaseConfig() {
        // MySQL Configuration
        ConfigurationNode mysqlNode = config.node("database", "mysql");
        MysqlConfig mysqlConfig = new MysqlConfig();
        mysqlConfig.setHost(mysqlNode.node("host").getString("localhost"));
        mysqlConfig.setClassName(mysqlNode.node("driver-class-name").getString("com.mysql.jdbc.Driver"));
        mysqlConfig.setMinimumIdleConnection(mysqlNode.node("minimum-idle-connection").getInt(1));
        mysqlConfig.setMaximumPoolSize(mysqlNode.node("maximum-pool-size").getInt(3));
        mysqlConfig.setPort(mysqlNode.node("port").getInt(3306));
        mysqlConfig.setDatabaseName(mysqlNode.node("database-name").getString("ndatabase"));
        mysqlConfig.setUser(mysqlNode.node("user").getString("user"));
        mysqlConfig.setPass(mysqlNode.node("pass").getString("pass"));

        // MariaDB Configuration
        ConfigurationNode mariadbNode = config.node("database", "mariadb");
        MariaDBConfig mariadbConfig = new MariaDBConfig();
        mariadbConfig.setHost(mariadbNode.node("host").getString("localhost"));
        mariadbConfig.setClassName(mariadbNode.node("driver-class-name").getString("org.mariadb.jdbc.Driver"));
        mariadbConfig.setMinimumIdleConnection(mariadbNode.node("minimum-idle-connection").getInt(1));
        mariadbConfig.setMaximumPoolSize(mariadbNode.node("maximum-pool-size").getInt(3));
        mariadbConfig.setPort(mariadbNode.node("port").getInt(3306));
        mariadbConfig.setDatabaseName(mariadbNode.node("database-name").getString("ndatabase"));
        mariadbConfig.setUser(mariadbNode.node("user").getString("user"));
        mariadbConfig.setPass(mariadbNode.node("pass").getString("pass"));

        // SQLite Configuration
        ConfigurationNode sqliteNode = config.node("database", "sqlite");
        SqliteConfig sqliteConfig = new SqliteConfig();
        String fileName = sqliteNode.node("file-name").getString("ndatabase.sqlite");
        sqliteConfig.setFileFullPath(Sponge.configManager().pluginConfig(NDatabasePlugin.getInstance()).configPath().getParent().resolve(fileName).toString());

        // MongoDB Configuration
        ConfigurationNode mongoDBNode = config.node("database", "mongodb");
        MongoDBConfig mongoDBConfig = new MongoDBConfig();
        mongoDBConfig.setHost(mongoDBNode.node("host").getString("localhost"));
        mongoDBConfig.setPort(mongoDBNode.node("port").getInt(27017));
        mongoDBConfig.setDatabase(mongoDBNode.node("database").getString("ndatabase"));
        mongoDBConfig.setUser(mongoDBNode.node("user").getString(""));
        mongoDBConfig.setPass(mongoDBNode.node("pass").getString(""));

        // General Settings
        boolean debug = config.node("debug-mode").getBoolean(false);

        SpongeNDatabaseConfig spongeNDatabaseConfig = new SpongeNDatabaseConfig();
        spongeNDatabaseConfig.setDebugMode(debug);
        spongeNDatabaseConfig.setIdleThreadPoolSize(config.node("idle-thread-pool-size").getInt(3));
        spongeNDatabaseConfig.setDatabaseType(DatabaseType.valueOf(config.node("database-type").getString("SQLITE")));
        spongeNDatabaseConfig.setMysqlConfig(mysqlConfig);
        spongeNDatabaseConfig.setMariaDBConfig(mariadbConfig);
        spongeNDatabaseConfig.setSqliteConfig(sqliteConfig);
        spongeNDatabaseConfig.setMongoDBConfig(mongoDBConfig);

        return spongeNDatabaseConfig;
    }

    @Override
    public LibraryManager supplyLibraryManager() {
        return new CustomSpongeLibraryManager<PluginContainer>(
                NDatabasePlugin.getLogger(),
                NDatabasePlugin.configRoot().directory(),
                NDatabasePlugin.getInstance(),
                "ndatabase-dependencies"
        );
    }
}
