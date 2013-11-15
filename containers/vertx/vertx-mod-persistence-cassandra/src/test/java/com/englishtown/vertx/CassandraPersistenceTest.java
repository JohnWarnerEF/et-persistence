package com.englishtown.vertx;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.englishtown.cassandra.EntityRef;
import com.englishtown.vertx.persistence.cassandra.*;
import com.google.common.util.concurrent.FutureCallback;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CassandraPersistence}
 */
@RunWith(MockitoJUnitRunner.class)
public class CassandraPersistenceTest {

    CassandraPersistence cassandraPersistence;
    JsonObject config = new JsonObject();
    JsonObject body = new JsonObject();

    @Mock
    Message<JsonObject> message;
    @Mock
    CassandraSession session;
    @Mock
    Vertx vertx;
    @Mock
    Container container;
    @Mock
    EventBus eventBus;
    @Mock
    Logger logger;
    @Mock
    EntityPersistor persistor;
    @Mock
    RowReader rowReader;
    @Captor
    ArgumentCaptor<JsonObject> jsonCaptor;
    @Captor
    ArgumentCaptor<FutureCallback<ResultSet>> storeCallbackCaptor;
    @Captor
    ArgumentCaptor<LoadCallback> loadCallbackCaptor;

    @Before
    public void setUp() throws Exception {

        when(vertx.eventBus()).thenReturn(eventBus);
        when(container.config()).thenReturn(config);
        when(container.logger()).thenReturn(logger);
        when(message.body()).thenReturn(body);

        cassandraPersistence = new CassandraPersistence(session, persistor, rowReader);

        cassandraPersistence.setVertx(vertx);
        cassandraPersistence.setContainer(container);

    }

    @Test
    public void testStart() throws Exception {

        String address = "unit.test.address";
        config.putString("address", address);

        cassandraPersistence.start();
        verify(session).init(eq(config));
        verify(eventBus).registerHandler(eq(address), eq(cassandraPersistence));

    }

    @Test
    public void testStop() throws Exception {
        cassandraPersistence.stop();
        verify(session).close();
    }

    @Test
    public void testHandle_Missing_Action() throws Exception {
        cassandraPersistence.start();
        cassandraPersistence.handle(message);
        verifyError("action must be specified");
    }

    @Test
    public void testHandle_Invalid_Action() throws Exception {
        String action = "invalid";
        body.putString("action", action);

        cassandraPersistence.start();
        cassandraPersistence.handle(message);
        verifyError("action " + action + " is not supported");
    }

    @Test
    public void testHandle_Store_Missing_Entities() throws Exception {
        cassandraPersistence.start();
        body.putString("action", "store");
        cassandraPersistence.handle(message);
        verifyError("store error: An entities json array must be provided");
    }

    @Test
    public void testHandle_Store() throws Exception {

        JsonArray entities = new JsonArray().addObject(new JsonObject());
        body.putString("action", "store")
                .putArray("entities", entities);

        cassandraPersistence.start();
        cassandraPersistence.handle(message);

        verify(persistor).store(eq(entities), any(JsonObject.class), storeCallbackCaptor.capture());

        ResultSet rs = mock(ResultSet.class);
        Throwable t = mock(Throwable.class);
        FutureCallback<ResultSet> callback = storeCallbackCaptor.getValue();
        callback.onSuccess(rs);
        callback.onFailure(t);

    }

    @Test
    public void testHandle_Load() throws Exception {

        JsonArray keys = new JsonArray().addObject(new JsonObject()
                .putString("id", UUID.randomUUID().toString())
                .putString("schema", "keyspaceA")
                .putString("table", "tableA")
        );

        body.putString("action", "load")
                .putArray("keys", keys);

        cassandraPersistence.start();
        cassandraPersistence.handle(message);

        ArgumentCaptor<EntityRef> refCaptor = ArgumentCaptor.forClass(EntityRef.class);
        ArgumentCaptor<LoadResults> resultsCaptor = ArgumentCaptor.forClass(LoadResults.class);
        verify(persistor).load(refCaptor.capture(), resultsCaptor.capture(), loadCallbackCaptor.capture());

        EntityRef ref = refCaptor.getValue();
        LoadResults results = resultsCaptor.getValue();
        Row row = mock(Row.class);
        Throwable t = mock(Throwable.class);

        LoadCallback callback = loadCallbackCaptor.getValue();
        assertEquals(0, results.completedCount);
        callback.onSuccess(ref, row);
        assertEquals(1, results.completedCount);
        callback.onFailure(ref, t);
        assertEquals(2, results.completedCount);

    }

    private void verifyError(String error) {
        verify(logger).error(anyString(), any(Exception.class));
        verify(message).reply(jsonCaptor.capture());
        assertEquals(error, jsonCaptor.getValue().getString("message"));
    }

    @Test
    public void testGetKeys() throws Exception {

    }

    @Test
    public void testHandleResults() throws Exception {

    }

}
