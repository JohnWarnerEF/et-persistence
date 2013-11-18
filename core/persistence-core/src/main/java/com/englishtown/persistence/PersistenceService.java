package com.englishtown.persistence;

import com.englishtown.promises.Promise;

import java.util.List;

/**
 * Service to store and load persistent maps
 */
public interface PersistenceService {

    /**
     * Load a list of persistent maps
     *
     * @param keys the persistent map keys to load
     * @return the list of persistent maps
     */
    public Promise<LoadResult, Void> load(List<EntityKey> keys);

    /**
     * Store a list of entities
     *
     * @param objects the entities to be stored
     */
    public Promise<StoreResult, Void> store(List<PersistentMap> objects);

}
