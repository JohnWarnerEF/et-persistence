package com.englishtown.persistence.impl;

import com.englishtown.persistence.PersistentMap;
import com.englishtown.persistence.SysFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link PersistentMap} used for loading
 */
public class DefaultPersistentMap implements PersistentMap {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPersistentMap.class.getName());

    private SysFields sysFields;
    private Map<String, Object> map;

    public DefaultPersistentMap() {
        sysFields = new DefaultSysFields();
        map = new HashMap<>();
    }

    public DefaultPersistentMap(SysFields sysFields, Map<String, Object> map) {
        this.sysFields = sysFields;
        this.map = map;
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
