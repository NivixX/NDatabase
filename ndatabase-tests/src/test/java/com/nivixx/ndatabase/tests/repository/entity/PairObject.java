package com.nivixx.ndatabase.tests.repository.entity;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import jdk.nashorn.internal.runtime.regexp.joni.ast.StringNode;

import java.io.IOException;

public class PairObject {

    private String value1;
    private String value2;

    public PairObject() {
    }


    public static class PairObjectSerializer extends JsonSerializer<PairObject> {

        @Override
        public void serialize(PairObject pairObject,
                              JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider)
                throws IOException, JsonProcessingException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("value1", pairObject.getValue1());
            jsonGenerator.writeStringField("value2", pairObject.getValue2());
            jsonGenerator.writeEndObject();
        }
    }

    public static class PairObjectDeSerializer extends JsonDeserializer<PairObject> {

        @Override
        public PairObject deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JacksonException {
            JsonNode node = jp.getCodec().readTree(jp);
            String value1 = node.get("value1").asText();
            String value2 = node.get("value2").asText();

            return new PairObject(value1, value2);
        }
    }

    public PairObject(String value1, String value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public String getValue1() {
        return value1;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }
}
