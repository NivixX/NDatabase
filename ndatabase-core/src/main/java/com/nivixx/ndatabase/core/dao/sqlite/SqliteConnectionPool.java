package com.nivixx.ndatabase.core.dao.sqlite;

import com.nivixx.ndatabase.core.config.SqliteConfig;
import com.nivixx.ndatabase.core.dao.jdbc.JdbcConnectionPool;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteConnectionPool implements JdbcConnectionPool {

    private SqliteConfig sqliteConfig;
    private DBLogger dbLogger;

    private Connection connection;

    public SqliteConnectionPool(SqliteConfig sqliteConfig, DBLogger dbLogger) {
        this.sqliteConfig = sqliteConfig;
        this.dbLogger = dbLogger;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if(connection != null && !connection.isClosed()){
            return connection;
        }
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            dbLogger.logError(e, "Failed to find sqlite driver, verify the sqlite driver is present in your java execution environment");
            return null;
        }
        File dataFolder = new File(sqliteConfig.getFileFullPath());
        if (!dataFolder.exists()) {
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                String msg = String.format("Failed to read/create sqlite file '%s'", sqliteConfig.getFileFullPath());
                dbLogger.logError(e, msg);
            }
        }

        if(connection!=null&&!connection.isClosed()){
            return connection;
        }
        connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
        return connection;
    }
}
