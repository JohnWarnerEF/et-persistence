package com.englishtown.persistence.impl;

import com.englishtown.persistence.EntityKey;
import com.englishtown.persistence.LoadedPersistentMap;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link DefaultLoadResult}
 */
public class DefaultLoadResultTest {

    DefaultLoadResult result = new DefaultLoadResult();

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testSucceeded() throws Exception {
        assertFalse(result.succeeded());

        result.getSucceeded().add(mock(LoadedPersistentMap.class));
        assertTrue(result.succeeded());

        EntityKey failed = mock(EntityKey.class);
        result.getFailed().add(failed);

        assertFalse(result.succeeded());
    }

    @Test
    public void testGetSucceeded() throws Exception {

        List<LoadedPersistentMap> succeeded = result.getSucceeded();
        assertNotNull(succeeded);
        assertEquals(0, succeeded.size());

        result.getSucceeded().add(mock(LoadedPersistentMap.class));
        assertEquals(1, succeeded.size());

    }

    @Test
    public void testGetFailed() throws Exception {

        List<EntityKey> failed = result.getFailed();
        assertNotNull(failed);
        assertEquals(0, failed.size());

        result.getFailed().add(mock(EntityKey.class));
        assertEquals(1, failed.size());

    }
}
