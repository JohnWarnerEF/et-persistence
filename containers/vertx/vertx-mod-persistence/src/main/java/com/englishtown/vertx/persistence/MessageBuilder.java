package com.englishtown.vertx.persistence;

import com.englishtown.persistence.EntityKey;
import com.englishtown.persistence.PersistentMap;
import org.vertx.java.core.json.JsonObject;

import java.util.List;

/**
 * Builds vert.x event bus load and store persistence messages
 */
public interface MessageBuilder {

    /**
     * Builds a json load message for the provided entity keys
     *
     * @param keys the entities to load
     * @return the event bus load message
     */
    JsonObject buildLoadMessage(List<EntityKey> keys);

    /**
     * Builds a json store message for the provided entities
     *
     * @param entities
     * @return
     */
    JsonObject buildStoreMessage(List<PersistentMap> entities);

}
