package com.englishtown.persistence.impl;

import com.englishtown.persistence.SysFields;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link DefaultPersistentMap}
 */
public class DefaultPersistentMapTest {

    @Test
    public void testGetSysFields() throws Exception {

        DefaultPersistentMap map = new DefaultPersistentMap();
        SysFields sysFields = map.getSysFields();
        assertNotNull(sysFields);

        map = new DefaultPersistentMap(sysFields, new HashMap<String, Object>());
        assertEquals(sysFields, map.getSysFields());

    }

    @Test
    public void testGetMap() throws Exception {

        DefaultPersistentMap map = new DefaultPersistentMap();
        Map<String, Object> fields = map.getMap();
        assertNotNull(fields);

        map = new DefaultPersistentMap(mock(SysFields.class), fields);
        assertEquals(fields, map.getMap());

    }
}
