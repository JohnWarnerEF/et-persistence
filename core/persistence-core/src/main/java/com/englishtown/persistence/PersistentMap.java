package com.englishtown.persistence;

import java.util.Map;

/**
 * Persistable object map
 */
public interface PersistentMap {

    /**
     * The persistent map's system fields
     *
     * @return
     */
    SysFields getSysFields();

    /**
     * The persistent map's data
     *
     * @return
     */
    Map<String, Object> getMap();

}
