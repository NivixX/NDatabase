package com.nivixx.ndatabase.core.dao.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoDatabase;
import com.nivixx.ndatabase.core.config.MongoDBConfig;

public class MongodbConnection {

    private MongoDatabase database;
    private MongoDBConfig config;

    public MongodbConnection(MongoDBConfig mongoDBConfig) {
        this.config = mongoDBConfig;
        connect();
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public void connect() {
        // Creating Credentials
        MongoCredential credential =
                MongoCredential.createCredential(config.getUser(), config.getDatabase(), config.getPass().toCharArray());

        // Creating a Mongo client
        MongoClient mongo = new MongoClient(config.getHost(), config.getPort());

        // Accessing the database
        this.database = mongo.getDatabase(config.getDatabase());
    }
}
