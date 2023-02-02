package com.nivixx.ndatabase.platforms.coreplatform.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class DBLogger {

    public boolean isDebugMode;
    private final ObjectMapper objectMapper;

    public DBLogger(boolean isDebugMode) {
        this.isDebugMode = isDebugMode;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public abstract Consumer<Supplier<String>> consumeDebugMessage();
    public abstract Consumer<Supplier<String>> consumeInfoMessage();
    public abstract BiConsumer<Throwable, String> consumeErrorMessage();
    public abstract Consumer<Supplier<String>> consumeWarnMessage();

    public void logInsert(Object value) {
        consumeDebugMessage().accept(() -> String.format("inserted entity %s", toJson(value)));
    }
    public void logUpsert(Object value) {
        consumeDebugMessage().accept(() -> String.format("upserted entity %s", toJson(value)));
    }
    public void logUpdate(Object value) {
        consumeDebugMessage().accept(() -> String.format("updated entity %s", toJson(value)));
    }
    public void logDelete(Object key) {
        consumeDebugMessage().accept(() ->String.format("deleted entity with key %s", key));
    }
    public void logDeleteAll() {
        consumeDebugMessage().accept(() ->String.format("deleted all %s", "<collection name>")); // TODO entity type
    }
    public void logGet(Object value) {
        consumeDebugMessage().accept(() ->String.format("get entity %s", toJson(value)));
    }
    public void logError(Throwable throwable, String message) {
        consumeErrorMessage().accept(throwable, message);
    }
    public void logWarn(String message) {
        consumeWarnMessage().accept(() -> message);
    }
    public void logDebug(String message) {
        consumeDebugMessage().accept(() -> message);
    }
    
    private String toJson(Object o) {
        if(o == null) {
            return "null value";
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "<FAILED TO CONVERT OBJECT TO JSON>";
        }
    }
    
}
