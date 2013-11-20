package com.englishtown.vertx.persistence.cassandra.impl;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.englishtown.vertx.persistence.cassandra.CassandraSession;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Default implementation of {@link CassandraSession}
 */
public class DefaultCassandraSession implements CassandraSession {

    protected final Provider<Cluster.Builder> builderProvider;

    protected Cluster cluster;
    protected Session session;
    protected Metadata metadata;
    protected boolean initialized;
    protected ConsistencyLevel consistency;

    public static final String CONFIG_SEEDS = "seeds";
    public static final String CONFIG_CONSISTENCY_LEVEL = "consistency_level";

    public static final String CONSISTENCY_ANY = "ANY";
    public static final String CONSISTENCY_ONE = "ONE";
    public static final String CONSISTENCY_TWO = "TWO";
    public static final String CONSISTENCY_THREE = "THREE";
    public static final String CONSISTENCY_QUORUM = "QUORUM";
    public static final String CONSISTENCY_ALL = "ALL";
    public static final String CONSISTENCY_LOCAL_QUORUM = "LOCAL_QUORUM";
    public static final String CONSISTENCY_EACH_QUORUM = "EACH_QUORUM";


    @Inject
    public DefaultCassandraSession(Provider<Cluster.Builder> builderProvider) {
        this.builderProvider = builderProvider;
    }

    @Override
    public void init(JsonObject config) {

        // Get array of IPs, default to localhost
        JsonArray seeds = config.getArray(CONFIG_SEEDS);
        if (seeds == null || seeds.size() == 0) {
            seeds = new JsonArray().addString("127.0.0.1");
        }

        Cluster.Builder builder = builderProvider.get();

        // Add cassandra cluster contact points
        for (int i = 0; i < seeds.size(); i++) {
            builder.addContactPoint(seeds.<String>get(i));
        }

        // Add policies to cluster builder
        initPolicies(builder, config);

        // Build cluster and session
        cluster = builder.build();
        session = cluster.connect();
        metadata = cluster.getMetadata();

        consistency = getConsistency(config);
        initialized = true;
    }

    protected void initPolicies(Cluster.Builder builder, JsonObject config) {

        JsonObject policyConfig = config.getObject("policies");

        if (policyConfig == null) {
            return;
        }

        JsonObject loadBalancing = policyConfig.getObject("load_balancing");
        if (loadBalancing != null) {
            String name = loadBalancing.getString("name");

            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("A load balancing policy must have a class name field");

            } else if ("DCAwareRoundRobinPolicy".equalsIgnoreCase(name)
                    || "com.datastax.driver.core.policies.DCAwareRoundRobinPolicy".equalsIgnoreCase(name)) {

                String localDc = loadBalancing.getString("local_dc");
                int usedHostsPerRemoteDc = loadBalancing.getInteger("used_hosts_per_remote_dc", 0);

                if (localDc == null || localDc.isEmpty()) {
                    throw new IllegalArgumentException("A DCAwareRoundRobinPolicy requires a local_dc in configuration.");
                }

                builder.withLoadBalancingPolicy(new DCAwareRoundRobinPolicy(localDc, usedHostsPerRemoteDc));

            } else {

                Class<?> clazz;
                try {
                    clazz = Thread.currentThread().getContextClassLoader().loadClass(name);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                if (LoadBalancingPolicy.class.isAssignableFrom(clazz)) {
                    try {
                        builder.withLoadBalancingPolicy((LoadBalancingPolicy) clazz.newInstance());
                    } catch (IllegalAccessException | InstantiationException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    throw new IllegalArgumentException("Class " + name + " does not implement LoadBalancingPolicy");
                }

            }
        }

    }

    protected ConsistencyLevel getConsistency(JsonObject config) {
        String consistency = config.getString(CONFIG_CONSISTENCY_LEVEL);

        if (consistency == null || consistency.isEmpty()) {
            return null;
        }

        if (consistency.equalsIgnoreCase(CONSISTENCY_ANY)) {
            return ConsistencyLevel.ANY;
        }
        if (consistency.equalsIgnoreCase(CONSISTENCY_ONE)) {
            return ConsistencyLevel.ONE;
        }
        if (consistency.equalsIgnoreCase(CONSISTENCY_TWO)) {
            return ConsistencyLevel.TWO;
        }
        if (consistency.equalsIgnoreCase(CONSISTENCY_THREE)) {
            return ConsistencyLevel.THREE;
        }
        if (consistency.equalsIgnoreCase(CONSISTENCY_QUORUM)) {
            return ConsistencyLevel.QUORUM;
        }
        if (consistency.equalsIgnoreCase(CONSISTENCY_ALL)) {
            return ConsistencyLevel.ALL;
        }
        if (consistency.equalsIgnoreCase(CONSISTENCY_LOCAL_QUORUM)) {
            return ConsistencyLevel.LOCAL_QUORUM;
        }
        if (consistency.equalsIgnoreCase(CONSISTENCY_EACH_QUORUM)) {
            return ConsistencyLevel.EACH_QUORUM;
        }

        throw new IllegalArgumentException("'" + consistency + "' is not a valid consistency level.");
    }

    @Override
    public void executeAsync(Statement statement, FutureCallback<ResultSet> callback) {
        checkInitialized();
        if (consistency != null && statement.getConsistencyLevel() == null) {
            statement.setConsistencyLevel(consistency);
        }
        final ResultSetFuture future = session.executeAsync(statement);
        Futures.addCallback(future, callback);
    }

    @Override
    public void executeAsync(String query, FutureCallback<ResultSet> callback) {
        executeAsync(new SimpleStatement(query), callback);
    }

    @Override
    public ResultSet execute(Statement statement) {
        checkInitialized();
        if (consistency != null && statement.getConsistencyLevel() == null) {
            statement.setConsistencyLevel(consistency);
        }
        return session.execute(statement);
    }

    @Override
    public ResultSet execute(String query) {
        return execute(new SimpleStatement(query));
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
