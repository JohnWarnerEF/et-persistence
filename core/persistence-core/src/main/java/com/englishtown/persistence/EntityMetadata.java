package com.englishtown.persistence;

import java.util.Map;

/**
 *
 */
public interface EntityMetadata {

    String getType();

    EntityMetadata setType(String type);

    /**
     * Returns the underlying table the entity will be stored in
     *
     * @return the entity storage table
     */
    String getTable();

    /**
     * Sets the underlying table the entity will be stored in
     *
     * @param table the entity storage table
     * @return the persistence metadata
     */
    EntityMetadata setTable(String table);

    /**
     * The underlying schema name the entity will be stored in
     *
     * @return the entity schema name
     */
    String getSchema();

    /**
     * Sets the underlying schema name the entity will be stored in
     *
     * @param schema the entity schema name
     * @return the persistence metadata
     */
    EntityMetadata setSchema(String schema);

    /**
     * A map of field names and type information
     *
     * @return
     */
    Map<String, TypeInfo> getFields();

    /**
     * A map of entity ref names and entity ref info
     *
     * @return
     */
    Map<String, EntityRefMember> getEntityRefs();

}
