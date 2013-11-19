package com.englishtown.vertx.persistence;

import org.vertx.java.core.json.JsonObject;

/**
 * Cache of entity JSON schemas
 */
public interface SchemaCache {

    JsonObject get(String type);

    void clear();

    JsonObject put(String type, JsonObject schema);

    int size();

}
