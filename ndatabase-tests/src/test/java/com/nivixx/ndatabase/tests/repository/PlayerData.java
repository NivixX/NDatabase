package com.nivixx.ndatabase.tests.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nivixx.ndatabase.api.annotation.Indexed;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.tests.repository.entity.PlayerStatistics;

import java.util.UUID;

public class PlayerData extends NEntity<UUID> {

    @JsonProperty("statistics")
    private com.nivixx.ndatabase.tests.repository.PlayerStatistics statistics;

    @JsonProperty("discordId")
    @Indexed
    private String discordId;

    public PlayerData() { }

    public String getDiscordId() { return discordId; }
    public void setDiscordId(String discordId) { this.discordId = discordId;}
}