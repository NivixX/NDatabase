package com.nivixx.ndatabase.tests.repository.crud;

import com.nivixx.ndatabase.api.NDatabase;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.exception.NDatabaseLoadException;
import com.nivixx.ndatabase.api.repository.Repository;
import com.nivixx.ndatabase.core.config.DatabaseType;
import com.nivixx.ndatabase.core.config.NDatabaseConfig;
import com.nivixx.ndatabase.core.config.SqliteConfig;
import com.nivixx.ndatabase.platforms.appplatform.AppNDatabaseConfig;
import com.nivixx.ndatabase.platforms.appplatform.AppPlatformLoader;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.tests.repository.entity.InvalidKeyTypeEntity;
import com.nivixx.ndatabase.tests.repository.entity.PlayerEntity;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SqliteRepositoryTest extends AbstractRepositoryTest {
    public SqliteRepositoryTest() {
    }

    @Before
    public void initApp() throws NDatabaseLoadException, SQLException, IOException, URISyntaxException {
        String jdbcURL = "jdbc:h2:mem:test;MODE=MYSQL";
        ExecutorService mainThread = Executors.newFixedThreadPool(1);

        URL url = getClass().getClassLoader().getResource("testsqlite.sqlite");
        File file = Paths.get(url.toURI()).toFile();
        appPlatformLoader = new AppPlatformLoader() {
            @Override
            public SyncExecutor supplySyncExecutor() {
                return mainThread::execute;
            }

            @Override
            public NDatabaseConfig supplyNDatabaseConfig() {
                AppNDatabaseConfig bukkitNDatabaseConfig = new AppNDatabaseConfig();
                SqliteConfig sqliteConfig = new SqliteConfig();
                sqliteConfig.setFileFullPath(url.getPath());
                bukkitNDatabaseConfig.setDatabaseType(DatabaseType.SQLITE);
                bukkitNDatabaseConfig.setSqliteConfig(sqliteConfig);
                return bukkitNDatabaseConfig;
            }
        };
        /*
        File dbFile = new File(resource.getPath());
        Files.createDirectories(Paths.get(resource.getPath()));
        dbFile.createNewFile();
        dbFile.deleteOnExit();

         */
        appPlatformLoader.load();
        repository = NDatabase.api().getOrCreateRepository(PlayerEntity.class);
    }

    @AfterClass
    public static void removeSqliteFile() {
    }

    @Test(expected = NDatabaseException.class)
    public void createDatabaseWithInvalidKeyType() {
        Repository<Thread, InvalidKeyTypeEntity> integerKeyDao =
                NDatabase.api().getOrCreateRepository(InvalidKeyTypeEntity.class);
        integerKeyDao.insert(new InvalidKeyTypeEntity(null));
    }
}
