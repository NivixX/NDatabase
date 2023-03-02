package com.nivixx.ndatabase.tests.repository.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nivixx.ndatabase.api.annotation.Indexed;

public class PlayerStatistics {

    @JsonProperty("kill")
    private int kill;

    @JsonProperty("death")
    private int death;

    @JsonProperty("score")
    @Indexed
    private int score;

}
