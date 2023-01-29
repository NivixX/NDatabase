package com.nivixx.ndatabase.tests.repository.entity;

import com.nivixx.ndatabase.api.annotation.NTable;
import com.nivixx.ndatabase.api.model.NEntity;

@NTable(name = "string_entity_key")
public class StringKeyEntity extends NEntity<String> {

    private String field;

    public StringKeyEntity() {
    }

    public StringKeyEntity(String key) {
        super(key);
    }
}
