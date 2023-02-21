package com.nivixx.ndatabase.core.dao.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoDatabase;
import com.nivixx.ndatabase.core.config.MongoDBConfig;
import com.nivixx.ndatabase.core.dao.DatabaseConnection;

public class MongodbConnection implements DatabaseConnection {

    private MongoDatabase database;
    private MongoDBConfig config;

    public MongodbConnection(MongoDBConfig mongoDBConfig) {
        this.config = mongoDBConfig;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    @Override
    public void connect() throws Exception {
        // Creating Credentials
        MongoCredential credential =
                MongoCredential.createCredential(config.getUser(), config.getDatabase(), config.getPass().toCharArray());

        // Creating a Mongo client
        MongoClient mongo = new MongoClient(config.getHost(), config.getPort());

        // Accessing the database
        this.database = mongo.getDatabase(config.getDatabase());
    }
}
