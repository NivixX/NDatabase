package com.nivixx.ndatabase.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class NEntity<K> {

    @JsonProperty("key")
    protected K key;

    public NEntity() {
    }

    public NEntity(K key) {
        this.key = key;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

}
