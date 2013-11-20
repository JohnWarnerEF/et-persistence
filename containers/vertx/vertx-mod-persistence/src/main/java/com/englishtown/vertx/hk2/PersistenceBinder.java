package com.englishtown.vertx.hk2;

import com.englishtown.persistence.*;
import com.englishtown.persistence.impl.*;
import com.englishtown.vertx.persistence.MessageBuilder;
import com.englishtown.vertx.persistence.MessageReader;
import com.englishtown.vertx.persistence.SchemaCache;
import com.englishtown.vertx.persistence.impl.DefaultMessageBuilder;
import com.englishtown.vertx.persistence.impl.DefaultMessageReader;
import com.englishtown.vertx.persistence.impl.DefaultPersistenceService;
import com.englishtown.vertx.persistence.impl.DefaultSchemaCache;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

/**
 * Default persistence HK2 binder
 */
public class PersistenceBinder extends AbstractBinder {
    /**
     * Implement to provide binding definitions using the exposed binding
     * methods.
     */
    @Override
    protected void configure() {

        bind(DefaultPersistenceService.class).to(PersistenceService.class);

        bind(DefaultMessageBuilder.class).to(MessageBuilder.class);
        bind(DefaultMessageReader.class).to(MessageReader.class);
        bind(DefaultSchemaCache.class).to(SchemaCache.class).in(Singleton.class);

        bind(DefaultEntityMetadataService.class).to(EntityMetadataService.class).in(Singleton.class);
        bind(DefaultIDGenerator.class).to(IDGenerator.class);

        bind(DefaultLoadedPersistentMapFactory.class).to(LoadedPersistentMapFactory.class);
        bind(DefaultStoreResult.class).to(StoreResult.class);
        bind(DefaultLoadResult.class).to(LoadResult.class);
        bind(DefaultIDGenerator.class).to(IDGenerator.class);

    }
}
