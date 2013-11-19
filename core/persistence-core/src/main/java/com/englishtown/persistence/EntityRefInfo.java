package com.englishtown.persistence;

/**
 * Represents an entity ref pointer
 */
public interface EntityRefInfo {

    String getId();

    String getTable();

    String getSchema();

    // TODO: Add getPersistentMap() and isLoaded()?

}
