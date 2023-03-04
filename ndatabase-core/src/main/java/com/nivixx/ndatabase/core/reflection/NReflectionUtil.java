package com.nivixx.ndatabase.core.reflection;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.reflect.Field;

public class NReflectionUtil {

    public static String resolveJsonFieldName(Field field) {
        String jsonFieldName;

        // Use jackson JsonProperty
        if(field.isAnnotationPresent(JsonProperty.class)) {
            JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
            jsonFieldName = jsonProperty.value();
        }
        // Or field name
        else {
            jsonFieldName = field.getName();
        }
        return jsonFieldName;
    }

    public static boolean isNativeJavaClass(Class<?> type) {
        return type.isPrimitive() || type.isEnum() || type.getPackage().getName().startsWith("java.");
    }
}
