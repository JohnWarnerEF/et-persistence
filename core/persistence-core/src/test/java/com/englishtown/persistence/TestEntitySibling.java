package com.englishtown.persistence;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Map;

/**
 * Sibling entity for unit tests
 */
@Entity()
@Table(name = "sibling", schema = "et_core_test")
public class TestEntitySibling implements PersistentMap {

    private SysFields sysFields;
    private Map<String, Object> map;

    public TestEntitySibling(PersistentMap persistentMap) {
        sysFields = persistentMap.getSysFields();
        map = persistentMap.getMap();
    }

    /**
     * The persistent map's sysFields
     *
     * @return
     */
    @Override
    public SysFields getSysFields() {
        return sysFields;
    }

    /**
     * The persistent map's data
     *
     * @return
     */
    @Override
    public Map<String, Object> getMap() {
        return map;
    }
}
