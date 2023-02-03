package com.nivixx.ndatabase.tests.repository.crud;

import com.nivixx.ndatabase.api.NDatabase;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.repository.Repository;
import com.nivixx.ndatabase.core.PlatformLoader;
import com.nivixx.ndatabase.tests.repository.entity.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public abstract class AbstractRepositoryTest {

    protected PlatformLoader appPlatformLoader;
    protected Repository<UUID, PlayerEntity> repository;

    public AbstractRepositoryTest() { }

    @Before
    public abstract void initApp() throws Exception;

    @After
    public void afterEach() {
        repository.deleteAll();
    }

    @Test
    public void insertAndGetValuePresent() {
        UUID uuid = UUID.randomUUID();
        PlayerEntity testEntity = new PlayerEntity(uuid);
        repository.insert(testEntity);
        PlayerEntity entityFromDb = repository.get(uuid);
        Assert.assertNotNull(entityFromDb);
    }

    @Test
    public void insertAndDeleteValueNotPresent() {
        UUID uuid = UUID.randomUUID();
        PlayerEntity playerEntity = new PlayerEntity(uuid);
        repository.insert(playerEntity);
        repository.delete(playerEntity.getKey());
        PlayerEntity entityFromDb = repository.get(uuid);
        Assert.assertNull(entityFromDb);
    }

    @Test(expected = NDatabaseException.class)
    public void insertDoubleKeyException() {
        UUID uuid = UUID.randomUUID();
        PlayerEntity playerEntity = new PlayerEntity(uuid);
        PlayerEntity playerEntity2 = new PlayerEntity(uuid);
        repository.insert(playerEntity);
        repository.insert(playerEntity2);
    }

    @Test
    public void updateExistingValue() {
        UUID uuid = UUID.randomUUID();
        PlayerEntity playerEntity = new PlayerEntity(uuid);
        playerEntity.setScore(10);
        repository.insert(playerEntity);
        playerEntity.setScore(20);
        repository.update(playerEntity);
        PlayerEntity entityFromDb = repository.get(uuid);
        Assert.assertEquals(entityFromDb.getScore(), 20);
    }

    @Test(expected = NDatabaseException.class)
    public void updateNonExistingEntityException() {
        UUID uuid = UUID.randomUUID();
        PlayerEntity playerEntity = new PlayerEntity(uuid);
        repository.update(playerEntity);
    }

    @Test
    public void upsertNotPresentEntity() {
        UUID uuid = UUID.randomUUID();
        PlayerEntity playerEntity = new PlayerEntity(uuid);
        repository.upsert(playerEntity);
        PlayerEntity entityFromDb = repository.get(uuid);
        Assert.assertNotNull(entityFromDb);
    }

    @Test
    public void upsertPresentEntityUpdatedValue() {
        UUID uuid = UUID.randomUUID();
        PlayerEntity playerEntity = new PlayerEntity(uuid);
        playerEntity.setScore(10);
        repository.upsert(playerEntity);
        PlayerEntity entityFromDb = repository.get(uuid);
        Assert.assertEquals(entityFromDb.getScore(), 10);
        playerEntity.setScore(20);
        repository.upsert(playerEntity);
        entityFromDb = repository.get(uuid);
        Assert.assertEquals(entityFromDb.getScore(), 20);
    }


    @Test
    public void deleteExistingEntity() {
        UUID uuid = UUID.randomUUID();
        PlayerEntity playerEntity = new PlayerEntity(uuid);
        repository.insert(playerEntity);
        repository.delete(playerEntity);
        PlayerEntity entityFromDb = repository.get(uuid);
        Assert.assertNull(entityFromDb);
    }


    @Test(expected = NDatabaseException.class)
    public void deleteNonExistingEntity() {
        repository.delete(UUID.randomUUID());
    }

    @Test
    public void findOneByScorePredicateAndPresent() {
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        PlayerEntity player1 = new PlayerEntity(player1Id, "name1", 10);
        PlayerEntity player2 = new PlayerEntity(player2Id, "name2", 20);
        repository.insert(player1);
        repository.insert(player2);
        Optional<PlayerEntity> playerWith20Score =
                repository.findOneAsync(playerEntity -> playerEntity.getScore() == 20).getResultFuture().join();
        Assert.assertTrue(playerWith20Score.isPresent());
        Assert.assertEquals(playerWith20Score.get().getKey(), player2Id);
    }

    @Test
    public void findOneByScorePredicateAndNotPresent() {
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        PlayerEntity player1 = new PlayerEntity(player1Id, "name1", 10);
        PlayerEntity player2 = new PlayerEntity(player2Id, "name2", 20);
        repository.insert(player1);
        repository.insert(player2);
        Optional<PlayerEntity> playerWith25Score =
                repository.findOneAsync(playerEntity -> playerEntity.getScore() == 25).getResultFuture().join();
        Assert.assertFalse(playerWith25Score.isPresent());
    }

    @Test
    public void findMultipleByScorePredicateAndPresent() {
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        UUID player3Id = UUID.randomUUID();
        PlayerEntity player1 = new PlayerEntity(player1Id, "name1", 10);
        PlayerEntity player2 = new PlayerEntity(player2Id, "name2", 20);
        PlayerEntity player3 = new PlayerEntity(player3Id, "name2", 25);
        repository.insert(player1);
        repository.insert(player2);
        repository.insert(player3);
        List<PlayerEntity> playersWithScoreHigherThan15 =
                repository.findAsync(playerEntity -> playerEntity.getScore() > 15).getResultFuture().join();
        Assert.assertEquals(playersWithScoreHigherThan15.size(), 2);
        Assert.assertTrue(playersWithScoreHigherThan15.stream()
                .anyMatch(playerEntity -> playerEntity.getKey().equals(player2Id)));
        Assert.assertTrue(playersWithScoreHigherThan15.stream()
                .anyMatch(playerEntity -> playerEntity.getKey().equals(player3Id)));
    }

    @Test
    public void findMultipleByScorePredicateNoResult() {
        UUID player1Id = UUID.randomUUID();
        PlayerEntity player1 = new PlayerEntity(player1Id, "name1", 10);
        repository.insert(player1);
        List<PlayerEntity> playersWithScoreHigherThan15 =
                repository.findAsync(playerEntity -> playerEntity.getScore() > 15).getResultFuture().join();
        Assert.assertTrue(playersWithScoreHigherThan15.isEmpty());
    }

    @Test
    public void streamEntities() {
        int entityCount = 1000;
        for(int i=0; i < entityCount; i++) {
            PlayerEntity playerEntity = new PlayerEntity(UUID.randomUUID());
            repository.insert(playerEntity);
        }
        long count = repository.streamAllValues().count();
        Assert.assertEquals(entityCount, count);
    }

    @Test
    public void streamEntitiesEmpty() {
        long count = repository.streamAllValues().count();
        Assert.assertEquals(0, count);
    }

    @Test // no exception
    public void createDatabaseWithIntegerKeyType() {
        Repository<Integer, IntegerKeyEntity> integerKeyDao =
                NDatabase.api().getOrCreateRepository(IntegerKeyEntity.class);
        integerKeyDao.insert(new IntegerKeyEntity(1));
    }

    @Test // no exception
    public void createDatabaseWithLongKeyType() {
        Repository<Long, LongKeyEntity> integerKeyDao =
                NDatabase.api().getOrCreateRepository(LongKeyEntity.class);
        integerKeyDao.insert(new LongKeyEntity(1L));
    }

    @Test // no exception
    public void createDatabaseWithStringKeyType() {
        Repository<String, StringKeyEntity> integerKeyDao =
                NDatabase.api().getOrCreateRepository(StringKeyEntity.class);
        integerKeyDao.insert(new StringKeyEntity("key"));
    }


}
