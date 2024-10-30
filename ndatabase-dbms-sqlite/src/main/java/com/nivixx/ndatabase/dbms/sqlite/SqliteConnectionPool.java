package com.nivixx.ndatabase.dbms.sqlite;

import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.dbms.jdbc.JdbcConnectionPool;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;
import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SqliteConnectionPool implements JdbcConnectionPool {

    private SqliteConfig sqliteConfig;
    private DBLogger dbLogger;

    private Connection connection;

    private LibraryManager libraryManager;

    private Constructor<?> connectionConstructor;

    public SqliteConnectionPool(SqliteConfig sqliteConfig, DBLogger dbLogger, LibraryManager libraryManager) {
        this.sqliteConfig = sqliteConfig;
        this.dbLogger = dbLogger;
        this.libraryManager = libraryManager;
    }

    @Override
    public void connect() throws Exception {
        installSqliteDriver();
        requireSqliteDriver();
        getConnection();
    }

    @Override
    public Connection getConnection() throws SQLException {
        if(connection != null && !connection.isClosed()){
            return connection;
        }

        File dataFolder = new File(sqliteConfig.getFileFullPath());
        if (!dataFolder.exists()) {
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                String msg = String.format("Failed to read/create sqlite file '%s'", sqliteConfig.getFileFullPath());
                throw new NDatabaseException(msg, e);
            }
        }

        try {
            Properties properties = new Properties();
            connection = (Connection) connectionConstructor.newInstance("jdbc:sqlite:" + sqliteConfig.getFileFullPath(), sqliteConfig.getFileFullPath(), properties);
        } catch (Exception e) {
            throw new NDatabaseException("Failed to get SQLITE connection", e);
        }

        return connection;
    }

    private void installSqliteDriver() {
        Library lib = Library.builder()
                .groupId("org{}xerial") // "{}" is replaced with ".", useful to avoid unwanted changes made by maven-shade-plugin
                .artifactId("sqlite-jdbc")
                .version("3.46.1.3")
                .isolatedLoad(true)
                .id("sqlite-jdbc")
                .build();
        dbLogger.logInfo("Loading SQLite driver");
        libraryManager.loadLibrary(lib);
        dbLogger.logInfo("SQLite driver has been loaded with success");
    }

    private void requireSqliteDriver() {
        try {
            Class<?> connectionClass = libraryManager.getIsolatedClassLoaderOf("sqlite-jdbc")
                    .loadClass("org.sqlite.jdbc4.JDBC4Connection");
            connectionConstructor = connectionClass.getConstructor(String.class, String.class, Properties.class);
        } catch (Exception e) {
            String msg = "Failed to find sqlite driver, verify the sqlite driver is present in your java execution environment";
            throw new NDatabaseException(msg, e);
        }
    }

}
