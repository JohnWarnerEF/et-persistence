package com.englishtown.persistence;

import java.util.Collection;
import java.util.Map;

/**
 * Factory to create {@link LoadedPersistentMap} instances
 */
public interface LoadedPersistentMapFactory {

    LoadedPersistentMap create(
            Map<String, Object> map,
            Map<String, EntityRefInfo> entityRefs,
            Map<String, Collection<EntityRefInfo>> entityRefCollections);

}
