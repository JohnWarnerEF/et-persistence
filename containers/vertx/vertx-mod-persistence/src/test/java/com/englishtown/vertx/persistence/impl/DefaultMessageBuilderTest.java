package com.englishtown.vertx.persistence.impl;

import com.englishtown.persistence.*;
import com.englishtown.persistence.impl.DefaultEntityMetadataService;
import com.englishtown.vertx.persistence.Member;
import com.englishtown.vertx.persistence.SchemaCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultMessageBuilder}
 */
@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class DefaultMessageBuilderTest {

    DefaultMessageBuilder builder;

    @Mock
    EntityMetadataService metadataService;
    @Mock
    IDGenerator idGenerator;
    @Mock
    SchemaCache schemaCache;

    @Before
    public void setUp() {

        when(metadataService.get(any(Class.class))).thenReturn(mock(EntityMetadata.class));
        when(idGenerator.createID()).thenReturn("new id");

        builder = new DefaultMessageBuilder(metadataService, idGenerator, schemaCache);

    }

    @Test
    public void testBuildLoadMessage_Null() throws Exception {

        List<EntityKey> keys = null;
        JsonObject message;

        message = builder.buildLoadMessage(keys);

        assertNotNull(message);
        assertEquals("load", message.getString("action"));
        JsonArray refArray = message.getArray("refs");
        assertNotNull(refArray);
        assertEquals(0, refArray.size());

    }

    @Test
    public void testBuildLoadMessage_Empty() throws Exception {

        List<EntityKey> keys = new ArrayList<>();
        JsonObject message;

        message = builder.buildLoadMessage(keys);

        assertNotNull(message);
        assertEquals("load", message.getString("action"));
        JsonArray refArray = message.getArray("refs");
        assertNotNull(refArray);
        assertEquals(0, refArray.size());

    }

    @Test
    public void testBuildLoadMessage_Success() throws Exception {

        List<EntityKey> keys = Arrays.asList(mock(EntityKey.class), mock(EntityKey.class));
        JsonObject message;

        message = builder.buildLoadMessage(keys);

        assertNotNull(message);
        assertEquals("load", message.getString("action"));
        JsonArray refArray = message.getArray("refs");
        assertNotNull(refArray);
        assertEquals(2, refArray.size());

    }

    @Test
    public void testBuildStoreMessage_Null() throws Exception {

        List<PersistentMap> entities = null;
        JsonObject message;

        message = builder.buildStoreMessage(entities);

        assertNotNull(message);
        assertEquals("store", message.getString("action"));

        JsonArray entityArray = message.getArray("entities");
        assertNotNull(entityArray);
        assertEquals(0, entityArray.size());

        JsonObject schemaMap = message.getObject("schemas");
        assertNotNull(schemaMap);
        assertEquals(0, schemaMap.size());

    }

    @Test
    public void testBuildStoreMessage_Empty() throws Exception {

        List<PersistentMap> entities = new ArrayList<>();
        JsonObject message;

        message = builder.buildStoreMessage(entities);

        assertNotNull(message);
        assertEquals("store", message.getString("action"));

        JsonArray entityArray = message.getArray("entities");
        assertNotNull(entityArray);
        assertEquals(0, entityArray.size());

        JsonObject schemaMap = message.getObject("schemas");
        assertNotNull(schemaMap);
        assertEquals(0, schemaMap.size());

    }

    @Test
    public void testBuildStoreMessage_Success() throws Exception {

        builder = new DefaultMessageBuilder(new DefaultEntityMetadataService(), idGenerator, schemaCache);

        List<PersistentMap> entities = new ArrayList<>();
        JsonObject message;

        Member member = Member.createInstance();
        entities.add(member);

        message = builder.buildStoreMessage(entities);

        assertNotNull(message);
        assertEquals("store", message.getString("action"));

        JsonArray entityArray = message.getArray("entities");
        assertNotNull(entityArray);
        assertEquals(5, entityArray.size());

        JsonObject schemaMap = message.getObject("schemas");
        assertNotNull(schemaMap);
        assertEquals(3, schemaMap.size());

    }

}
