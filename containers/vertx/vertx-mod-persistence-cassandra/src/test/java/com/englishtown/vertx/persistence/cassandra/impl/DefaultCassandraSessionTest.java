package com.englishtown.vertx.persistence.cassandra.impl;

import com.datastax.driver.core.*;
import com.google.common.util.concurrent.FutureCallback;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.concurrent.Executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DefaultCassandraSession}
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultCassandraSessionTest {

    DefaultCassandraSession cassandraSession;
    JsonObject config = new JsonObject();

    @Mock
    Cluster.Builder clusterBuilder;
    @Mock
    Cluster cluster;
    @Mock
    Session session;
    @Mock
    Metadata metadata;
    @Mock
    FutureCallback<ResultSet> callback;

    @Before
    public void setUp() {

        when(clusterBuilder.build()).thenReturn(cluster);
        when(cluster.connect()).thenReturn(session);
        when(cluster.getMetadata()).thenReturn(metadata);

        cassandraSession = new DefaultCassandraSession(clusterBuilder);

    }

    @Test
    public void testInit() throws Exception {

        cassandraSession.init(config);

        verify(clusterBuilder).addContactPoint(anyString());
        verify(clusterBuilder).build();
        verify(cluster).connect();
        verify(cluster).getMetadata();

        config.putArray("ips", new JsonArray().addString("127.0.0.1").addString("127.0.0.2"));

        cassandraSession.init(config);
        verify(clusterBuilder, times(3)).addContactPoint(anyString());

    }

    @Test
    public void testExecuteAsync() throws Exception {

        Statement statement = mock(Statement.class);
        ResultSetFuture future = mock(ResultSetFuture.class);
        when(session.executeAsync(any(Statement.class))).thenReturn(future);

        try {
            cassandraSession.executeAsync(statement, callback);
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }

        cassandraSession.init(config);
        cassandraSession.executeAsync(statement, callback);
        verify(session).executeAsync(eq(statement));
        verify(future).addListener(any(Runnable.class), any(Executor.class));

    }

    @Test
    public void testExecute() throws Exception {

        String query = "SELECT * FROM table;";

        try {
            cassandraSession.execute(query);
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }

        cassandraSession.init(config);
        cassandraSession.execute(query);
        verify(session).execute(eq(query));

    }

    @Test
    public void testGetMetadata() throws Exception {

        try {
            cassandraSession.getMetadata();
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }

        cassandraSession.init(config);
        assertEquals(metadata, cassandraSession.getMetadata());

    }

    @Test
    public void testClose() throws Exception {
        cassandraSession.init(config);
        cassandraSession.close();
        verify(cluster).shutdown();
    }
}
