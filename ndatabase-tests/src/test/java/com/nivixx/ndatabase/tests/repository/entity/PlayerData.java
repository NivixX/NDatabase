package com.nivixx.ndatabase.tests.repository.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nivixx.ndatabase.api.annotation.Indexed;
import com.nivixx.ndatabase.api.model.NEntity;

import java.util.UUID;

public class PlayerData extends NEntity<UUID> {

    @JsonProperty("statistics")
    private PlayerStatistics statistics;

    @JsonProperty("discordId")
    @Indexed
    private String discordId;

}
