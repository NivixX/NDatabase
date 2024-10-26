package com.nivixx.ndatabase.dbms.mariadb;

public class MariaDBConfig {
    private String host;
    private int port;
    private String databaseName;
    private String user;
    private String pass;
    private String className = "org.mariadb.jdbc.Driver";
    private int minimumIdleConnection = 3;
    private int maximumPoolSize = 10;

    public String getClassName() {
        return className;
    }

    public int getMinimumIdleConnection() {
        return minimumIdleConnection;
    }

    public void setMinimumIdleConnection(int minimumIdleConnection) {
        this.minimumIdleConnection = minimumIdleConnection;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}
