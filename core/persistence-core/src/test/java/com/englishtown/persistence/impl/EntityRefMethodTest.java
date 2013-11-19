package com.englishtown.persistence.impl;

import com.englishtown.persistence.TestEntityParent;
import com.englishtown.persistence.TypeInfo;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link EntityRefMethod}
 */
public class EntityRefMethodTest {
    @Test
    public void testSimple() throws Exception {

        String value = "test.value";
        DefaultPersistentMap persistentMap = new DefaultPersistentMap();
        TestEntityParent entityParent = new TestEntityParent(persistentMap);
        entityParent.setString(value);

        String name = "string_val";
        Method method = entityParent.getClass().getDeclaredMethod("getString");
        assertNotNull(method);
        TypeInfo typeInfo = new TypeInfo(entityParent.getClass());

        EntityRefMethod refInfo = new EntityRefMethod(name, method, typeInfo);

        assertEquals(name, refInfo.getName());
        assertEquals(method, refInfo.getMember());
        assertEquals(typeInfo, refInfo.getTypeInfo());
        assertEquals(value, refInfo.getValue(entityParent));

    }
}
