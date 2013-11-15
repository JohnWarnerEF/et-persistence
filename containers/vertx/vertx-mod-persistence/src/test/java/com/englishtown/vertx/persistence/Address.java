package com.englishtown.vertx.persistence;

import com.englishtown.persistence.MapUtils;
import com.englishtown.persistence.PersistentMap;
import com.englishtown.persistence.SysFields;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Map;

/**
 * Address entity for tests
 */
@Entity(name = "com.englishtown.vertx.persistence.Address")
@Table(name = "address", schema = "vertx_mod_persistence_test")
public class Address implements PersistentMap {

    private SysFields sysFields;
    private Map<String, Object> map;

    public Address(PersistentMap persistentMap) {
        this.sysFields = persistentMap.getSysFields();
        this.map = persistentMap.getMap();
    }

    @Column(name = "address1")
    public String getAddress1() {
        return MapUtils.get("address1", map);
    }

    public Address setAddress1(String value) {
        map.put("address1", value);
        return this;
    }

    @Column(name = "address2")
    public String getAddress2() {
        return MapUtils.get("address2", map);
    }

    public Address setAddress2(String value) {
        map.put("address2", value);
        return this;
    }

    @Column(name = "city")
    public String getCity() {
        return MapUtils.get("city", map);
    }

    public Address setCity(String value) {
        map.put("city", value);
        return this;
    }

    @Column(name = "state")
    public String getState() {
        return MapUtils.get("state", map);
    }

    public Address setState(String value) {
        map.put("state", value);
        return this;
    }

    @Column(name = "postal_code")
    public String getPostalCode() {
        return MapUtils.get("postal_code", map);
    }

    public Address setPostalCode(String value) {
        map.put("postal_code", value);
        return this;
    }

    @Column(name = "country")
    public String getCountry() {
        return MapUtils.get("country", map);
    }

    public Address setCountry(String value) {
        map.put("country", value);
        return this;
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
