package com.nivixx.ndatabase.dbms.jdbc.adapter;

import java.sql.*;

public class MySQLAdapterResolver {

    public static MySQLAdapter resolveMySQLAdapter(Connection connection) throws SQLException {

        DatabaseMetaData metaData = connection.getMetaData();
        String dbName = metaData.getDatabaseProductName();

        if (dbName.equalsIgnoreCase("SQLite")) {
            return new MySQLAdapter_SQLITE();
        }

        String version = getMySQLVersion(connection);
        boolean isMySQL57OrBelow = isMySQL57OrBelow(version);

        if (isMySQL57OrBelow) {
            return new MySQLAdapter_MYSQL_5_7();
        }

        // Add more if needed

        return new MySQLAdapter_MYSQL_MODERN();

    }


    private static String getMySQLVersion(Connection conn) throws SQLException {
        String version = null;
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT VERSION()");
            if (rs.next()) {
                version = rs.getString(1);
            }
        }
        return version;
    }

    private static boolean isMySQL57OrBelow(String version) {
        if (version != null && version.matches("^\\d+\\.\\d+\\.\\d+")) {
            String[] parts = version.split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            return major < 6 || (major == 5 && minor <= 7);
        }
        return false;
    }
}
