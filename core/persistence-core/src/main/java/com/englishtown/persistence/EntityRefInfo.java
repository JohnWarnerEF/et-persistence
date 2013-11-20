package com.englishtown.persistence;

/**
 * Represents an entity ref pointer
 */
public interface EntityRefInfo {

    String getId();

    String getTable();

    String getSchema();

    PersistentMap getPersistentMap();

    EntityRefInfo setPersistentMap(PersistentMap map);

    boolean isLoaded();

}
