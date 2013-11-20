package com.englishtown.vertx.persistence.cassandra.impl;

import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.querybuilder.Insert;
import com.englishtown.cassandra.EntityRef;
import com.englishtown.cassandra.db.marshal.EntityRefType;
import com.englishtown.cassandra.serializers.EntityRefSerializer;
import com.englishtown.vertx.persistence.cassandra.SchemaBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.nio.ByteBuffer;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DefaultInsertBuilder}
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultInsertBuilderTest {

    DefaultInsertBuilder builder;

    @Mock
    Insert insert;
    @Mock
    EntityRefSerializer serializer;
    @Mock
    TableMetadata tableMetadata;
    @Mock
    KeyspaceMetadata keyspaceMetadata;
    @Mock
    ColumnMetadata columnMetadata;

    @Before
    public void setUp() {

        builder = new DefaultInsertBuilder(serializer);

        when(tableMetadata.getKeyspace()).thenReturn(keyspaceMetadata);

    }

    @Test
    public void testBuild_Illegal_Arg() throws Exception {

        JsonObject json = new JsonObject();

        try {
            builder.build(json, tableMetadata);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
            assertEquals(SchemaBuilder.JSON_FIELD_SYS_FIELDS + " is a required field of an entity", e.getMessage());
        }

        JsonObject sysFields = new JsonObject();
        json.putObject(SchemaBuilder.JSON_FIELD_SYS_FIELDS, sysFields);

        try {
            builder.build(json, tableMetadata);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
            assertEquals(SchemaBuilder.JSON_FIELD_ID + " is a required field of an entity " + SchemaBuilder.JSON_FIELD_SYS_FIELDS, e.getMessage());
        }

    }

    @Test
    public void testBuild() throws Exception {

        JsonObject json = new JsonObject()
                .putObject(SchemaBuilder.JSON_FIELD_SYS_FIELDS, new JsonObject()
                        .putString("id", UUID.randomUUID().toString())
                )
                .putObject(SchemaBuilder.JSON_FIELD_FIELDS, new JsonObject()
                        .putString("prop1", "value")
                );

        when(tableMetadata.getColumn(eq("prop1"))).thenReturn(columnMetadata);

        Insert insert = builder.build(json, tableMetadata);
        assertNotNull(insert);

    }

    @Test
    public void testInsertSysValues_None() throws Exception {

        JsonObject sysFields = new JsonObject();

        builder.insertSysValues(insert, tableMetadata, sysFields);
        verify(insert, never()).value(anyString(), anyObject());

    }

    @Test
    public void testInsertSysValues_Missing_Type() throws Exception {

        JsonObject sysFields = new JsonObject();

        ColumnMetadata columnMetadata = mock(ColumnMetadata.class);
        when(tableMetadata.getColumn(eq("sys_type"))).thenReturn(columnMetadata);

        try {
            builder.insertSysValues(insert, tableMetadata, sysFields);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
            assertEquals(SchemaBuilder.JSON_FIELD_TYPE + " is a required field of an entity sys_fields", e.getMessage());
        }

        sysFields.putString(SchemaBuilder.JSON_FIELD_TYPE, "type");
        builder.insertSysValues(insert, tableMetadata, sysFields);
        verify(insert).value(anyString(), anyObject());

    }

    @Test
    public void testInsertSysValues() throws Exception {

        JsonObject sysFields = new JsonObject()
                .putString(SchemaBuilder.JSON_FIELD_TYPE, "type")
                .putArray(SchemaBuilder.JSON_FIELD_ACL, new JsonArray()
                        .addString(UUID.randomUUID().toString())
                        .addString(UUID.randomUUID().toString())
                );

        when(tableMetadata.getColumn(eq("sys_update_date"))).thenReturn(columnMetadata);
        when(tableMetadata.getColumn(eq("sys_version"))).thenReturn(columnMetadata);
        when(tableMetadata.getColumn(eq("sys_type"))).thenReturn(columnMetadata);
        when(tableMetadata.getColumn(eq("sys_acl"))).thenReturn(columnMetadata);


        builder.insertSysValues(insert, tableMetadata, sysFields);
        verify(insert, times(4)).value(anyString(), anyObject());

    }

    @Test
    public void testInsertValue_Missing() throws Exception {

        String fieldName = "prop1";
        Object value = null;

        try {
            builder.insertValue(insert, fieldName, value, tableMetadata);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }

    }

    @Test
    public void testInsertValue_Object() throws Exception {

        String fieldName = "prop1";
        Object value = "";

        when(tableMetadata.getColumn(eq(fieldName))).thenReturn(columnMetadata);

        builder.insertValue(insert, fieldName, value, tableMetadata);
        verify(insert).value(eq(fieldName), anyString());

    }

    @Test
    public void testInsertValue_JsonObject() throws Exception {

        String fieldName = "prop1";
        Object value = new JsonObject().putString(fieldName, "value");

        when(columnMetadata.getName()).thenReturn(fieldName);
        DataType dataType = DataType.map(DataType.text(), DataType.text());
        when(tableMetadata.getColumn(eq(fieldName))).thenReturn(columnMetadata);
        when(columnMetadata.getType()).thenReturn(dataType);

        builder.insertValue(insert, fieldName, value, tableMetadata);
        verify(insert).value(eq(fieldName), anyMap());

    }

    @Test
    public void testInsertValue_JsonObject_Not_Map() throws Exception {

        String fieldName = "prop1";
        Object value = new JsonObject();

        when(columnMetadata.getName()).thenReturn(fieldName);
        DataType dataType = DataType.text();
        when(tableMetadata.getColumn(eq(fieldName))).thenReturn(columnMetadata);
        when(columnMetadata.getType()).thenReturn(dataType);

        try {
            builder.insertValue(insert, fieldName, value, tableMetadata);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
            Assert.assertEquals("Column " + fieldName + " is not a map", e.getMessage());
        }

    }

    @Test
    public void testInsertValue_EntityRef() throws Exception {

        String fieldName = "prop1";
        Object value = new JsonObject()
                .putString("id", UUID.randomUUID().toString())
                .putString("schema", "keyspace1")
                .putString("table", "table1");

        when(columnMetadata.getName()).thenReturn(fieldName);
        DataType dataType = DataType.custom(EntityRefType.class.getName());
        when(tableMetadata.getColumn(eq(fieldName))).thenReturn(columnMetadata);
        when(columnMetadata.getType()).thenReturn(dataType);

        builder.insertValue(insert, fieldName, value, tableMetadata);
        verify(insert).value(eq(fieldName), any(ByteBuffer.class));

    }

    @Test
    public void testInsertValue_JsonArray_List() throws Exception {

        String fieldName = "prop1";
        Object value = new JsonArray().addString("value");

        when(columnMetadata.getName()).thenReturn(fieldName);
        DataType dataType = DataType.list(DataType.text());
        when(tableMetadata.getColumn(eq(fieldName))).thenReturn(columnMetadata);
        when(columnMetadata.getType()).thenReturn(dataType);

        builder.insertValue(insert, fieldName, value, tableMetadata);
        verify(insert).value(eq(fieldName), anyList());

    }

    @Test
    public void testInsertValue_EntityRef_List() throws Exception {

        String fieldName = "prop1";
        Object value = new JsonArray().addObject(new JsonObject()
                .putString("id", UUID.randomUUID().toString())
                .putString("schema", "keyspace1")
                .putString("table", "table1")
        );

        when(columnMetadata.getName()).thenReturn(fieldName);
        DataType dataType = DataType.list(DataType.custom(EntityRefType.class.getName()));
        when(tableMetadata.getColumn(eq(fieldName))).thenReturn(columnMetadata);
        when(columnMetadata.getType()).thenReturn(dataType);

        builder.insertValue(insert, fieldName, value, tableMetadata);
        verify(insert).value(eq(fieldName), anyList());

    }

    @Test
    public void testInsertValue_JsonArray_Set() throws Exception {

        String fieldName = "prop1";
        Object value = new JsonArray().addString("value");

        when(columnMetadata.getName()).thenReturn(fieldName);
        DataType dataType = DataType.set(DataType.text());
        when(tableMetadata.getColumn(eq(fieldName))).thenReturn(columnMetadata);
        when(columnMetadata.getType()).thenReturn(dataType);

        builder.insertValue(insert, fieldName, value, tableMetadata);
        verify(insert).value(eq(fieldName), anySet());

    }

    @Test
    public void testInsertValue_JsonArray_Not_Collection() throws Exception {

        String fieldName = "prop1";
        Object value = new JsonArray().addString("value");

        when(columnMetadata.getName()).thenReturn(fieldName);
        DataType dataType = DataType.text();
        when(tableMetadata.getColumn(eq(fieldName))).thenReturn(columnMetadata);
        when(columnMetadata.getType()).thenReturn(dataType);

        try {
            builder.insertValue(insert, fieldName, value, tableMetadata);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
            assertEquals("Column " + fieldName + " is not a list or set", e.getMessage());
        }

    }

    @Test
    public void testGetEntityRef() throws Exception {

        JsonObject json = new JsonObject()
                .putString("id", UUID.randomUUID().toString())
                .putString("schema", "keyspaceA")
                .putString("table", "tableA");

        builder.getEntityRef(json);
        verify(serializer).serialize(any(EntityRef.class));

    }

    @Test
    public void testGetEntityRef_IllegalArg() throws Exception {

        JsonObject json = new JsonObject();

        try {
            builder.getEntityRef(json);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
            Assert.assertEquals("An entity ref requires an id field", e.getMessage());
        }

        json.putString("id", UUID.randomUUID().toString());

        try {
            builder.getEntityRef(json);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
            Assert.assertEquals("An entity ref requires a table field", e.getMessage());
        }

        json.putString("table", "tableA");

        try {
            builder.getEntityRef(json);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
            Assert.assertEquals("An entity ref requires a schema (Cassandra keyspace) field", e.getMessage());
        }

    }

}
