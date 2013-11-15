package com.datastax.driver.core;

/**
 * Test wrapper for
 */
public class DefinitionWrapper extends ColumnDefinitions.Definition {

    public DefinitionWrapper(String keyspace, String table, String name, DataType type) {
        super(keyspace, table, name, type);
    }
}
