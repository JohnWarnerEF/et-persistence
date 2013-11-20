package com.englishtown.vertx.persistence.impl;

import com.englishtown.persistence.*;
import com.englishtown.persistence.impl.DefaultLoadResult;
import com.englishtown.persistence.impl.DefaultLoadedPersistentMap;
import com.englishtown.persistence.impl.DefaultStoreResult;
import com.englishtown.promises.Resolver;
import com.englishtown.promises.Value;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.inject.Provider;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertFalse;

/**
 * Unit tests for {@link DefaultMessageReader}
 */
@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class DefaultMessageReaderTest {

    DefaultMessageReader reader;
    JsonObject body = new JsonObject();

    @Mock
    LoadedPersistentMapFactory mapFactory;
    @Mock
    Message<JsonObject> message;
    @Mock
    Resolver<LoadResult, Void> loadResolver;
    @Mock
    Provider<StoreResult> storeResultProvider;
    @Mock
    Provider<LoadResult> loadResultProvider;
    @Mock
    Resolver<StoreResult, Void> resolver;
    @Mock
    EntityMetadataService metadataService;
    @Captor
    ArgumentCaptor<StoreResult> storeResultCaptor;

    @Before
    public void setUp() {
        when(mapFactory.create(any(Map.class), any(Map.class), any(Map.class))).thenReturn(new DefaultLoadedPersistentMap());
        when(message.body()).thenReturn(body);

        reader = new DefaultMessageReader(mapFactory, storeResultProvider, loadResultProvider, metadataService);
    }

    @Test
    public void testReadLoadReply_No_Status() throws Exception {

        reader.readLoadReply(message, null, loadResolver);
        verify(loadResolver).reject(any(Value.class));

    }

    @Test
    public void testReadLoadReply_Error_Status() throws Exception {

        body.putString("status", "error");

        reader.readLoadReply(message, null, loadResolver);
        verify(loadResolver).reject(any(Value.class));

    }

    @Test
    public void testReadLoadReply_No_Entities() throws Exception {

        body.putString("status", "ok");

        reader.readLoadReply(message, null, loadResolver);
        verify(loadResolver).reject(any(Value.class));

    }

    @Test
    public void testReadLoadReply_Success() throws Exception {

        when(loadResultProvider.get()).thenReturn(new DefaultLoadResult());

        JsonObject fields = new JsonObject();
        JsonObject sysFields = new JsonObject();

        JsonArray entities = new JsonArray()
                .addObject(new JsonObject()
                        .putObject("fields", fields)
                        .putObject("sys_fields", sysFields));

        sysFields.putArray("acl", new JsonArray().addString(UUID.randomUUID().toString()));

        body.putString("status", "ok")
                .putArray("entities", entities);

        reader.readLoadReply(message, null, loadResolver);
        verify(loadResolver).resolve(any(LoadResult.class));

    }

    @Test
    public void testReadMissing() throws Exception {

        String id = "test.id";
        String table = "test.table";
        String schema = "test.schema";

        JsonObject entityRef = new JsonObject()
                .putString("id", id)
                .putString("schema", schema)
                .putString("table", table);
        JsonArray missing = new JsonArray().addObject(entityRef);
        List<EntityKey> keys = new ArrayList<>();
        List<EntityKey> missingKeys = new ArrayList<>();
        LoadResult result = mock(LoadResult.class);
        when(result.getFailed()).thenReturn(missingKeys);

        EntityKey key = mock(EntityKey.class);
        when(key.getId()).thenReturn(id);
        keys.add(key);

        EntityMetadata metadata = mock(EntityMetadata.class);
        when(metadata.getTable()).thenReturn(table);
        when(metadata.getSchema()).thenReturn(schema);
        when(metadataService.get(any(Class.class))).thenReturn(metadata);

        reader.readMissing(missing, keys, result);
        assertEquals(1, missingKeys.size());
        assertEquals(key, missingKeys.get(0));

    }

    @Test
    public void testReadMissing_Invalid() throws Exception {

        JsonObject entityRef = new JsonObject();
        JsonArray missing = new JsonArray().addObject(entityRef);
        List<EntityKey> keys = new ArrayList<>();
        LoadResult result = mock(LoadResult.class);

        try {
            reader.readMissing(missing, keys, result);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }

        entityRef.putString("id", "test.id");

        try {
            reader.readMissing(missing, keys, result);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }

        entityRef.putString("table", "test.table");

        try {
            reader.readMissing(missing, keys, result);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }

        entityRef.putString("schema", "test.schema");

        reader.readMissing(missing, keys, result);
        verifyZeroInteractions(result);

    }

    @Test
    public void testRemoveEntityRefs() throws Exception {

        String id = "test.id";
        String table = "test.table";
        String schema = "test.schema";

        JsonObject entityRef = new JsonObject()
                .putString("id", id)
                .putString("schema", schema)
                .putString("table", table)
                .putString("type", "EntityRef");

        JsonObject fields = new JsonObject()
                .putString("str", "value")
                .putNumber("num", 30)
                .putBoolean("bool", true)
                .putArray("arr1", new JsonArray())
                .putArray("arr2", new JsonArray().addString("a").addString("b"))
                .putArray("arr3", new JsonArray().addObject(entityRef).addObject(entityRef))
                .putObject("obj1", new JsonObject().putString("prop1", "value"))
                .putObject("obj2", entityRef);

        Map<String, EntityRefInfo> entityRefs = new HashMap<>();
        Map<String, Collection<EntityRefInfo>> entityRefCollections = new HashMap<>();
        List<EntityRefInfo> allEntityRefs = new ArrayList<>();

        assertEquals(8, fields.getFieldNames().size());
        reader.removeEntityRefs(fields, entityRefs, entityRefCollections, allEntityRefs);

        assertEquals(6, fields.getFieldNames().size());
        assertNull(fields.getObject("obj2"));
        assertNull(fields.getObject("arr3"));

        assertEquals(1, entityRefs.size());
        assertEquals(1, entityRefCollections.size());
        assertEquals(2, entityRefCollections.get("arr3").size());

    }

    @Test
    public void testReadStoreReply_OK() throws Exception {

        when(storeResultProvider.get()).thenReturn(new DefaultStoreResult());
        List<PersistentMap> entities = new ArrayList<>();
        PersistentMap entity1 = mock(PersistentMap.class);
        PersistentMap entity2 = mock(PersistentMap.class);

        entities.add(entity1);
        entities.add(entity2);

        body.putString("status", "ok");

        reader.readStoreReply(message, entities, resolver);
        verify(resolver).resolve(storeResultCaptor.capture());

        StoreResult result = storeResultCaptor.getValue();
        assertTrue(result.succeeded());
        assertEquals(2, result.getSucceeded().size());
        assertEquals(entity1, result.getSucceeded().get(0));
        assertEquals(entity2, result.getSucceeded().get(1));

    }

    @Test
    public void testReadStoreReply_Error() throws Exception {

        when(storeResultProvider.get()).thenReturn(new DefaultStoreResult());
        List<PersistentMap> entities = new ArrayList<>();
        PersistentMap entity1 = mock(PersistentMap.class);
        PersistentMap entity2 = mock(PersistentMap.class);

        entities.add(entity1);
        entities.add(entity2);

        String testMessage = "test message";
        body.putString("status", "error")
                .putString("message", testMessage);

        reader.readStoreReply(message, entities, resolver);
        verify(resolver).reject(storeResultCaptor.capture());

        StoreResult result = storeResultCaptor.getValue();
        assertFalse(result.succeeded());
        assertEquals(0, result.getSucceeded().size());
        assertEquals(2, result.getFailed().size());

        FailedPersistentMap failed1 = result.getFailed().get(0);
        FailedPersistentMap failed2 = result.getFailed().get(1);

        assertEquals(entity1, failed1.getPersistentMap());
        assertEquals(entity2, failed2.getPersistentMap());

        assertEquals(testMessage, failed1.getCause().getMessage());
        assertEquals(testMessage, failed2.getCause().getMessage());

    }

}
