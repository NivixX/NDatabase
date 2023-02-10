package com.nivixx.ndatabase.core.dao.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public interface JdbcConnectionPool {

    Connection getConnection() throws SQLException;

}
