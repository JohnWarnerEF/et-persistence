package com.englishtown.vertx.persistence.cassandra.impl;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.DefinitionWrapper;
import com.datastax.driver.core.Row;
import com.englishtown.cassandra.EntityRef;
import com.englishtown.cassandra.db.marshal.EntityRefType;
import com.englishtown.cassandra.serializers.EntityRefSerializer;
import com.englishtown.vertx.persistence.cassandra.EntityPersistor;
import com.englishtown.vertx.persistence.cassandra.LoadCallback;
import com.englishtown.vertx.persistence.cassandra.LoadResults;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DefaultRowReader}
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultRowReaderTest {

    DefaultRowReader reader;
    LoadResults results = new LoadResults();

    @Mock
    EntityRefSerializer serializer;
    @Mock
    Row row;
    @Mock
    EntityPersistor persistor;
    @Mock
    LoadCallback callback;

    @Before
    public void setUp() {

        reader = new DefaultRowReader(serializer, persistor);

    }

    @Test
    public void testRead() throws Exception {

        ColumnDefinitions columnDefinitions = mock(ColumnDefinitions.class);
        ColumnDefinitions.Definition col1 = new DefinitionWrapper("keyspace", "table", "col1", DataType.text());
        ColumnDefinitions.Definition col2 = new DefinitionWrapper("keyspace", "table", "col2", DataType.text());
        ColumnDefinitions.Definition col3 = new DefinitionWrapper("keyspace", "table", "col3", DataType.text());

        List<ColumnDefinitions.Definition> columns = new ArrayList<>();
        columns.add(col1);
        columns.add(col2);
        columns.add(col3);

        when(row.isNull(eq(1))).thenReturn(true);
        when(row.getColumnDefinitions()).thenReturn(columnDefinitions);
        when(columnDefinitions.iterator()).thenReturn(columns.iterator());

        EntityRef ref = mock(EntityRef.class);

        reader.read(row, ref, results, callback);
        assertEquals(1, results.entities.size());
        JsonObject entity = results.entities.get(0);
        assertEquals(4, entity.size());
    }

    @Test
    public void testReadColumn_SysColumn() throws Exception {
        when(row.getUUID(eq(0))).thenReturn(UUID.randomUUID());
        JsonObject sysFields = mock(JsonObject.class);
        JsonObject fields = mock(JsonObject.class);

        ColumnDefinitions.Definition column = new DefinitionWrapper("keyspace", "table", "id", DataType.uuid());
        reader.readColumn(row, results, sysFields, fields, 0, column, callback);

        verify(sysFields).putString(eq("id"), anyString());
    }

    @Test
    public void testReadColumn_ASCII() throws Exception {
        JsonObject fields = testReadColumn(DataType.ascii());
        verify(fields).putString(eq("prop1"), anyString());
    }

    @Test
    public void testReadColumn_TEXT() throws Exception {
        JsonObject fields = testReadColumn(DataType.text());
        verify(fields).putString(eq("prop1"), anyString());
    }

    @Test
    public void testReadColumn_VARCHAR() throws Exception {
        JsonObject fields = testReadColumn(DataType.varchar());
        verify(fields).putString(eq("prop1"), anyString());
    }

    @Test
    public void testReadColumn_BIGINT() throws Exception {
        JsonObject fields = testReadColumn(DataType.bigint());
        verify(fields).putNumber(eq("prop1"), any(Number.class));
    }

    @Test
    public void testReadColumn_COUNTER() throws Exception {
        JsonObject fields = testReadColumn(DataType.counter());
        verify(fields).putNumber(eq("prop1"), any(Number.class));
    }

    @Test
    public void testReadColumn_BLOB() throws Exception {
        when(row.getBytes(eq(0))).thenReturn(ByteBuffer.allocate(0));
        JsonObject fields = testReadColumn(DataType.blob());
        verify(fields).putBinary(eq("prop1"), any(byte[].class));
    }

    @Test
    public void testReadColumn_BOOLEAN() throws Exception {
        JsonObject fields = testReadColumn(DataType.cboolean());
        verify(fields).putBoolean(eq("prop1"), anyBoolean());
    }

    @Test
    public void testReadColumn_DECIMAL() throws Exception {
        JsonObject fields = testReadColumn(DataType.decimal());
        verify(fields).putNumber(eq("prop1"), any(Number.class));
    }

    @Test
    public void testReadColumn_DOUBLE() throws Exception {
        JsonObject fields = testReadColumn(DataType.cdouble());
        verify(fields).putNumber(eq("prop1"), any(Number.class));
    }

    @Test
    public void testReadColumn_FLOAT() throws Exception {
        JsonObject fields = testReadColumn(DataType.cfloat());
        verify(fields).putNumber(eq("prop1"), any(Number.class));
    }

    @Test
    public void testReadColumn_INET() throws Exception {
        when(row.getInet(eq(0))).thenReturn(InetAddress.getLocalHost());
        JsonObject fields = testReadColumn(DataType.inet());
        verify(fields).putString(eq("prop1"), anyString());
    }

    @Test
    public void testReadColumn_INT() throws Exception {
        JsonObject fields = testReadColumn(DataType.cint());
        verify(fields).putNumber(eq("prop1"), any(Number.class));
    }

    @Test
    public void testReadColumn_TIMESTAMP() throws Exception {
        when(row.getDate(eq(0))).thenReturn(new Date());
        JsonObject fields = testReadColumn(DataType.timestamp());
        verify(fields).putNumber(eq("prop1"), any(Number.class));
    }

    @Test
    public void testReadColumn_UUID() throws Exception {
        when(row.getUUID(eq(0))).thenReturn(UUID.randomUUID());
        JsonObject fields = testReadColumn(DataType.uuid());
        verify(fields).putString(eq("prop1"), anyString());
    }

    @Test
    public void testReadColumn_TIMEUUID() throws Exception {
        when(row.getUUID(eq(0))).thenReturn(UUID.randomUUID());
        JsonObject fields = testReadColumn(DataType.timeuuid());
        verify(fields).putString(eq("prop1"), anyString());
    }

    @Test
    public void testReadColumn_VARINT() throws Exception {
        JsonObject fields = testReadColumn(DataType.varint());
        verify(fields).putNumber(eq("prop1"), any(Number.class));
    }

    @Test
    public void testReadColumn_LIST() throws Exception {
        JsonObject fields = testReadColumn(DataType.list(DataType.text()));
        verify(fields).putArray(eq("prop1"), any(JsonArray.class));
    }

    @Test
    public void testReadColumn_LIST_ENTITYREF() throws Exception {
        JsonObject fields = testReadColumn(DataType.list(DataType.custom(EntityRefType.class.getName())));
        verify(fields).putArray(eq("prop1"), any(JsonArray.class));
    }

    @Test
    public void testReadColumn_SET() throws Exception {
        JsonObject fields = testReadColumn(DataType.set(DataType.text()));
        verify(fields).putArray(eq("prop1"), any(JsonArray.class));
    }

    @Test
    public void testReadColumn_SET_ENTITYREF() throws Exception {
        JsonObject fields = testReadColumn(DataType.set(DataType.custom(EntityRefType.class.getName())));
        verify(fields).putArray(eq("prop1"), any(JsonArray.class));
    }

    @Test
    public void testReadColumn_MAP() throws Exception {
        JsonObject fields = testReadColumn(DataType.map(DataType.text(), DataType.text()));
        verify(fields).putObject(eq("prop1"), any(JsonObject.class));
    }

    @Test
    public void testReadColumn_CUSTOM_ENTITYREF() throws Exception {
        when(serializer.deserialize(any(ByteBuffer.class))).thenReturn(new EntityRef(UUID.randomUUID(), "table", "keyspace"));
        JsonObject fields = testReadColumn(DataType.custom(EntityRefType.class.getName()));
        verify(fields).putObject(eq("prop1"), any(JsonObject.class));
    }

    private JsonObject testReadColumn(DataType dataType) {

        JsonObject sysFields = mock(JsonObject.class);
        JsonObject fields = mock(JsonObject.class);

        ColumnDefinitions.Definition column = new DefinitionWrapper("keyspace", "table", "prop1", dataType);
        reader.readColumn(row, results, sysFields, fields, 0, column, callback);
        return fields;
    }

    @Test
    public void testReadSysColumn_id() throws Exception {
        String name = "id";
        when(row.getUUID(eq(0))).thenReturn(UUID.randomUUID());
        JsonObject sysFields = testReadSysColumn(name, DataType.uuid());
        verify(sysFields).putString(eq(name), anyString());
    }

    @Test
    public void testReadSysColumn_sys_version() throws Exception {
        String name = "sys_version";
        when(row.getInt(eq(0))).thenReturn(2);
        JsonObject sysFields = testReadSysColumn(name, DataType.uuid());
        verify(sysFields).putNumber(eq("version"), eq(2));
    }

    @Test
    public void testReadSysColumn_sys_acl() throws Exception {
        String name = "sys_acl";
        Set<UUID> set = new HashSet<>();
        set.add(UUID.randomUUID());
        set.add(UUID.randomUUID());
        when(row.getSet(eq(0), eq(UUID.class))).thenReturn(set);
        JsonObject sysFields = testReadSysColumn(name, DataType.uuid());
        verify(sysFields).putArray(eq("acl"), any(JsonArray.class));
    }

    @Test
    public void testReadSysColumn_sys_type() throws Exception {
        String name = "sys_type";
        when(row.getString(eq(0))).thenReturn("com.type");
        JsonObject sysFields = testReadSysColumn(name, DataType.uuid());
        verify(sysFields).putString(eq("type"), eq("com.type"));
    }

    @Test
    public void testReadSysColumn_sys_update_date() throws Exception {
        String name = "sys_update_date";
        Date d = new Date();
        when(row.getDate(eq(0))).thenReturn(d);
        JsonObject sysFields = testReadSysColumn(name, DataType.timestamp());
        verify(sysFields).putNumber(eq("update_date"), eq(d.getTime()));
    }

    public JsonObject testReadSysColumn(String name, DataType dataType) {
        JsonObject sysFields = mock(JsonObject.class);
        ColumnDefinitions.Definition column = new DefinitionWrapper("keyspace", "table", name, dataType);

        reader.readSysColumn(row, sysFields, 0, column);
        return sysFields;
    }

}
