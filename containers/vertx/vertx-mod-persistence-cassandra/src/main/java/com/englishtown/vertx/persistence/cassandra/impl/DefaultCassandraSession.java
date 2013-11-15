package com.englishtown.vertx.persistence.cassandra.impl;

import com.datastax.driver.core.*;
import com.englishtown.vertx.persistence.cassandra.CassandraSession;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.inject.Inject;

/**
 * Default implementation of {@link CassandraSession}
 */
public class DefaultCassandraSession implements CassandraSession {

    protected final Cluster.Builder clusterBuilder;

    protected Cluster cluster;
    protected Session session;
    protected Metadata metadata;
    protected boolean initialized;

    @Inject
    public DefaultCassandraSession(Cluster.Builder clusterBuilder) {
        this.clusterBuilder = clusterBuilder;
    }

    @Override
    public void init(JsonObject config) {

        // Get array of IPs, default to localhost
        JsonArray ips = config.getArray("ips");
        if (ips == null || ips.size() == 0) {
            ips = new JsonArray().addString("127.0.0.1");
        }

        // Add cassandra cluster contact points
        for (int i = 0; i < ips.size(); i++) {
            clusterBuilder.addContactPoint(ips.<String>get(i));
        }

        // TODO: Add support for configuring DCAwareRoundRobinPolicy similar to CassandraBinaryStore

        // Build cluster and session
        cluster = clusterBuilder.build();
        session = cluster.connect();
        metadata = cluster.getMetadata();

        initialized = true;
    }

    @Override
    public void executeAsync(Statement statement, FutureCallback<ResultSet> callback) {
        checkInitialized();
        final ResultSetFuture future = session.executeAsync(statement);
        Futures.addCallback(future, callback);
    }

    @Override
    public ResultSet execute(String query) {
        checkInitialized();
        return session.execute(query);
    }

    @Override
    public Metadata getMetadata() {
        checkInitialized();
        return metadata;
    }

    @Override
    public void close() throws Exception {
        if (cluster != null) {
            cluster.shutdown();
            cluster = null;
        }
    }

    protected void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("The DefaultCassandraSession has not been initialized.");
        }
    }

}
