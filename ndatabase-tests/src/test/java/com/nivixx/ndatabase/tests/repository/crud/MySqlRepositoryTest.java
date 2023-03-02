/*
package com.nivixx.ndatabase.tests.repository.crud;

import com.nivixx.ndatabase.api.NDatabase;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.exception.NDatabaseLoadException;
import com.nivixx.ndatabase.api.repository.Repository;
import com.nivixx.ndatabase.core.config.DatabaseType;
import com.nivixx.ndatabase.core.config.MysqlConfig;
import com.nivixx.ndatabase.core.config.NDatabaseConfig;
import com.nivixx.ndatabase.platforms.appplatform.AppNDatabaseConfig;
import com.nivixx.ndatabase.platforms.appplatform.AppPlatformLoader;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.tests.repository.entity.InvalidKeyTypeEntity;
import com.nivixx.ndatabase.tests.repository.entity.PlayerEntity;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MySqlRepositoryTest extends AbstractRepositoryTest {
    public MySqlRepositoryTest() {
    }

    @Before
    public void initApp() throws NDatabaseLoadException, SQLException {
        String jdbcURL = "jdbc:h2:mem:test;MODE=MYSQL";
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
                mysqlConfig.setClassName("org.h2.Driver");
                mysqlConfig.setDatabaseName("");
                mysqlConfig.setPass("");
                mysqlConfig.setUser("");
                bukkitNDatabaseConfig.setDatabaseType(DatabaseType.MYSQL);
                bukkitNDatabaseConfig.setMysqlConfig(mysqlConfig);
                return bukkitNDatabaseConfig;
            }
        };
        appPlatformLoader.load();
        repository = NDatabase.api().getOrCreateRepository(PlayerEntity.class);
    }

    @Test(expected = NDatabaseException.class)
    public void createDatabaseWithInvalidKeyType() {
        Repository<Thread, InvalidKeyTypeEntity> integerKeyDao =
                NDatabase.api().getOrCreateRepository(InvalidKeyTypeEntity.class);
        integerKeyDao.insert(new InvalidKeyTypeEntity(null));
    }
}
*/