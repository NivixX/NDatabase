package com.nivixx.ndatabase.core;


/**
 * This injector contains all resolved instance depending on the platform
 * For example in Bukkit/Spigot it will use a different Scheduler than in Sponge
 */
public class Injector {

    private static com.google.inject.Injector injector;

    public static <T> T resolveInstance(Class<T> type) {
        T instance = injector.getInstance(type);
        if(instance == null) {
            throw new IllegalArgumentException(String.format("Cannot resolve instance for class type %s", type));
        }
        return instance;
    }

    public static void set(com.google.inject.Injector injector) {
        Injector.injector = injector;
    }
}
