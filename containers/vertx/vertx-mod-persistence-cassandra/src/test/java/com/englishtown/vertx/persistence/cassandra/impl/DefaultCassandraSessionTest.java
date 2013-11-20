package com.englishtown.vertx.persistence.cassandra.impl;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
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

import javax.inject.Provider;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Executor;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
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
    @Captor
    ArgumentCaptor<Statement> statementCaptor;
    @Captor
    ArgumentCaptor<LoadBalancingPolicy> loadBalancingPolicyCaptor;

    public static class TestLoadBalancingPolicy implements LoadBalancingPolicy {
        @Override
        public void init(Cluster cluster, Collection<Host> hosts) {
        }

        @Override
        public HostDistance distance(Host host) {
            return null;
        }

        @Override
        public Iterator<Host> newQueryPlan(String loggedKeyspace, Statement statement) {
            return null;
        }

        @Override
        public void onAdd(Host host) {
        }

        @Override
        public void onUp(Host host) {
        }

        @Override
        public void onDown(Host host) {
        }

        @Override
        public void onRemove(Host host) {
        }
    }

    @Before
    public void setUp() {

        when(clusterBuilder.build()).thenReturn(cluster);
        when(cluster.connect()).thenReturn(session);
        when(cluster.getMetadata()).thenReturn(metadata);

        config.putString(DefaultCassandraSession.CONFIG_CONSISTENCY_LEVEL, DefaultCassandraSession.CONSISTENCY_LOCAL_QUORUM);
        cassandraSession = new DefaultCassandraSession(new Provider<Cluster.Builder>() {
            @Override
            public Cluster.Builder get() {
                return clusterBuilder;
            }
        });

    }

    @Test
    public void testInit() throws Exception {

        cassandraSession.init(config);

        verify(clusterBuilder).addContactPoint(anyString());
        verify(clusterBuilder).build();
        verify(cluster).connect();
        verify(cluster).getMetadata();

        config.putArray(DefaultCassandraSession.CONFIG_SEEDS, new JsonArray().addString("127.0.0.1").addString("127.0.0.2"));

        cassandraSession.init(config);
        verify(clusterBuilder, times(3)).addContactPoint(anyString());

    }

    @Test
    public void testInitPolicies_LoadBalancing_No_Policies() throws Exception {
        cassandraSession.initPolicies(clusterBuilder, config);
        verify(clusterBuilder, never()).withLoadBalancingPolicy(any(LoadBalancingPolicy.class));
    }

    @Test
    public void testInitPolicies_LoadBalancing_Missing_Name() throws Exception {
        config.putObject("policies", new JsonObject().putObject("load_balancing", new JsonObject()));
        try {
            cassandraSession.initPolicies(clusterBuilder, config);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testInitPolicies_LoadBalancing_DCAwareRoundRobinPolicy() throws Exception {

        config.putObject("policies", new JsonObject()
                .putObject("load_balancing", new JsonObject()
                        .putString("name", "DCAwareRoundRobinPolicy")
                        .putString("local_dc", "US1")));

        cassandraSession.initPolicies(clusterBuilder, config);
        verify(clusterBuilder).withLoadBalancingPolicy(loadBalancingPolicyCaptor.capture());
        assertThat(loadBalancingPolicyCaptor.getValue(), instanceOf(DCAwareRoundRobinPolicy.class));

    }

    @Test
    public void testInitPolicies_LoadBalancing_Custom() throws Exception {

        config.putObject("policies", new JsonObject()
                .putObject("load_balancing", new JsonObject()
                        .putString("name", "com.englishtown.vertx.persistence.cassandra.impl.DefaultCassandraSessionTest$TestLoadBalancingPolicy")
                ));

        cassandraSession.initPolicies(clusterBuilder, config);
        verify(clusterBuilder).withLoadBalancingPolicy(loadBalancingPolicyCaptor.capture());
        assertThat(loadBalancingPolicyCaptor.getValue(), instanceOf(TestLoadBalancingPolicy.class));

    }

    @Test
    public void testGetConsistency() throws Exception {

        config.removeField(DefaultCassandraSession.CONFIG_CONSISTENCY_LEVEL);
        ConsistencyLevel consistency = cassandraSession.getConsistency(config);
        assertNull(consistency);

        config.putString(DefaultCassandraSession.CONFIG_CONSISTENCY_LEVEL, "");
        consistency = cassandraSession.getConsistency(config);
        assertNull(consistency);

        config.putString(DefaultCassandraSession.CONFIG_CONSISTENCY_LEVEL, DefaultCassandraSession.CONSISTENCY_ALL);
        consistency = cassandraSession.getConsistency(config);
        assertEquals(ConsistencyLevel.ALL, consistency);

        config.putString(DefaultCassandraSession.CONFIG_CONSISTENCY_LEVEL, DefaultCassandraSession.CONSISTENCY_ANY);
        consistency = cassandraSession.getConsistency(config);
        assertEquals(ConsistencyLevel.ANY, consistency);

        config.putString(DefaultCassandraSession.CONFIG_CONSISTENCY_LEVEL, DefaultCassandraSession.CONSISTENCY_EACH_QUORUM);
        consistency = cassandraSession.getConsistency(config);
        assertEquals(ConsistencyLevel.EACH_QUORUM, consistency);

        config.putString(DefaultCassandraSession.CONFIG_CONSISTENCY_LEVEL, DefaultCassandraSession.CONSISTENCY_LOCAL_QUORUM);
        consistency = cassandraSession.getConsistency(config);
        assertEquals(ConsistencyLevel.LOCAL_QUORUM, consistency);

        config.putString(DefaultCassandraSession.CONFIG_CONSISTENCY_LEVEL, DefaultCassandraSession.CONSISTENCY_ONE);
        consistency = cassandraSession.getConsistency(config);
        assertEquals(ConsistencyLevel.ONE, consistency);

        config.putString(DefaultCassandraSession.CONFIG_CONSISTENCY_LEVEL, DefaultCassandraSession.CONSISTENCY_QUORUM);
        consistency = cassandraSession.getConsistency(config);
        assertEquals(ConsistencyLevel.QUORUM, consistency);

        config.putString(DefaultCassandraSession.CONFIG_CONSISTENCY_LEVEL, DefaultCassandraSession.CONSISTENCY_THREE);
        consistency = cassandraSession.getConsistency(config);
        assertEquals(ConsistencyLevel.THREE, consistency);

        config.putString(DefaultCassandraSession.CONFIG_CONSISTENCY_LEVEL, DefaultCassandraSession.CONSISTENCY_TWO);
        consistency = cassandraSession.getConsistency(config);
        assertEquals(ConsistencyLevel.TWO, consistency);

        try {
            config.putString(DefaultCassandraSession.CONFIG_CONSISTENCY_LEVEL, "invalid consistency");
            cassandraSession.getConsistency(config);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }

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
    public void testExecuteAsync_Query() throws Exception {

        String query = "SELECT * FROM table";
        ResultSetFuture future = mock(ResultSetFuture.class);
        when(session.executeAsync(any(Statement.class))).thenReturn(future);

        try {
            cassandraSession.executeAsync(query, callback);
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }

        cassandraSession.init(config);
        cassandraSession.executeAsync(query, callback);
        verify(session).executeAsync(statementCaptor.capture());
        assertEquals(query, statementCaptor.getValue().toString());
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
        verify(session).execute(statementCaptor.capture());
        assertEquals(query, statementCaptor.getValue().toString());

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
