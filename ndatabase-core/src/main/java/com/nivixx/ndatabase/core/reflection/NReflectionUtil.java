package com.nivixx.ndatabase.core.reflection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nivixx.ndatabase.api.annotation.Indexed;
import com.nivixx.ndatabase.core.Injector;
import com.nivixx.ndatabase.expressiontree.SingleNodePath;
import com.nivixx.ndatabase.platforms.coreplatform.logging.DBLogger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.List;

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

    public static void resolveIndexedFieldsFromEntity(List<SingleNodePath> nodePaths, SingleNodePath parentNode, Object entity) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Field[] declaredFields = entity.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            Class<?> type = declaredField.getType();
            String jsonFieldName = NReflectionUtil.resolveJsonFieldName(declaredField);


            if (declaredField.isAnnotationPresent(Indexed.class)) {
                SingleNodePath children = new SingleNodePath();
                children.setPathName(jsonFieldName);
                children.setType(type);
                parentNode.setChild(children);
                nodePaths.add(parentNode);
                parentNode = new SingleNodePath();
            }
            else if (!NReflectionUtil.isNativeJavaClass(type)) {
                if (!hasDefaultConstructor(type)) continue;
                SingleNodePath children = new SingleNodePath();
                children.setPathName(jsonFieldName);
                parentNode.setChild(children);
                resolveIndexedFieldsFromEntity(nodePaths, children, type.getDeclaredConstructor().newInstance());
            }
        }
    }

    private static boolean hasDefaultConstructor(Class<?> type) {
        try {
            type.getDeclaredConstructor();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
}
