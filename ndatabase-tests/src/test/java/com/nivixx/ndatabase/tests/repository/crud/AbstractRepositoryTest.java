package com.nivixx.ndatabase.tests.repository.crud;

import com.nivixx.ndatabase.api.NDatabase;
import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.query.NQuery;
import com.nivixx.ndatabase.api.repository.Repository;
import com.nivixx.ndatabase.core.PlatformLoader;
import com.nivixx.ndatabase.tests.repository.entity.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractRepositoryTest {

    protected PlatformLoader appPlatformLoader;
    protected Repository<UUID, PlayerEntity> repository;
    protected Repository<UUID, PlayerEntityNoIndex> repositoryNoIndex;
    protected Repository<UUID, EmbeddedBukkitLocation> repositoryEmbeddedBukkitLocation;

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

    @Test
    public void insertUsingCustomJsonSerializer() {
        UUID uuid = UUID.randomUUID();
        PlayerEntity testEntity = new PlayerEntity(uuid);
        testEntity.setPairObject(new PairObject("value1","value2"));
        repository.insert(testEntity);
        PlayerEntity entityFromDb = repository.get(uuid);
        Assert.assertEquals("value1", entityFromDb.getPairObject().getValue1());
        Assert.assertEquals("value2", entityFromDb.getPairObject().getValue2());
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
        playerEntity.setPlayerScore(10);
        repository.insert(playerEntity);
        playerEntity.setPlayerScore(20);
        repository.update(playerEntity);
        PlayerEntity entityFromDb = repository.get(uuid);
        Assert.assertEquals(entityFromDb.getPlayerScore(), 20);
    }

    @Test
    public void updateExistingValueWithConcurrentMultiThreads() {

        for (int i=0; i<50; i++) {
            UUID uuid = UUID.randomUUID();
            PlayerEntity playerEntity = new PlayerEntity(uuid);
            playerEntity.setPlayerScore(10);
            repository.insert(playerEntity);

            repository.getAsync(uuid).thenAsync((playerEntity1, throwable) -> {
                if(throwable != null) {
                    throwable.printStackTrace();
                }
                playerEntity1.setPlayerScore(20);
                repository.update(playerEntity1);
                PlayerEntity entityFromDb = repository.get(uuid);
                Assert.assertEquals(entityFromDb.getPlayerScore(), 20);
            });
        }
    }
    @Test
    public void updateExistingValueWithoutIndexedField() {
        UUID uuid = UUID.randomUUID();
        PlayerEntityNoIndex playerEntity = new PlayerEntityNoIndex(uuid);
        playerEntity.setPlayerScore(10);
        repositoryNoIndex.insert(playerEntity);
        playerEntity.setPlayerScore(20);
        repositoryNoIndex.update(playerEntity);
        PlayerEntityNoIndex entityFromDb = repositoryNoIndex.get(uuid);
        Assert.assertEquals(entityFromDb.getPlayerScore(), 20);
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
        playerEntity.setPlayerScore(10);
        repository.upsert(playerEntity);
        PlayerEntity entityFromDb = repository.get(uuid);
        Assert.assertEquals(entityFromDb.getPlayerScore(), 10);
        playerEntity.setPlayerScore(20);
        repository.upsert(playerEntity);
        entityFromDb = repository.get(uuid);
        Assert.assertEquals(entityFromDb.getPlayerScore(), 20);
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
                repository.findOneAsync(playerEntity -> playerEntity.getPlayerScore() == 20).getResultFuture().join();
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
                repository.findOneAsync(playerEntity -> playerEntity.getPlayerScore() == 25).getResultFuture().join();
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
                repository.findAsync(playerEntity -> playerEntity.getPlayerScore() > 15).getResultFuture().join();
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
                repository.findAsync(playerEntity -> playerEntity.getPlayerScore() > 15).getResultFuture().join();
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
    public void computeTop() {

        PlayerEntity playerEntityTop1 = new PlayerEntity(UUID.randomUUID());
        playerEntityTop1.setPlayerScore(100);
        PlayerEntity playerEntityTop2 = new PlayerEntity(UUID.randomUUID());
        playerEntityTop2.setPlayerScore(50);
        PlayerEntity playerEntityTop3 = new PlayerEntity(UUID.randomUUID());
        playerEntityTop3.setPlayerScore(20);
        PlayerEntity playerEntityTop4 = new PlayerEntity(UUID.randomUUID());
        playerEntityTop4.setPlayerScore(10);
        PlayerEntity playerEntityTop5 = new PlayerEntity(UUID.randomUUID());
        playerEntityTop5.setPlayerScore(1);
        repository.insert(playerEntityTop1);
        repository.insert(playerEntityTop5);
        repository.insert(playerEntityTop2);
        repository.insert(playerEntityTop4);
        repository.insert(playerEntityTop3);

        List<PlayerEntity> join = repository.computeTopAsync(3, (o1, o2) -> o2.getPlayerScore() - o1.getPlayerScore()).getResultFuture().join();

        Assert.assertEquals(playerEntityTop1.getKey(), join.get(0).getKey());
        Assert.assertEquals(playerEntityTop2.getKey(), join.get(1).getKey());
        Assert.assertEquals(playerEntityTop3.getKey(), join.get(2).getKey());
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


    @Test
    public void findOneIndex_simple() {
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        PlayerEntity player1 = new PlayerEntity(player1Id, "name1", 10);
        PlayerEntity player2 = new PlayerEntity(player2Id, "name2", 25);
        repository.insert(player1);
        repository.insert(player2);
        NQuery.Predicate expression = NQuery.predicate("$.score == 25");
        Optional<PlayerEntity> playerWith25Score = repository.findOne(expression);
        Assert.assertTrue(playerWith25Score.isPresent());
    }

    @Test
    public void findOneIndex_simple_compareString() {
        UUID player1Id = UUID.randomUUID();
        PlayerEntity player1 = new PlayerEntity(player1Id, "name1", 10);
        repository.insert(player1);
        NQuery.Predicate expression = NQuery.predicate("$.name == name1");
        Optional<PlayerEntity> playerWith25Score = repository.findOne(expression);
        Assert.assertTrue(playerWith25Score.isPresent());
    }
    @Test
    public void findOneIndex_simple_compareStringDiff() {
        UUID player1Id = UUID.randomUUID();
        PlayerEntity player1 = new PlayerEntity(player1Id, "name1", 10);
        repository.insert(player1);
        NQuery.Predicate expression = NQuery.predicate("$.name != name1");
        List<PlayerEntity> collect = repository.streamAllValues().collect(Collectors.toList());
        Optional<PlayerEntity> playerWith25Score = repository.findOne(expression);
        Assert.assertFalse(playerWith25Score.isPresent());
    }

    @Test
    public void insert_and_increment_and_then_get() {
        Repository<String, BlockStatistics> repo = NDatabase.api().getOrCreateRepository(BlockStatistics.class);
        String diamond = Material.DIAMOND.toString();
        BlockStatistics blockStatistics = new BlockStatistics();
        blockStatistics.setKey(diamond);
        blockStatistics.setMaterialType(diamond);

        // insert
        repo.insert(blockStatistics);

        // increment break
        BlockStatistics diamondBlockStats = repo.get(diamond);
        diamondBlockStats.setBreakCount(diamondBlockStats.getBreakCount()+1);
        repo.update(diamondBlockStats);

        // search by diamond and breakCount >= 1
        Optional<BlockStatistics> search =
                repo.findOne(NQuery.predicate("$.materialType == DIAMOND && $.breakCount >= 1"));
        assert search.isPresent();
    }

    @Test
    public void findOneIndex_simple_logical_different() {
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        PlayerEntity player1 = new PlayerEntity(player1Id, "name1", 10);
        PlayerEntity player2 = new PlayerEntity(player2Id, "name2", 25);
        repository.insert(player1);
        repository.insert(player2);
        NQuery.Predicate expression = NQuery.predicate("$.score != 1");
        Optional<PlayerEntity> playerWith25Score = repository.findOne(expression);
        Assert.assertTrue(playerWith25Score.isPresent());
    }

    @Test
    public void findOneIndex_simple_logical_greater() {
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        PlayerEntity player1 = new PlayerEntity(player1Id, "name1", 10);
        PlayerEntity player2 = new PlayerEntity(player2Id, "name2", 25);
        repository.insert(player1);
        repository.insert(player2);
        NQuery.Predicate expression = NQuery.predicate("$.score > 24");
        Optional<PlayerEntity> playerWith25Score = repository.findOne(expression);
        Assert.assertTrue(playerWith25Score.isPresent());
    }

    @Test
    public void findOneIndex_simple_logical_greaterOrEquals() {
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        PlayerEntity player1 = new PlayerEntity(player1Id, "name1", 10);
        PlayerEntity player2 = new PlayerEntity(player2Id, "name2", 25);
        repository.insert(player1);
        repository.insert(player2);
        NQuery.Predicate expression = NQuery.predicate("$.score >= 25");
        Optional<PlayerEntity> playerWith25Score = repository.findOne(expression);
        Assert.assertTrue(playerWith25Score.isPresent());
    }


    @Test
    public void findOneIndex_simple_logical_less() {
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        PlayerEntity player1 = new PlayerEntity(player1Id, "name1", 10);
        PlayerEntity player2 = new PlayerEntity(player2Id, "name2", 25);
        repository.insert(player1);
        repository.insert(player2);
        NQuery.Predicate expression = NQuery.predicate("$.score < 30");
        Optional<PlayerEntity> playerWith25Score = repository.findOne(expression);
        Assert.assertTrue(playerWith25Score.isPresent());
    }


    @Test
    public void findOneIndex_simple_logical_lessOrEquals() {
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        PlayerEntity player1 = new PlayerEntity(player1Id, "name1", 10);
        PlayerEntity player2 = new PlayerEntity(player2Id, "name2", 25);
        repository.insert(player1);
        repository.insert(player2);
        NQuery.Predicate expression = NQuery.predicate("$.score <= 10");
        Optional<PlayerEntity> playerWith25Score = repository.findOne(expression);
        Assert.assertTrue(playerWith25Score.isPresent());
    }

    @Test
    public void findOneIndex_simpleNotMatch() {
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        PlayerEntity player1 = new PlayerEntity(player1Id, "name1", 10);
        PlayerEntity player2 = new PlayerEntity(player2Id, "name2", 25);
        repository.insert(player1);
        repository.insert(player2);
        NQuery.Predicate expression = NQuery.predicate("$.score==24");
        Optional<PlayerEntity> playerWith25Score = repository.findOne(expression);
        Assert.assertFalse(playerWith25Score.isPresent());
    }

    @Test
    public void findOneIndex_bySubObjectField() {
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        PlayerEntity player1 = new PlayerEntity(player1Id, "name1", 10);
        PlayerEntity player2 = new PlayerEntity(player2Id, "name2", 25);
        player2.getEmbeddedObject().setField(50);
        repository.insert(player1);
        repository.insert(player2);
        NQuery.Predicate expression = NQuery.predicate("$.embeddedObject.field ==50");
        Optional<PlayerEntity> playerWith25Score = repository.findOne(expression);
        Assert.assertTrue(playerWith25Score.isPresent());
    }

    @Test
    public void findOneIndex_or_true_false() {
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        PlayerEntity player1 = new PlayerEntity(player1Id, "name1", 10);
        PlayerEntity player2 = new PlayerEntity(player2Id, "name2", 25);
        repository.insert(player1);
        repository.insert(player2);
        NQuery.Predicate expression = NQuery.predicate("$.score== 25 || $.score == 10");
        Optional<PlayerEntity> playerWith25Score = repository.findOne(expression);
        Assert.assertTrue(playerWith25Score.isPresent());
    }

    @Test
    public void findOneIndex_and_true_false() {
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        PlayerEntity player1 = new PlayerEntity(player1Id, "name1", 10);
        PlayerEntity player2 = new PlayerEntity(player2Id, "name2", 25);
        repository.insert(player1);
        repository.insert(player2);
        NQuery.Predicate expression = NQuery.predicate("$.score == 25 && $.score == 10");
        Optional<PlayerEntity> playerWith25Score = repository.findOne(expression);
        Assert.assertFalse(playerWith25Score.isPresent());
    }


    @Test
    public void findOneIndex_and_true_true() {
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        PlayerEntity player1 = new PlayerEntity(player1Id, "name1", 10);
        PlayerEntity player2 = new PlayerEntity(player2Id, "name2", 25);
        repository.insert(player1);
        repository.insert(player2);
        NQuery.Predicate expression = NQuery.predicate("$.score == 25 && $.name == name2");
        Optional<PlayerEntity> playerWith25Score = repository.findOne(expression);
        Assert.assertTrue(playerWith25Score.isPresent());
    }


    @Test
    public void findOneIndex_brackets() {
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        PlayerEntity player1 = new PlayerEntity(player1Id, "name1", 10);
        PlayerEntity player2 = new PlayerEntity(player2Id, "name2", 25);
        repository.insert(player1);
        repository.insert(player2);
        NQuery.Predicate expression = NQuery.predicate("($.embeddedObject.field == 2 && $.score == 24 ) || $.score == 10");
        Optional<PlayerEntity> playerWith25Score = repository.findOne(expression);
        Assert.assertTrue(playerWith25Score.isPresent());
    }


    @Test
    public void findMultipleIndex_simple() {
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        PlayerEntity player1 = new PlayerEntity(player1Id, "name1", 10);
        PlayerEntity player2 = new PlayerEntity(player2Id, "name2", 25);
        repository.insert(player1);
        repository.insert(player2);
        NQuery.Predicate expression = NQuery.predicate("($.embeddedObject.field == 2 && $.score == 24 ) || $.score == 10");
        Optional<PlayerEntity> playerWith25Score = repository.findOne(expression);
        Assert.assertTrue(playerWith25Score.isPresent());
    }


    // CRUD with an NEntity that not contains an index

    @Test
    public void noIndex_insert() {
        PlayerEntityNoIndex data = new PlayerEntityNoIndex(UUID.randomUUID(), "name", 10);
        repositoryNoIndex.insert(data);
    }

    @Test
    public void noIndex_upsert() {
        PlayerEntityNoIndex data = new PlayerEntityNoIndex(UUID.randomUUID(), "name", 10);
        repositoryNoIndex.upsert(data);
    }

    @Test
    public void noIndex_delete() {
        PlayerEntityNoIndex data = new PlayerEntityNoIndex(UUID.randomUUID(), "name", 10);
        repositoryNoIndex.upsert(data);
        repositoryNoIndex.delete(data);
        Assert.assertNull(repositoryNoIndex.get(data.getKey()));
    }

    @Test
    public void noIndex_get() {
        PlayerEntityNoIndex data = new PlayerEntityNoIndex(UUID.randomUUID(), "name", 10);
        repositoryNoIndex.upsert(data);
        PlayerEntityNoIndex playerEntityNoIndex = repositoryNoIndex.get(data.getKey());
        Assert.assertNotNull(playerEntityNoIndex);
    }

    @Test
    public void embeddedBukkitObject_insert() {
        EmbeddedBukkitLocation data = buildMockedLocation();
        repositoryEmbeddedBukkitLocation.insert(data);
    }
    @Test
    public void embeddedBukkitObject_upsert() {
        EmbeddedBukkitLocation data = buildMockedLocation();
        repositoryEmbeddedBukkitLocation.upsert(data);
    }

    @Test
    public void embeddedBukkitObject_get() {
        EmbeddedBukkitLocation data = buildMockedLocation();
        repositoryEmbeddedBukkitLocation.upsert(data);
        EmbeddedBukkitLocation savedData = repositoryEmbeddedBukkitLocation.get(data.getKey());
        Assert.assertNotNull(savedData);
        Assert.assertNotNull(data.getLocation());
        Assert.assertNotNull(savedData.getLocation());
        Assert.assertNotNull(data.getLocation().getWorld());
        Assert.assertNotNull(savedData.getLocation().getWorld());
        Assert.assertEquals(data.getLocation().getWorld().getName(), savedData.getLocation().getWorld().getName());
        Assert.assertEquals(data.getLocation().getX(), savedData.getLocation().getX(), 0.0);
        Assert.assertEquals(data.getLocation().getY(), savedData.getLocation().getY(), 0.0);
        Assert.assertEquals(data.getLocation().getZ(), savedData.getLocation().getZ(), 0.0);
        Assert.assertEquals(data.getLocation().getYaw(), savedData.getLocation().getYaw(), 0.0);
        Assert.assertEquals(data.getLocation().getPitch(), savedData.getLocation().getPitch(), 0.0);
    }

    private EmbeddedBukkitLocation buildMockedLocation() {
        World world = mock(World.class);
        when(world.getName()).thenReturn("worldName");

        double x = 32, y = 64, z = 32;
        float yawl = 50, pitch = 55;
        Location location = mock(Location.class);
        when(location.getWorld()).thenReturn(world);
        when(location.getX()).thenReturn(x);
        when(location.getY()).thenReturn(y);
        when(location.getZ()).thenReturn(z);
        when(location.getYaw()).thenReturn(yawl);
        when(location.getPitch()).thenReturn(pitch);
        UUID uuid = UUID.randomUUID();
        return new EmbeddedBukkitLocation(uuid, location);
    }
}
