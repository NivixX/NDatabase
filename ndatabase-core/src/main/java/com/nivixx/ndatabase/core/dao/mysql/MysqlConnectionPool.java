package com.nivixx.ndatabase.core.dao.mysql;

import com.nivixx.ndatabase.core.config.MysqlConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class MysqlConnectionPool {


    private MysqlConfig mysqlConfig;
    private HikariDataSource dataSource;

    private String hostname;
    private String port;
    private String database;
    private String username;
    private String password;


    public MysqlConnectionPool(MysqlConfig mysqlConfig) {
        this.mysqlConfig = mysqlConfig;
        setupPool();
    }

    public void init() {
        /*
        hostname = plugin.getConfig().getString("database.MYSQL.host");
        port = plugin.getConfig().getString("database.MYSQL.port");
        database = plugin.getConfig().getString("database.MYSQL.database-name");
        username = plugin.getConfig().getString("database.MYSQL.user");
        password = plugin.getConfig().getString("database.MYSQL.pass");

         */
    }

    private void setupPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(mysqlConfig.getHost()
        );
        config.setDriverClassName(mysqlConfig.getClassName());
        config.setUsername(mysqlConfig.getUser());
        config.setPassword(mysqlConfig.getPass());
        config.setMinimumIdle(2);
        config.setMaximumPoolSize(20);
        //config.setLeakDetectionThreshold(60 * 1000);
        config.setConnectionTimeout(4000);
        //config.setMaxLifetime(60 * 1);

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