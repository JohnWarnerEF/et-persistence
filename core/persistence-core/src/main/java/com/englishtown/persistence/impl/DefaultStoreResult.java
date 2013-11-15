package com.englishtown.persistence.impl;

import com.englishtown.persistence.FailedPersistentMap;
import com.englishtown.persistence.PersistentMap;
import com.englishtown.persistence.StoreResult;

/**
 * Default implementation of {@link StoreResult}
 */
public class DefaultStoreResult extends DefaultPersistenceResult<PersistentMap, FailedPersistentMap> implements StoreResult {
}
