package com.englishtown.persistence.impl;

import com.englishtown.persistence.TestEntityParent;
import com.englishtown.persistence.TypeInfo;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link EntityRefField}
 */
public class EntityRefFieldTest {

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

        EntityRefField refInfo = new EntityRefField(name, field, typeInfo);

        assertEquals(name, refInfo.getName());
        assertEquals(field, refInfo.getMember());
        assertEquals(typeInfo, refInfo.getTypeInfo());
        assertEquals(value, refInfo.getValue(entityParent));

    }

}
