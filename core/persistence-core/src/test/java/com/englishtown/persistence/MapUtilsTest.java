package com.englishtown.persistence;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

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

        result = MapUtils.get(key, map, String.class);
        assertEquals(value, result);

        Object result2 = MapUtils.get(key, map, Object.class);
        assertEquals(value, result2);

    }

    @Test
    public void testGet_IllegalCast() throws Exception {

        String result;
        int value = 3;
        map.put(key, value);

        try {
            //noinspection UnusedAssignment
            result = MapUtils.get(key, map);
            fail();
        } catch (ClassCastException e) {
            // Expected
        }

        result = MapUtils.get(key, map, String.class);
        assertNull(result);

    }

}
