package com.englishtown.vertx.persistence.cassandra;

import com.datastax.driver.core.Row;
import com.englishtown.cassandra.EntityRef;

/**
 *
 */
public interface RowReader {

    public static final String ENTITY_REF_TYPE_CLASS_NAME = "com.englishtown.cassandra.db.marshal.EntityRefType";

    /**
     * Reads a Cassandra result set row and adds to the results
     *
     * @param row
     * @param ref
     * @param results
     * @param callback
     */
    void read(Row row, EntityRef ref, LoadResults results, LoadCallback callback);

}
