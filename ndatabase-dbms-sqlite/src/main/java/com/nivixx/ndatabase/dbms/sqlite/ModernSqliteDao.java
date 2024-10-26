package com.nivixx.ndatabase.dbms.sqlite;

import com.nivixx.ndatabase.api.exception.DatabaseException;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.dbms.jdbc.JdbcDao;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Not used yet
 * most of sqlite drivers doesn't support JSON operation,
 * thus the denormalization cannot be done with GENERATED COLUMNS for these drivers
 * TODO write an adapter to use the modern version if the driver is > 3.38
 */
public class ModernSqliteDao<K, V extends NEntity<K>> extends JdbcDao<K,V> {

    public ModernSqliteDao(String collectionName,
                           String schema,
                           Class<K> keyType,
                           Class<V> nEntityType,
                           V instantiatedNEntity,
                           SqliteConnectionPool sqliteConnectionPool,
                           DBLogger dbLogger) {
        super(collectionName, schema, keyType, nEntityType, instantiatedNEntity, sqliteConnectionPool,  dbLogger);
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
                    "INSERT INTO " + collectionName + " VALUES(?,?) ON CONFLICT DO UPDATE " + DATA_IDENTIFIER + " = ?"
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
