package com.englishtown.vertx.persistence.impl;

import com.englishtown.persistence.*;
import com.englishtown.vertx.persistence.MessageBuilder;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;

import javax.inject.Inject;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Default implementation of {@link MessageBuilder}
 */
public class DefaultMessageBuilder implements MessageBuilder {

    private final EntityMetadataService metadataService;
    private final IDGenerator idGenerator;

    @Inject
    public DefaultMessageBuilder(EntityMetadataService metadataService, IDGenerator idGenerator) {
        this.metadataService = metadataService;
        this.idGenerator = idGenerator;
    }

    /**
     * Builds a json load message for the provided entity keys
     *
     * @param keys the entities to load
     * @return the event bus load message
     */
    @Override
    public JsonObject buildLoadMessage(List<EntityKey> keys) {
        JsonArray keyArray = new JsonArray();

        if (keys != null && keys.size() > 0) {
            for (EntityKey key : keys) {
                EntityMetadata metadata = metadataService.get(key.getEntityClass());
                keyArray.addObject(new JsonObject()
                        .putString("id", key.getId())
                        .putString("table", metadata.getTable())
                        .putString("schema", metadata.getSchema())
                );
            }
        }

        return new JsonObject()
                .putString("action", "load")
                .putArray("keys", keyArray);
    }

    /**
     * Builds a json store message for the provided entities
     *
     * @param entities
     * @return
     */
    @Override
    public JsonObject buildStoreMessage(List<PersistentMap> entities) {

        JsonArray entityArray = new JsonArray();

        JsonObject message = new JsonObject()
                .putString("action", "store")
                .putArray("entities", entityArray);

        Map<String, EntityMetadata> schemaMap = new HashMap<>();

        if (entities != null && entities.size() > 0) {
            Set<PersistentMap> completeSet = new HashSet<>();

            for (PersistentMap entity : entities) {
                traverseEntity(entity, entityArray, completeSet, schemaMap);
            }
        }

        message.putObject("schemas", buildSchemas(schemaMap));

        return message;

    }

    protected void traverseEntity(PersistentMap obj, JsonArray entities, Set<PersistentMap> completeSet, Map<String, EntityMetadata> schemaMap) {

        // Build message for entity and add entity refs to list of related data maps
        List<PersistentMap> related = new ArrayList<>();
        entities.addObject(buildEntity(obj, schemaMap, related));

        // Keep track of all entities to prevent trying to save twice
        completeSet.add(obj);

        // Traverse related entities
        if (!related.isEmpty()) {
            for (PersistentMap rel : related) {
                if (!completeSet.contains(rel)) {
                    traverseEntity(rel, entities, completeSet, schemaMap);
                }
            }
        }
    }

    private JsonObject buildEntity(PersistentMap entity, Map<String, EntityMetadata> schemaMap, List<PersistentMap> related) {

        ensureID(entity);

        EntityMetadata metadata = metadataService.get(entity.getClass());
        schemaMap.put(metadata.getType(), metadata);

        JsonObject fields = new JsonObject(entity.getMap());
        JsonObject sysFields = new JsonObject()
                .putString("id", entity.getSysFields().getId())
                .putString("type", metadata.getType())
                .putNumber("version", entity.getSysFields().getVersion());

        // Add ACLs
        if (entity.getSysFields().getACL() != null) {
            JsonArray acl = new JsonArray();
            sysFields.putArray("acl", acl);
            for (UUID ac : entity.getSysFields().getACL()) {
                acl.addString(ac.toString());
            }
        }

        // Add entity refs
        for (Map.Entry<String, EntityRefInfo> entry : metadata.getEntityRefs().entrySet()) {
            EntityRefInfo ref = entry.getValue();
            Object refObj = ref.getValue(entity);

            if (refObj != null) {
                fields.putValue(entry.getKey(), getEntityRef(refObj, related));
            }
        }

        return new JsonObject()
                .putString("schema", metadata.getSchema())
                .putString("table", metadata.getTable())
                .putObject("sys_fields", sysFields)
                .putObject("fields", fields);

    }

    private JsonObject buildSchemas(Map<String, EntityMetadata> schemaMap) {
        JsonObject schemas = new JsonObject();

        for (Map.Entry<String, EntityMetadata> entry : schemaMap.entrySet()) {
            String name = entry.getKey();
            EntityMetadata metadata = entry.getValue();

            JsonObject schema = new JsonObject();

            schema.putArray("sys_fields", new JsonArray()
                    .addObject(new JsonObject()
                            .putString("name", "id")
                            .putString("type", UUID.class.getName()))
                    .addObject(new JsonObject()
                            .putString("name", "version")
                            .putString("type", Integer.class.getName()))
                    .addObject(new JsonObject()
                            .putString("name", "acl")
                            .putString("type", Set.class.getName())
                            .putArray("typeArgs", new JsonArray().addString(UUID.class.getName())))
                    .addObject(new JsonObject()
                            .putString("name", "type")
                            .putString("type", String.class.getName()))
                    .addObject(new JsonObject()
                            .putString("name", "update_date")
                            .putString("type", Date.class.getName()))
            );

            JsonArray fields = new JsonArray();
            schema.putArray("fields", fields);

            for (Map.Entry<String, TypeInfo> field : metadata.getFields().entrySet()) {
                TypeInfo typeInfo = field.getValue();

                JsonObject type = new JsonObject()
                        .putString("name", field.getKey())
                        .putString("type", typeInfo.getRawType().getName());

                if (typeInfo.getTypeArguments() != null && typeInfo.getTypeArguments().length > 0) {
                    JsonArray typeArgs = new JsonArray();
                    for (Type t : typeInfo.getTypeArguments()) {
                        if (t instanceof Class<?>) {
                            Class<?> clazz = (Class<?>) t;
                            typeArgs.addString(clazz.getName());
                        } else {
                            throw new IllegalArgumentException("Unsupported type argument: " + t.toString());
                        }
                    }
                    type.putArray("typeArgs", typeArgs);
                }

                fields.addObject(type);
            }

            for (Map.Entry<String, EntityRefInfo> refEntry : metadata.getEntityRefs().entrySet()) {
                TypeInfo typeInfo = refEntry.getValue().getTypeInfo();

                JsonObject ref = new JsonObject()
                        .putString("name", refEntry.getKey());

                if (typeInfo.getTypeArguments().length == 0) {
                    ref.putString("type", "EntityRef");
                } else {
                    ref.putString("type", typeInfo.getRawType().getName())
                            .putArray("typeArgs", new JsonArray().addString("EntityRef"));
                }

                fields.addObject(ref);
            }

            schemas.putObject(name, schema);
        }

        return schemas;
    }

    protected JsonElement getEntityRef(Object refObj, List<PersistentMap> related) {

        if (refObj instanceof Collection) {
            Collection refs = (Collection) refObj;
            JsonArray array = new JsonArray();
            for (Object refObj2 : refs) {
                array.add(getEntityRef(refObj2, related));
            }
            return array;

        } else if (refObj instanceof PersistentMap) {
            PersistentMap refPersistentMap = (PersistentMap) refObj;
            ensureID(refPersistentMap);
            EntityMetadata refMetadata = metadataService.get(refPersistentMap.getClass());
            related.add(refPersistentMap);

            return new JsonObject()
                    .putString("type", "ref")
                    .putString("id", refPersistentMap.getSysFields().getId())
                    .putString("table", refMetadata.getTable())
                    .putString("schema", refMetadata.getSchema());

        } else {
            throw new IllegalArgumentException("EntityRef class " + refObj.getClass().getName() + " does not implement PersistentMap.");
        }

    }

    protected void ensureID(PersistentMap persistentMap) {
        String id = persistentMap.getSysFields().getId();
        if (id == null || id.isEmpty()) {
            persistentMap.getSysFields().setId(idGenerator.createID());
        }
    }

}