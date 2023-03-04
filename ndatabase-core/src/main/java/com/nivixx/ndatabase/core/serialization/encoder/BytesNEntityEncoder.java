package com.nivixx.ndatabase.core.serialization.encoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivixx.ndatabase.api.exception.NEntityDecodingException;
import com.nivixx.ndatabase.api.exception.NEntityEncodingException;
import com.nivixx.ndatabase.api.model.NEntity;

import java.io.IOException;

public class BytesNEntityEncoder<V extends NEntity<?>> extends NEntityEncoder<V, byte[]> {

    private final ObjectMapper objectMapper;

    public BytesNEntityEncoder() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public byte[] encode(V value) throws NEntityEncodingException {
        if(value == null) { return new byte[0]; }
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            String msg = String.format("Failed to encode NEntity '%s' for database saving", value.getClass().getCanonicalName());
            throw new NEntityEncodingException(msg, e);
        }
    }

    @Override
    public V decode(byte[] outputValue, Class<V> classz) throws NEntityDecodingException {
        if(outputValue == null) { return null; }
        try {
            return objectMapper.readValue(outputValue, classz);
        } catch (IOException e) {
            String msg = String.format("Failed to decode for NEntity type '%s' for database saving", classz.getCanonicalName());
            throw new NEntityEncodingException(msg, e);
        }
    }
}
