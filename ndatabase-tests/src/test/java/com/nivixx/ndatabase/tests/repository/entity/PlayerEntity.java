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

@NTable(name = "My_table_name")
public class PlayerEntity extends NEntity<UUID> {

    @Indexed
    private String name = "name";

    @JsonSerialize(using = PairObject.PairObjectSerializer.class)
    @JsonDeserialize(using = PairObject.PairObjectDeSerializer.class)
    private PairObject pairObject;

    @Indexed
    @JsonProperty("score")
    private int playerScore = 10;

    private EmbeddedObject embeddedObject = new EmbeddedObject();

    private List<EmbeddedObject> embeddedObjects =
            Arrays.asList(new EmbeddedObject(), new EmbeddedObject());

    private boolean bool = true;

    public PlayerEntity() {
        embeddedObjects = Arrays.asList(new EmbeddedObject(), new EmbeddedObject());
    }

    public PlayerEntity(UUID key, String name, int score) {
        super(key);
        embeddedObjects = Arrays.asList(new EmbeddedObject(), new EmbeddedObject());
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


    public PlayerEntity(UUID key) {
        super(key);
    }

    public List<EmbeddedObject> getEmbeddedObjects() {
        return embeddedObjects;
    }

    public void setEmbeddedObjects(List<EmbeddedObject> embeddedObjects) {
        this.embeddedObjects = embeddedObjects;
    }

    public boolean isBool() {
        return bool;
    }

    public void setBool(boolean bool) {
        this.bool = bool;
    }

    public EmbeddedObject getEmbeddedObject() {
        return embeddedObject;
    }

    public void setEmbeddedObject(EmbeddedObject embeddedObject) {
        this.embeddedObject = embeddedObject;
    }
}
