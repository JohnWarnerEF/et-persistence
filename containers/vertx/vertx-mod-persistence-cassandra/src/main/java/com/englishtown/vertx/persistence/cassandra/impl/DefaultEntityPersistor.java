package com.englishtown.vertx.persistence.cassandra.impl;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.TableMetadata;
import com.englishtown.cassandra.EntityRef;
import com.englishtown.vertx.persistence.cassandra.*;
import com.google.common.util.concurrent.FutureCallback;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link com.englishtown.vertx.persistence.cassandra.EntityPersistor}
 */
public class DefaultEntityPersistor implements EntityPersistor {

    private final CassandraSession session;
    private final SelectBuilder selectBuilder;
    private final SchemaBuilder schemaBuilder;
    private final InsertBuilder insertBuilder;

    @Inject
    public DefaultEntityPersistor(CassandraSession session, SelectBuilder selectBuilder, SchemaBuilder schemaBuilder, InsertBuilder insertBuilder) {
        this.session = session;
        this.selectBuilder = selectBuilder;
        this.schemaBuilder = schemaBuilder;
        this.insertBuilder = insertBuilder;
    }

    @Override
    public void load(final EntityRef ref, LoadResults results, final LoadCallback callback) {

        // Create dynamic cql select statement
        Statement select = selectBuilder.build(ref);

        // Increment total count
        results.totalCount++;

        // Execute select statement
        session.executeAsync(select, new FutureCallback<ResultSet>() {
            @Override
            public void onSuccess(ResultSet rs) {
                callback.onSuccess(ref, rs.one());
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(ref, t);
            }
        });

    }

    @Override
    public void store(JsonArray entities, JsonObject schemas, FutureCallback<ResultSet> callback) {

        BatchStatement batch = new BatchStatement();
        Map<String, TableMetadata> buildCache = new HashMap<>();

        for (int i = 0; i < entities.size(); i++) {
            JsonObject entity = entities.get(i);

            TableMetadata table = schemaBuilder.build(entity, schemas, buildCache);
            Statement statement = insertBuilder.build(entity, table);
            batch.add(statement);
        }

        session.executeAsync(batch, callback);

    }
}
