package com.nivixx.ndatabase.tests.repository.crud;

import com.nivixx.ndatabase.api.NDatabase;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.exception.NDatabaseLoadException;
import com.nivixx.ndatabase.api.repository.Repository;
import com.nivixx.ndatabase.core.config.DatabaseType;
import com.nivixx.ndatabase.core.config.NDatabaseConfig;
import com.nivixx.ndatabase.dbms.mysql.MysqlConfig;
import com.nivixx.ndatabase.platforms.appplatform.AppNDatabaseConfig;
import com.nivixx.ndatabase.platforms.appplatform.AppPlatformLoader;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.tests.repository.entity.EmbeddedBukkitLocation;
import com.nivixx.ndatabase.tests.repository.entity.InvalidKeyTypeEntity;
import com.nivixx.ndatabase.tests.repository.entity.PlayerEntity;
import com.nivixx.ndatabase.tests.repository.entity.PlayerEntityNoIndex;
import org.junit.*;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LegacyMySqlRepositoryTest extends AbstractRepositoryTest {


    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:5.7"))
            .withUsername("test")
            .withPassword("test")
            .withDatabaseName("test");


    public LegacyMySqlRepositoryTest() {
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
                MysqlConfig mysqlConfig = new MysqlConfig();
                mysqlConfig.setHost(jdbcURL);
                mysqlConfig.setClassName("com.mysql.jdbc.Driver");
                mysqlConfig.setDatabaseName("");
                mysqlConfig.setPass("test");
                mysqlConfig.setUser("test");
                bukkitNDatabaseConfig.setDatabaseType(DatabaseType.MYSQL);
                bukkitNDatabaseConfig.setMysqlConfig(mysqlConfig);
                return bukkitNDatabaseConfig;
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