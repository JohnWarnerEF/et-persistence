package com.englishtown.vertx.persistence.cassandra;

import org.vertx.java.core.json.JsonArray;

/**
 * Load result aggregator
 */
public class LoadResults {

    public int totalCount;
    public int completedCount;

    public final JsonArray missing = new JsonArray();
    public final JsonArray entities = new JsonArray();

}
