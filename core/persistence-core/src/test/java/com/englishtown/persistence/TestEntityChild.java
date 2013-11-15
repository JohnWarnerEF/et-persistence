package com.englishtown.persistence;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Map;

/**
 * Child entity for unit tests
 */
@Entity()
@Table(name = "child", schema = "et_core_test")
public class TestEntityChild implements PersistentMap {

    private SysFields sysFields;
    private Map<String, Object> map;

    public TestEntityChild(PersistentMap persistentMap) {
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
