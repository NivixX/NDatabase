package com.nivixx.ndatabase.core.dao.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.mongodb.internal.connection.ServerAddressHelper;
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
        // Creating a Mongo client
        ServerAddress serverAddress = ServerAddressHelper.createServerAddress(config.getHost(), config.getPort());

        // Options
        MongoClientOptions mongoClientOptions = MongoClientOptions.builder().build();

        MongoClient mongo;
        if(config.getUser().isEmpty() || config.getPass().isEmpty()) {
            mongo = new MongoClient(serverAddress, mongoClientOptions);
        }
        else {
            // Creating Credentials
            MongoCredential credential =
                    MongoCredential.createCredential(config.getUser(), config.getDatabase(), config.getPass().toCharArray());
            mongo = new MongoClient(serverAddress, credential, mongoClientOptions);
        }

        // Accessing the database
        this.database = mongo.getDatabase(config.getDatabase());
    }
}
