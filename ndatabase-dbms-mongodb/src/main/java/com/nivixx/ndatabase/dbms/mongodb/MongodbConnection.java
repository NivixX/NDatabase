package com.nivixx.ndatabase.dbms.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.mongodb.internal.connection.ServerAddressHelper;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.exception.NDatabaseLoadException;
import com.nivixx.ndatabase.dbms.api.DatabaseConnection;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;
import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;
import net.byteflux.libby.classloader.IsolatedClassLoader;

public class MongodbConnection implements DatabaseConnection {

    private MongoDatabase database;
    private MongoDBConfig config;
    private DBLogger dbLogger;
    private LibraryManager libraryManager;

    public MongodbConnection(MongoDBConfig mongoDBConfig, DBLogger dbLogger, LibraryManager libraryManager) {
        this.config = mongoDBConfig;
        this.dbLogger = dbLogger;
        this.libraryManager = libraryManager;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    @Override
    public void connect() throws Exception {
        //installMongoDbDriver();

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

    /*
        private void installMongoDbDriver() {
            Library lib = Library.builder()
                    .groupId("org.mongodb") // "{}" is replaced with ".", useful to avoid unwanted changes made by maven-shade-plugin
                    .artifactId("mongo-java-driver")
                    .version("3.12.12")
                    .isolatedLoad(true)
                    .id("mongo-java-driver")
                    .build();
            dbLogger.logInfo("Loading MongoDB driver");
            libraryManager.loadLibrary(lib);
            IsolatedClassLoader mongoDBClassLoader = libraryManager.getIsolatedClassLoaderOf("mongo-java-driver");
            try {
                Class<?> serverAddressClass = mongoDBClassLoader.loadClass("com.mongodb.ServerAddress");
            } catch (Exception e) {
                throw new NDatabaseException("Failed to load MongoDB classes", e);
            }
            dbLogger.logInfo("MongoDB driver has been loaded with success");
        }
     */
}
