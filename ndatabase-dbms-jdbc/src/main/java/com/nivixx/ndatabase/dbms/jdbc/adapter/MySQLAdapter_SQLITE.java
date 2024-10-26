package com.nivixx.ndatabase.dbms.jdbc.adapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;

public class MySQLAdapter_SQLITE extends MySQLAdapter {

    @Override
    public void createIndexIfNotExist(String tableName, String columnName, Class<?> fieldType, Connection connection) throws SQLException {

        String createIndexQuery = MessageFormat.format(
                "CREATE INDEX IF NOT EXISTS {0}_index ON {1}({2})",
                columnName, tableName, columnName);
        try (PreparedStatement ps = connection.prepareStatement(createIndexQuery)) {
            ps.execute();
        }
    }
}
