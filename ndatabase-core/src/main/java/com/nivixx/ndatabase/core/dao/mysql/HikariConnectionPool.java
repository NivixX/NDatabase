package com.nivixx.ndatabase.core.dao.mysql;

import com.nivixx.ndatabase.core.config.MysqlConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class HikariConnectionPool {

    private final MysqlConfig mysqlConfig;
    private HikariDataSource dataSource;

    public HikariConnectionPool(MysqlConfig mysqlConfig) {
        this.mysqlConfig = mysqlConfig;
        setupPool();
    }

    private void setupPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(mysqlConfig.getHost());
        config.setDriverClassName(mysqlConfig.getClassName());
        config.setUsername(mysqlConfig.getUser());
        config.setPassword(mysqlConfig.getPass());
        config.setMinimumIdle(mysqlConfig.getMinimumIdleConnection());
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(4000);

        dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

}