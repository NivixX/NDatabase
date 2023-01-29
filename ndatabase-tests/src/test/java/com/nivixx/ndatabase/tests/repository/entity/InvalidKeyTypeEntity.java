package com.nivixx.ndatabase.tests.repository.entity;

import com.nivixx.ndatabase.api.annotation.NTable;
import com.nivixx.ndatabase.api.model.NEntity;

@NTable(name = "invalid_key_type")
public class InvalidKeyTypeEntity extends NEntity<Object> {

    private String field;

    public InvalidKeyTypeEntity() {
    }

    public InvalidKeyTypeEntity(Object key) {
        super(key);
    }
}