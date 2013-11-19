package com.englishtown.persistence;

import java.util.Map;

/**
 * Extends {@link PersistentMap} added related entity refs
 */
public interface LoadedPersistentMap extends PersistentMap {

    Map<String, EntityRefInfo> getEntityRefs();

}
