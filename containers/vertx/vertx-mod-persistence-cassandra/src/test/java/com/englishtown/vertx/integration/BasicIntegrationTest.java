package com.englishtown.vertx.integration;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import com.englishtown.vertx.CassandraPersistence;
import com.englishtown.vertx.persistence.cassandra.SchemaBuilder;
import com.englishtown.vertx.persistence.cassandra.impl.DefaultCassandraSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.vertx.testtools.VertxAssert.*;

/**
 *
 */
@RunWith(CPJavaClassRunner.class)
public class BasicIntegrationTest extends TestVerticle {

    String keyspace = "vertx_mod_persistence_cassandra_tests";
    String memberTable = "member";
    String profileTable = "profile";
    String addressTable = "address";

    UUID member_id = UUID.randomUUID();
    UUID profile_id = UUID.randomUUID();
    UUID address_id1 = UUID.randomUUID();
    UUID address_id2 = UUID.randomUUID();
    UUID address_id3 = UUID.randomUUID();

    private String memberType = "com.englishtown.test.Member";
    private String profileType = "com.englishtown.test.Profile";
    private String addressType = "com.englishtown.test.Address";

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(final Future<Void> startedResult) {

        String ip = "127.0.0.1";
        initSchemas(ip);
        JsonObject config = new JsonObject()
                .putString("config_keyspace", keyspace)
                .putArray(DefaultCassandraSession.CONFIG_SEEDS, new JsonArray().addString(ip));

        container.deployVerticle(CassandraPersistence.class.getName(), config, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
                if (result.succeeded()) {
                    start();
                    startedResult.setResult(null);
                } else {
                    startedResult.setFailure(result.cause());
                }
            }
        });

    }

    private void initSchemas(String ip) {

        Cluster.Builder builder = new Cluster.Builder();
        Cluster cluster = builder.addContactPoint(ip).build();
        Session session = cluster.connect();

        Metadata metadata = cluster.getMetadata();

        // Drop keyspace if it already exists
        if (metadata.getKeyspace(keyspace) != null) {
            session.execute("DROP KEYSPACE " + keyspace);
        }

        // Create keyspace
        session.execute("CREATE KEYSPACE " + keyspace + " WITH replication " +
                "= {'class':'SimpleStrategy', 'replication_factor':3};");

        session.execute(
                "CREATE TABLE " + keyspace + "." + memberTable + " (" +
                        "id uuid PRIMARY KEY," +
                        "sys_version int," +
                        "sys_acl set<uuid>," +
                        "sys_type text," +
                        "sys_update_date timestamp," +
//                        "username text," +
//                        "profile 'com.englishtown.cassandra.db.marshal.EntityRef'," +
//                        "addresses set<'com.englishtown.cassandra.db.marshal.EntityRef'>" +
                        ");");

//        session.execute(
//                "CREATE TABLE " + keyspace + "." + profileTable + " (" +
//                        "id uuid PRIMARY KEY," +
//                        "sys_version int," +
//                        "sys_acl set<uuid>," +
//                        "sys_type text," +
//                        "sys_update_date timestamp," +
//                        "first_name text," +
//                        "last_name text," +
//                        "phone_numbers map<text, text>," +
//                        "email_addresses set<text>" +
//                        ");");

//        session.execute(
//                "CREATE TABLE " + keyspace + "." + addressTable + " (" +
//                        "id uuid PRIMARY KEY," +
//                        "sys_version int," +
//                        "sys_acl set<uuid>," +
//                        "sys_type text," +
//                        "sys_update_date timestamp," +
//                        "address1 text," +
//                        "address2 text," +
//                        "city text," +
//                        "state text," +
//                        "postal_code text," +
//                        "country text," +
//                        ");");

    }

    @Test
    public void testStoreAndLoad() throws Exception {

        JsonArray entities = createEntities();
        JsonObject schemas = createSchemas();

        JsonObject message = new JsonObject()
                .putString("action", "store")
                .putArray("entities", entities)
                .putObject("schemas", schemas);

        final EventBus eb = vertx.eventBus();

        eb.send(CassandraPersistence.DEFAULT_ADDRESS, message, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {

                // Verify status, return if not "ok"
                String status = reply.body().getString("status");
                assertEquals("ok", status);
                if (!"ok".equals(status)) {
                    testComplete();
                    return;
                }

                testLoad();
            }
        });

    }

    private void testLoad() {

        final EventBus eb = vertx.eventBus();

        // Create load message
        JsonObject message = new JsonObject()
                .putString("action", "load")
                .putArray("refs", new JsonArray()
                        .addObject(new JsonObject()
                                .putString("id", member_id.toString())
                                .putString("schema", keyspace)
                                .putString("table", memberTable)
                                .putString("type", "EntityRef")));

        eb.send(CassandraPersistence.DEFAULT_ADDRESS, message, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                assertEquals("ok", reply.body().getString("status"));
                JsonArray results = reply.body().getArray("entities");
                assertNotNull(results);
                assertEquals(5, results.size());
                JsonArray missing = reply.body().getArray("missing");
                assertNotNull(missing);
                assertEquals(0, missing.size());

                testLoadMissing();
            }
        });

    }

    private void testLoadMissing() {

        final EventBus eb = vertx.eventBus();

        // Create load message
        JsonObject message = new JsonObject()
                .putString("action", "load")
                .putArray("refs", new JsonArray()
                        .addObject(new JsonObject()
                                .putString("id", member_id.toString())
                                .putString("schema", keyspace)
                                .putString("table", memberTable)
                                .putString("type", "EntityRef"))
                        .addObject(new JsonObject()
                                .putString("id", UUID.randomUUID().toString())
                                .putString("schema", keyspace)
                                .putString("table", memberTable)
                                .putString("type", "EntityRef"))
                );

        eb.send(CassandraPersistence.DEFAULT_ADDRESS, message, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> reply) {
                assertEquals("ok", reply.body().getString("status"));
                JsonArray results = reply.body().getArray("entities");
                assertNotNull(results);
                assertEquals(5, results.size());
                JsonArray missing = reply.body().getArray("missing");
                assertNotNull(missing);
                assertEquals(1, missing.size());
                testComplete();
            }
        });

    }

    private JsonArray createEntities() {

        JsonArray acl = new JsonArray().add(UUID.randomUUID()).add(UUID.randomUUID());

        JsonObject member = new JsonObject()
                .putString("schema", keyspace)
                .putString("table", memberTable)
                .putObject(SchemaBuilder.JSON_FIELD_SYS_FIELDS, new JsonObject()
                        .putString(SchemaBuilder.JSON_FIELD_ID, member_id.toString())
                        .putArray(SchemaBuilder.JSON_FIELD_ACL, acl)
                        .putString(SchemaBuilder.JSON_FIELD_TYPE, memberType)
                        .putNumber(SchemaBuilder.JSON_FIELD_VERSION, 1)
                )
                .putObject(SchemaBuilder.JSON_FIELD_FIELDS, new JsonObject()
                        .putString("username", "test.user.name")
                        .putObject("profile", new JsonObject()
                                .putString("id", profile_id.toString())
                                .putString("schema", keyspace)
                                .putString("table", profileTable))
                        .putArray("addresses", new JsonArray()
                                .addObject(new JsonObject()
                                        .putString("id", address_id1.toString())
                                        .putString("schema", keyspace)
                                        .putString("table", addressTable))
                                .addObject(new JsonObject()
                                        .putString("id", address_id2.toString())
                                        .putString("schema", keyspace)
                                        .putString("table", addressTable))
                                .addObject(new JsonObject()
                                        .putString("id", address_id3.toString())
                                        .putString("schema", keyspace)
                                        .putString("table", addressTable))
                        )
                );

        JsonObject profile = new JsonObject()
                .putString("schema", keyspace)
                .putString("table", profileTable)
                .putObject(SchemaBuilder.JSON_FIELD_SYS_FIELDS, new JsonObject()
                        .putString(SchemaBuilder.JSON_FIELD_ID, profile_id.toString())
                        .putArray(SchemaBuilder.JSON_FIELD_ACL, acl)
                        .putString(SchemaBuilder.JSON_FIELD_TYPE, profileType)
                        .putNumber(SchemaBuilder.JSON_FIELD_VERSION, 1)
                )
                .putObject(SchemaBuilder.JSON_FIELD_FIELDS, new JsonObject()
                        .putString("first_name", "Bob")
                        .putString("last_name", "Smith")
                        .putObject("phone_numbers", new JsonObject()
                                .putString("home", "617.619.1000")
                                .putString("work", "617.619.1001")
                                .putString("mobile", "617.619.1001")
                        )
                        .putArray("email_addresses", new JsonArray()
                                .addString("bob.smith@ef.com")
                                .addString("bob.smith@hotmail.com")
                                .addString("bob.smith@gmail.com")
                        )
                );

        JsonObject address1 = new JsonObject()
                .putString("schema", keyspace)
                .putString("table", addressTable)
                .putObject(SchemaBuilder.JSON_FIELD_SYS_FIELDS, new JsonObject()
                        .putString(SchemaBuilder.JSON_FIELD_ID, address_id1.toString())
                        .putArray(SchemaBuilder.JSON_FIELD_ACL, acl)
                        .putString(SchemaBuilder.JSON_FIELD_TYPE, addressType)
                        .putNumber(SchemaBuilder.JSON_FIELD_VERSION, 1)
                )
                .putObject(SchemaBuilder.JSON_FIELD_FIELDS, new JsonObject()
                        .putString("address1", "1 Education St")
                        .putString("city", "Cambridge")
                        .putString("state", "MA")
                        .putString("postal_code", "02111")
                        .putString("country", "USA")
                );

        JsonObject address2 = new JsonObject()
                .putString("schema", keyspace)
                .putString("table", addressTable)
                .putObject(SchemaBuilder.JSON_FIELD_SYS_FIELDS, new JsonObject()
                        .putString(SchemaBuilder.JSON_FIELD_ID, address_id2.toString())
                        .putArray(SchemaBuilder.JSON_FIELD_ACL, acl)
                        .putString(SchemaBuilder.JSON_FIELD_TYPE, "com.englishtown.test.Address")
                        .putNumber(SchemaBuilder.JSON_FIELD_VERSION, 1)
                )
                .putObject(SchemaBuilder.JSON_FIELD_FIELDS, new JsonObject()
                        .putString("address1", "258 Tongren Rd")
                        .putString("city", "Shanghai")
                        .putString("postal_code", "200040")
                        .putString("country", "CN")
                );

        JsonObject address3 = new JsonObject()
                .putString("schema", keyspace)
                .putString("table", addressTable)
                .putObject(SchemaBuilder.JSON_FIELD_SYS_FIELDS, new JsonObject()
                        .putString(SchemaBuilder.JSON_FIELD_ID, address_id3.toString())
                        .putArray(SchemaBuilder.JSON_FIELD_ACL, acl)
                        .putString(SchemaBuilder.JSON_FIELD_TYPE, "com.englishtown.test.Address")
                        .putNumber(SchemaBuilder.JSON_FIELD_VERSION, 1)
                )
                .putObject(SchemaBuilder.JSON_FIELD_FIELDS, new JsonObject()
                        .putString("address1", " 22 Chelsea Manor St")
                        .putString("city", ", London")
                        .putString("postal_code", "SW3 5RL")
                        .putString("country", "UK")
                );

        return new JsonArray()
                .addObject(member)
                .addObject(profile)
                .addObject(address1)
                .addObject(address2)
                .addObject(address3);

    }

    private JsonObject createSchemas() {

        JsonObject schemas = new JsonObject();

        schemas.putObject(memberType, new JsonObject()
                .putArray(SchemaBuilder.JSON_FIELD_SYS_FIELDS, new JsonArray()
                        .addObject(new JsonObject()
                                .putString("name", "id")
                                .putString("type", UUID.class.getName()))
                        .addObject(new JsonObject()
                                .putString("name", "version")
                                .putString("type", Integer.class.getName()))
                        .addObject(new JsonObject()
                                .putString("name", "acl")
                                .putString("type", Set.class.getName())
                                .putArray("typeArgs", new JsonArray().addString(UUID.class.getName())))
                        .addObject(new JsonObject()
                                .putString("name", "type")
                                .putString("type", String.class.getName()))
                        .addObject(new JsonObject()
                                .putString("name", "update_date")
                                .putString("type", Date.class.getName()))
                )
                .putArray(SchemaBuilder.JSON_FIELD_FIELDS, new JsonArray()
                        .addObject(new JsonObject()
                                .putString("name", "username")
                                .putString("type", String.class.getName()))
                        .addObject(new JsonObject()
                                .putString("name", "profile")
                                .putString("type", "EntityRef"))
                        .addObject(new JsonObject()
                                .putString("name", "addresses")
                                .putString("type", Set.class.getName())
                                .putArray("typeArgs", new JsonArray().addString("EntityRef")))
                ));

        schemas.putObject(profileType, new JsonObject()
                .putArray(SchemaBuilder.JSON_FIELD_SYS_FIELDS, new JsonArray()
                        .addObject(new JsonObject()
                                .putString("name", "id")
                                .putString("type", UUID.class.getName()))
                        .addObject(new JsonObject()
                                .putString("name", "version")
                                .putString("type", Integer.class.getName()))
                        .addObject(new JsonObject()
                                .putString("name", "acl")
                                .putString("type", Set.class.getName())
                                .putArray("typeArgs", new JsonArray().addString(UUID.class.getName())))
                        .addObject(new JsonObject()
                                .putString("name", "type")
                                .putString("type", String.class.getName()))
                        .addObject(new JsonObject()
                                .putString("name", "update_date")
                                .putString("type", Date.class.getName()))
                )
                .putArray(SchemaBuilder.JSON_FIELD_FIELDS, new JsonArray()
                        .addObject(new JsonObject()
                                .putString("name", "first_name")
                                .putString("type", String.class.getName()))
                        .addObject(new JsonObject()
                                .putString("name", "last_name")
                                .putString("type", String.class.getName()))
                        .addObject(new JsonObject()
                                .putString("name", "phone_numbers")
                                .putString("type", Map.class.getName())
                                .putArray("typeArgs", new JsonArray().addString(String.class.getName()).addString(String.class.getName())))
                        .addObject(new JsonObject()
                                .putString("name", "email_addresses")
                                .putString("type", Set.class.getName())
                                .putArray("typeArgs", new JsonArray().addString(String.class.getName())))
                ));

        schemas.putObject(addressType, new JsonObject()
                .putArray(SchemaBuilder.JSON_FIELD_SYS_FIELDS, new JsonArray()
                        .addObject(new JsonObject()
                                .putString("name", "id")
                                .putString("type", UUID.class.getName()))
                        .addObject(new JsonObject()
                                .putString("name", "version")
                                .putString("type", Integer.class.getName()))
                        .addObject(new JsonObject()
                                .putString("name", "acl")
                                .putString("type", Set.class.getName())
                                .putArray("typeArgs", new JsonArray().addString(UUID.class.getName())))
                        .addObject(new JsonObject()
                                .putString("name", "type")
                                .putString("type", String.class.getName()))
                        .addObject(new JsonObject()
                                .putString("name", "update_date")
                                .putString("type", Date.class.getName()))
                )
                .putArray(SchemaBuilder.JSON_FIELD_FIELDS, new JsonArray()
                        .addObject(new JsonObject()
                                .putString("name", "address1")
                                .putString("type", String.class.getName()))
                        .addObject(new JsonObject()
                                .putString("name", "address2")
                                .putString("type", String.class.getName()))
                        .addObject(new JsonObject()
                                .putString("name", "city")
                                .putString("type", String.class.getName()))
                        .addObject(new JsonObject()
                                .putString("name", "state")
                                .putString("type", String.class.getName()))
                        .addObject(new JsonObject()
                                .putString("name", "postal_code")
                                .putString("type", String.class.getName()))
                        .addObject(new JsonObject()
                                .putString("name", "country")
                                .putString("type", String.class.getName()))
                ));

        return schemas;

    }

}
