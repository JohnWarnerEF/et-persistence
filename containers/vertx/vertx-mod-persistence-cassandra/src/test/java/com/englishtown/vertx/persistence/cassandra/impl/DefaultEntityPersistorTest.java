package com.englishtown.vertx.persistence.cassandra.impl;

import com.datastax.driver.core.*;
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
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DefaultSelectBuilder}
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultEntityPersistorTest {

    DefaultEntityPersistor persistor;

    @Mock
    CassandraSession session;
    @Mock
    SelectBuilder selectBuilder;
    @Mock
    SchemaBuilder schemaBuilder;
    @Mock
    InsertBuilder insertBuilder;
    @Mock
    FutureCallback<ResultSet> futureCallback;
    @Captor
    ArgumentCaptor<FutureCallback<ResultSet>> callbackCaptor;

    @Before
    public void setUp() throws Exception {

        persistor = new DefaultEntityPersistor(session, selectBuilder, schemaBuilder, insertBuilder);

    }

    @Test
    public void testLoad() throws Exception {

        EntityRef ref = mock(EntityRef.class);
        LoadResults results = new LoadResults();
        LoadCallback callback = mock(LoadCallback.class);

        persistor.load(ref, results, callback);

        verify(selectBuilder).build(eq(ref));
        assertEquals(1, results.totalCount);
        verify(session).executeAsync(any(Statement.class), callbackCaptor.capture());

        ResultSet rs = mock(ResultSet.class);
        Row row = mock(Row.class);
        when(rs.one()).thenReturn(row);
        FutureCallback<ResultSet> futureCallback = callbackCaptor.getValue();

        futureCallback.onSuccess(rs);
        verify(callback).onSuccess(eq(ref), eq(row));

        Throwable t = mock(Throwable.class);
        futureCallback.onFailure(t);
        verify(callback).onFailure(eq(ref), eq(t));

    }

    @Test
    public void testStore() throws Exception {

        JsonArray entities = new JsonArray()
                .addObject(new JsonObject())
                .addObject(new JsonObject())
                .addObject(new JsonObject());

        JsonObject schemas = new JsonObject();

        persistor.store(entities, schemas, futureCallback);

        verify(schemaBuilder, times(3)).build(any(JsonObject.class), eq(schemas), anyMapOf(String.class, TableMetadata.class));
        verify(insertBuilder, times(3)).build(any(JsonObject.class), any(TableMetadata.class));

        ArgumentCaptor<Statement> captor = ArgumentCaptor.forClass(Statement.class);
        verify(session).executeAsync(captor.capture(), eq(futureCallback));

        Statement statement = captor.getValue();
        assertThat(statement, instanceOf(BatchStatement.class));

    }
}
