package com.nivixx.ndatabase.dbms.sqlite;

import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.dbms.jdbc.JdbcConnectionPool;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class SqliteConnectionPool implements JdbcConnectionPool {

    private final SqliteConfig sqliteConfig;
    private final DBLogger dbLogger;
    private final LibraryManager libraryManager;

    private HikariDataSource dataSource;
    private ClassLoader driverClassLoader;

    public SqliteConnectionPool(SqliteConfig sqliteConfig, DBLogger dbLogger, LibraryManager libraryManager) {
        this.sqliteConfig = sqliteConfig;
        this.dbLogger = dbLogger;
        this.libraryManager = libraryManager;
    }

    @Override
    public void connect() throws Exception {
        installSqliteDriver();

        // Set up the SQLite database file
        File dataFile = new File(sqliteConfig.getFileFullPath());
        if (!dataFile.exists()) {
            if (!dataFile.createNewFile()) {
                throw new IOException("Failed to create SQLite database file: " + sqliteConfig.getFileFullPath());
            }
        }

        // Switch current class loader with the isolated class loader that loads the SQLite driver at runtime
        Thread thread = Thread.currentThread();
        ClassLoader previousClassLoader = thread.getContextClassLoader();
        thread.setContextClassLoader(driverClassLoader);

        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + sqliteConfig.getFileFullPath());
            config.setDriverClassName("org.sqlite.JDBC"); // Use the appropriate SQLite driver
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(30000);
            config.setMaxLifetime(1800000);
            config.addDataSourceProperty("journal_mode", "WAL"); // Enable Write-Ahead Logging

            dataSource = new HikariDataSource(config);
            dbLogger.logInfo("SQLite HikariCP DataSource initialized successfully.");
        } catch (Exception e) {
            throw new NDatabaseException("Failed to initialize SQLite DataSource", e);
        } finally {
            // Restore the previous class loader
            thread.setContextClassLoader(previousClassLoader);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new NDatabaseException("DataSource is not initialized or closed.");
        }
        return dataSource.getConnection();
    }

    private void installSqliteDriver() {
        Library lib = Library.builder()
                .groupId("org{}xerial") // "{}" is replaced with ".", useful to avoid unwanted changes made by maven-shade-plugin
                .artifactId("sqlite-jdbc")
                .version("3.47.0.0")
                .isolatedLoad(true)
                .id("sqlite-jdbc")
                .build();
        dbLogger.logInfo("Loading SQLite driver");
        libraryManager.loadLibrary(lib);
        driverClassLoader = libraryManager.getIsolatedClassLoaderOf("sqlite-jdbc");
        dbLogger.logInfo("SQLite driver has been loaded successfully.");
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dbLogger.logInfo("SQLite HikariCP DataSource closed.");
        }
    }
}
