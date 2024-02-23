package com.nivixx.ndatabase.tests.repository.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.nivixx.ndatabase.api.annotation.NTable;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.tests.repository.entity.serialize.LocationSerializer;
import com.nivixx.ndatabase.tests.repository.entity.serialize.LocationDeserializer;
import org.bukkit.Location;

import java.util.UUID;

@NTable(name = "bukkit_locations")
public class EmbeddedBukkitLocation extends NEntity<UUID> {

    @JsonSerialize(using = LocationSerializer.class)
    @JsonDeserialize(using = LocationDeserializer.class)
    @JsonProperty("pos")
    private Location location;

    public EmbeddedBukkitLocation() {
    }

    public EmbeddedBukkitLocation(UUID id, Location location) {
        this.key = id;
        this.location = location;
    }



    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
