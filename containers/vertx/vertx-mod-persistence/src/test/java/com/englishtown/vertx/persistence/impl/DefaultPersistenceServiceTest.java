package com.englishtown.vertx.persistence.impl;

import com.englishtown.persistence.*;
import com.englishtown.persistence.impl.DefaultEntityKey;
import com.englishtown.persistence.impl.DefaultLoadResult;
import com.englishtown.persistence.impl.DefaultPersistentMap;
import com.englishtown.persistence.impl.DefaultStoreResult;
import com.englishtown.promises.Done2;
import com.englishtown.promises.Resolver;
import com.englishtown.vertx.persistence.Member;
import com.englishtown.vertx.persistence.MessageBuilder;
import com.englishtown.vertx.persistence.MessageReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DefaultPersistenceService}
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultPersistenceServiceTest {

    DefaultPersistenceService service;
    String address = "et.persistence.cassandra";
    JsonObject replyJson = new JsonObject();
    JsonObject config = new JsonObject();

    @Mock
    Vertx vertx;
    @Mock
    Container container;
    @Mock
    EventBus eventBus;
    @Mock
    Message<JsonObject> replyMessage;
    @Mock
    MessageBuilder messageBuilder;
    @Mock
    MessageReader messageReader;
    @Mock
    EntityMetadata entityMetadata;
    @Captor
    ArgumentCaptor<JsonObject> jsonObjectCaptor;
    @Captor
    ArgumentCaptor<Handler<Message<JsonObject>>> handlerCaptor;
    @Captor
    ArgumentCaptor<Resolver<LoadResult, Void>> resolverCaptor;
    @Captor
    ArgumentCaptor<Resolver<StoreResult, Void>> resolverCaptor2;


    @Before
    public void setUp() {
        when(vertx.eventBus()).thenReturn(eventBus);
        when(replyMessage.body()).thenReturn(replyJson);
        when(container.config()).thenReturn(config);
        config.putString(DefaultPersistenceService.CONFIG_ADDRESS, address);
        service = new DefaultPersistenceService(vertx, container, messageBuilder, messageReader);
    }

    @Test
    public void testLoad_Illegal_Arg() throws Exception {

        try {
            service.load(null);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }

        try {
            service.load(new ArrayList<EntityKey>());
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }

    }

    @Test
    public void testLoad_Success() throws Exception {

        Done2<LoadResult> done = new Done2<>();

        List<EntityKey> keys = new ArrayList<>();
        keys.add(new DefaultEntityKey("123", Member.class));
        keys.add(new DefaultEntityKey("124", Member.class));

        service.load(keys).then(done.onSuccess, done.onFail);

        verify(eventBus).send(eq(address), any(JsonObject.class), handlerCaptor.capture());
        handlerCaptor.getValue().handle(replyMessage);

        verify(messageReader).readLoadReply(eq(replyMessage), eq(keys), resolverCaptor.capture());
        resolverCaptor.getValue().resolve(new DefaultLoadResult());

        done.assertSuccess();

//        replyJson
//                .putString("status", "ok")
//                .putArray("entities", new JsonArray()
//                        .addObject(new JsonObject()
//                                .putObject("fields", new JsonObject())
//                                .putObject("sys_fields", new JsonObject())
//                        )
//                        .addObject(new JsonObject()
//                                .putObject("fields", new JsonObject())
//                                .putObject("sys_fields", new JsonObject())
//                        )
//                );
//
//        testLoadHelper(done);
//
    }

//    @Test
//    public void testLoad_Fail_Missing_Status() throws Exception {
//
//        Done2<List<PersistentMap>> done = new Done2<>();
//        testLoadHelper(done);
//
//        done.assertFailed();
//    }
//
//    @Test
//    public void testLoad_Fail_Missing_Results() throws Exception {
//
//        replyJson.putString("status", "ok");
//
//        Done2<List<PersistentMap>> done = new Done2<>();
//        testLoadHelper(done);
//
//        done.assertFailed();
//    }
//
//    private void testLoadHelper(Done2<List<PersistentMap>> done) {
//
//        List<EntityKey> keys = new ArrayList<>();
//        keys.add(new DefaultEntityKey("123", Member.class));
//        keys.add(new DefaultEntityKey("124", Member.class));
//
//        service.load(keys)
//                .then(
//                        new Runnable<Promise<List<PersistentMap>, Void>, List<PersistentMap>>() {
//                            @Override
//                            public Promise<List<PersistentMap>, Void> run(List<PersistentMap> entities) {
//                                assertNotNull(entities);
//                                assertEquals(2, entities.size());
//                                return null;
//                            }
//                        })
//                .then(done.onSuccess, done.onFail);
//
//
//        verify(eventBus).send(eq(address), jsonObjectCaptor.capture(), handlerCaptor.capture());
//
//        JsonObject json = jsonObjectCaptor.getValue();
//        assertNotNull(json);
//        assertEquals("load", json.getString("action"));
//
//        JsonArray jsonKeys = json.getArray("refs");
//        assertNotNull(jsonKeys);
//        assertEquals(2, jsonKeys.size());
//        assertEquals("123", jsonKeys.<JsonObject>get(0).getString("id"));
//        assertEquals("124", jsonKeys.<JsonObject>get(1).getString("id"));
//
//        handlerCaptor.getValue().handle(replyMessage);
//
//    }

    @Test
    public void testStore_Fail_Illegal_Arg() throws Exception {

        try {
            service.store(null);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }

        try {
            service.store(new ArrayList<PersistentMap>());
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }

    }

    @Test
    public void testStore_Success() throws Exception {

        Done2<StoreResult> done = new Done2<>();
        List<PersistentMap> entities = new ArrayList<>();
        entities.add(new DefaultPersistentMap());
        entities.add(new DefaultPersistentMap());

        service.store(entities).then(done.onSuccess, done.onFail);

        verify(eventBus).send(eq(address), any(JsonObject.class), handlerCaptor.capture());
        handlerCaptor.getValue().handle(replyMessage);

        verify(messageReader).readStoreReply(eq(replyMessage), eq(entities), resolverCaptor2.capture());
        resolverCaptor2.getValue().resolve(new DefaultStoreResult());

//        testStoreHelper(done);
        done.assertSuccess();
    }

//    @Test
//    public void testStore_Fail() throws Exception {
//
//        Done2<StorageResult> done = new Done2<>();
//        testStoreHelper(done);
//
//        done.assertFailed();
//    }
//
//    private void testStoreHelper(Done2<StorageResult> done) {
//
//        PersistentMap dm1 = new DefaultPersistentMap();
//        dm1.getSysFields().setId("123").setType("test").setVersion(1);
//        PersistentMap dm2 = new DefaultPersistentMap();
//        dm2.getSysFields().setId("124");
//        List<PersistentMap> objects = Arrays.asList(dm1, dm2);
//
//        service.store(objects).then(new Runnable<Promise<StorageResult, Void>, StorageResult>() {
//            @Override
//            public Promise<StorageResult, Void> run(StorageResult value) {
//                assertTrue(value.succeeded());
//                return null;
//            }
//        }).then(done.onSuccess, done.onFail);
//
//        verify(eventBus).send(eq(address), jsonObjectCaptor.capture(), handlerCaptor.capture());
//
//        JsonObject message = jsonObjectCaptor.getValue();
//        assertEquals("store", message.getString("action"));
//        JsonArray entities = message.getArray("entities");
//        assertNotNull(entities);
//        assertEquals(2, entities.size());
//        assertEquals("123", entities.<JsonObject>get(0).getObject("sys_fields").getString("id"));
//        assertEquals("124", entities.<JsonObject>get(1).getObject("sys_fields").getString("id"));
//
//        handlerCaptor.getValue().handle(replyMessage);
//
//    }
}
