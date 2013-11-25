package com.englishtown.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 *
 */
public class MapUtils {

    private static final Logger logger = LoggerFactory.getLogger(MapUtils.class);

    public static <T> T get(String key, Map<String, Object> map, Class<T> clazz) {
        T val = get(key, map);
        if (val != null) {
            if (!clazz.isAssignableFrom(val.getClass())) {
                logger.error("Map value {} ({}) for key '{}' is not assignable to {}.", val, val.getClass(), key, clazz);
                return null;
            }
        }
        return val;
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key, Map<String, Object> map) {
        Object val = map.get(key);
        return (T) val;
    }

}
