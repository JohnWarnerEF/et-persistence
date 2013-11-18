package com.englishtown.persistence;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for {@link MapUtils}
 */
public class MapUtilsTest {

    Map<String, Object> map = new HashMap<>();
    String key = "test.key";

    @Test
    public void testGet() throws Exception {

        String value = "test.value";
        map.put(key, value);
        String result = MapUtils.get(key, map);
        assertEquals(value, result);

    }

    @Test
    public void testGet_IllegalCast() throws Exception {

        int value = 3;
        map.put(key, value);
        try {
            String result = MapUtils.get(key, map);
            assertNull(result);
        } catch (ClassCastException e) {
            // TODO: How to handle ClassCastException in MapUtils?  Only way is getString(), getInt(), getInteger() etc.?  Or also pass the expected class type through to validate?
        }

    }
}
