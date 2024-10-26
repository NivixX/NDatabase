package com.nivixx.ndatabase.tests.repository.crud;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.nivixx.ndatabase.api.NDatabase;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.exception.NDatabaseLoadException;
import com.nivixx.ndatabase.api.repository.Repository;
import com.nivixx.ndatabase.core.config.DatabaseType;
import com.nivixx.ndatabase.core.config.NDatabaseConfig;
import com.nivixx.ndatabase.dbms.mongodb.MongoDBConfig;
import com.nivixx.ndatabase.platforms.appplatform.AppNDatabaseConfig;
import com.nivixx.ndatabase.platforms.appplatform.AppPlatformLoader;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.tests.repository.entity.EmbeddedBukkitLocation;
import com.nivixx.ndatabase.tests.repository.entity.InvalidKeyTypeEntity;
import com.nivixx.ndatabase.tests.repository.entity.PlayerEntity;
import com.nivixx.ndatabase.tests.repository.entity.PlayerEntityNoIndex;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.MongodStarter;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.mongo.types.DistributionBaseUrl;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;
import org.bson.Document;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MongoDBRepositoryTest extends AbstractRepositoryTest {

    private static TransitionWalker.ReachedState<RunningMongodProcess> running;

    public MongoDBRepositoryTest() {
    }

    @Before
    public void initApp() throws NDatabaseLoadException {
        running = Mongod.instance().start(Version.Main.PRODUCTION);
        ExecutorService mainThread = Executors.newFixedThreadPool(1);

        appPlatformLoader = new AppPlatformLoader() {
            @Override
            public SyncExecutor supplySyncExecutor() {
                return mainThread::execute;
            }

            @Override
            public NDatabaseConfig supplyNDatabaseConfig() {
                AppNDatabaseConfig bukkitNDatabaseConfig = new AppNDatabaseConfig();
                MongoDBConfig mongoDBConfig = new MongoDBConfig();
                mongoDBConfig.setHost(running.current().getServerAddress().getHost());
                mongoDBConfig.setPort(running.current().getServerAddress().getPort());
                mongoDBConfig.setDatabase("ndatabase");
                bukkitNDatabaseConfig.setDatabaseType(DatabaseType.MONGODB);
                bukkitNDatabaseConfig.setMongoDBConfig(mongoDBConfig);
                return bukkitNDatabaseConfig;
            }
        };
        appPlatformLoader.load();
        repository = NDatabase.api().getOrCreateRepository(PlayerEntity.class);
        repositoryNoIndex = NDatabase.api().getOrCreateRepository(PlayerEntityNoIndex.class);
        repositoryEmbeddedBukkitLocation = NDatabase.api().getOrCreateRepository(EmbeddedBukkitLocation.class);
    }

    @AfterClass
    public static void closeServer() {
        running.close();
    }

}
