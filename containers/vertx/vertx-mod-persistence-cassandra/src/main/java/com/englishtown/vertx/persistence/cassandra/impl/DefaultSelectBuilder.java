package com.englishtown.vertx.persistence.cassandra.impl;

import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.englishtown.cassandra.EntityRef;
import com.englishtown.vertx.persistence.cassandra.SelectBuilder;

/**
 * Default implementation of {@link SelectBuilder}
 */
public class DefaultSelectBuilder implements SelectBuilder {

    @Override
    public Statement build(EntityRef ref) {

        return QueryBuilder
                .select()
                .from(ref.getKeyspace(), ref.getTable())
                .where(QueryBuilder.eq("id", ref.getId()));

    }

}
