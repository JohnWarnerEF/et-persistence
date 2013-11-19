package com.englishtown.vertx.persistence.integration;

import com.englishtown.persistence.*;
import com.englishtown.persistence.impl.*;
import com.englishtown.promises.Promise;
import com.englishtown.promises.Runnable;
import com.englishtown.promises.Value;
import com.englishtown.vertx.persistence.Member;
import com.englishtown.vertx.persistence.MessageBuilder;
import com.englishtown.vertx.persistence.MessageReader;
import com.englishtown.vertx.persistence.impl.DefaultMessageBuilder;
import com.englishtown.vertx.persistence.impl.DefaultMessageReader;
import com.englishtown.vertx.persistence.impl.DefaultPersistenceService;
import com.englishtown.vertx.persistence.impl.DefaultSchemaCache;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.vertx.testtools.VertxAssert.*;

/**
 * Basic integration test
 */
public class CassandraIntegrationTest extends TestVerticle {

    String address = "et.persistence.cassandra";
    DefaultPersistenceService service;
    LoadedPersistentMapFactory dataMapFactory;
    MessageBuilder messageBuilder;
    MessageReader messageReader;

    String id1 = "747bce60-3c27-468f-be65-68778097b13b";

    @Test
    public void testStore() {

        Member member = Member.createInstance();
        member.getSysFields().setId(id1);

        List<PersistentMap> objects = new ArrayList<>();
        objects.add(member);

        service.store(objects).then(
                new Runnable<Promise<StoreResult, Void>, StoreResult>() {
                    @Override
                    public Promise<StoreResult, Void> run(StoreResult value) {
                        testComplete();
                        return null;
                    }
                },
                new Runnable<Promise<StoreResult, Void>, Value<StoreResult>>() {
                    @Override
                    public Promise<StoreResult, Void> run(Value<StoreResult> value) {
                        fail();
                        return null;
                    }
                }
        );

    }

    @Test
    public void testLoad() {

        List<EntityKey> keys = new ArrayList<>();
        keys.add(new DefaultEntityKey(id1, Member.class));

        service.load(keys).then(
                new Runnable<Promise<LoadResult, Void>, LoadResult>() {
                    @Override
                    public Promise<LoadResult, Void> run(LoadResult value) {
                        assertEquals(5, value.getSucceeded().size());
                        testComplete();
                        return null;
                    }
                },
                new Runnable<Promise<LoadResult, Void>, Value<LoadResult>>() {
                    @Override
                    public Promise<LoadResult, Void> run(Value<LoadResult> value) {
                        fail();
                        return null;
                    }
                }
        );

    }

    @Override
    public void start() {

        container.config().putString(DefaultPersistenceService.CONFIG_ADDRESS, address);

        //noinspection unchecked
        Provider<StoreResult> storeResultProvider = mock(Provider.class);
        when(storeResultProvider.get()).thenReturn(new DefaultStoreResult());
        //noinspection unchecked
        Provider<LoadResult> loadResultProvider = mock(Provider.class);
        when(loadResultProvider.get()).thenReturn(new DefaultLoadResult());

        EntityMetadataService metadataService = new DefaultEntityMetadataService();
        dataMapFactory = new DefaultLoadedPersistentMapFactory();
        messageBuilder = new DefaultMessageBuilder(metadataService, new DefaultIDGenerator(), new DefaultSchemaCache());
        messageReader = new DefaultMessageReader(dataMapFactory, storeResultProvider, loadResultProvider, metadataService);

        service = new DefaultPersistenceService(vertx, container, messageBuilder, messageReader);

        super.start();
    }

    /**
     * Override this method to signify that start is complete sometime _after_ the start() method has returned
     * This is useful if your verticle deploys other verticles or modules and you don't want this verticle to
     * be considered started until the other modules and verticles have been started.
     *
     * @param startedResult When you are happy your verticle is started set the result
     */
    @Override
    public void start(final Future<Void> startedResult) {

        JsonObject config = new JsonObject().putString("address", address);

        container.deployModule("com.englishtown~vertx-mod-persistence-cassandra~1.0.0-SNAPSHOT", config, new Handler<AsyncResult<String>>() {
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
}
