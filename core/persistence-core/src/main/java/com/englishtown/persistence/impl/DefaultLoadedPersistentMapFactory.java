package com.englishtown.persistence.impl;

import com.englishtown.persistence.EntityRefInfo;
import com.englishtown.persistence.LoadedPersistentMap;
import com.englishtown.persistence.LoadedPersistentMapFactory;

import java.util.Map;

/**
 * Default implementation of {@link LoadedPersistentMapFactory}
 */
public class DefaultLoadedPersistentMapFactory implements LoadedPersistentMapFactory {

    @Override
    public LoadedPersistentMap create(Map<String, Object> map, Map<String, EntityRefInfo> entityRefs) {
        return new DefaultLoadedPersistentMap(new DefaultSysFields(), map, entityRefs);
    }

}
