package com.englishtown.vertx.persistence.impl;

import com.englishtown.persistence.*;
import com.englishtown.persistence.acl.AccessControlList;
import com.englishtown.promises.Resolver;
import com.englishtown.promises.Value;
import com.englishtown.vertx.persistence.MessageReader;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.UUID;

/**
 * Default implementation of {@link MessageReader}
 */
public class DefaultMessageReader implements MessageReader {

    private final PersistentMapFactory persistentMapFactory;
    private final Provider<StoreResult> storeResultProvider;
    private final Provider<LoadResult> loadResultProvider;

    @Inject
    public DefaultMessageReader(PersistentMapFactory persistentMapFactory, Provider<StoreResult> storeResultProvider, Provider<LoadResult> loadResultProvider) {
        this.persistentMapFactory = persistentMapFactory;
        this.storeResultProvider = storeResultProvider;
        this.loadResultProvider = loadResultProvider;
    }

    /**
     * Read the reply message from a load operation
     *
     * @param reply
     * @param keys
     * @param resolver
     */
    @Override
    public void readLoadReply(Message<JsonObject> reply, List<EntityKey> keys, Resolver<LoadResult, Void> resolver) {

        LoadResult results = loadResultProvider.get();

        try {
            JsonObject body = reply.body();
            String status = body.getString("status");

            if (!"ok".equals(status)) {
                String message = body.getString("message", "Unknown error");
                throw new IllegalArgumentException(message);
            }

            JsonArray entities = body.getArray("entities");

            if (entities == null) {
                throw new IllegalArgumentException("No entities json array");
            }

            for (int i = 0; i < entities.size(); i++) {

                JsonObject entity = entities.get(i);
                JsonObject fields = entity.getObject("fields");
                JsonObject sysFields = entity.getObject("sys_fields");

                // TODO: Handle when fields or sys_fields is missing
                // TODO: Handle EntityRefs

                PersistentMap pm = persistentMapFactory.create(fields.toMap());

                pm.getSysFields()
                        .setId(sysFields.getString("id"))
                        .setType(sysFields.getString("type"))
                        .setVersion(sysFields.getInteger("version", 1))
                        .setACL(new AccessControlList());

                JsonArray acl = sysFields.getArray("acl");
                if (acl != null) {
                    for (int j = 0; j < acl.size(); j++) {
                        String ac = acl.get(j);
                        pm.getSysFields().getACL().add(UUID.fromString(ac));
                    }
                }

                results.getSucceeded().add(pm);
            }

            // TODO: Add missing to results

            resolver.resolve(results);

        } catch (RuntimeException e) {
            resolver.reject(new Value<>(results, e));
        }

    }

    /**
     * Read the reply message from a store operation
     *
     * @param reply
     * @param entities
     * @param resolver
     */
    @Override
    public void readStoreReply(Message<JsonObject> reply, List<PersistentMap> entities, Resolver<StoreResult, Void> resolver) {

        final StoreResult result = storeResultProvider.get();

        try {

            JsonObject body = reply.body();
            String status = body.getString("status");

            if ("ok".equals(status)) {
                result.getSucceeded().addAll(entities);
                resolver.resolve(result);
            } else {
                String message = body.getString("message", "Unknown error");
                for (PersistentMap entity : entities) {
                    addFailed(result, entity, new RuntimeException(message));
                }
                resolver.reject(result);
            }

        } catch (RuntimeException e) {
            resolver.reject(new Value<>(result, e));
        }

    }

    protected void addFailed(PersistenceResult<PersistentMap, FailedPersistentMap> result, final PersistentMap persistentMap, final Throwable t) {

        result.getFailed().add(new FailedPersistentMap() {
            @Override
            public PersistentMap getPersistentMap() {
                return persistentMap;
            }

            @Override
            public Throwable getCause() {
                return t;
            }
        });

    }

}
