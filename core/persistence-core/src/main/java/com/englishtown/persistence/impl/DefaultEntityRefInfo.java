package com.englishtown.persistence.impl;

import com.englishtown.persistence.EntityRefInfo;
import com.englishtown.persistence.PersistentMap;

/**
 * Default implementation of {@link EntityRefInfo}
 */
public class DefaultEntityRefInfo implements EntityRefInfo {

    private final String id;
    private final String table;
    private final String schema;
    private PersistentMap map;

    public DefaultEntityRefInfo(String id, String table, String schema) {
        this.id = id;
        this.table = table;
        this.schema = schema;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTable() {
        return table;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public PersistentMap getPersistentMap() {
        return map;
    }

    @Override
    public EntityRefInfo setPersistentMap(PersistentMap map) {
        this.map = map;
        return this;
    }

    @Override
    public boolean isLoaded() {
        return this.getPersistentMap() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityRefInfo) {
            EntityRefInfo ref = (EntityRefInfo) obj;

            return getId().equals(ref.getId())
                    && getTable().equalsIgnoreCase(ref.getTable())
                    && getSchema().equalsIgnoreCase(ref.getSchema());
        } else {
            return false;
        }
    }
}
