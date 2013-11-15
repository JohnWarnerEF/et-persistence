package com.englishtown.persistence;

/**
 * Entity key for loading persistent maps
 */
public interface EntityKey {

    /**
     * The entity id
     *
     * @return
     */
    String getId();

    /**
     * The entity class
     *
     * @return
     */
    Class<? extends PersistentMap> getEntityClass();

}
