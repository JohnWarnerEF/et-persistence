package com.englishtown.persistence.acl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Persistence access control list
 */

public class AccessControlList extends TreeSet<UUID> {

    private static final Logger logger = LoggerFactory.getLogger(AccessControlList.class.getName());

    public AccessControlList() {
    }

    public AccessControlList(Collection<? extends UUID> c) {
        super(c);
    }

}
