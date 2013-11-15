package com.englishtown.persistence.impl;

import com.englishtown.persistence.PersistenceResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link PersistenceResult}
 */
public class DefaultPersistenceResult<TSuccess, TFail> implements PersistenceResult<TSuccess, TFail> {

    protected List<TFail> failed = new ArrayList<>();
    protected List<TSuccess> succeeded = new ArrayList<>();

    @Override
    public boolean succeeded() {
        return getFailed().isEmpty() && !getSucceeded().isEmpty();
    }

    @Override
    public List<TSuccess> getSucceeded() {
        return succeeded;
    }

    @Override
    public List<TFail> getFailed() {
        return failed;
    }

}
