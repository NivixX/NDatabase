package com.nivixx.ndatabase.core.dao.sqlite;

import com.nivixx.ndatabase.api.exception.DatabaseException;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.core.dao.jdbc.JdbcDao;
import com.nivixx.ndatabase.core.dao.mysql.HikariConnectionPool;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SqliteDao<K, V extends NEntity<K>> extends JdbcDao<K,V> {

    public SqliteDao(String collectionName,
                     String schema, Class<K> keyType,
                     SqliteConnectionPool sqliteConnectionPool,
                     DBLogger dbLogger) {
        super(collectionName, schema, keyType, sqliteConnectionPool,  dbLogger);
    }

    // Sqlite handle upsert differently
    @Override
    public void upsert(V value) throws NDatabaseException {
        K key = value.getKey();
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(
                    "INSERT INTO " + collectionName + " VALUES(?,?) ON CONFLICT DO UPDATE SET " + DATA_IDENTIFIER + " = ?"
            );
            byte[] valueBytes = byteObjectSerializer.encode(value);
            bindKeyToStatement(ps,1, key);
            ps.setObject(2, valueBytes);
            ps.setObject(3, valueBytes);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            close(connection, ps);
        }
        dbLogger.logUpsert(value);
    }
}
