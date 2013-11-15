package com.englishtown.persistence;

import java.util.Map;

/**
 * Factory to create {@link PersistentMap} instances
 */
public interface PersistentMapFactory {

    PersistentMap create();

    PersistentMap create(Map<String, Object> map);

}
