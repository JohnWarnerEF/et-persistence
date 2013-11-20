package com.englishtown.persistence.impl;

import com.englishtown.persistence.EntityRefInfo;
import com.englishtown.persistence.LoadedPersistentMap;
import com.englishtown.persistence.SysFields;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link LoadedPersistentMap}
 */
public class DefaultLoadedPersistentMap extends DefaultPersistentMap implements LoadedPersistentMap {

    private final Map<String, EntityRefInfo> entityRefs;
    private final Map<String, Collection<EntityRefInfo>> entityRefCollections;

    public DefaultLoadedPersistentMap() {
        super();
        entityRefs = new HashMap<>();
        entityRefCollections = new HashMap<>();
    }

    public DefaultLoadedPersistentMap(
            SysFields sysFields,
            Map<String, Object> map,
            Map<String, EntityRefInfo> entityRefs,
            Map<String, Collection<EntityRefInfo>> entityRefCollections) {
        super(sysFields, map);
        this.entityRefs = entityRefs;
        this.entityRefCollections = entityRefCollections;
    }

    @Override
    public Map<String, EntityRefInfo> getEntityRefs() {
        return entityRefs;
    }

    @Override
    public Map<String, Collection<EntityRefInfo>> getEntityRefCollections() {
        return entityRefCollections;
    }
}
