package com.englishtown.persistence;

/**
 * Represents an entity ref pointer
 */
public interface EntityRefInfo {

    String getName();

    String getTable();

    String getSchema();

    TypeInfo getTypeInfo();

    Object getValue(Object entity);

}
