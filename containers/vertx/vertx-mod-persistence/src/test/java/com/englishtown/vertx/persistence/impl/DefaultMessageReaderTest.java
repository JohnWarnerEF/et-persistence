package com.englishtown.vertx.persistence.impl;

import com.englishtown.persistence.LoadResult;
import com.englishtown.persistence.PersistentMapFactory;
import com.englishtown.persistence.StoreResult;
import com.englishtown.persistence.impl.DefaultLoadResult;
import com.englishtown.persistence.impl.DefaultPersistentMap;
import com.englishtown.promises.Resolver;
import com.englishtown.promises.Value;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.inject.Provider;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultMessageReader}
 */
@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class DefaultMessageReaderTest {

    DefaultMessageReader reader;
    JsonObject body = new JsonObject();

    @Mock
    PersistentMapFactory mapFactory;
    @Mock
    Message<JsonObject> message;
    @Mock
    Resolver<LoadResult, Void> loadResolver;
    @Mock
    Provider<StoreResult> storeResultProvider;
    @Mock
    Provider<LoadResult> loadResultProvider;

    @Before
    public void setUp() {
        when(mapFactory.create(any(Map.class))).thenReturn(new DefaultPersistentMap());
        when(message.body()).thenReturn(body);

        reader = new DefaultMessageReader(mapFactory, storeResultProvider, loadResultProvider);
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

        body.putString("status", "ok")
                .putArray("entities", entities);

        reader.readLoadReply(message, null, loadResolver);
        verify(loadResolver).resolve(any(LoadResult.class));

    }

    @Test
    public void testReadStoreReply() throws Exception {

    }
}
