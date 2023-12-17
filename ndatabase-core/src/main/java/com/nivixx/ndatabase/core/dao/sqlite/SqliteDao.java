package com.nivixx.ndatabase.core.dao.sqlite;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivixx.ndatabase.api.exception.DatabaseCreationException;
import com.nivixx.ndatabase.api.exception.DatabaseException;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.exception.NEntityNotFoundException;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.core.dao.jdbc.JdbcDao;
import com.nivixx.ndatabase.core.dao.mysql.HikariConnectionPool;
import com.nivixx.ndatabase.core.expressiontree.SingleNodePath;
import com.nivixx.ndatabase.core.reflection.NReflectionUtil;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SqliteDao<K, V extends NEntity<K>> extends JdbcDao<K,V> {

    private List<SingleNodePath> indexedFields;

    public SqliteDao(String collectionName,
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
            List<Map.Entry<String, JsonNode>> indexedColumnNamesValues = getIndexedColumnNames(value);
            List<String> columnNameList = indexedColumnNamesValues.stream()
                    .map(Map.Entry::getKey).collect(Collectors.toList());
            List<JsonNode> values = indexedColumnNamesValues.stream()
                    .map(Map.Entry::getValue).collect(Collectors.toList());
            String columnNames = DATA_KEY_IDENTIFIER + "," + DATA_IDENTIFIER;
            String columnWildCards = "?,?";
            String valueNames = DATA_IDENTIFIER + " = ?,";
            for (String columnName : columnNameList) {
                columnNames += "," + columnName;
                columnWildCards += "," + "?";
                valueNames += columnName + " = ?,";
            }
            if(valueNames.charAt(valueNames.length()-1) == ',') {
                valueNames = valueNames.substring(0, valueNames.length()-1);
            }

            connection = pool.getConnection();
            String query = MessageFormat.format(
                    "INSERT INTO {0} ({1}) VALUES({2})" +
                            " ON CONFLICT({3}) DO UPDATE SET {4}"
                    , collectionName, columnNames, columnWildCards, DATA_KEY_IDENTIFIER, valueNames);
            ps = connection.prepareStatement(query);

            byte[] valueBytes = byteObjectSerializer.encode(value);
            bindKeyToStatement(ps,1, key);
            ps.setObject(2, valueBytes);
            int index = 3;
            for (JsonNode jsonNode : values) {
                ps.setObject(index, extractJsonNodeValue(jsonNode));
                index++;
            }
            ps.setObject(index, valueBytes);
            index++;
            for (JsonNode jsonNode : values) {
                ps.setObject(index, extractJsonNodeValue(jsonNode));
                index++;
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            close(connection, ps);
        }
        dbLogger.logUpsert(value);
    }

    private Object extractJsonNodeValue(JsonNode node) {
        if(node == null) {
            return null;
        }
        if(node.isTextual()) {
            return node.textValue();
        }
        if(node.isBoolean()) {
            return node.booleanValue();
        }
        if(node.isNumber()) {
            return node.numberValue();
        }
        if(node.isBigDecimal()) {
            return node.decimalValue();
        }
        if(node.isBigInteger()) {
            return node.bigIntegerValue();
        }
        if(node.isLong()) {
            return node.longValue();
        }
        if(node.isInt()) {
            return node.intValue();
        }
        if(node.isDouble()) {
            return node.doubleValue();
        }
        if(node.isFloat()) {
            return node.floatValue();
        }
        if(node.isShort()) {
            return node.shortValue();
        }
        return node.toString();
    }

    @Override
    public void update(V value) throws NDatabaseException {
        K key = value.getKey();
        Connection connection = null;
        PreparedStatement ps = null;

        List<Map.Entry<String, JsonNode>> indexedColumnNamesValues = getIndexedColumnNames(value);
        List<String> columnNameList = indexedColumnNamesValues.stream()
                .map(Map.Entry::getKey).collect(Collectors.toList());
        List<JsonNode> values = indexedColumnNamesValues.stream()
                .map(Map.Entry::getValue).collect(Collectors.toList());
        String valueNames = DATA_IDENTIFIER + " = ?, ";
        for (String columnName : columnNameList) {
            valueNames += columnName + " = ?,";
        }
        if(valueNames.charAt(valueNames.length()-1) == ',') {
            valueNames = valueNames.substring(0, valueNames.length()-1);
        }

        try {
            connection = pool.getConnection();
            String updateQuery = MessageFormat.format(
                    "UPDATE {0} SET {1} WHERE {2} = ?",
                    collectionName, valueNames, DATA_KEY_IDENTIFIER);
            ps = connection.prepareStatement(updateQuery);

            ps.setObject(1, byteObjectSerializer.encode(value));
            int index = 2;
            for (JsonNode jsonNode : values) {
                ps.setObject(index, extractJsonNodeValue(jsonNode));
                index++;
            }
            bindKeyToStatement(ps,index, key);
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
    public void insert(V value) throws NDatabaseException {
        K key = value.getKey();
        Connection connection = null;
        PreparedStatement ps = null;

        List<Map.Entry<String, JsonNode>> indexedColumnNamesValues = getIndexedColumnNames(value);
        List<String> columnNameList = indexedColumnNamesValues.stream()
                .map(Map.Entry::getKey).collect(Collectors.toList());
        List<JsonNode> values = indexedColumnNamesValues.stream()
                .map(Map.Entry::getValue).collect(Collectors.toList());
        String columnNames = DATA_KEY_IDENTIFIER + "," + DATA_IDENTIFIER;
        String columnWildCards = "?,?";
        for (String columnName : columnNameList) {
            columnNames += "," + columnName;
            columnWildCards += "," + "?";
        }

        try {
            connection = pool.getConnection();
            String updateQuery = MessageFormat.format(
                    "INSERT INTO {0} ({1}) VALUES({2})",
                    collectionName, columnNames, columnWildCards);
            ps = connection.prepareStatement(updateQuery);

            bindKeyToStatement(ps,1, key);
            ps.setObject(2, byteObjectSerializer.encode(value));
            int index = 3;
            for (JsonNode jsonNode : values) {
                ps.setObject(index, extractJsonNodeValue(jsonNode));
                index++;
            }
            if(ps.executeUpdate() <= 0) {
                throw new NEntityNotFoundException("There is no value with the key " + key + " in the database for collection " + collectionName);
            }
        } catch (SQLException e) {
            throw new NDatabaseException(e);
        } finally {
            close(connection, ps);
        }
        dbLogger.logInsert(value);
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

    @Override
    protected void denormalizeFieldIntoColumn(Connection connection, SingleNodePath singleNodePath) throws SQLException {

        // path.to.field
        // MYSQL doesn't allow "." in column names
        String columnName = singleNodePath.getFullPath("_");
        String fieldPath = singleNodePath.getFullPath(".");
        Class<?> fieldType = singleNodePath.getLastNodeType();;

        String addColumnQuery = MessageFormat.format(
                "ALTER TABLE {0} ADD COLUMN {1} {2}",
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

    private List<Map.Entry<String, JsonNode>> getIndexedColumnNames(V nEntity) {
        List<Map.Entry<String, JsonNode>> entries = new ArrayList<>();
        List<SingleNodePath> indexedFields = getIndexedFields();
        ObjectMapper objectMapper = new ObjectMapper();
        for (SingleNodePath singleNodePath : indexedFields) {

            byte[] valueBytes = byteObjectSerializer.encode(nEntity);
            try {
                JsonNode root = objectMapper.readTree(valueBytes);
                String jsonPathFromNode = getJsonPathFromNode(singleNodePath);
                JsonNode jsonNodeValue = root.at(jsonPathFromNode);
                String columnName = jsonPathFromNode.substring(1).replaceAll("/","_");
                entries.add(new AbstractMap.SimpleEntry<>(columnName, jsonNodeValue));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return entries;
    }

    private String getJsonPathFromNode(SingleNodePath singleNodePath) {
        StringBuilder fieldPath = new StringBuilder("");
        while(singleNodePath != null) {
            if(!singleNodePath.getPathName().isEmpty()) {
                fieldPath.append("/").append(singleNodePath.getPathName());
            }
            singleNodePath = singleNodePath.getChild();
        }
        return fieldPath.toString();
    }

    private List<SingleNodePath> getIndexedFields() {
        if(indexedFields != null) {
            return indexedFields;
        }
        else {
            try {
                List<SingleNodePath> singleNodePaths = new ArrayList<>();
                NReflectionUtil.resolveIndexedFieldsFromEntity(singleNodePaths, new SingleNodePath(), instantiatedNEntity);
                return singleNodePaths;
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new NDatabaseException("Failed to get indexed fields from entity " + nEntityType);
            }
        }
    }
}
