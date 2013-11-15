package com.englishtown.vertx.persistence.cassandra;

import com.datastax.driver.core.Row;
import com.englishtown.cassandra.EntityRef;

/**
 * Future callback for load completion
 */
public interface LoadCallback {

    void onSuccess(EntityRef ref, Row row);

    void onFailure(EntityRef ref, Throwable t);

}
