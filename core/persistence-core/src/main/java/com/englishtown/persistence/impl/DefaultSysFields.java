package com.englishtown.persistence.impl;

import com.englishtown.persistence.SysFields;
import com.englishtown.persistence.acl.AccessControlList;

/**
 * Default implementation of {@link com.englishtown.persistence.SysFields}
 */
public class DefaultSysFields implements SysFields {

    private String id;
    private int version;
    private AccessControlList acl;
    private String type;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public DefaultSysFields setId(String i) {
        this.id = i;
        return this;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public DefaultSysFields setVersion(int v) {
        this.version = v;
        return this;
    }

    @Override
    public AccessControlList getACL() {
        return acl;
    }

    @Override
    public DefaultSysFields setACL(AccessControlList a) {
        this.acl = a;
        return this;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public DefaultSysFields setType(String t) {
        this.type = t;
        return this;
    }

}
