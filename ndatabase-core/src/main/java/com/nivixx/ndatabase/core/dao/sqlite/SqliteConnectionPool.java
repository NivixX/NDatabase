package com.nivixx.ndatabase.core.dao.sqlite;

import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.core.config.SqliteConfig;
import com.nivixx.ndatabase.core.dao.DatabaseConnection;
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
    public void connect() throws Exception {
        getConnection();
    }

    @Override
    public Connection getConnection() throws SQLException {
        if(connection != null && !connection.isClosed()){
            return connection;
        }

        requireSqliteDriver();

        File dataFolder = new File(sqliteConfig.getFileFullPath());
        if (!dataFolder.exists()) {
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                String msg = String.format("Failed to read/create sqlite file '%s'", sqliteConfig.getFileFullPath());
                throw new NDatabaseException(msg, e);
            }
        }

        connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
        return connection;
    }

    private void requireSqliteDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            String msg = "Failed to find sqlite driver, verify the sqlite driver is present in your java execution environment";
            throw new NDatabaseException(msg, e);
        }
    }

}
