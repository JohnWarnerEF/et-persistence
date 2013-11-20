package com.englishtown.vertx.persistence.impl;

import com.englishtown.persistence.*;
import com.englishtown.persistence.acl.AccessControlList;
import com.englishtown.persistence.impl.DefaultEntityRefInfo;
import com.englishtown.promises.Resolver;
import com.englishtown.promises.Value;
import com.englishtown.vertx.persistence.MessageReader;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;

/**
 * Default implementation of {@link MessageReader}
 */
public class DefaultMessageReader implements MessageReader {

    private final LoadedPersistentMapFactory mapFactory;
    private final Provider<StoreResult> storeResultProvider;
    private final Provider<LoadResult> loadResultProvider;
    private final EntityMetadataService metadataService;

    protected static class LoadedEntity {
        public final LoadedPersistentMap map;
        public final EntityRefInfo ref;

        public LoadedEntity(LoadedPersistentMap map, EntityRefInfo ref) {
            this.map = map;
            this.ref = ref;
        }
    }

    @Inject
    public DefaultMessageReader(LoadedPersistentMapFactory mapFactory, Provider<StoreResult> storeResultProvider, Provider<LoadResult> loadResultProvider, EntityMetadataService metadataService) {
        this.mapFactory = mapFactory;
        this.storeResultProvider = storeResultProvider;
        this.loadResultProvider = loadResultProvider;
        this.metadataService = metadataService;
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

            List<EntityRefInfo> allEntityRefs = new ArrayList<>();
            List<LoadedEntity> allEntities = new ArrayList<>();

            for (int i = 0; i < entities.size(); i++) {
                LoadedEntity entity = readEntity(entities.<JsonObject>get(i), allEntityRefs);
                if (entity != null) {
                    allEntities.add(entity);
                    results.getSucceeded().add(entity.map);
                }
            }

            setRelatedEntityRefs(allEntities);
            readMissing(body.getArray("missing"), keys, results);

            resolver.resolve(results);

        } catch (RuntimeException e) {
            resolver.reject(new Value<>(results, e));
        }

    }

    protected void setRelatedEntityRefs(List<LoadedEntity> allEntities) {

        // Populate related entity refs
        for (LoadedEntity entity : allEntities) {
            // Individual entity refs
            for (EntityRefInfo ref : entity.map.getEntityRefs().values()) {
                for (LoadedEntity related : allEntities) {
                    if (related.ref.equals(ref)) {
                        ref.setPersistentMap(related.map);
                        break;
                    }
                }
            }

            // Collection entity refs
            for (Collection<EntityRefInfo> refs : entity.map.getEntityRefCollections().values()) {
                for (EntityRefInfo ref : refs) {
                    for (LoadedEntity related : allEntities) {
                        if (related.ref.equals(ref)) {
                            ref.setPersistentMap(related.map);
                            break;
                        }
                    }
                }
            }
        }

    }

    protected LoadedEntity readEntity(JsonObject entity, List<EntityRefInfo> allEntityRefs) {

        JsonObject fields = entity.getObject("fields");
        JsonObject sysFields = entity.getObject("sys_fields");

        if (fields == null || sysFields == null) {
            throw new IllegalArgumentException("Entities must have a fields and sys_fields json object: "
                    + entity.encode());
        }

        Map<String, EntityRefInfo> entityRefs = new HashMap<>();
        Map<String, Collection<EntityRefInfo>> entityRefCollections = new HashMap<>();
        removeEntityRefs(fields, entityRefs, entityRefCollections, allEntityRefs);

        LoadedPersistentMap pm = mapFactory.create(fields.toMap(), entityRefs, entityRefCollections);

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

        EntityRefInfo ref = new DefaultEntityRefInfo(
                sysFields.getString("id"),
                entity.getString("table"),
                entity.getString("schema"));

        ref.setPersistentMap(pm);
        return new LoadedEntity(pm, ref);
    }

    protected void readMissing(JsonArray missing, List<EntityKey> keys, LoadResult result) {

        if (missing == null || missing.size() == 0) {
            return;
        }

        for (int i = 0; i < missing.size(); i++) {
            JsonObject json = missing.get(i);
            EntityRefInfo ref = createEntityRef(json);

            for (EntityKey key : keys) {
                if (key.getId().equals(ref.getId())) {
                    EntityMetadata metadata = metadataService.get(key.getEntityClass());

                    if (ref.getTable().equalsIgnoreCase(metadata.getTable())
                            && ref.getSchema().equalsIgnoreCase(metadata.getSchema())) {
                        result.getFailed().add(key);
                        break;
                    }
                }
            }
        }

    }

    protected void removeEntityRefs(
            JsonObject fields,
            Map<String, EntityRefInfo> entityRefs,
            Map<String, Collection<EntityRefInfo>> entityRefLists,
            List<EntityRefInfo> allEntityRefs) {

        Iterator<String> iterator = fields.getFieldNames().iterator();

        while (iterator.hasNext()) {
            String name = iterator.next();
            Object val = fields.getValue(name);

            if (val instanceof JsonObject) {
                final JsonObject json = (JsonObject) val;
                if ("EntityRef".equalsIgnoreCase(json.getString("type"))) {
                    EntityRefInfo ref = createEntityRef(json);
                    allEntityRefs.add(ref);
                    entityRefs.put(name, ref);
                    // Remove entity ref from fields
                    iterator.remove();
                }
            } else if (val instanceof JsonArray) {
                JsonArray json = (JsonArray) val;
                if (json.size() == 0) {
                    continue;
                }
                Object val1 = json.get(0);
                if (!(val1 instanceof JsonObject)) {
                    continue;
                }
                JsonObject json1 = (JsonObject) val1;
                if (!"EntityRef".equalsIgnoreCase(json1.getString("type"))) {
                    continue;
                }

                List<EntityRefInfo> list = new ArrayList<>();
                for (int i = 0; i < json.size(); i++) {
                    EntityRefInfo ref = createEntityRef(json.<JsonObject>get(i));
                    allEntityRefs.add(ref);
                    list.add(ref);
                }
                entityRefLists.put(name, list);
                // Remove entity ref array from fields
                iterator.remove();
            }
        }

    }

    private EntityRefInfo createEntityRef(JsonObject json) {

        final String id = json.getString("id");
        final String table = json.getString("table");
        final String schema = json.getString("schema");

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("EntityRef must have an id field: " + json.encode());
        }
        if (table == null || table.isEmpty()) {
            throw new IllegalArgumentException("EntityRef must have a table field: " + json.encode());
        }
        if (schema == null || schema.isEmpty()) {
            throw new IllegalArgumentException("EntityRef must have a schema field: " + json.encode());
        }

        return new DefaultEntityRefInfo(id, table, schema);
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
