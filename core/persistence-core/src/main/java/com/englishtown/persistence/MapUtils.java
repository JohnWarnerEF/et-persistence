package com.englishtown.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 *
 */
public class MapUtils {

    private static final Logger logger = LoggerFactory.getLogger(MapUtils.class);

    @SuppressWarnings("unchecked")
    public static <T> T get(String key, Map<String, Object> map) {
        try {
            Object val = map.get(key);
            return (T) val;
        } catch (Throwable t) {
            logger.error("Error casting map value for key " + key, t);
            return null;
        }
    }

}
