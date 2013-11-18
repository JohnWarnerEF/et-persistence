package com.englishtown.persistence.impl;

import org.junit.Test;

import static org.junit.Assert.assertNotEquals;

/**
 * Unit tests for {@link DefaultIDGenerator}
 */
public class DefaultIDGeneratorTest {

    DefaultIDGenerator generator = new DefaultIDGenerator();

    @Test
    public void testCreateID() throws Exception {

        String id1 = generator.createID();
        String id2 = generator.createID();
        String id3 = generator.createID();

        assertNotEquals(id1, id2);
        assertNotEquals(id1, id3);
        assertNotEquals(id2, id3);

    }
}
