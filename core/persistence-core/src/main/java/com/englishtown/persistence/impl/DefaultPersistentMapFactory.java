package com.englishtown.persistence.impl;

import com.englishtown.persistence.PersistentMap;
import com.englishtown.persistence.PersistentMapFactory;

import java.util.Map;

/**
 * Default implementation of {@link com.englishtown.persistence.PersistentMapFactory}
 */
public class DefaultPersistentMapFactory implements PersistentMapFactory {
    @Override
    public PersistentMap create() {
        return new DefaultPersistentMap();
    }

    @Override
    public PersistentMap create(Map<String, Object> map) {
        return new DefaultPersistentMap(new DefaultSysFields(), map);
    }
}
