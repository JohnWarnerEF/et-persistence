package com.englishtown.persistence.impl;

import com.englishtown.persistence.PersistentMap;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link DefaultPersistentMapFactory}
 */
public class DefaultPersistentMapFactoryTest {

    DefaultPersistentMapFactory factory = new DefaultPersistentMapFactory();

    @Test
    public void testCreate() throws Exception {

        PersistentMap map = factory.create();
        assertNotNull(map);

    }

    @Test
    public void testCreate_From_Map() throws Exception {

        Map<String, Object> fields = new HashMap<>();
        PersistentMap map = factory.create(fields);
        assertEquals(fields, map.getMap());

    }

}
