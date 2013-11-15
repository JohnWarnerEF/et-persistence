package com.englishtown.persistence;

import java.util.List;

/**
 * {@link StorageService} operation result
 */
public interface PersistenceResult<TSuccess, TFail> {

    /**
     * Whether the persistence operation was 100% successful
     *
     * @return true if succeeded
     */
    boolean succeeded();

    /**
     * List of result objects that succeeded
     *
     * @return
     */
    List<TSuccess> getSucceeded();

    /**
     * List of objects that failed
     *
     * @return
     */
    List<TFail> getFailed();

}
