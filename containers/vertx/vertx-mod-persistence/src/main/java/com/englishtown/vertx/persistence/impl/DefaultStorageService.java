package com.englishtown.vertx.persistence.impl;

import com.englishtown.persistence.*;
import com.englishtown.promises.Deferred;
import com.englishtown.promises.Promise;
import com.englishtown.promises.When;
import com.englishtown.vertx.persistence.MessageBuilder;
import com.englishtown.vertx.persistence.MessageReader;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import javax.inject.Inject;
import java.util.List;

/**
 * Default implementation of {@link StorageService}
 */
public class DefaultStorageService implements StorageService {

    private final EventBus eventBus;
    private final MessageBuilder messageBuilder;
    private final String address;
    private final MessageReader messageReader;

    public static final String CONFIG_ADDRESS = "et.persistence.address";

    @Inject
    public DefaultStorageService(
            Vertx vertx,
            Container container,
            MessageBuilder messageBuilder,
            MessageReader messageReader) {

        this.eventBus = vertx.eventBus();
        this.messageBuilder = messageBuilder;
        this.messageReader = messageReader;
        this.address = container.config().getString(CONFIG_ADDRESS);

        if (this.address == null || this.address.isEmpty()) {
            throw new IllegalArgumentException("Configuration is missing " + CONFIG_ADDRESS);
        }
    }

    protected String getAddress() {
        return address;
    }

    @Override
    public Promise<LoadResult, Void> load(final List<EntityKey> keys) {

        if (keys == null || keys.size() == 0) {
            throw new IllegalArgumentException("No keys provided to load.");
        }

        final Deferred<LoadResult, Void> d = new When<LoadResult, Void>().defer();
        JsonObject message = messageBuilder.buildLoadMessage(keys);

        eventBus.send(getAddress(), message, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                messageReader.readLoadReply(reply, keys, d.getResolver());
            }
        });

        return d.getPromise();
    }

    @Override
    public Promise<StoreResult, Void> store(final List<PersistentMap> entities) {

        if (entities == null || entities.size() == 0) {
            throw new IllegalArgumentException("No entities provided to store.");
        }

        JsonObject message = messageBuilder.buildStoreMessage(entities);

        final Deferred<StoreResult, Void> d = new When<StoreResult, Void>().defer();

        eventBus.send(getAddress(), message, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                messageReader.readStoreReply(reply, entities, d.getResolver());
            }
        });

        return d.getPromise();
    }

}
