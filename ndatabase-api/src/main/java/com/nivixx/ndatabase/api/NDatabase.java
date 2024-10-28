package com.nivixx.ndatabase.api;

import java.util.Objects;

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
            throw new IllegalStateException("NDatabase has not loaded yet. Verify that your plugin configuration include NDatabase in dependencies.");
        } else {
            return instance;
        }
    }

    // Note: the NDatabaseAPI instance is set by the core module after loaded
    static void set(NDatabaseAPI nDatabaseAPI) {
        Objects.requireNonNull(nDatabaseAPI,
                "The NDatabase instance is null, probably due to a Google Guice incompatibility issue with java/spigot or other plugin.");
        instance = nDatabaseAPI;
    }

    private NDatabase() {
        throw new AssertionError("illegal constructor called");
    }
}
