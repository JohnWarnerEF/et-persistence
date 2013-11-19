package com.englishtown.persistence.impl;

import com.englishtown.persistence.EntityRefInfo;
import com.englishtown.persistence.LoadedPersistentMap;
import com.englishtown.persistence.SysFields;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link LoadedPersistentMap}
 */
public class DefaultLoadedPersistentMap extends DefaultPersistentMap implements LoadedPersistentMap {

    private final Map<String, EntityRefInfo> entityRefs;

    public DefaultLoadedPersistentMap() {
        super();
        entityRefs = new HashMap<>();
    }

    public DefaultLoadedPersistentMap(SysFields sysFields, Map<String, Object> map, Map<String, EntityRefInfo> entityRefs) {
        super(sysFields, map);
        this.entityRefs = entityRefs;
    }

    @Override
    public Map<String, EntityRefInfo> getEntityRefs() {
        return entityRefs;
    }
}
