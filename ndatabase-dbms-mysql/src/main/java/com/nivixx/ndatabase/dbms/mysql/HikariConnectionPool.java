package com.nivixx.ndatabase.dbms.mysql;

import com.nivixx.ndatabase.dbms.jdbc.JdbcConnectionPool;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;
import net.byteflux.libby.classloader.IsolatedClassLoader;

import java.sql.Connection;
import java.sql.SQLException;

public class HikariConnectionPool implements JdbcConnectionPool {

    private final MysqlConfig mysqlConfig;
    private HikariDataSource dataSource;
    private LibraryManager libraryManager;
    private DBLogger dbLogger;

    private ClassLoader driverClassLoader;

    public HikariConnectionPool(MysqlConfig mysqlConfig, DBLogger dbLogger, LibraryManager libraryManager) {
        this.mysqlConfig = mysqlConfig;
        this.libraryManager = libraryManager;
        this.dbLogger = dbLogger;
    }


    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void connect() throws Exception {
        installMysqlDriver();

        // Switch current class loader with the isolated class loader
        // That load mysql driver on runtime
        Thread thread = Thread.currentThread();
        ClassLoader previousClassLoader = thread.getContextClassLoader();
        thread.setContextClassLoader(driverClassLoader);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(mysqlConfig.getHost());
        config.setDriverClassName(mysqlConfig.getClassName());
        config.setUsername(mysqlConfig.getUser());
        config.setPassword(mysqlConfig.getPass());
        config.setMinimumIdle(mysqlConfig.getMinimumIdleConnection());
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(4000);

        dataSource = new HikariDataSource(config);

        // Put back the previous original class loader
        thread.setContextClassLoader(previousClassLoader);
    }

    private void installMysqlDriver() {
        Library lib = Library.builder()
                .groupId("mysql") // "{}" is replaced with ".", useful to avoid unwanted changes made by maven-shade-plugin
                .artifactId("mysql-connector-java")
                .version("8.0.30")
                .isolatedLoad(true)
                .id("mysql-connector-java")
                .build();
        dbLogger.logInfo("Loading MYSQL driver");
        libraryManager.loadLibrary(lib);
        driverClassLoader = libraryManager.getIsolatedClassLoaderOf("mysql-connector-java");
        dbLogger.logInfo("MYSQL driver has been loaded with success");
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

}