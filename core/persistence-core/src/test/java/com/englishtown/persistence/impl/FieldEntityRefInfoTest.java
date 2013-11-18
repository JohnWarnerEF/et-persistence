package com.englishtown.persistence.impl;

import com.englishtown.persistence.TestEntityParent;
import com.englishtown.persistence.TypeInfo;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link FieldEntityRefInfo}
 */
public class FieldEntityRefInfoTest {

    @Test
    public void testSimple() throws Exception {

        String value = "test.value";
        DefaultPersistentMap persistentMap = new DefaultPersistentMap();
        TestEntityParent entityParent = new TestEntityParent(persistentMap);
        entityParent.setStringField(value);

        String name = "string_field";
        Field field = entityParent.getClass().getDeclaredField("stringField");
        assertNotNull(field);
        TypeInfo typeInfo = new TypeInfo(entityParent.getClass());

        FieldEntityRefInfo refInfo = new FieldEntityRefInfo(name, field, typeInfo);

        assertEquals(name, refInfo.getName());
        assertEquals("parent", refInfo.getTable());
        assertEquals("et_core_test", refInfo.getSchema());
        assertEquals(field, refInfo.getMember());
        assertEquals(typeInfo, refInfo.getTypeInfo());
        assertEquals(value, refInfo.getValue(entityParent));

    }

}
