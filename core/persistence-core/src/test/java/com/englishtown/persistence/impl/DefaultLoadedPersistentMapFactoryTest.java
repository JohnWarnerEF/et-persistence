package com.englishtown.persistence.impl;

import com.englishtown.persistence.EntityRefInfo;
import com.englishtown.persistence.LoadedPersistentMap;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link DefaultLoadedPersistentMapFactory}
 */
public class DefaultLoadedPersistentMapFactoryTest {

    DefaultLoadedPersistentMapFactory factory = new DefaultLoadedPersistentMapFactory();

    @Test
    public void testCreate() throws Exception {

        Map<String, Object> fields = new HashMap<>();
        Map<String, EntityRefInfo> entityRefs = new HashMap<>();
        Map<String, Collection<EntityRefInfo>> entityRefCollections = new HashMap<>();
        LoadedPersistentMap map = factory.create(fields, entityRefs, entityRefCollections);
        assertEquals(fields, map.getMap());
        assertEquals(entityRefs, map.getEntityRefs());

    }

}
