package com.englishtown.vertx.persistence.cassandra.impl;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.englishtown.cassandra.EntityRef;
import com.englishtown.cassandra.serializers.EntityRefSerializer;
import com.englishtown.vertx.persistence.cassandra.*;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Default implementation of {@link com.englishtown.vertx.persistence.cassandra.RowReader}
 */
public class DefaultRowReader implements RowReader {

    private final EntityRefSerializer serializer;
    private final EntityPersistor persistor;

    @Inject
    public DefaultRowReader(EntityRefSerializer serializer, EntityPersistor persistor) {
        this.serializer = serializer;
        this.persistor = persistor;
    }

    /**
     * Reads a Cassandra result set row and adds to the results
     *
     * @param row
     * @param results
     * @param callback
     */
    @Override
    public void read(Row row, LoadResults results, LoadCallback callback) {

        JsonObject sysFields = new JsonObject();
        JsonObject fields = new JsonObject();
        JsonObject entity = new JsonObject()
                .putObject(SchemaBuilder.JSON_FIELD_SYS_FIELDS, sysFields)
                .putObject(SchemaBuilder.JSON_FIELD_FIELDS, fields);
        int index = 0;

        for (ColumnDefinitions.Definition column : row.getColumnDefinitions()) {
            // Skip null values
            if (row.isNull(index)) {
                index++;
                continue;
            }

            readColumn(row, results, sysFields, fields, index, column, callback);
            index++;
        }

        results.entities.addObject(entity);

    }

    protected void readColumn(
            Row row,
            LoadResults results,
            JsonObject sysFields,
            JsonObject fields,
            int index,
            ColumnDefinitions.Definition column,
            LoadCallback callback) {

        if (isSysColumn(column)) {
            readSysColumn(row, sysFields, index, column);

        } else {

            switch (column.getType().getName()) {
                case ASCII:
                case TEXT:
                case VARCHAR:
                    fields.putString(column.getName(), row.getString(index));
                    break;
                case BIGINT:
                case COUNTER:
                    fields.putNumber(column.getName(), row.getLong(index));
                    break;
                case BLOB:
                    fields.putBinary(column.getName(), row.getBytes(index).array());
                    break;
                case BOOLEAN:
                    fields.putBoolean(column.getName(), row.getBool(index));
                    break;
                case DECIMAL:
                    fields.putNumber(column.getName(), row.getDecimal(index));
                    break;
                case DOUBLE:
                    fields.putNumber(column.getName(), row.getDouble(index));
                    break;
                case FLOAT:
                    fields.putNumber(column.getName(), row.getFloat(index));
                    break;
                case INET:
                    fields.putString(column.getName(), row.getInet(index).toString());
                    break;
                case INT:
                    fields.putNumber(column.getName(), row.getInt(index));
                    break;
                case TIMESTAMP:
                    fields.putNumber(column.getName(), row.getDate(index).getTime());
                    break;
                case UUID:
                case TIMEUUID:
                    fields.putString(column.getName(), row.getUUID(index).toString());
                    break;
                case VARINT:
                    fields.putNumber(column.getName(), row.getVarint(index));
                    break;
                case LIST:
                    DataType typeArg = column.getType().getTypeArguments().get(0);
                    JsonArray list;
                    if (isEntityRefColumn(typeArg)) {
                        List<EntityRef> entityRefs = serializer.deserializeList(row.getList(index, ByteBuffer.class));
                        list = readEntityRefs(entityRefs, results, callback);
                    } else {
                        list = new JsonArray(row.getList(index, Object.class));
                    }
                    fields.putArray(column.getName(), list);
                    break;
                case SET:
                    typeArg = column.getType().getTypeArguments().get(0);
                    JsonArray set;
                    if (isEntityRefColumn(typeArg)) {
                        Set<EntityRef> entityRefs = serializer.deserializeSet(row.getSet(index, ByteBuffer.class));
                        set = readEntityRefs(entityRefs, results, callback);
                    } else {
                        Set<Object> tmp = row.getSet(index, Object.class);
                        set = new JsonArray(tmp.toArray(new Object[tmp.size()]));
                    }
                    fields.putArray(column.getName(), set);
                    break;
                case MAP:
                    fields.putObject(column.getName(), new JsonObject(row.getMap(index, String.class, Object.class)));
                    break;
                case CUSTOM:
                    if (isEntityRefColumn(column.getType())) {
                        EntityRef ref = serializer.deserialize(row.getBytesUnsafe(index));
                        fields.putObject(column.getName(), readEntityRef(ref, results, callback));
                    } else {
                        fields.putBinary(column.getName(), row.getBytesUnsafe(index).array());
                    }
                    break;
                default:
                    fields.putBinary(column.getName(), row.getBytesUnsafe(index).array());
                    break;
            }
        }
    }

    protected void readSysColumn(Row row, JsonObject sysFields, int index, ColumnDefinitions.Definition column) {

        switch (column.getName()) {
            case "id":
                sysFields.putString(SchemaBuilder.JSON_FIELD_ID, row.getUUID(index).toString());
                break;
            case "sys_version":
                sysFields.putNumber(SchemaBuilder.JSON_FIELD_VERSION, row.getInt(index));
                break;
            case "sys_acl":
                Set<UUID> set = row.getSet(index, UUID.class);
                JsonArray acl = new JsonArray();
                for (UUID id : set) {
                    acl.addString(id.toString());
                }
                sysFields.putArray(SchemaBuilder.JSON_FIELD_ACL, acl);
                break;
            case "sys_type":
                sysFields.putString(SchemaBuilder.JSON_FIELD_TYPE, row.getString(index));
                break;
            case "sys_update_date":
                sysFields.putNumber("update_date", row.getDate(index).getTime());
                break;
        }

    }

    protected boolean isSysColumn(ColumnDefinitions.Definition column) {
        return column.getName().equals("id") || column.getName().startsWith("sys_");
    }

    protected boolean isEntityRefColumn(DataType dataType) {
        return dataType.getName() == DataType.Name.CUSTOM
                && dataType.getCustomTypeClassName().equals(ENTITY_REF_TYPE_CLASS_NAME);
    }

    protected JsonArray readEntityRefs(Collection<EntityRef> entityRefs, LoadResults results, LoadCallback callback) {
        JsonArray json = new JsonArray();

        for (EntityRef ref : entityRefs) {
            json.addObject(readEntityRef(ref, results, callback));
        }

        return json;
    }

    protected JsonObject readEntityRef(EntityRef ref, LoadResults results, LoadCallback callback) {

        persistor.load(ref, results, callback);

        return new JsonObject()
                .putString("id", ref.getId().toString())
                .putString("table", ref.getTable())
                .putString("schema", ref.getKeyspace()
                );
    }

}
