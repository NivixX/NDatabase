package com.nivixx.ndatabase.core.dao.mariadb;

import com.nivixx.ndatabase.api.exception.DatabaseCreationException;
import com.nivixx.ndatabase.api.exception.DatabaseException;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.api.query.SingleNodePath;
import com.nivixx.ndatabase.core.dao.jdbc.JdbcDao;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.List;

public class MariaDao<K, V extends NEntity<K>> extends JdbcDao<K,V> {

    public MariaDao(String collectionName,
                    String schema, Class<K> keyType,
                    HikariMariaConnectionPool hikariConnectionPool,
                    DBLogger dbLogger) {
        super(collectionName, schema, keyType, hikariConnectionPool,  dbLogger);
    }

    @Override
    public void createIndexes(List<SingleNodePath> singleNodePaths) throws DatabaseCreationException {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = pool.getConnection();
            for (SingleNodePath singleNodePath : singleNodePaths) {
                SingleNodePath currentNode = singleNodePath;
                String path = "";
                Class<?> fieldType = null;
                while(currentNode != null) {
                    path = path.isEmpty() ? currentNode.getPathName() : path + "." + currentNode.getPathName();
                    currentNode = currentNode.getChild();
                    if(currentNode != null) {
                        fieldType = currentNode.getType();
                    }
                }
                // path.to.field
                // MYSQL doesn't allow "." in column names
                String columnName = path.replaceAll("\\.","_");

                // Create column if not exist
                try {
                    ps = connection.prepareStatement(
                            "ALTER TABLE " + collectionName + " ADD COLUMN " + columnName + " " + getColumnType(false, fieldType) + " GENERATED ALWAYS AS "
                                    + "("
                                    + "JSON_VALUE(`"+ DATA_IDENTIFIER + "`,'$." + path +"')"
                                    + ")"
                    );
                    ps.execute();
                } catch (SQLException e) {
                    // TODO better way may be possible
                    if(!e.getMessage().toLowerCase().contains("duplicate column name")) {
                        throw new DatabaseException("Error during index creation by de-normalization", e);
                    }
                }

                // Index this column if not exist
                ps = connection.prepareStatement(
                        "CREATE INDEX IF NOT EXISTS " + columnName + "_index ON " + collectionName + " (" + columnName + ")"
                );
                ps.execute();
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        finally {
            close(connection, ps);
        }
    }
}
