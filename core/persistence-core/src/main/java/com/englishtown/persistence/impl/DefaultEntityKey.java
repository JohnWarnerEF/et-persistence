package com.englishtown.persistence.impl;

import com.englishtown.persistence.EntityKey;
import com.englishtown.persistence.PersistentMap;

/**
 * Default implementation of {@link EntityKey}
 */
public class DefaultEntityKey implements EntityKey {

    private String id;
    private Class<? extends PersistentMap> clazz;

    public DefaultEntityKey(String id, Class<? extends PersistentMap> clazz) {
        this.id = id;
        this.clazz = clazz;
    }

    /**
     * The entity id
     *
     * @return
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * The entity class
     *
     * @return
     */
    @Override
    public Class<? extends PersistentMap> getEntityClass() {
        return clazz;
    }
}
