package com.englishtown.vertx.persistence.cassandra.impl;

import com.datastax.driver.core.Statement;
import com.englishtown.cassandra.EntityRef;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link DefaultSelectBuilder}
 */
public class DefaultSelectBuilderTest {

    DefaultSelectBuilder builder = new DefaultSelectBuilder();

    @Test
    public void testBuild() throws Exception {

        EntityRef ref = new EntityRef(UUID.randomUUID(), "tableA", "keyspaceA");

        Statement statement = builder.build(ref);
        assertNotNull(statement);

        assertEquals("SELECT * FROM " + ref.getKeyspace() + "." + ref.getTable() + " WHERE id="
                + ref.getId().toString() + ";", statement.toString());

    }
}
