package com.englishtown.vertx.persistence.cassandra.impl;

import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.englishtown.cassandra.EntityRef;
import com.englishtown.cassandra.serializers.EntityRefSerializer;
import com.englishtown.vertx.persistence.cassandra.InsertBuilder;
import com.englishtown.vertx.persistence.cassandra.RowReader;
import com.englishtown.vertx.persistence.cassandra.SchemaBuilder;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Default implementation of {@link InsertBuilder}
 */
public class DefaultInsertBuilder implements InsertBuilder {

    private final EntityRefSerializer serializer;

    @Inject
    public DefaultInsertBuilder(EntityRefSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * Builds a batch of insert statements for the provided datamaps
     *
     * @param entity
     * @param table
     * @return
     */
    @Override
    public Insert build(JsonObject entity, TableMetadata table) {

        JsonObject sysFields = entity.getObject(SchemaBuilder.JSON_FIELD_SYS_FIELDS);
        JsonObject fields = entity.getObject(SchemaBuilder.JSON_FIELD_FIELDS);

        if (sysFields == null) {
            throw new IllegalArgumentException(SchemaBuilder.JSON_FIELD_SYS_FIELDS + " is a required field of an entity");
        }

        String idStr = sysFields.getString(SchemaBuilder.JSON_FIELD_ID);
        if (idStr == null || idStr.isEmpty()) {
            throw new IllegalArgumentException(SchemaBuilder.JSON_FIELD_ID + " is a required field of an entity " + SchemaBuilder.JSON_FIELD_SYS_FIELDS);
        }
        UUID id = UUID.fromString(idStr);

        Insert insert = QueryBuilder
                .insertInto(table)
                .value("id", id);

        insertSysValues(insert, table, sysFields);

        if (fields != null) {
            for (String fieldName : fields.getFieldNames()) {
                insertValue(insert, fieldName, fields.getValue(fieldName), table);
            }
        }

        return insert;
    }

    public void insertSysValues(Insert insert, TableMetadata table, JsonObject sysFields) {

        if (table.getColumn("sys_update_date") != null) {
            insert.value("sys_update_date", new Date());
        }

        if (table.getColumn("sys_version") != null) {
            int version = sysFields.getInteger(SchemaBuilder.JSON_FIELD_VERSION, 1);
            insert.value("sys_version", version);
        }

        if (table.getColumn("sys_type") != null) {
            String type = sysFields.getString(SchemaBuilder.JSON_FIELD_TYPE);
            if (type == null || type.isEmpty()) {
                throw new IllegalArgumentException(SchemaBuilder.JSON_FIELD_TYPE + " is a required field of an entity sys_fields");
            }

            insert.value("sys_type", type);
        }

        if (table.getColumn("sys_acl") != null) {
            JsonArray aclArray = sysFields.getArray(SchemaBuilder.JSON_FIELD_ACL);
            if (aclArray != null) {
                Set<UUID> acl = new HashSet<>();
                for (int i = 0; i < aclArray.size(); i++) {
                    acl.add(UUID.fromString(aclArray.<String>get(i)));
                }
                insert.value("sys_acl", acl);
            }
        }

    }

    public void insertValue(Insert insert, String fieldName, Object value, TableMetadata table) {

        fieldName = fieldName.toLowerCase();
        ColumnMetadata column = table.getColumn(fieldName);

        if (column == null) {
            throw new IllegalArgumentException("Column " + fieldName + " does not exist for table " + table.getName());
        }

        if (value instanceof JsonObject) {
            insertValue(insert, column, (JsonObject) value);
        } else if (value instanceof JsonArray) {
            insertValue(insert, column, (JsonArray) value);
        } else {
            insert.value(fieldName, value);
        }

    }

    public void insertValue(Insert insert, ColumnMetadata column, JsonObject json) {

        // Only custom type supported is entity ref
        if (column.getType().getName() == DataType.Name.CUSTOM) {
            if (column.getType().getCustomTypeClassName().equals(RowReader.ENTITY_REF_TYPE_CLASS_NAME)) {
                insert.value(column.getName(), getEntityRef(json));

            } else {
                throw new IllegalArgumentException("Unsupported custom type " + column.getType().getCustomTypeClassName());
            }
        } else {

            if (column.getType().getName() == DataType.Name.MAP) {
                insert.value(column.getName(), json.toMap());

            } else {
                throw new IllegalArgumentException("Column " + column.getName() + " is not a map");
            }

        }

    }

    public void insertValue(Insert insert, ColumnMetadata column, JsonArray json) {

        Collection<Object> collection;

        switch (column.getType().getName()) {
            case LIST:
                collection = new ArrayList<>();
                break;
            case SET:
                collection = new TreeSet<>();
                break;
            default:
                throw new IllegalArgumentException("Column " + column.getName() + " is not a list or set");
        }

        DataType typeArg = column.getType().getTypeArguments().get(0);

        if (typeArg.getName() == DataType.Name.CUSTOM) {
            if (typeArg.getCustomTypeClassName().equals(RowReader.ENTITY_REF_TYPE_CLASS_NAME)) {
                for (int i = 0; i < json.size(); i++) {
                    JsonObject obj = json.get(i);
                    collection.add(getEntityRef(obj));
                }
            } else {
                throw new IllegalArgumentException("Unsupported custom type " + typeArg.getCustomTypeClassName());
            }
        } else {
            for (int i = 0; i < json.size(); i++) {
                collection.add(json.get(i));
            }
        }

        insert.value(column.getName(), collection);

    }

    public ByteBuffer getEntityRef(JsonObject json) {

        String idStr = json.getString("id");
        String table = json.getString("table");
        String keyspace = json.getString("schema");

        if (idStr == null || idStr.isEmpty()) {
            throw new IllegalArgumentException("An entity ref requires an id field");
        }

        UUID id = UUID.fromString(idStr);

        if (table == null || table.isEmpty()) {
            throw new IllegalArgumentException("An entity ref requires a table field");
        }

        if (keyspace == null || keyspace.isEmpty()) {
            throw new IllegalArgumentException("An entity ref requires a schema (Cassandra keyspace) field");
        }

        EntityRef ref = new EntityRef(id, table.toLowerCase(), keyspace.toLowerCase());
        return serializer.serialize(ref);
    }

}
