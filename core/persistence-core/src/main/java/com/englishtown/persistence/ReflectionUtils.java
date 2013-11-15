package com.englishtown.persistence;

import java.lang.annotation.Annotation;

/**
 * Reflection helpers
 */
public class ReflectionUtils {

    public static <T extends Annotation> T getAnnotation(Class<T> annotationClass, Class<?> clazz) {

        while (clazz != null && clazz != Object.class) {
            T annotation = clazz.getAnnotation(annotationClass);
            if (annotation != null) {
                return annotation;
            }
            clazz = clazz.getSuperclass();
        }

        return null;
    }

}
