package com.nivixx.ndatabase.core.config;

import com.nivixx.ndatabase.dbms.mariadb.MariaDBConfig;
import com.nivixx.ndatabase.dbms.mongodb.MongoDBConfig;
import com.nivixx.ndatabase.dbms.mysql.MysqlConfig;
import com.nivixx.ndatabase.dbms.sqlite.SqliteConfig;

import java.util.Objects;

public class NDatabaseConfig {

    protected DatabaseType databaseType = DatabaseType.SQLITE;

    protected MariaDBConfig mariaDBConfig;
    protected MysqlConfig mysqlConfig;
    protected SqliteConfig sqliteConfig;
    protected MongoDBConfig mongoDBConfig;

    protected boolean isDebugMode = false;
    protected int idleThreadPoolSize = 1;

    public void verifyConfig() {
        if(databaseType == null) {
            throw new IllegalArgumentException("Database Type not provided in the configuration");
        }
        if(databaseType == DatabaseType.MYSQL) {
            Objects.requireNonNull(mysqlConfig.getHost(), "mysql host is null, check your mysql configuration");
            Objects.requireNonNull(mysqlConfig.getDatabaseName(), "mysql database name is null, check your mysql configuration");
            Objects.requireNonNull(mysqlConfig.getUser(), "mysql user is null, check your mysql configuration");
            Objects.requireNonNull(mysqlConfig.getPass(), "mysql pass is null, check your mysql configuration");
        }
    }

    public MariaDBConfig getMariaDBConfig() {
        return mariaDBConfig;
    }

    public void setMariaDBConfig(MariaDBConfig mariaDBConfig) {
        this.mariaDBConfig = mariaDBConfig;
    }

    public MongoDBConfig getMongoDBConfig() {
        return mongoDBConfig;
    }

    public void setMongoDBConfig(MongoDBConfig mongoDBConfig) {
        this.mongoDBConfig = mongoDBConfig;
    }

    public boolean isDebugMode() {
        return isDebugMode;
    }

    public void setDebugMode(boolean debugMode) {
        isDebugMode = debugMode;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    public MysqlConfig getMysqlConfig() {
        return mysqlConfig;
    }

    public void setMysqlConfig(MysqlConfig mysqlConfig) {
        this.mysqlConfig = mysqlConfig;
    }

    public SqliteConfig getSqliteConfig() {
        return sqliteConfig;
    }

    public void setSqliteConfig(SqliteConfig sqliteConfig) {
        this.sqliteConfig = sqliteConfig;
    }

    public int getIdleThreadPoolSize() {
        return idleThreadPoolSize;
    }

    public void setIdleThreadPoolSize(int idleThreadPoolSize) {
        this.idleThreadPoolSize = idleThreadPoolSize;
    }
}