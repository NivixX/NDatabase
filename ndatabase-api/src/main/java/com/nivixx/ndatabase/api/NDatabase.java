package com.nivixx.ndatabase.api;

/**
 * NDatabase - KeyValue store database
 * Report any issue or contribute here https://github.com/NivixX/NDatabase
 */
public class NDatabase {

    private static NDatabaseAPI instance;

    /**
     *
     * @return the api interface to operate NDatabase.
     * The actual implementation will differ depending on what platform
     * you are currently using.
     */
    public static NDatabaseAPI api() {
        NDatabaseAPI instance = NDatabase.instance;
        if (instance == null) {
            throw new IllegalStateException("NDatabase has not loaded yet");
        } else {
            return instance;
        }
    }

    // Note: the NDatabaseAPI instance is set by the core module after loaded
    private static void set(NDatabaseAPI impl) {
        instance = impl;
    }

    private NDatabase() {
        throw new AssertionError("illegal constructor called");
    }
}
