package com.nivixx.ndatabase.tests.repository.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nivixx.ndatabase.api.annotation.NTable;
import com.nivixx.ndatabase.api.model.NEntity;

import java.util.UUID;

@NTable(name = "player_statistics", schema = "", catalog = "")
public class PlayerStats extends NEntity<UUID> {

    @JsonProperty("killCount")
    private int kills;

    @JsonProperty("deathCount")
    private int deaths;

    // ... with getters / setters

    // Note that you need a default constructor as the Framework will use it
    public PlayerStats() { }
}
