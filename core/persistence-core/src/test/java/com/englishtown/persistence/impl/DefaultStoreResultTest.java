package com.englishtown.persistence.impl;

import com.englishtown.persistence.FailedPersistentMap;
import com.englishtown.persistence.PersistentMap;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link DefaultStoreResult}
 */
public class DefaultStoreResultTest {

    DefaultStoreResult result = new DefaultStoreResult();

    @Test
    public void testSucceeded() throws Exception {

        assertFalse(result.succeeded());

        result.getSucceeded().add(mock(PersistentMap.class));
        assertTrue(result.succeeded());

        result.getFailed().add(mock(FailedPersistentMap.class));
        assertFalse(result.succeeded());

    }

    @Test
    public void testGetSucceeded() throws Exception {

        List<PersistentMap> succeeded = result.getSucceeded();
        assertNotNull(succeeded);
        assertEquals(0, succeeded.size());

        result.getSucceeded().add(mock(PersistentMap.class));
        assertEquals(1, succeeded.size());

    }

    @Test
    public void testGetFailed() throws Exception {

        List<FailedPersistentMap> failed = result.getFailed();
        assertNotNull(failed);
        assertEquals(0, failed.size());

        result.getFailed().add(mock(FailedPersistentMap.class));
        assertEquals(1, failed.size());

    }
}
