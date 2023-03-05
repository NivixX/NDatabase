package com.nivixx.ndatabase.core.dao.jdbc;

import com.nivixx.ndatabase.api.exception.*;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.api.query.NQuery;
import com.nivixx.ndatabase.core.expressiontree.SingleNodePath;
import com.nivixx.ndatabase.core.dao.Dao;
import com.nivixx.ndatabase.core.expressiontree.ExpressionTree;
import com.nivixx.ndatabase.core.expressiontree.visitor.SqlExpressionTreeVisitor;
import com.nivixx.ndatabase.core.serialization.encoder.BytesNEntityEncoder;
import com.nivixx.ndatabase.core.serialization.encoder.NEntityEncoder;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.sql.*;
import java.text.MessageFormat;
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
                   Class<V> nEntityType,
                   JdbcConnectionPool connectionPoolManager,
                   DBLogger dbLogger) {
        super(collectionName, schema, keyType, nEntityType, dbLogger);
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
                    "INSERT INTO " + collectionName + "(" + DATA_KEY_IDENTIFIER + "," + DATA_IDENTIFIER + ") VALUES(?,?)"
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
                    "INSERT INTO " + collectionName + "(" + DATA_KEY_IDENTIFIER + "," + DATA_IDENTIFIER + ") VALUES(?,?) ON DUPLICATE KEY UPDATE " + DATA_IDENTIFIER + " = ?"
            );
            byte[] valueBytes = byteObjectSerializer.encode(value);
            bindKeyToStatement(ps,1, key);
            ps.setObject(2, valueBytes);
            ps.setObject(3, valueBytes);
            ps.executeUpdate();
        } catch (Exception e) {
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
            String updateQuery = MessageFormat.format(
                    "UPDATE {0} SET {1} = ? WHERE {2} = ?",
                    collectionName, DATA_IDENTIFIER, DATA_KEY_IDENTIFIER);
            ps = connection.prepareStatement(updateQuery);

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
            ps = connection.prepareStatement("DELETE FROM " + collectionName);
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
            final PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + collectionName);
            final ResultSet rs = ps.executeQuery();

            return StreamSupport.stream(new Spliterators.AbstractSpliterator<V>(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE) {
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
            ps = connection.prepareStatement("SELECT data FROM " + collectionName + " WHERE "+ DATA_KEY_IDENTIFIER + " = ?");
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

        V returnedValue = null;
        if(dataFound != null) {
            returnedValue = byteObjectSerializer.decode(dataFound, classz);
        }
        dbLogger.logGet(returnedValue);
        return returnedValue;
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
    public void createIndexes(List<SingleNodePath> singleNodePaths) throws DatabaseCreationException {
        try (Connection connection = pool.getConnection()) {
            for (SingleNodePath singleNodePath : singleNodePaths) {
                // Create column if not exist and index it
                denormalizeFieldIntoColumn(connection, singleNodePath);
            }
        } catch (SQLException e) {
            throw new DatabaseCreationException(
                    "Error during index creation by de-normalization for NEntity " + nEntityType.getCanonicalName(), e);
        }
    }

    private void denormalizeFieldIntoColumn(Connection connection, SingleNodePath singleNodePath) throws SQLException {
        SingleNodePath currentNode = singleNodePath;
        String fieldPath = "";
        Class<?> fieldType = null;
        while(currentNode != null) {
            fieldPath = fieldPath.isEmpty() ? currentNode.getPathName() : fieldPath + "." + currentNode.getPathName();
            currentNode = currentNode.getChild();
            if(currentNode != null) {
                fieldType = currentNode.getType();
            }
        }
        // path.to.field
        // MYSQL doesn't allow "." in column names
        String columnName = fieldPath.replaceAll("\\.","_");

        String addColumnQuery = MessageFormat.format(
                "ALTER TABLE {0} ADD COLUMN {1} {2} GENERATED ALWAYS AS" +
                        "(`{3}`->> ''$.{4}'')",
                collectionName, columnName, getColumnType(false, fieldType),
                DATA_IDENTIFIER, fieldPath);

        try (PreparedStatement ps = connection.prepareStatement(addColumnQuery)) {
            ps.execute();
        }
        catch (SQLException e) {
            // TODO better way may be possible
            if(!e.getMessage().toLowerCase().contains("duplicate column name")) {
                throw e;
            }
        }

        // Index this column if not exist
        String createIndexQuery = MessageFormat.format(
                "CREATE INDEX IF NOT EXISTS {0}_index ON {1}({2})",
                columnName, collectionName, columnName);
        try (PreparedStatement ps = connection.prepareStatement(createIndexQuery)) {
            ps.execute();
        }
    }

    @Override
    public Optional<V> findOne(NQuery.Predicate expression, Class<V> classz) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        byte[] dataFound = null;
        try {
            connection = pool.getConnection();

            ExpressionTree<K,V> expressionTree = ExpressionTree.fromExpressionString(expression.getPredicate(), classz);
            SqlExpressionTreeVisitor<K,V> visitor = new SqlExpressionTreeVisitor<>("SELECT data FROM " + collectionName + " WHERE ");
            visitor.visit(expressionTree);
            ps = visitor.getPreparedStatement(connection);
            rs = ps.executeQuery();
            while (rs.next()) {
                dataFound = rs.getBytes(DATA_IDENTIFIER);
            }
        } catch (SQLException e) {
            throw new NDatabaseException(e);
        } finally {
            close(connection, ps, rs);
        }


        V returnedValue = null;
        if(dataFound != null) {
            returnedValue = byteObjectSerializer.decode(dataFound, classz);
        }
        dbLogger.logGet(returnedValue);
        return Optional.ofNullable(returnedValue);
    }

    @Override
    public List<V> find(NQuery.Predicate expression, Class<V> classz) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        byte[] dataFound = null;
        List<V> entities = new ArrayList<>();
        try {
            connection = pool.getConnection();

            ExpressionTree<K,V> expressionTree = ExpressionTree.fromExpressionString(expression.getPredicate(), classz);
            SqlExpressionTreeVisitor<K,V> visitor = new SqlExpressionTreeVisitor<>("SELECT data FROM " + collectionName + " WHERE ");
            visitor.visit(expressionTree);
            ps = visitor.getPreparedStatement(connection);
            rs = ps.executeQuery();
            while (rs.next()) {
                dataFound = rs.getBytes(DATA_IDENTIFIER);
                if(dataFound != null) {
                    entities.add(byteObjectSerializer.decode(dataFound, classz));
                }
            }
        } catch (SQLException e) {
            throw new NDatabaseException(e);
        } finally {
            close(connection, ps, rs);
        }

        return entities;
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
                            + DATA_KEY_IDENTIFIER + " " + getColumnType(true, keyType) + " PRIMARY KEY,"
                            + "data JSON"
                            + ")"
            );
            ps.execute();
        } catch (SQLException e) {
            throw new DatabaseCreationException("Failed to create table for NEntity " + nEntityType.getCanonicalName(), e);
        } finally {
            close(connection, ps);
        }
    }

    @Override
    public void validateConnection() throws NDatabaseLoadException {
        try {
            pool.connect();
        } catch (Exception e) {
            throw new NDatabaseLoadException("Failed to connect to the database", e);
        }
    }

    protected String getColumnType(boolean isForKey, Class<?> type) {
        if(type.isAssignableFrom(UUID.class)) {
            return "CHAR(36)";
        }
        if(type.isAssignableFrom(String.class)) {
            if(isForKey) {
                return "VARCHAR(1200)"; // TODO manage max row size
            }
            else {
                return "TEXT";
            }
        }
        if(type.isAssignableFrom(Long.class) ||  type.getName().equals("long")) {
            return "LONG";
        }
        if(type.isAssignableFrom(Integer.class) || type.getName().equals("int")) {
            return "INTEGER";
        }
        throw new DatabaseCreationException(
                String.format("Mysql doesn't support type '%s', verify that your NEntity" +
                        " use a key of type String, UUID, Long, or Integer", type)
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
