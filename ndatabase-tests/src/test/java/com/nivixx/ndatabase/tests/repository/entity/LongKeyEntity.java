package com.nivixx.ndatabase.tests.repository.entity;

import com.nivixx.ndatabase.api.annotation.NTable;
import com.nivixx.ndatabase.api.model.NEntity;

import java.util.UUID;

@NTable(name = "long_entity_key")
public class LongKeyEntity extends NEntity<Long> {

    private String field;

    public LongKeyEntity() {
    }

    public LongKeyEntity(Long key) {
        super(key);
    }
}
