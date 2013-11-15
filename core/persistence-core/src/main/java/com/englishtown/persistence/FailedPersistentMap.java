package com.englishtown.persistence;

/**
 * Contains a failed {@link PersistentMap} and the {@link Throwable} cause
 */
public interface FailedPersistentMap {

    PersistentMap getPersistentMap();

    Throwable getCause();

}
