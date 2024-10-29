package com.nivixx.ndatabase.api;

import java.util.Objects;

/**
 * NDatabase - KeyValue store database
 * Report any issue or contribute here https://github.com/NivixX/NDatabase
 */
public class NDatabase {

    private static NDatabaseAPI instance;

    private final static String API_BASE_PACKAGE = "com.nivixx.ndatabase.api";

    /**
     * @return the api interface to operate NDatabase.
     * The actual implementation will differ depending on what platform
     * you are currently using.
     */
    public static NDatabaseAPI api() {
        NDatabaseAPI instance = NDatabase.instance;

        if (instance != null) {
            return instance;
        }

        // Check if NDatabase is not shaded into the plugin
        // That may prevent from retrieving the correct singleton API instance
        String callerPackage = NDatabase.class.getPackage().getName();
        if (!Objects.equals(API_BASE_PACKAGE, callerPackage)) {
            throw new IllegalStateException(String.format(
                    "NDatabase has not loaded yet. " +
                            "it looks like you are calling NDatabase.api() from a different package location:" +
                            "%s instead of the original instance located in %s." +
                            "Perhaps you are shading NDatabase into your .jar ? Make sure to declare " +
                            "NDatabase dependency with scope \"provided\" or exclude it from your packaging.",
                    callerPackage, API_BASE_PACKAGE));

        }

        // Trying to get api while not loaded yet, probably because NDatabase is not added into dependencies
        throw new IllegalStateException("NDatabase has not loaded yet. Verify that your plugin configuration include NDatabase in dependencies.");
    }

    // Note: the NDatabaseAPI instance is set by the core module after loaded
    static void set(NDatabaseAPI nDatabaseAPI){
        Objects.requireNonNull(nDatabaseAPI,
                "The NDatabase instance is null, probably due to a Google Guice incompatibility issue with java/spigot or other plugin.");
        instance = nDatabaseAPI;
    }

    private NDatabase() {
        throw new AssertionError("illegal constructor called");
    }

}