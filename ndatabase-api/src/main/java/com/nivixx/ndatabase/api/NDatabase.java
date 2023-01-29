package com.nivixx.ndatabase.api;

import com.nivixx.ndatabase.api.exception.NDatabaseException;
import com.nivixx.ndatabase.api.model.NEntity;
import com.nivixx.ndatabase.api.repository.Repository;

public class NDatabase {

    private static NDatabaseAPI instance;

    public static NDatabaseAPI api() {
        NDatabaseAPI instance = NDatabase.instance;
        if (instance == null) {
            throw new IllegalStateException("NDatabase has not loaded yet");
        } else {
            return instance;
        }
    }

    // Not setable trough the API, will be set by the core
    static void set(NDatabaseAPI impl) {
        instance = impl;
    }

    private NDatabase() {
        throw new AssertionError("illegal constructor called");
    }
}
