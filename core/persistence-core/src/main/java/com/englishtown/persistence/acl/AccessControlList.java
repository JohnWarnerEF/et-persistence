package com.englishtown.persistence.acl;

import java.util.Collection;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Persistence access control list
 */

public class AccessControlList extends TreeSet<UUID> {

    public AccessControlList() {
    }

    public AccessControlList(Collection<? extends UUID> c) {
        super(c);
    }

}
