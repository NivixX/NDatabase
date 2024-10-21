package com.nivixx.ndatabase.tests.repository.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nivixx.ndatabase.api.annotation.Indexed;
import com.nivixx.ndatabase.api.annotation.NTable;
import com.nivixx.ndatabase.api.model.NEntity;

import java.util.UUID;

@NTable(name = "block_statistics")
public class BlockStatistics extends NEntity<String> {

    @Indexed
    private String materialType;

    @Indexed
    private long breakCount;

    @Indexed
    private long placeCount;

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public long getBreakCount() {
        return breakCount;
    }

    public void setBreakCount(long breakCount) {
        this.breakCount = breakCount;
    }

    public long getPlaceCount() {
        return placeCount;
    }

    public void setPlaceCount(long placeCount) {
        this.placeCount = placeCount;
    }
}
