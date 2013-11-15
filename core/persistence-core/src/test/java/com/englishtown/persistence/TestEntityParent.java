package com.englishtown.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test entity with all supported data types
 */
@Entity(name = "com.englishtown.persistence.TestEntityParent")
@Table(name = "parent", schema = "et_core_test")
public class TestEntityParent implements PersistentMap {

    private SysFields sysFields;
    private Map<String, Object> map;

    private TestEntitySibling sibling;
    private Set<TestEntityChild> children;

    public TestEntityParent(PersistentMap persistentMap) {
        this.sysFields = persistentMap.getSysFields();
        this.map = persistentMap.getMap();
    }

    @Column(name = "string_field")
    private String stringField;

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


    @Column(name = "string_val")
    public String getString() {
        return MapUtils.get("string_val", map);
    }

    public TestEntityParent setString(String value) {
        map.put("string_val", value);
        return this;
    }

    @Column(name = "int_val")
    public int getInt() {
        return MapUtils.get("int_val", map);
    }

    public TestEntityParent setInt(int value) {
        map.put("int_val", value);
        return this;
    }

    @Column(name = "integer_val")
    public Integer getInteger() {
        return MapUtils.get("integer_val", map);
    }

    public TestEntityParent setInteger(Integer value) {
        map.put("integer_val", value);
        return this;
    }

    @Column(name = "long_val")
    public long getLong() {
        return MapUtils.get("long_val", map);
    }

    public TestEntityParent setLong(long value) {
        map.put("long_val", value);
        return this;
    }


    // Collection types

    @Column(name = "set_string")
    public Set<String> getSetString() {
        return MapUtils.get("set_string", map);
    }

    public TestEntityParent setSetString(Set<String> value) {
        map.put("set_string", value);
        return this;
    }

    @Column(name = "list_string")
    public List<String> getListString() {
        return MapUtils.get("list_string", map);
    }

    public TestEntityParent setListString(List<String> value) {
        map.put("list_string", value);
        return this;
    }

    @Column(name = "map_string")
    public Map<String, Integer> getMapString() {
        return MapUtils.get("map_string", map);
    }

    public TestEntityParent setMapString(Map<String, Integer> value) {
        map.put("map_string", value);
        return this;
    }


    // Entity refs

    @EntityRef(name = "sibling")
    public TestEntitySibling getSibling() {
        return sibling;
    }

    public TestEntityParent setSibling(TestEntitySibling sibling) {
        this.sibling = sibling;
        return this;
    }

    @EntityRef(name = "children")
    public Set<TestEntityChild> getChildren() {
        return children;
    }

    public TestEntityParent setChildren(Set<TestEntityChild> children) {
        this.children = children;
        return this;
    }

}
