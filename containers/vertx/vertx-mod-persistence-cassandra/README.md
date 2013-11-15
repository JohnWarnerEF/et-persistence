# vertx-mod-persistence-cassandra

Allows persisting entity maps to Cassandra


## Load

The load operation expects a JSON message with the following structure:

```json
{
    "action": "load",
    "keys": [
        {
            "id": "UUID",
            "schema": "keyspace_name",
            "table": "table_name"
        }
    ]
}
```

* `keys` - a json array of key objects to be loaded.  A key consists of the following fields:
    * `id` - the row primary key, must be a UUID
    * `schema` - the C* keyspace where the entity exists
    * `table` - the C* table where the entity exists

Each entity key is loaded and read into a json object.  If a column is of type `com.englishtown.cassandra.db.marshal.EntityRefType`, the corresponding entity is also loaded.

#### Reply message

The reply json message has the following structure:

```json
{
    "status": "ok",
    "entities": [
        {
            "sys_fields": {
                "id": "UUID",
                ...
            },
            "fields": {
                "field1": "value1",
                ...
            }
        }
    ],
    "missing": [
        {
            "id": "UUID",
            "schema": "keyspace_name",
            "table": "table_name"
        }
    ]
}
```

* `status` - `ok` or `error`
* `entities` - a json array of the loaded entities.  An entity consists of:
    * `sys_fields` - a json object of key/value system fields
    * `fields' - a json object of key/value fields
* `missing` - a json array of keys that could not be found in C*.  Hopefully empty.


## Store

The store operation expects a JSON message with the following structure:

```json
{
    "action": "store",
    "entities": [ ... ],
    "schemas": { ... }
}
```

The provided entities are written to C* in a single batch.  A standard vert.x JSON reply message is returned with status `ok` or `error`.


#### The `entities` json array

The `entities` json array is a list of json objects to persist.  The structure of an entity object is as follows:

```json
{
    "schema": "keyspace_name",
    "table": "table_name",
    "sys_fields": {
        "id": "UUID",
        "type": "class_type",
        "version": 1,
        "acl": ["UUID1", "UUID2"]
    },
    "fields": {
        "field1": "value",
        "field2": 2,
        "field3": true,
        "field4": ["value1", "value2"],
        "field5": {
            "key1": "value1",
            "key2": "value2"
        },
        ...
    }
}
```

* `schema` - a string representing the C* keyspace to store the entity
* `table` - a string representing the C* CQL table to store the entity
* `sys_fields` - a json object of system fields.  Possible values include
    * `id` - the table primary key, must be a valid UUID
    * `type` - the class type being persisted
    * `version` - the schema version (optional)
    * `acl` - a json array of unique UUIDs representing an access control list (optional)
* `fields` - a json object of fields to persist.


#### The `schemas` json object

The `schemas` json object is an optional key/value pair of entity schemas.  If a schema is not provided for an entity to be persisted, the CQL table must be created ahead of time.

```json
{
    "class_type": {
        "sys_fields": [
            {
                "name": "sys_field_name_1",
                "type": "java_type"
            },
            {
                "name": "sys_field_name_2",
                "type": "java_type"
            }
        ],
        "fields": [
            {
                "name": "field_name_1",
                "type": "java_type"
            },
            {
                "name": "field_name_2",
                "type": "java_type"
            }
        ]
    }
}
```

* `class_type` - the keys of the `schemas` object are the entity types to be persisted (these correspond to entity.sys_fields.type).
* `sys_fields` - an array of json objects defining the system fields.  See `com.englishtown.vertx.persistence.cassandra.impl.DefaultSchemaBuilder.getCqlType()` for valid types.
* `fields` - an array of json objects defining the fields.  See `com.englishtown.vertx.persistence.cassandra.impl.DefaultSchemaBuilder.getCqlType()` for valid types.


