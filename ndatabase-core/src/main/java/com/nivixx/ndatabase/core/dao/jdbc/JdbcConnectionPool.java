package com.nivixx.ndatabase.core.dao.jdbc;

import com.nivixx.ndatabase.core.dao.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public interface JdbcConnectionPool extends DatabaseConnection {

    Connection getConnection() throws SQLException;

}
