package com.englishtown.vertx;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.englishtown.cassandra.EntityRef;
import com.englishtown.vertx.persistence.cassandra.*;
import com.google.common.util.concurrent.FutureCallback;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * An EventBus module to save persistence entities in Cassandra
 */
public class CassandraPersistence extends BusModBase implements Handler<Message<JsonObject>> {

    public static final String DEFAULT_ADDRESS = "et.persistence.cassandra";

    private final CassandraSession session;
    private final EntityPersistor persistor;
    private final RowReader rowReader;

    @Inject
    public CassandraPersistence(
            CassandraSession session,
            EntityPersistor persistor,
            RowReader rowReader) {

        this.session = session;
        this.persistor = persistor;
        this.rowReader = rowReader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        super.start();

        JsonObject config = container.config();
        session.init(config);

        // Main Message<JsonObject> handler that inspects an "action" field
        String address = config.getString("address", DEFAULT_ADDRESS);
        eb.registerHandler(address, this);
        logger.info("CassandraPersistence verticle listening on event bus address: " + address);

    }

    @Override
    public void stop() {
        try {
            session.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handle(Message<JsonObject> message) {

        String action = getMandatoryString("action", message);
        if (action == null) {
            return;
        }

        try {
            switch (action) {
                case "store":
                    store(message);
                    break;
                case "load":
                    load(message);
                    break;
                default:
                    sendError(message, "action " + action + " is not supported");
            }

        } catch (Exception e) {
            sendError(message, action + " error: " + e.getMessage(), e);
        }
    }

    public void store(final Message<JsonObject> message) {

        JsonObject body = message.body();
        final JsonArray entities = body.getArray("entities");
        JsonObject schemas = body.getObject("schemas");

        if (entities == null || entities.size() == 0) {
            throw new IllegalArgumentException("An entities json array must be provided");
        }

        persistor.store(entities, schemas, new FutureCallback<ResultSet>() {
            @Override
            public void onSuccess(ResultSet result) {
                sendOK(message);
            }

            @Override
            public void onFailure(Throwable t) {
                sendError(message, "Error storing datamaps: " + t.getMessage(), new RuntimeException(t));
            }
        });

    }

    public void load(final Message<JsonObject> message) {

        final JsonObject body = message.body();

        final List<EntityRef> keys = getKeys(body);
        if (keys == null) {
            return;
        }

        final LoadResults results = new LoadResults();

        for (final EntityRef ref : keys) {
            persistor.load(ref, results, new LoadCallback() {
                @Override
                public void onSuccess(EntityRef ref, Row row) {
                    handleResults(ref, row, results, message);
                }

                @Override
                public void onFailure(EntityRef ref, Throwable t) {
                    logger.error("Load error", t);
                    handleResults(ref, null, results, message);
                }
            });
        }

    }

    public List<EntityRef> getKeys(JsonObject body) {

        final JsonArray keys = body.getArray("keys");

        if (keys == null || keys.size() == 0) {
            throw new IllegalArgumentException("keys json array is required");
        }

        List<EntityRef> list = new ArrayList<>(keys.size());

        for (int i = 0; i < keys.size(); i++) {
            JsonObject json = keys.get(i);

            String idStr = json.getString("id");
            if (idStr == null) {
                throw new IllegalArgumentException("id is a required field");
            }

            UUID id = UUID.fromString(idStr);

            String keyspace = json.getString("schema");
            if (keyspace == null || keyspace.isEmpty()) {
                throw new IllegalArgumentException("schema is a required key field");
            }

            String table = json.getString("table");
            if (table == null || table.isEmpty()) {
                throw new IllegalArgumentException("table is a required key field");
            }

            list.add(new EntityRef(id, table, keyspace));
        }

        return list;
    }

    public void handleResults(EntityRef ref, Row row, final LoadResults results, final Message<JsonObject> message) {

        if (row == null) {
            results.missing.addObject(new JsonObject()
                    .putString("id", ref.getId().toString())
                    .putString("table", ref.getTable())
                    .putString("schema", ref.getKeyspace()));

        } else {
            rowReader.read(row, results, new LoadCallback() {
                @Override
                public void onSuccess(EntityRef ref, Row row) {
                    handleResults(ref, row, results, message);
                }

                @Override
                public void onFailure(EntityRef ref, Throwable t) {
                    logger.error("Load error", t);
                    handleResults(ref, null, results, message);
                }
            });

        }

        results.completedCount++;

        if (results.completedCount == results.totalCount) {
            sendOK(message, new JsonObject()
                    .putArray("entities", results.entities)
                    .putArray("missing", results.missing));
        }
    }

}
