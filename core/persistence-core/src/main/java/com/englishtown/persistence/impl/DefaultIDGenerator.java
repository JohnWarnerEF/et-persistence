package com.englishtown.persistence.impl;

import com.englishtown.persistence.IDGenerator;

import java.util.UUID;

/**
 * Default implementation of {@link IDGenerator}
 */
public class DefaultIDGenerator implements IDGenerator {
    /**
     * Creates a new ID
     *
     * @return
     */
    @Override
    public String createID() {
        return UUID.randomUUID().toString();
    }
}
