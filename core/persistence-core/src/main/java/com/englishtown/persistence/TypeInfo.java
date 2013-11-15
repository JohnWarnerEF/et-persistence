package com.englishtown.persistence;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * Contains reflection type information
 */
public class TypeInfo {

    private final Type type;
    private Class<?> rawType;
    private Type[] typeArguments;

    public TypeInfo(Type type) {
        this.type = type;
        init();
    }

    public Type getType() {
        return type;
    }

    public Class<?> getRawType() {
        return rawType;
    }

    public Type[] getTypeArguments() {
        return typeArguments;
    }

    private void init() {

        if (type instanceof Class<?>) {
            rawType = (Class<?>) type;
            typeArguments = new Type[0];

        } else if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            rawType = (Class<?>) p.getRawType();
            typeArguments = p.getActualTypeArguments();

        } else if (type instanceof GenericArrayType) {
            rawType = Object[].class;
            typeArguments = new Type[0];

        } else if (type instanceof WildcardType) {
            throw new IllegalArgumentException("WildcardType is not supported " + type.toString());

        } else {
            throw new IllegalArgumentException("Unsupported type " + type.toString());

        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return type.toString();
    }
}
