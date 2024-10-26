package com.nivixx.ndatabase.dbms.jdbc;


import com.nivixx.ndatabase.dbms.api.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public interface JdbcConnectionPool extends DatabaseConnection {

    Connection getConnection() throws SQLException;

}
