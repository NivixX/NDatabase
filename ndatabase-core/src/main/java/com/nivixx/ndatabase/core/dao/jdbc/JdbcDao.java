package com.nivixx.ndatabase.core.dao.jdbc;

import com.nivixx.ndatabase.api.exception.DatabaseCreationException;
import com.nivixx.ndatabase.api.exception.DatabaseException;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.exception.NEntityNotFoundException;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.core.dao.Dao;
import com.nivixx.ndatabase.core.dao.mysql.HikariConnectionPool;
import com.nivixx.ndatabase.core.serialization.BytesNEntityEncoder;
import com.nivixx.ndatabase.core.serialization.NEntityEncoder;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.sql.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class JdbcDao<K, V extends NEntity<K>> extends Dao<K, V> {

    protected final JdbcConnectionPool pool;
    protected final String DATA_IDENTIFIER = "data";
    protected final String DATA_KEY_IDENTIFIER = "data_key";
    protected NEntityEncoder<V, byte[]> byteObjectSerializer;


    public JdbcDao(String collectionName,
                   String schema,
                   Class<K> keyType,
                   JdbcConnectionPool connectionPoolManager,
                   DBLogger dbLogger) {
        super(collectionName, schema, keyType, dbLogger);
        this.pool = connectionPoolManager;
        this.byteObjectSerializer = new BytesNEntityEncoder<>();
    }

    @Override
    public void insert(V value) throws NDatabaseException {
        K key = value.getKey();
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(
                    "INSERT INTO " + collectionName + " VALUES(?,?)"
            );
            bindKeyToStatement(ps,1, key);
            ps.setObject(2, byteObjectSerializer.encode(value));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            close(connection, ps);
        }
        dbLogger.logInsert(value);
    }


    @Override
    public void upsert(V value) throws NDatabaseException {
        K key = value.getKey();
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(
                    "INSERT INTO " + collectionName + " VALUES(?,?) ON DUPLICATE KEY UPDATE " + DATA_IDENTIFIER + " = ?"
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

    protected void bindKeyToStatement(PreparedStatement ps, int fieldIndex, K key) throws SQLException {
        if(key instanceof UUID) {
            ps.setString(fieldIndex, key.toString());
        }
        else if(key instanceof String) {
            ps.setString(fieldIndex, (String) key);
        }
        else if(key instanceof Long) {
            ps.setLong(fieldIndex, (Long) key);
        }
        else if(key instanceof Integer) {
            ps.setLong(fieldIndex, (Integer) key);
        }
    }

    @Override
    public void delete(K key) throws NDatabaseException {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(
                    "DELETE FROM " + collectionName + " WHERE " + DATA_KEY_IDENTIFIER + "= ?"
            );
            bindKeyToStatement(ps,1, key);
            if(ps.executeUpdate() <= 0) {
                throw new NEntityNotFoundException("There is no value with the key " + key + " in the database for collection " + collectionName);
            }
        } catch (SQLException e) {
            throw new NDatabaseException(e);
        } finally {
            close(connection, ps);
        }
        dbLogger.logDelete(key);
    }

    @Override
    public void update(V value) throws NDatabaseException {
        K key = value.getKey();
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(
                    "UPDATE  " + collectionName + " SET "+ DATA_IDENTIFIER +  "= ? WHERE " + DATA_KEY_IDENTIFIER +"= ?"
            );

            ps.setObject(1, byteObjectSerializer.encode(value));
            bindKeyToStatement(ps,2, key);
            if(ps.executeUpdate() <= 0) {
                throw new NEntityNotFoundException("There is no value with the key " + key + " in the database for collection " + collectionName);
            }
        } catch (SQLException e) {
            throw new NDatabaseException(e);
        } finally {
            close(connection, ps);
        }
        dbLogger.logUpdate(value);
    }

    @Override
    public void deleteAll() {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(
                    "DELETE FROM  " + collectionName
            );
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new NDatabaseException(e);
        } finally {
            close(connection, ps);
        }
        dbLogger.logDeleteAll();
    }


    @Override
    public Stream<V> streamAllValues(Class<V> classz) throws NDatabaseException {
        try {
            final Connection connection = pool.getConnection();
            final PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM " + collectionName
            );
            final ResultSet rs = ps.executeQuery();

            Stream<V> resultStream = StreamSupport.stream(new Spliterators.AbstractSpliterator<V>(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE) {
                @Override
                public boolean tryAdvance(Consumer<? super V> action) {
                    try {
                        if(!rs.next()) {
                            close(connection, ps, rs);
                            return false;
                        }
                        final V value = byteObjectSerializer.decode(rs.getBytes(DATA_IDENTIFIER), classz);
                        action.accept(value);
                        return true;
                    } catch (Exception e) {
                        throw new NDatabaseException(e);
                    }

                }
            }, false).onClose(() -> {
                try {
                    close(connection, ps, rs);
                } catch (Exception e) {
                    throw new DatabaseException(e);
                }
            });
            return resultStream;

        } catch (SQLException e) {
            throw new NDatabaseException(e);
        }

    }

    @Override
    public V get(K key, Class<V> classz) throws NDatabaseException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        byte[] dataFound = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(
                    "SELECT data FROM " + collectionName + " WHERE "+ DATA_KEY_IDENTIFIER + " = ?"
            );
            bindKeyToStatement(ps, 1, key);
            rs = ps.executeQuery();
            while (rs.next()) {
                dataFound = rs.getBytes(DATA_IDENTIFIER);
            }
        } catch (SQLException e) {
            throw new NDatabaseException(e);
        } finally {
            close(connection, ps, rs);
        }
        dbLogger.logGet(dataFound);
        if(dataFound == null) { return null; }
        return byteObjectSerializer.decode(dataFound, classz);
    }

    @Override
    public Optional<V> findOne(Predicate<V> predicate, Class<V> classz) throws NDatabaseException {
        return streamAllValues(classz).filter(predicate).findFirst();
    }

    @Override
    public List<V> find(Predicate<V> predicate, Class<V> classz) throws NDatabaseException {
        return streamAllValues(classz).filter(predicate).collect(Collectors.toList());
    }

    @Override
    public void createDatabaseIfNotExist(Class<K> keyType) throws DatabaseCreationException {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + collectionName
                            + "("
                            + DATA_KEY_IDENTIFIER + " " + getDatabaseKeyType() + " PRIMARY KEY,"
                            + "data MEDIUMBLOB"
                            + ")"
            );
            ps.execute();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            close(connection, ps);
        }
    }

    protected String getDatabaseKeyType() {
        if(keyType.isAssignableFrom(UUID.class)) {
            return "CHAR(36)";
        }
        if(keyType.isAssignableFrom(String.class)) {
            return "VARCHAR(80)";
        }
        if(keyType.isAssignableFrom(Long.class)) {
            return "LONG";
        }
        if(keyType.isAssignableFrom(Integer.class)) {
            return "INTEGER";
        }
        throw new DatabaseCreationException(
                String.format("Mysql doesn't support key of type '%s', verify that your NEntity" +
                        " use a key of type String, UUID, Long, or Integer", keyType)
        );
    }

    public void close(Connection conn, PreparedStatement ps, ResultSet res) {
        if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
        if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
        if (res != null) try { res.close(); } catch (SQLException ignored) {}
    }
    public void close(Connection conn, PreparedStatement ps) {
        if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
        if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
    }
}
