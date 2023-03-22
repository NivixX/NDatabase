package com.nivixx.ndatabase.tests.repository.crud;

import com.nivixx.ndatabase.api.NDatabase;
import com.nivixx.ndatabase.api.exception.NDatabaseLoadException;
import com.nivixx.ndatabase.api.query.NQuery;
import com.nivixx.ndatabase.api.repository.Repository;
import com.nivixx.ndatabase.core.config.DatabaseType;
import com.nivixx.ndatabase.core.config.MariaDBConfig;
import com.nivixx.ndatabase.core.config.NDatabaseConfig;
import com.nivixx.ndatabase.platforms.appplatform.AppNDatabaseConfig;
import com.nivixx.ndatabase.platforms.appplatform.AppPlatformLoader;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {

        ExecutorService mainThread = Executors.newFixedThreadPool(1);

        AppPlatformLoader appPlatformLoader = new AppPlatformLoader() {
            @Override
            public SyncExecutor supplySyncExecutor() {
                return mainThread::execute;
            }

            @Override
            public NDatabaseConfig supplyNDatabaseConfig() {
                AppNDatabaseConfig bukkitNDatabaseConfig = new AppNDatabaseConfig();
                MariaDBConfig mariaDBConfig = new MariaDBConfig();
                mariaDBConfig.setHost("jdbc:mysql://localhost:3306/ouioui");
                mariaDBConfig.setClassName("org.mariadb.jdbc.Driver");
                mariaDBConfig.setDatabaseName("ouioui");
                mariaDBConfig.setPass("passpass");
                mariaDBConfig.setUser("root");
                bukkitNDatabaseConfig.setDatabaseType(DatabaseType.MARIADB);
                bukkitNDatabaseConfig.setMariaDBConfig(mariaDBConfig);
                return bukkitNDatabaseConfig;
            }
        };
        try {
            appPlatformLoader.load();
        } catch (NDatabaseLoadException e) {
            throw new RuntimeException(e);
        }

        Repository<UUID, PlayerData> repository = NDatabase.api().getOrCreateRepository(PlayerData.class);

        repository.upsert(new PlayerData(UUID.randomUUID(), new Random().nextInt(101)));
        List<PlayerData> playerData1 = repository.find(NQuery.predicate("$.coins >= 0"));
        System.out.println("size " + playerData1.size());
        for (PlayerData playerData : repository.find(NQuery.predicate("$.coins >= 0"))) {
            System.out.println(playerData);
        }
        System.out.println(repository.findOne(NQuery.predicate("$.coins == 44")));

        System.out.println();
        System.out.println();
        repository.streamAllValues().forEach(System.out::println);
    }
}
