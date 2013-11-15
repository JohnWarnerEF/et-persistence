package com.englishtown.persistence.impl;

import com.englishtown.persistence.EntityMetadata;
import com.englishtown.persistence.EntityRefInfo;
import com.englishtown.persistence.TypeInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link EntityMetadata}
 */
public class DefaultEntityMetadata implements EntityMetadata {

    private String type;
    private String table;
    private String schema;

    private Map<String, TypeInfo> schemaMap = new HashMap<>();
    private Map<String, EntityRefInfo> entityRefs = new HashMap<>();

    @Override
    public String getType() {
        return type;
    }

    @Override
    public EntityMetadata setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String getTable() {
        return table;
    }

    @Override
    public EntityMetadata setTable(String table) {
        this.table = table;
        return this;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public EntityMetadata setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    @Override
    public Map<String, TypeInfo> getFields() {
        return schemaMap;
    }

    @Override
    public Map<String, EntityRefInfo> getEntityRefs() {
        return entityRefs;
    }
}
