package com.englishtown.persistence.impl;

import com.englishtown.persistence.EntityMetadata;
import com.englishtown.persistence.TestEntityParent2;
import com.englishtown.persistence.TypeInfo;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link DefaultEntityMetadataService}
 */
public class DefaultEntityMetadataServiceTest {

    DefaultEntityMetadataService metadataService = new DefaultEntityMetadataService();

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testGet_TestEntity() throws Exception {

        EntityMetadata metadata = metadataService.get(TestEntityParent2.class);

        assertNotNull(metadata);
        assertEquals("com.englishtown.persistence.TestEntityParent", metadata.getType());
        assertEquals("parent", metadata.getTable());
        assertEquals("et_core_test", metadata.getSchema());

        Map<String, TypeInfo> schema = metadata.getFields();
        assertNotNull(schema);

        assertEquals(String.class, schema.get("string_val").getRawType());
        assertEquals(int.class, schema.get("int_val").getRawType());
        assertEquals(Integer.class, schema.get("integer_val").getRawType());
        assertEquals(long.class, schema.get("long_val").getRawType());
        assertEquals(String.class, schema.get("string_field").getRawType());

        // Collection types
        assertEquals(Set.class, schema.get("set_string").getRawType());
        assertEquals(String.class, schema.get("set_string").getTypeArguments()[0]);
        assertEquals(List.class, schema.get("list_string").getRawType());
        assertEquals(String.class, schema.get("list_string").getTypeArguments()[0]);
        assertEquals(Map.class, schema.get("map_string").getRawType());
        assertEquals(String.class, schema.get("map_string").getTypeArguments()[0]);
        assertEquals(Integer.class, schema.get("map_string").getTypeArguments()[1]);

        // Comes from TestEntityParent2
        assertEquals(String.class, schema.get("string_val2").getRawType());

        // Entity refs
        assertEquals(2, metadata.getEntityRefs().size());

    }
}
