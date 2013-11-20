package com.englishtown.persistence;

import org.junit.Test;

import javax.persistence.Entity;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for {@link ReflectionUtils}
 */
public class ReflectionUtilsTest {

    @Entity
    private static class EntityClass {
    }

    private static class InheritedEntityClass extends EntityClass {
    }

    private static class NonEntityClass {
    }

    @Test
    public void testGetAnnotation() throws Exception {
        Entity annotation = ReflectionUtils.getAnnotation(Entity.class, EntityClass.class);
        assertNotNull(annotation);
    }

    @Test
    public void testGetAnnotation_Inherited() throws Exception {
        Entity annotation = ReflectionUtils.getAnnotation(Entity.class, InheritedEntityClass.class);
        assertNotNull(annotation);
    }

    @Test
    public void testGetAnnotation_NonEntity() throws Exception {
        Entity annotation = ReflectionUtils.getAnnotation(Entity.class, NonEntityClass.class);
        assertNull(annotation);
    }

}
