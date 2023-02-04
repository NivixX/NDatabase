package com.nivixx.ndatabase.core;


import com.google.inject.Key;
import com.google.inject.TypeLiteral;

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

    public static <T> T resolveInstance(TypeLiteral<T> typeLiteral) {
        T instance = injector.getInstance(Key.get(typeLiteral));
        if(instance == null) {
            throw new IllegalArgumentException(String.format("Cannot resolve instance for class generic type %s", typeLiteral));
        }
        return instance;
    }

    public static <T> T resolveInstance(Key<T> key) {
        T instance = injector.getInstance(key);
        if(instance == null) {
            throw new IllegalArgumentException(String.format("Cannot resolve instance for class generic type %s", key));
        }
        return instance;
    }

    public static void set(com.google.inject.Injector injector) {
        Injector.injector = injector;
    }
}
