package com.nivixx.ndatabase.tests.repository.crud;

import com.nivixx.ndatabase.api.NDatabase;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.exception.NDatabaseLoadException;
import com.nivixx.ndatabase.api.repository.Repository;
import com.nivixx.ndatabase.core.config.DatabaseType;
import com.nivixx.ndatabase.core.config.NDatabaseConfig;
import com.nivixx.ndatabase.dbms.mariadb.MariaDBConfig;
import com.nivixx.ndatabase.platforms.appplatform.AppNDatabaseConfig;
import com.nivixx.ndatabase.platforms.appplatform.AppPlatformLoader;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.tests.repository.entity.EmbeddedBukkitLocation;
import com.nivixx.ndatabase.tests.repository.entity.InvalidKeyTypeEntity;
import com.nivixx.ndatabase.tests.repository.entity.PlayerEntity;
import com.nivixx.ndatabase.tests.repository.entity.PlayerEntityNoIndex;
import net.byteflux.libby.LibraryManager;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MariaDBRepositoryTest extends AbstractRepositoryTest {


    private static final MariaDBContainer<?> mysqlContainer = new MariaDBContainer<>(DockerImageName.parse("mariadb:11.0"))
            .withUsername("test")
            .withPassword("test")
            .withDatabaseName("test");


    public MariaDBRepositoryTest() {
    }

    @BeforeClass
    public static void initContainer() {
        mysqlContainer.start();
    }

    @AfterClass
    public static void stopContainer() {
        mysqlContainer.stop();
    }

    @Before
    public void initApp() throws NDatabaseLoadException, SQLException {
        String jdbcURL = mysqlContainer.getJdbcUrl();
        ExecutorService mainThread = Executors.newFixedThreadPool(1);

        appPlatformLoader = new AppPlatformLoader() {
            @Override
            public SyncExecutor supplySyncExecutor() {
                return mainThread::execute;
            }

            @Override
            public NDatabaseConfig supplyNDatabaseConfig() {
                AppNDatabaseConfig bukkitNDatabaseConfig = new AppNDatabaseConfig();
                MariaDBConfig mariaDBConfig = new MariaDBConfig();
                mariaDBConfig.setHost(jdbcURL);
                mariaDBConfig.setClassName("org.mariadb.jdbc.Driver");
                mariaDBConfig.setDatabaseName("");
                mariaDBConfig.setPass("test");
                mariaDBConfig.setUser("test");
                bukkitNDatabaseConfig.setDatabaseType(DatabaseType.MARIADB);
                bukkitNDatabaseConfig.setMariaDBConfig(mariaDBConfig);
                return bukkitNDatabaseConfig;
            }

            @Override
            public LibraryManager supplyLibraryManager() {
                return null;
            }
        };
        appPlatformLoader.load();
        repository = NDatabase.api().getOrCreateRepository(PlayerEntity.class);
        repositoryNoIndex = NDatabase.api().getOrCreateRepository(PlayerEntityNoIndex.class);
        repositoryEmbeddedBukkitLocation = NDatabase.api().getOrCreateRepository(EmbeddedBukkitLocation.class);
    }

    @Test(expected = NDatabaseException.class)
    public void createDatabaseWithInvalidKeyType() {
        Repository<Thread, InvalidKeyTypeEntity> integerKeyDao =
                NDatabase.api().getOrCreateRepository(InvalidKeyTypeEntity.class);
        integerKeyDao.insert(new InvalidKeyTypeEntity(null));
    }
}