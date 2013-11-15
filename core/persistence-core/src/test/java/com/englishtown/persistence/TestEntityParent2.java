package com.englishtown.persistence;

import javax.persistence.Column;
import java.util.Map;

/**
 * Extends parent entity for unit tests
 */
public class TestEntityParent2 extends TestEntityParent {

    private Map<String, Object> map;

    public TestEntityParent2(PersistentMap persistentMap) {
        super(persistentMap);
        map = persistentMap.getMap();
    }

    @Column(name = "string_val2")
    public String getString2() {
        return MapUtils.get("string_val2", map);
    }

    public TestEntityParent2 setString2(String value) {
        map.put("string_val2", value);
        return this;
    }

}
