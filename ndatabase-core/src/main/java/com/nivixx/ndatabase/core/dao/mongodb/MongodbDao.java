package com.nivixx.ndatabase.core.dao.mongodb;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.nivixx.ndatabase.api.exception.DatabaseCreationException;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.exception.NDatabaseLoadException;
import com.nivixx.ndatabase.api.exception.NEntityNotFoundException;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.api.query.NQuery;
import com.nivixx.ndatabase.api.query.SingleNodePath;
import com.nivixx.ndatabase.core.dao.Dao;
import com.nivixx.ndatabase.core.expressiontree.ExpressionTree;
import com.nivixx.ndatabase.core.expressiontree.MongoExpressionTreeVisitor;
import com.nivixx.ndatabase.core.serialization.encoder.JsonStringNEntityEncoder;
import com.nivixx.ndatabase.core.serialization.encoder.NEntityEncoder;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MongodbDao<K, V extends NEntity<K>> extends Dao<K, V> {

    private final JsonWriterSettings jsonWriterSettings =
            JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build();

    private MongodbConnection mongodbConnection;
    protected NEntityEncoder<V, String> jsonStringObjectSerializer;

    private MongoDatabase getDatabase() {
        return mongodbConnection.getDatabase();
    }

    private MongoCollection<Document> getCollection() {
        return getDatabase().getCollection(collectionName);
    }

    public MongodbDao(String collectionName,
                      String schema,
                      Class<K> keyType,
                      MongodbConnection mongodbConnection,
                      DBLogger dbLogger) {
        super(collectionName, schema, keyType, dbLogger);
        this.mongodbConnection = mongodbConnection;
        this.jsonStringObjectSerializer = new JsonStringNEntityEncoder<>();
    }

    private String convertToJson(V entity) {
        try {
            return jsonStringObjectSerializer.encode(entity);
        } catch (Exception e) {
            String msg = String.format("Failed to parse entity %s to JSON", entity.getClass().getCanonicalName());
            throw new NDatabaseException(msg, e);
        }
    }

    private Document convertToDocument(V entity) {
        Document document = Document.parse(convertToJson(entity));
        document.append("_id", entity.getKey().toString());
        return document;
    }

    @Override
    public void insert(V value) throws NDatabaseException {
        try {
            getCollection().insertOne(convertToDocument(value));
            dbLogger.logInsert(value);
        } catch (MongoWriteException e) {
            throw new NDatabaseException(e);
        }
    }

    private Document getId(K key) {
        return new Document("_id", key.toString());
    }

    @Override
    public void upsert(V value) throws NDatabaseException {
        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.upsert(true);
        getCollection().replaceOne(getId(value.getKey()), convertToDocument(value), updateOptions);
        dbLogger.logUpsert(value);
    }

    @Override
    public void delete(K key) throws NDatabaseException {
        DeleteResult deleteResult = getCollection().deleteOne(getId(key));
        if(deleteResult.getDeletedCount() == 0) {
            throw new NEntityNotFoundException("There is no value with the key " + key + " in the database for collection " + collectionName);
        }
        dbLogger.logDelete(key);
    }

    @Override
    public void update(V value) throws NDatabaseException {
        try {
            UpdateResult updateResult = getCollection().replaceOne(getId(value.getKey()), convertToDocument(value));
            if(updateResult.getModifiedCount() == 0) {
                throw new NEntityNotFoundException("There is no value with the key " + value.getKey() + " in the database for collection " + collectionName);
            }
            dbLogger.logUpdate(value);
        } catch (MongoServerException e) {
            throw new NDatabaseException(e);
        }
    }

    @Override
    public void deleteAll() throws NDatabaseException {
        getCollection().drop();
        dbLogger.logDeleteAll();
    }

    @Override
    public Stream<V> streamAllValues(Class<V> classz) throws NDatabaseException {

        FindIterable<Document> allDocuments = getCollection().find();
        MongoCursor<Document> cursor = allDocuments.iterator();

        return StreamSupport.stream(new Spliterators.AbstractSpliterator<V>(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE) {
            @Override
            public boolean tryAdvance(Consumer<? super V> action) {
                if(!cursor.hasNext()) {
                    return false;
                }
                Document document = cursor.next();
                action.accept(jsonStringObjectSerializer.decode(document.toJson(jsonWriterSettings), classz));
                return true;
            }
        }, false).onClose(cursor::close);
    }

    @Override
    public V get(K key, Class<V> classz) throws NDatabaseException {
        Document first = getCollection().find(getId(key)).first();

        V returnedValue = null;
        if(first != null) {
            returnedValue = jsonStringObjectSerializer.decode(first.toJson(jsonWriterSettings), classz);
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
    public void validateConnection() throws NDatabaseLoadException {
        try {
            mongodbConnection.connect();
        } catch (Exception e) {
            throw new NDatabaseLoadException("Failed to connect to MongoDB database", e);
        }
    }

    @Override
    public void createDatabaseIfNotExist(Class<K> keyType) throws DatabaseCreationException {
        boolean collectionExists = getDatabase().listCollectionNames()
                .into(new ArrayList<>()).contains(collectionName);
        if(!collectionExists) {
            getDatabase().createCollection(collectionName);
        }
    }

    @Override
    public void createIndexes(List<SingleNodePath> singleNodePaths) throws DatabaseCreationException {
        List<String> fullPathsToIndex = new ArrayList<>();
        for (SingleNodePath singleNodePath : singleNodePaths) {
            SingleNodePath currentNode = singleNodePath;
            String path = "";
            while(currentNode != null) {
                path = path.isEmpty() ? currentNode.getPathName() : path + "." + currentNode.getPathName();
                currentNode = currentNode.getChild();
            }
            fullPathsToIndex.add(path);
        }

        for (String pathsToIndex : fullPathsToIndex) {
            getCollection().createIndex(Indexes.ascending(pathsToIndex));
            dbLogger.logDebug("Created index if not exists " + pathsToIndex);
        }
    }

    @Override
    public Optional<V> findOne(NQuery.Predicate expression, Class<V> classz) {

        ExpressionTree<K,V> expressionTree = ExpressionTree.fromExpressionString(expression.getPredicate(), classz);
        MongoExpressionTreeVisitor<K,V> visitor = new MongoExpressionTreeVisitor<>();
        visitor.visit(expressionTree);
        Bson filter = visitor.getBson();
        FindIterable<Document> documents = getCollection().find(filter);
        Document first = documents.first();
        if(first == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(jsonStringObjectSerializer.decode(first.toJson(jsonWriterSettings), classz));
    }

    @Override
    public List<V> find(NQuery.Predicate expression, Class<V> classz) {
        List<V> entities = new ArrayList<>();
        ExpressionTree<K,V> expressionTree = ExpressionTree.fromExpressionString(expression.getPredicate(), classz);
        MongoExpressionTreeVisitor<K,V> visitor = new MongoExpressionTreeVisitor<>();
        visitor.visit(expressionTree);
        Bson filter = visitor.getBson();
        FindIterable<Document> documents = getCollection().find(filter);
        MongoCursor<Document> cursor = documents.cursor();
        while(cursor.hasNext()) {
            Document next = cursor.next();
            entities.add(jsonStringObjectSerializer.decode(next.toJson(jsonWriterSettings), classz));
        }
        return entities;
    }
}
