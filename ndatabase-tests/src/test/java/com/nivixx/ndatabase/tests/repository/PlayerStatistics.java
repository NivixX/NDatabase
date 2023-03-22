package com.nivixx.ndatabase.tests.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nivixx.ndatabase.api.NDatabase;
import com.nivixx.ndatabase.api.NDatabaseAPI;
import com.nivixx.ndatabase.api.annotation.Indexed;
import com.nivixx.ndatabase.api.query.NQuery;
import com.nivixx.ndatabase.api.repository.Repository;

import java.util.UUID;

public class PlayerStatistics {

    @JsonProperty("kill")
    private int kill;

    @JsonProperty("death")
    private int death;

    @JsonProperty("score")
    @Indexed
    private int score;

    public PlayerStatistics() { }

    public int getKill() { return kill; }
    public void setKill(int kill) { this.kill = kill; }
    public int getDeath() { return death; }
    public void setDeath(int death) { this.death = death; }
    public int getScore() { return score; }
    public void setScore(int score) {
        this.score = score;
    }
}