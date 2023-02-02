package com.nivixx.ndatabase.core.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivixx.ndatabase.api.model.NEntity;

import java.io.IOException;
import java.io.Serializable;

public class Serializer {

    public static <T extends NEntity<?>> byte[] toByteArray(T value) {// throws IOException {
        if(value == null) { return null; }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] toByteArray(Serializable obj) {// throws IOException {
        if(obj == null) { return null; }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.disable(MapperFeature.USE_ANNOTATIONS);
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T extends NEntity<?>> T deserialize(byte[] bytes, Class<T> classz) {
        if(bytes == null) { return null; }
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return (T) objectMapper.readValue(bytes, classz);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
