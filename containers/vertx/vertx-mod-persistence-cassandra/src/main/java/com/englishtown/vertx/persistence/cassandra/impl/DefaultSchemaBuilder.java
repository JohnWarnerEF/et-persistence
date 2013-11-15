package com.englishtown.vertx.persistence.cassandra.impl;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.TableMetadata;
import com.englishtown.vertx.persistence.cassandra.CassandraSession;
import com.englishtown.vertx.persistence.cassandra.SchemaBuilder;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link SchemaBuilder}
 */
public class DefaultSchemaBuilder implements SchemaBuilder {

    private final CassandraSession session;

    @Inject
    public DefaultSchemaBuilder(CassandraSession session) {
        this.session = session;
    }

    /**
     * Ensures a keyspace and table schema exists.  Creating or updating as needed.
     *
     * @param entity
     * @param schemas
     * @param buildCache
     * @return the up to date table metadata
     */
    @Override
    public TableMetadata build(
            JsonObject entity,
            JsonObject schemas,
            Map<String, TableMetadata> buildCache) {

        String keyspaceName = entity.getString("schema");
        if (keyspaceName == null || keyspaceName.isEmpty()) {
            throw new IllegalArgumentException("An entity must have a schema (Cassandra keyspace) specified");
        }

        keyspaceName = keyspaceName.toLowerCase();
        KeyspaceMetadata keyspace = session.getMetadata().getKeyspace(keyspaceName);
        if (keyspace == null) {
            // Default to SimpleStrategy
            session.execute("CREATE KEYSPACE " + keyspaceName + " WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 3};");
            keyspace = session.getMetadata().getKeyspace(keyspaceName);
        }

        String tableName = entity.getString("table");
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("An entity must have a table specified");
        }

        JsonObject sysFields = entity.getObject(SchemaBuilder.JSON_FIELD_SYS_FIELDS);
        JsonObject fields = entity.getObject(SchemaBuilder.JSON_FIELD_FIELDS);

        if (sysFields == null) {
            throw new IllegalArgumentException("An entity must contain sys_fields");
        }
        if (fields == null) {
            throw new IllegalArgumentException("An entity must contain fields");
        }

        String type = sysFields.getString(JSON_FIELD_TYPE);
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("An entity sys_fields must contain a " + JSON_FIELD_TYPE + " field");
        }

        tableName = tableName.toLowerCase();
        TableMetadata table = keyspace.getTable(tableName);
        JsonObject schema = null;

        // Get schema if provided
        if (schemas != null) {
            schema = schemas.getObject(type);
        }

        if (table == null) {
            // Throw exception, if schema is missing we can't create no the fly
            if (schema == null) {
                throw new IllegalArgumentException("Table " + keyspaceName + "." + tableName
                        + " does not exist.  Create the table or provide a schema.");
            }
            table = createTable(keyspaceName, tableName, schema);
            buildCache.put(type, table);
        } else if (schema != null) {
            if (!buildCache.containsKey(type)) {
                table = verifyTable(table, schema);
                buildCache.put(type, table);
            }
        }

        return table;
    }

    public TableMetadata createTable(String keyspace, String table, JsonObject schema) {

        JsonArray fields = schema.getArray(JSON_FIELD_FIELDS);
        JsonArray sysFields = schema.getArray(JSON_FIELD_SYS_FIELDS);

        if (fields == null) {
            throw new IllegalArgumentException("The schema is missing a fields json array: " + schema.encode());
        }
        if (sysFields == null) {
            throw new IllegalArgumentException("The schema is missing a sys_fields json array: " + schema.encode());
        }

        String pk = schema.getString("pk");
        if (pk == null || pk.isEmpty()) {
            pk = "id";
        }

        // Create table
        StringBuilder sb = new StringBuilder("CREATE TABLE ")
                .append(keyspace).append(".").append(table)
                .append(" (");

        // Add columns
        addColumns(sysFields, true, sb);
        addColumns(fields, false, sb);

        // Add primary key
        sb.append(" PRIMARY KEY (").append(pk).append(")");

        // Close column specification
        sb.append(");");

        session.execute(sb.toString());
        return session.getMetadata().getKeyspace(keyspace).getTable(table);
    }

    public void addColumns(JsonArray fields, boolean isSys, StringBuilder sb) {

        if (fields == null || fields.size() == 0) {
            return;
        }

        for (int i = 0; i < fields.size(); i++) {
            JsonObject field = fields.get(i);
            String name = getFieldName(field, isSys);
            getCqlColumnDef(name, field, sb);
            sb.append(", ");
        }

    }

    public TableMetadata verifyTable(TableMetadata table, JsonObject schema) {

        List<String> missing = new ArrayList<>();
        addMissingColumns(schema.getArray(SchemaBuilder.JSON_FIELD_SYS_FIELDS), true, table, missing);
        addMissingColumns(schema.getArray(SchemaBuilder.JSON_FIELD_FIELDS), false, table, missing);

        if (missing.isEmpty()) {
            return table;
        }

        for (String col : missing) {
            session.execute("ALTER TABLE " + table.getKeyspace().getName() + "." + table.getName() + " ADD " + col + ";");
        }

        return session.getMetadata().getKeyspace(table.getKeyspace().getName()).getTable(table.getName());
    }

    public void addMissingColumns(JsonArray fields, boolean isSys, TableMetadata table, List<String> missing) {

        if (fields == null || fields.size() == 0) {
            return;
        }

        for (int i = 0; i < fields.size(); i++) {
            JsonObject field = fields.get(i);
            String name = getFieldName(field, isSys);

            if (table.getColumn(name) == null) {
                StringBuilder sb = new StringBuilder();
                getCqlColumnDef(name, field, sb);
                missing.add(sb.toString());
            }
        }

    }

    public void getCqlColumnDef(String name, JsonObject field, StringBuilder sb) {

        String type = field.getString(SchemaBuilder.JSON_FIELD_TYPE);

        String cqlType = getCqlType(type);
        sb.append(name).append(" ").append(cqlType);

        if (cqlType.equals("set") || cqlType.equals("list")) {
            JsonArray typeArgs = field.getArray("typeArgs");
            if (typeArgs == null || typeArgs.size() != 1) {
                throw new IllegalArgumentException("A collection must provide 1 typeArgs");
            }
            sb.append("<")
                    .append(getCqlType(typeArgs.<String>get(0)))
                    .append(">");

        } else if (cqlType.equals("map")) {
            JsonArray typeArgs = field.getArray("typeArgs");
            if (typeArgs == null || typeArgs.size() != 2) {
                throw new IllegalArgumentException("A map must provide 2 typeArgs");
            }
            sb.append("<")
                    .append(getCqlType(typeArgs.<String>get(0)))
                    .append(", ")
                    .append(getCqlType(typeArgs.<String>get(1)))
                    .append(">");
        }

    }

    public String getFieldName(JsonObject field, boolean isSys) {

        String name = field.getString("name").toLowerCase();

        if (isSys) {
            return ("id".equals(name) ? name : "sys_" + name);
        }

        return name;
    }

    public String getCqlType(String javaType) {

        switch (javaType) {

            case "java.lang.String":
            case "String":
                return "text";

            case "java.lang.Long":
            case "Long":
            case "long":
                return "bigint";

            case "java.nio.ByteBuffer":
                return "blob";

            case "java.lang.Boolean":
            case "Boolean":
            case "boolean":
                return "boolean";

            case "java.math.BigDecimal":
                return "decimal";

            case "java.lang.Double":
            case "Double":
            case "double":
                return "double";

            case "java.lang.Float":
            case "Float":
            case "float":
                return "float";

            case "java.net.InetAddress":
                return "inet";

            case "java.lang.Integer":
            case "Integer":
            case "int":
                return "int";

            case "java.util.Date":
                return "timestamp";

            case "java.util.UUID":
                return "uuid";

            case "java.math.BigInteger":
                return "varint";

            case "java.util.Set":
                return "set";

            case "java.util.List":
                return "list";

            case "java.util.Map":
                return "map";

            case "com.englishtown.cassandra.EntityRef":
            case "EntityRef":
                return "'com.englishtown.cassandra.db.marshal.EntityRefType'";

            default:
                throw new IllegalArgumentException("Java type " + javaType + " is not supported.");

        }

    }

}
