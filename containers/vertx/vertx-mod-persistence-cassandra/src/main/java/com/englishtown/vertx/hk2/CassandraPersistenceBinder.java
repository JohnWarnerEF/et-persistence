package com.englishtown.vertx.hk2;

import com.datastax.driver.core.Cluster;
import com.englishtown.cassandra.serializers.EntityRefSerializer;
import com.englishtown.vertx.persistence.cassandra.*;
import com.englishtown.vertx.persistence.cassandra.impl.*;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

/**
 * HK2 binder for Cassandra persistence
 */
public class CassandraPersistenceBinder extends AbstractBinder {
    @Override
    protected void configure() {

        bind(Cluster.Builder.class).to(Cluster.Builder.class);
        bind(EntityRefSerializer.class).to(EntityRefSerializer.class).in(Singleton.class);

        bind(DefaultCassandraSession.class).to(CassandraSession.class).in(Singleton.class);
        bind(DefaultEntityPersistor.class).to(EntityPersistor.class).in(Singleton.class);
        bind(DefaultSchemaBuilder.class).to(SchemaBuilder.class);
        bind(DefaultInsertBuilder.class).to(InsertBuilder.class);
        bind(DefaultSelectBuilder.class).to(SelectBuilder.class);
        bind(DefaultRowReader.class).to(RowReader.class);

    }
}
