package com.englishtown.vertx.persistence;

import com.englishtown.persistence.MapUtils;
import com.englishtown.persistence.PersistentMap;
import com.englishtown.persistence.SysFields;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Map;
import java.util.Set;

/**
 * Profile entity for tests
 * <p/>
 * Note: a name is not specified for the entity annotation, the fully qualified class name should be used.
 */
@Entity
@Table(name = "profile", schema = "vertx_mod_persistence_test")
public class Profile implements PersistentMap {

    private SysFields sysFields;
    private Map<String, Object> map;

    public Profile(PersistentMap persistentMap) {
        this.sysFields = persistentMap.getSysFields();
        this.map = persistentMap.getMap();
    }

    @Override
    public SysFields getSysFields() {
        return sysFields;
    }

    @Override
    public Map<String, Object> getMap() {
        return map;
    }

    @Column(name = "first_name")
    public String getFirstName() {
        return MapUtils.get("first_name", map);
    }

    public Profile setFirstName(String value) {
        map.put("first_name", value);
        return this;
    }

    @Column(name = "last_name")
    public String getLastName() {
        return MapUtils.get("last_name", map);
    }

    public Profile setLastName(String value) {
        map.put("last_name", value);
        return this;
    }

    @Column(name = "email_addresses")
    public Set<String> getEmailAddresses() {
        return MapUtils.get("email_addresses", map);
    }

    public Profile setEmailAddresses(Set<String> value) {
        map.put("email_addresses", value);
        return this;
    }

    @Column(name = "phone_numbers")
    public Map<String, String> getPhoneNumbers() {
        return MapUtils.get("phone_numbers", map);
    }

    public Profile setPhoneNumbers(Map<String, String> value) {
        map.put("phone_numbers", value);
        return this;
    }

    @Column(name = "age")
    public int getAge() {
        return MapUtils.get("age", map);
    }

    public Profile setAge(int value) {
        map.put("age", value);
        return this;
    }

}
