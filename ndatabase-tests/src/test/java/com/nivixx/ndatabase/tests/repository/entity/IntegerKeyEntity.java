package com.nivixx.ndatabase.tests.repository.entity;

import com.nivixx.ndatabase.api.annotation.NTable;
import com.nivixx.ndatabase.api.model.NEntity;

@NTable(name = "string_entity_key")
public class IntegerKeyEntity extends NEntity<Integer> {

    private String field;

    public IntegerKeyEntity() {
    }

    public IntegerKeyEntity(Integer key) {
        super(key);
    }
}
