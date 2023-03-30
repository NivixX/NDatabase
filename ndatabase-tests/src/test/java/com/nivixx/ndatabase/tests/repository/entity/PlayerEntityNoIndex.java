package com.nivixx.ndatabase.tests.repository.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.nivixx.ndatabase.api.annotation.Indexed;
import com.nivixx.ndatabase.api.annotation.NTable;
import com.nivixx.ndatabase.api.model.NEntity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@NTable(name = "my_table_name")
public class PlayerEntityNoIndex extends NEntity<UUID> {

    private String name = "name";

    @JsonSerialize(using = PairObject.PairObjectSerializer.class)
    @JsonDeserialize(using = PairObject.PairObjectDeSerializer.class)
    private PairObject pairObject;

    @JsonProperty("score")
    private int playerScore = 10;

    public PlayerEntityNoIndex() { }

    public PlayerEntityNoIndex(UUID key, String name, int score) {
        super(key);
        this.name = name;
        this.playerScore = score;
    }

    public PairObject getPairObject() {
        return pairObject;
    }

    public void setPairObject(PairObject pairObject) {
        this.pairObject = pairObject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPlayerScore() {
        return playerScore;
    }

    public void setPlayerScore(int playerScore) {
        this.playerScore = playerScore;
    }


    public PlayerEntityNoIndex(UUID key) {
        super(key);
    }
}
