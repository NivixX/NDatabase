package com.nivixx.ndatabase.core.serialization.encoder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivixx.ndatabase.api.exception.NEntityDecodingException;
import com.nivixx.ndatabase.api.exception.NEntityEncodingException;
import com.nivixx.ndatabase.api.model.NEntity;

import java.io.IOException;

public abstract class NEntityEncoder<V extends NEntity<?>, O> {

    public abstract O encode(V value) throws NEntityEncodingException;

    public abstract V decode(O outputValue, Class<V> classz) throws NEntityDecodingException;
}
