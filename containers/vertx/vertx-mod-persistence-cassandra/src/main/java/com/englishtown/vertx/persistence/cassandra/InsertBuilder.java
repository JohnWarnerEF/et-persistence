package com.englishtown.vertx.persistence.cassandra;

import com.datastax.driver.core.Statement;
import com.datastax.driver.core.TableMetadata;
import org.vertx.java.core.json.JsonObject;

/**
 * A CQL Insert statement builder
 */
public interface InsertBuilder {

    /**
     * Builds an insert statement for the provided datamap
     *
     * @param entity
     * @param table
     * @return
     */
    Statement build(JsonObject entity, TableMetadata table);

}
