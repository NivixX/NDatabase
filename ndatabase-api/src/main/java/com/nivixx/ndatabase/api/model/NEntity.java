package com.nivixx.ndatabase.api.model;

import java.time.Instant;

public abstract class NEntity<K> {

    protected K id;
    protected Instant createdAt;
    protected Instant updatedAt;

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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

}
