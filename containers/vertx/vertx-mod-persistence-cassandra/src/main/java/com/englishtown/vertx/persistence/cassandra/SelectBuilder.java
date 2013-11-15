package com.englishtown.vertx.persistence.cassandra;

import com.datastax.driver.core.Statement;
import com.englishtown.cassandra.EntityRef;

/**
 * A select statement builder
 */
public interface SelectBuilder {

    /**
     * Create CQL select statement
     *
     * @param ref
     * @return
     */
    Statement build(EntityRef ref);

}
