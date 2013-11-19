package com.englishtown.vertx.persistence.impl;

import com.englishtown.vertx.persistence.SchemaCache;
import org.vertx.java.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link SchemaCache}
 */
public class DefaultSchemaCache implements SchemaCache {

    private final Map<String, JsonObject> cache = new HashMap<>();

    @Override
    public JsonObject get(String type) {
        return cache.get(type);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public JsonObject put(String type, JsonObject schema) {
        return cache.put(type, schema);
    }

    @Override
    public int size() {
        return cache.size();
    }

}
