package com.nivixx.ndatabase.core.dao.mysql.adapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;

public class MySQLAdapter_MYSQL_5_7 extends MySQLAdapter {

    @Override
    public void createIndexIfNotExist(String tableName, String columnName, Class<?> fieldType, Connection connection) throws SQLException {
        // Check if the index exists
        boolean indexExists = indexExists(tableName, columnName, connection);

        // If the index doesn't exist, create it
        if (!indexExists) {
            // In Legacy sql version, we need to specify index length for TEXT
            String createIndexQuery =
                    fieldType.isAssignableFrom(String.class) ?
                            "CREATE INDEX " + columnName + "_index ON " + tableName + "(" + columnName + "(255))" :
                            "CREATE INDEX " + columnName + "_index ON " + tableName + "(" + columnName + ")";
            try (PreparedStatement ps = connection.prepareStatement(createIndexQuery)) {
                ps.executeUpdate();
            }
        }
    }


    private boolean indexExists(String tableName, String columnName, Connection connection) throws SQLException {
        String indexExistsQuery = "SHOW INDEX FROM " + tableName + " WHERE Key_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(indexExistsQuery)) {
            ps.setString(1, columnName + "_index");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
