package com.nivixx.ndatabase.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class NEntity<K> {

    //TODO @JsonIgnore // Don't store the id twice (in the K-V value)
    @JsonProperty("key")
    protected /*transient*/ K key;

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
