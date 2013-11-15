package com.englishtown.vertx.persistence.cassandra;

import com.datastax.driver.core.TableMetadata;
import org.vertx.java.core.json.JsonObject;

import java.util.Map;

/**
 * Ensures CQL key spaces and tables exist with the correct columns
 */
public interface SchemaBuilder {

    public final static String JSON_FIELD_SYS_FIELDS = "sys_fields";
    public final static String JSON_FIELD_FIELDS = "fields";

    public final static String JSON_FIELD_ID = "id";
    public final static String JSON_FIELD_TYPE = "type";
    public final static String JSON_FIELD_VERSION = "version";
    public final static String JSON_FIELD_ACL = "acl";

    /**
     * Ensures a keyspace and table schema exists.  Creating or updating as needed.
     *
     * @param entity
     * @param schemas
     * @param buildCache
     * @return the up to date table metadata
     */
    TableMetadata build(
            JsonObject entity,
            JsonObject schemas,
            Map<String, TableMetadata> buildCache);

}
