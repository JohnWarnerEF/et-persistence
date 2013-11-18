package com.englishtown.persistence.impl;

import com.englishtown.persistence.TestEntityParent;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link DefaultEntityKey}
 */
public class DefaultEntityKeyTest {

    DefaultEntityKey key;
    String id = "test id";
    Class<TestEntityParent> clazz = TestEntityParent.class;

    @Before
    public void setUp() throws Exception {
        key = new DefaultEntityKey(id, clazz);
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals(id, key.getId());
    }

    @Test
    public void testGetEntityClass() throws Exception {
        assertEquals(clazz, key.getEntityClass());
    }
}
