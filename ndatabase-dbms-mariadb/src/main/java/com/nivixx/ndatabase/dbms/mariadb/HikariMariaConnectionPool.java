package com.nivixx.ndatabase.dbms.mariadb;

import com.nivixx.ndatabase.dbms.jdbc.JdbcConnectionPool;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class HikariMariaConnectionPool implements JdbcConnectionPool {

    private final MariaDBConfig mariaDBConfig;
    private HikariDataSource dataSource;

    public HikariMariaConnectionPool(MariaDBConfig mariaDBConfig) {
        this.mariaDBConfig = mariaDBConfig;
    }


    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void connect() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(mariaDBConfig.getHost());
        config.setDriverClassName(mariaDBConfig.getClassName());
        config.setUsername(mariaDBConfig.getUser());
        config.setPassword(mariaDBConfig.getPass());
        config.setMinimumIdle(mariaDBConfig.getMinimumIdleConnection());
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(4000);

        dataSource = new HikariDataSource(config);
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

}