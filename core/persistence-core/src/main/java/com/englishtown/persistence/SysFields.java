package com.englishtown.persistence;

import com.englishtown.persistence.acl.AccessControlList;

/**
 * Persistence system fields
 */
public interface SysFields {

    /**
     * The persistent map id
     *
     * @return the id
     */
    String getId();

    /**
     * Sets the persistent map id
     *
     * @param id the id
     */
    SysFields setId(String id);

    /**
     * The persistent map object structure version
     *
     * @return the version
     */
    int getVersion();

    /**
     * Sets the persistent map object structure version
     *
     * @param version the current version
     */
    SysFields setVersion(int version);

    /**
     * Returns the access control list for the persistent map
     *
     * @return the acl
     */
    AccessControlList getACL();

    /**
     * Sets the access control list for the persistent map
     *
     * @param acl the access control list
     */
    SysFields setACL(AccessControlList acl);

    /**
     * The object type represented by the persistent map
     *
     * @return the persistent map object type
     */
    String getType();

    /**
     * Sets the object type represented by the persistent map
     *
     * @param type the entity object type
     */
    SysFields setType(String type);


}
