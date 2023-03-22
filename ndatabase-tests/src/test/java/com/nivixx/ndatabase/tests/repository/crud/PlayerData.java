package com.nivixx.ndatabase.tests.repository.crud;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nivixx.ndatabase.api.annotation.Indexed;
import com.nivixx.ndatabase.api.annotation.NTable;
import com.nivixx.ndatabase.api.model.NEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;
@NTable(name = "player_data")
public class PlayerData extends NEntity<UUID> {

    @JsonProperty("coins")
    @Indexed
    private int coins;

    public PlayerData() {

    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public PlayerData(UUID uuid, int coins) {
        super(uuid);
        this.coins = coins;
    }
}