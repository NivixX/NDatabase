package com.nivixx.ndatabase.dbms.jdbc.adapter;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class MySQLAdapter {

    public abstract void createIndexIfNotExist(
            String tableName, String columnName, Class<?> fieldType, Connection connection) throws SQLException;
}
