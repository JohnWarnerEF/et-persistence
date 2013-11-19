package com.englishtown.vertx.persistence.impl;

import org.junit.Test;
import org.vertx.java.core.json.JsonObject;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link DefaultSchemaCache}
 */
public class DefaultSchemaCacheTest {

    DefaultSchemaCache schemaCache = new DefaultSchemaCache();
    String type = "test.type";
    JsonObject json = new JsonObject();

    @Test
    public void testGet() throws Exception {

        JsonObject result = schemaCache.get(type);
        assertNull(result);

        schemaCache.put(type, json);

        result = schemaCache.get(type);
        assertNotNull(result);
        assertEquals(json, result);

    }

    @Test
    public void testClear() throws Exception {

        assertEquals(0, schemaCache.size());
        schemaCache.put(type, json);
        assertEquals(1, schemaCache.size());
        schemaCache.put(type + "1", json);
        assertEquals(2, schemaCache.size());

        schemaCache.clear();
        assertEquals(0, schemaCache.size());

    }

    @Test
    public void testPut() throws Exception {

        JsonObject existing = schemaCache.put(type, json);
        assertNull(existing);

        existing = schemaCache.put(type, json);
        assertNotNull(existing);

    }
}
