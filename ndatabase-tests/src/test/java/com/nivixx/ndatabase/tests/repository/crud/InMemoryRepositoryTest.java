package com.nivixx.ndatabase.tests.repository.crud;

import com.nivixx.ndatabase.api.NDatabase;
import com.nivixx.ndatabase.api.exception.NDatabaseLoadException;
import com.nivixx.ndatabase.core.config.DatabaseType;
import com.nivixx.ndatabase.core.config.NDatabaseConfig;
import com.nivixx.ndatabase.platforms.appplatform.AppNDatabaseConfig;
import com.nivixx.ndatabase.platforms.appplatform.AppPlatformLoader;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.tests.repository.entity.PlayerEntity;
import org.junit.Before;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InMemoryRepositoryTest extends AbstractRepositoryTest {
    public InMemoryRepositoryTest() {
    }

    @Before
    public void initApp() throws NDatabaseLoadException {
        ExecutorService mainThread = Executors.newFixedThreadPool(1);

        appPlatformLoader = new AppPlatformLoader() {
            @Override
            public SyncExecutor supplySyncExecutor() {
                return mainThread::execute;
            }

            @Override
            public NDatabaseConfig supplyNDatabaseConfig() {
                AppNDatabaseConfig bukkitNDatabaseConfig = new AppNDatabaseConfig();
                bukkitNDatabaseConfig.setDatabaseType(DatabaseType.IN_MEMORY);
                return bukkitNDatabaseConfig;
            }
        };
        appPlatformLoader.load();
        repository = NDatabase.api().getOrCreateRepository(PlayerEntity.class);
    }
}
