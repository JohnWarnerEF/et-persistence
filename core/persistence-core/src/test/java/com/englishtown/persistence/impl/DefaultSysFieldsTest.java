package com.englishtown.persistence.impl;

import com.englishtown.persistence.acl.AccessControlList;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link DefaultSysFields}
 */
public class DefaultSysFieldsTest {

    DefaultSysFields sysFields = new DefaultSysFields();

    @Test
    public void testGetId() throws Exception {
        String id = "test.id";
        assertEquals(id, sysFields.setId(id).getId());
    }

    @Test
    public void testGetVersion() throws Exception {
        int version = 3;
        assertEquals(version, sysFields.setVersion(version).getVersion());
    }

    @Test
    public void testGetACL() throws Exception {
        AccessControlList acl = new AccessControlList();
        acl.add(UUID.randomUUID());
        assertEquals(acl, sysFields.setACL(acl).getACL());
    }

    @Test
    public void testGetType() throws Exception {
        String type = "test.type";
        assertEquals(type, sysFields.setType(type).getType());
    }

}
