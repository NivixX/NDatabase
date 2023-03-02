package com.nivixx.ndatabase.core.serialization.encoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivixx.ndatabase.api.exception.NEntityDecodingException;
import com.nivixx.ndatabase.api.exception.NEntityEncodingException;
import com.nivixx.ndatabase.api.model.NEntity;

import java.io.IOException;

public class JsonStringNEntityEncoder<V extends NEntity<?>> extends NEntityEncoder<V, String> {

    private ObjectMapper objectMapper;

    public JsonStringNEntityEncoder() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper = objectMapper;
    }

    @Override
    public String encode(V value) throws NEntityEncodingException {
        if(value == null) { return null; }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            String msg = String.format("Failed to encode NEntity '%s' for database saving", value.getClass().getCanonicalName());
            throw new NEntityEncodingException(msg, e);
        }
    }

    @Override
    public V decode(String outputValue, Class<V> classz) throws NEntityDecodingException {
        if(outputValue == null) { return null; }
        try {
            return objectMapper.readValue(outputValue, classz);
        } catch (IOException e) {
            String msg = String.format("Failed to decode for NEntity type '%s' for database saving", classz.getCanonicalName());
            throw new NEntityEncodingException(msg, e);
        }
    }
}
