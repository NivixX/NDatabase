package com.nivixx.ndatabase.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;

public abstract class NEntity<K> {

    //TODO @JsonIgnore // Don't store the id twice (in the K-V value)
    protected /*transient*/ K id;

    public NEntity() {
    }

    public NEntity(K key) {
        this.id = key;
    }

    public K getId() {
        return id;
    }

    public void setId(K id) {
        this.id = id;
    }


}
