package com.englishtown.vertx.persistence.cassandra.impl;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.TableMetadata;
import com.englishtown.cassandra.EntityRef;
import com.englishtown.vertx.persistence.cassandra.CassandraSession;
import com.englishtown.vertx.persistence.cassandra.RowReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DefaultSchemaBuilder}
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultSchemaBuilderTest {

    DefaultSchemaBuilder builder;

    @Mock
    CassandraSession session;

    @Before
    public void setUp() {
        builder = new DefaultSchemaBuilder(session);
    }

    @Test
    public void testBuild() throws Exception {

        JsonObject entity = new JsonObject();
        JsonObject schemas = new JsonObject();
        Map<String, TableMetadata> buildCache = new HashMap<>();

        Metadata metadata = mock(Metadata.class);
        KeyspaceMetadata keyspaceMetadata = mock(KeyspaceMetadata.class);
        TableMetadata tableMetadata = mock(TableMetadata.class);
        when(session.getMetadata()).thenReturn(metadata);
        when(metadata.getKeyspace(anyString())).thenReturn(null).thenReturn(keyspaceMetadata);
        when(keyspaceMetadata.getTable(anyString())).thenReturn(null).thenReturn(null).thenReturn(tableMetadata);

        try {
            builder.build(entity, schemas, buildCache);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
            assertEquals("An entity must have a schema (Cassandra keyspace) specified", e.getMessage());
        }

        String keyspaceName = "keyspaceA";
        entity.putString("schema", keyspaceName);

        try {
            builder.build(entity, schemas, buildCache);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
            assertEquals("An entity must have a table specified", e.getMessage());
        }

        verify(session).execute(eq("CREATE KEYSPACE " + keyspaceName.toLowerCase()
                + " WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 3};"));

        String tableName = "tableA";
        entity.putString("table", tableName);

        try {
            builder.build(entity, schemas, buildCache);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
            assertEquals("An entity must contain sys_fields", e.getMessage());
        }

        JsonObject sysFields = new JsonObject();
        entity.putObject("sys_fields", sysFields);

        try {
            builder.build(entity, schemas, buildCache);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
            assertEquals("An entity must contain fields", e.getMessage());
        }

        JsonObject fields = new JsonObject();
        entity.putObject("fields", fields);

        try {
            builder.build(entity, schemas, buildCache);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
            assertEquals("An entity sys_fields must contain a type field", e.getMessage());
        }

        String type = "com.englishtown.test.Entity";
        sysFields.putString("type", type);

        try {
            builder.build(entity, schemas, buildCache);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
            assertEquals("Table " + keyspaceName.toLowerCase() + "." + tableName.toLowerCase()
                    + " does not exist.  Create the table or provide a schema.", e.getMessage());
        }

        JsonObject schema = new JsonObject()
                .putArray("sys_fields", new JsonArray())
                .putArray("fields", new JsonArray());

        schemas.putObject("com.englishtown.test.Entity", schema);

        assertEquals(0, buildCache.size());
        TableMetadata result;

        result = builder.build(entity, schemas, buildCache);
        assertNotNull(result);
        assertEquals(1, buildCache.size());
        verify(session).execute(eq("CREATE TABLE " + keyspaceName.toLowerCase()
                + "." + tableName.toLowerCase() + " ( PRIMARY KEY (id));"));
        verify(session, times(2)).execute(anyString());
        buildCache.clear();

        result = builder.build(entity, schemas, buildCache);
        assertNotNull(result);
        assertEquals(1, buildCache.size());
        verify(session, times(2)).execute(anyString());

    }

    @Test
    public void testCreateTable() throws Exception {

        String keyspace = "keyspaceA";
        String table = "tableA";
        JsonObject schema = new JsonObject();

        try {
            builder.createTable(keyspace, table, schema);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
            assertEquals("The schema is missing a fields json array: " + schema.encode(), e.getMessage());
        }

        schema.putArray("fields", new JsonArray());

        try {
            builder.createTable(keyspace, table, schema);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
            assertEquals("The schema is missing a sys_fields json array: " + schema.encode(), e.getMessage());
        }

        schema.putArray("sys_fields", new JsonArray());
        schema.putString("pk", "custom_id");

        Metadata metadata = mock(Metadata.class);
        KeyspaceMetadata keyspaceMetadata = mock(KeyspaceMetadata.class);
        TableMetadata tableMetadata = mock(TableMetadata.class);
        when(session.getMetadata()).thenReturn(metadata);
        when(metadata.getKeyspace(anyString())).thenReturn(keyspaceMetadata);
        when(keyspaceMetadata.getTable(anyString())).thenReturn(tableMetadata);

        TableMetadata result = builder.createTable(keyspace, table, schema);
        assertEquals(tableMetadata, result);
        verify(session).execute(eq("CREATE TABLE " + keyspace
                + "." + table + " ( PRIMARY KEY (custom_id));"));

    }

    @Test
    public void testAddColumns() throws Exception {

        boolean isSys = false;
        StringBuilder sb = new StringBuilder();
        JsonArray fields = new JsonArray().addObject(
                new JsonObject()
                        .putString("name", "prop1")
                        .putString("type", "java.lang.String")
        );

        builder.addColumns(fields, isSys, sb);

    }

    @Test
    public void testVerifyTable() throws Exception {

        Metadata metadata = mock(Metadata.class);
        TableMetadata tableMetadata = mock(TableMetadata.class);
        KeyspaceMetadata keyspaceMetadata = mock(KeyspaceMetadata.class);
        String tableName = "tableA";
        String keyspaceName = "keyspaceA";

        when(session.getMetadata()).thenReturn(metadata);
        when(metadata.getKeyspace(anyString())).thenReturn(keyspaceMetadata);
        when(keyspaceMetadata.getTable(anyString())).thenReturn(tableMetadata);
        when(tableMetadata.getName()).thenReturn(tableName);
        when(tableMetadata.getKeyspace()).thenReturn(keyspaceMetadata);
        when(keyspaceMetadata.getName()).thenReturn(keyspaceName);

        JsonObject schema = new JsonObject()
                .putArray("fields", new JsonArray()
                        .addObject(new JsonObject()
                                .putString("name", "prop1")
                                .putString("type", "java.lang.String")
                        ))
                .putArray("sys_fields", new JsonArray()
                        .addObject(new JsonObject()
                                .putString("name", "prop1")
                                .putString("type", "java.lang.String")
                        ));

        builder.verifyTable(tableMetadata, schema);

        verify(session).execute(eq("ALTER TABLE " + keyspaceName + "." + tableName + " ADD sys_prop1 text;"));
        verify(session).execute(eq("ALTER TABLE " + keyspaceName + "." + tableName + " ADD prop1 text;"));

    }

    @Test
    public void testGetCqlColumnDef_Set() throws Exception {

        StringBuilder sb = new StringBuilder();
        JsonObject field = new JsonObject().putString("type", Set.class.getName());

        try {
            builder.getCqlColumnDef("prop1", field, sb);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
            assertEquals("A collection must provide 1 typeArgs", e.getMessage());
        }

        sb = new StringBuilder();
        field.putArray("typeArgs", new JsonArray().addString(String.class.getName()));

        builder.getCqlColumnDef("prop1", field, sb);
        assertEquals("prop1 set<text>", sb.toString());

    }

    @Test
    public void testGetCqlColumnDef_Map() throws Exception {

        StringBuilder sb = new StringBuilder();
        JsonObject field = new JsonObject().putString("type", Map.class.getName());

        try {
            builder.getCqlColumnDef("prop1", field, sb);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
            assertEquals("A map must provide 2 typeArgs", e.getMessage());
        }

        sb = new StringBuilder();
        field.putArray("typeArgs", new JsonArray()
                .addString(String.class.getName())
                .addString(Integer.class.getName()));


        builder.getCqlColumnDef("prop1", field, sb);
        assertEquals("prop1 map<text, int>", sb.toString());

    }

    @Test
    public void testGetFieldName() throws Exception {

        JsonObject field = new JsonObject().putString("name", "update_date");
        boolean isSys = false;
        String name;

        name = builder.getFieldName(field, isSys);
        assertEquals("update_date", name);

        isSys = true;
        name = builder.getFieldName(field, isSys);
        assertEquals("sys_update_date", name);

        field.putString("name", "id");
        name = builder.getFieldName(field, isSys);
        assertEquals("id", name);

    }

    @Test
    public void testGetCqlType_text() throws Exception {

        String expected = "text";
        String result;

        result = builder.getCqlType(String.class.getName());
        assertEquals(expected, result);

        result = builder.getCqlType("String");
        assertEquals(expected, result);

    }

    @Test
    public void testGetCqlType_bigint() throws Exception {

        String expected = "bigint";
        String result;

        result = builder.getCqlType(Long.class.getName());
        assertEquals(expected, result);

        result = builder.getCqlType("Long");
        assertEquals(expected, result);

        result = builder.getCqlType("long");
        assertEquals(expected, result);

    }

    @Test
    public void testGetCqlType_blob() throws Exception {

        String expected = "blob";
        String result;

        result = builder.getCqlType(ByteBuffer.class.getName());
        assertEquals(expected, result);

    }

    @Test
    public void testGetCqlType_boolean() throws Exception {

        String expected = "boolean";
        String result;

        result = builder.getCqlType(Boolean.class.getName());
        assertEquals(expected, result);

        result = builder.getCqlType("Boolean");
        assertEquals(expected, result);

        result = builder.getCqlType("boolean");
        assertEquals(expected, result);

    }

    @Test
    public void testGetCqlType_decimal() throws Exception {

        String expected = "decimal";
        String result;

        result = builder.getCqlType(BigDecimal.class.getName());
        assertEquals(expected, result);

    }

    @Test
    public void testGetCqlType_double() throws Exception {

        String expected = "double";
        String result;

        result = builder.getCqlType(Double.class.getName());
        assertEquals(expected, result);

        result = builder.getCqlType("Double");
        assertEquals(expected, result);

        result = builder.getCqlType("double");
        assertEquals(expected, result);

    }

    @Test
    public void testGetCqlType_float() throws Exception {

        String expected = "float";
        String result;

        result = builder.getCqlType(Float.class.getName());
        assertEquals(expected, result);

        result = builder.getCqlType("Float");
        assertEquals(expected, result);

        result = builder.getCqlType("float");
        assertEquals(expected, result);

    }

    @Test
    public void testGetCqlType_inet() throws Exception {

        String expected = "inet";
        String result;

        result = builder.getCqlType(InetAddress.class.getName());
        assertEquals(expected, result);

    }

    @Test
    public void testGetCqlType_int() throws Exception {

        String expected = "int";
        String result;

        result = builder.getCqlType(Integer.class.getName());
        assertEquals(expected, result);

        result = builder.getCqlType("Integer");
        assertEquals(expected, result);

        result = builder.getCqlType("int");
        assertEquals(expected, result);

    }

    @Test
    public void testGetCqlType_timestamp() throws Exception {

        String expected = "timestamp";
        String result;

        result = builder.getCqlType(Date.class.getName());
        assertEquals(expected, result);

    }

    @Test
    public void testGetCqlType_uuid() throws Exception {

        String expected = "uuid";
        String result;

        result = builder.getCqlType(UUID.class.getName());
        assertEquals(expected, result);

    }

    @Test
    public void testGetCqlType_varint() throws Exception {

        String expected = "varint";
        String result;

        result = builder.getCqlType(BigInteger.class.getName());
        assertEquals(expected, result);

    }

    @Test
    public void testGetCqlType_set() throws Exception {

        String expected = "set";
        String result;

        result = builder.getCqlType(Set.class.getName());
        assertEquals(expected, result);

    }

    @Test
    public void testGetCqlType_list() throws Exception {

        String expected = "list";
        String result;

        result = builder.getCqlType(List.class.getName());
        assertEquals(expected, result);

    }

    @Test
    public void testGetCqlType_map() throws Exception {

        String expected = "map";
        String result;

        result = builder.getCqlType(Map.class.getName());
        assertEquals(expected, result);

    }

    @Test
    public void testGetCqlType_EntityRefType() throws Exception {

        String expected = "'" + RowReader.ENTITY_REF_TYPE_CLASS_NAME + "'";
        String result;

        result = builder.getCqlType(EntityRef.class.getName());
        assertEquals(expected, result);

        result = builder.getCqlType("EntityRef");
        assertEquals(expected, result);

    }

    @Test
    public void testGetCqlType_unknown() throws Exception {

        try {
            builder.getCqlType(DefaultSchemaBuilder.class.getName());
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }

    }

}
