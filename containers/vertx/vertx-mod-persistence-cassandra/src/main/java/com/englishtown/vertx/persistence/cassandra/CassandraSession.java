package com.englishtown.vertx.persistence.cassandra;

import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;
import com.google.common.util.concurrent.FutureCallback;
import org.vertx.java.core.json.JsonObject;

/**
 *
 */
public interface CassandraSession extends AutoCloseable {

    void init(JsonObject config);

    void executeAsync(Statement statement, final FutureCallback<ResultSet> callback);

    ResultSet execute(String query);

    Metadata getMetadata();

}
