package com.nivixx.ndatabase.tests.repository.entity;

import com.nivixx.ndatabase.api.annotation.NTable;
import com.nivixx.ndatabase.api.model.NEntity;

import java.util.UUID;

@NTable(name = "player_statistics")
public class PlayerStats extends NEntity<UUID> {

    private int kill, death, blocMinedCount;
    // ... with getters / setters

    public PlayerStats() { }
}
