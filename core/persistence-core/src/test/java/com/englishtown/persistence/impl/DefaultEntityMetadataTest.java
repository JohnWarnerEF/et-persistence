package com.englishtown.persistence.impl;

import com.englishtown.persistence.EntityRefInfo;
import com.englishtown.persistence.TypeInfo;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link DefaultEntityMetadata}
 */
public class DefaultEntityMetadataTest {

    DefaultEntityMetadata metadata = new DefaultEntityMetadata();

    @Test
    public void testGetType() throws Exception {
        String type = "test.type";
        metadata.setType(type);
        assertEquals(type, metadata.getType());
    }

    @Test
    public void testGetTable() throws Exception {
        String table = "test.table";
        metadata.setTable(table);
        assertEquals(table, metadata.getTable());
    }

    @Test
    public void testGetSchema() throws Exception {
        String schema = "test.schema";
        metadata.setSchema(schema);
        assertEquals(schema, metadata.getSchema());
    }

    @Test
    public void testGetFields() throws Exception {
        Map<String, TypeInfo> fields = metadata.getFields();
        assertNotNull(fields);
        assertEquals(fields, metadata.getFields());
    }

    @Test
    public void testGetEntityRefs() throws Exception {
        Map<String, EntityRefInfo> entityRefs = metadata.getEntityRefs();
        assertNotNull(entityRefs);
        assertEquals(entityRefs, metadata.getFields());
    }
}
