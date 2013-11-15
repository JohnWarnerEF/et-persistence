package com.englishtown.vertx.persistence;

import com.englishtown.persistence.EntityKey;
import com.englishtown.persistence.LoadResult;
import com.englishtown.persistence.PersistentMap;
import com.englishtown.persistence.StoreResult;
import com.englishtown.promises.Resolver;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import java.util.List;

/**
 * Reads persistence message results
 */
public interface MessageReader {

    /**
     * Read the reply message from a load operation
     *
     * @param reply
     * @param keys
     * @param resolver
     */
    void readLoadReply(Message<JsonObject> reply, List<EntityKey> keys, Resolver<LoadResult, Void> resolver);

    /**
     * Read the reply message from a store operation
     *
     * @param reply
     * @param entities
     * @param resolver
     */
    void readStoreReply(Message<JsonObject> reply, List<PersistentMap> entities, Resolver<StoreResult, Void> resolver);

}
