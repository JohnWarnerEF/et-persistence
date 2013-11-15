package com.englishtown.vertx.persistence.cassandra;

import com.datastax.driver.core.ResultSet;
import com.englishtown.cassandra.EntityRef;
import com.google.common.util.concurrent.FutureCallback;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Load and Store entities
 */
public interface EntityPersistor {

    /**
     * Load entities via an entity ref recursively
     *
     * @param ref
     * @param results
     * @param callback
     */
    void load(EntityRef ref, LoadResults results, final LoadCallback callback);

    /**
     * Store entities
     *
     * @param entities
     * @param schemas
     * @param callback
     */
    void store(JsonArray entities, JsonObject schemas, FutureCallback<ResultSet> callback);

}
