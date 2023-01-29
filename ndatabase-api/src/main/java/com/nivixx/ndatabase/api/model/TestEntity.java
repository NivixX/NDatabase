package com.nivixx.ndatabase.api.model;


import com.nivixx.ndatabase.api.annotation.NTable;

@NTable(name = "my_table_name")
public class TestEntity extends NEntity<String> {


    public TestEntity(String key) {
        super(key);
    }
}
