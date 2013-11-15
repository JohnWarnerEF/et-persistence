package com.englishtown.persistence;

/**
 * Retrieves and entity metadata
 */
public interface EntityMetadataService {

    EntityMetadata get(Class<? extends PersistentMap> clazz);

}
