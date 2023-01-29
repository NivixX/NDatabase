package com.nivixx.ndatabase.tests.repository.entity;

import com.google.inject.Inject;
import com.nivixx.ndatabase.api.annotation.NTable;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.platforms.coreplatform.executor.SyncExecutor;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.util.UUID;

@NTable(name = "my_table_name")
public class PlayerEntity extends NEntity<UUID> {

    private String name;
    private int score;


    public PlayerEntity() {
    }

    public PlayerEntity(UUID key, String name, int score) {
        super(key);
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }


    public PlayerEntity(UUID key) {
        super(key);
    }
}
