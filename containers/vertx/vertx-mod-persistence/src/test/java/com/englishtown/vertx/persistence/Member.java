package com.englishtown.vertx.persistence;

import com.englishtown.persistence.EntityRef;
import com.englishtown.persistence.MapUtils;
import com.englishtown.persistence.PersistentMap;
import com.englishtown.persistence.SysFields;
import com.englishtown.persistence.acl.AccessControlList;
import com.englishtown.persistence.impl.DefaultPersistentMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

/**
 * Member entity for tests
 */
@Entity(name = "com.englishtown.vertx.persistence.Member")
@Table(name = "member", schema = "vertx_mod_persistence_test")
public class Member implements PersistentMap {

    private SysFields sysFields;
    private Map<String, Object> map;

    private Profile profile;
    private Set<Address> addresses;

    public Member(PersistentMap persistentMap) {
        this.sysFields = persistentMap.getSysFields();
        this.map = persistentMap.getMap();
        this.addresses = new HashSet<>();
    }

    @Override
    public SysFields getSysFields() {
        return sysFields;
    }

    @Override
    public Map<String, Object> getMap() {
        return map;
    }

    @Column(name = "username")
    public String getUsername() {
        return MapUtils.get("username", map);
    }

    public Member setUsername(String value) {
        map.put("username", value);
        return this;
    }

    @EntityRef(name = "profile")
    public Profile getProfile() {
        return profile;
    }

    public Member setProfile(Profile value) {
        this.profile = value;
        return this;
    }

    @EntityRef(name = "addresses")
    public Set<Address> getAddresses() {
        return addresses;
    }

    public Member setAddresses(Set<Address> value) {
        this.addresses = value;
        return this;
    }

    public static Member createInstance() {

        AccessControlList acl = new AccessControlList();
        acl.add(UUID.randomUUID());
        acl.add(UUID.randomUUID());
        acl.add(UUID.randomUUID());

        Member member = new Member(new DefaultPersistentMap())
                .setUsername("test.user");

        member.getSysFields().setACL(acl);

        Set<String> emails = new TreeSet<>();
        emails.add("test.user@ef.com");
        emails.add("test.user@gmail.com");
        emails.add("test.user@hotmail.com");

        Map<String, String> phoneNumbers = new HashMap<>();
        phoneNumbers.put("h", "617.619.1000");
        phoneNumbers.put("w", "617.619.1001");
        phoneNumbers.put("c", "617.619.1002");

        Profile profile = new Profile(new DefaultPersistentMap())
                .setAge(18)
                .setFirstName("test")
                .setLastName("user")
                .setEmailAddresses(emails)
                .setPhoneNumbers(phoneNumbers);

        member.setProfile(profile);

        member.getAddresses().add(new Address(new DefaultPersistentMap())
                .setAddress1("EF Education")
                .setAddress2("1 Education St")
                .setCity("Cambridge")
                .setState("MA")
                .setPostalCode("02111")
                .setCountry("USA")
        );
        member.getAddresses().add(new Address(new DefaultPersistentMap())
                .setAddress1("EF English First")
                .setAddress2("258 Tongren Rd")
                .setCity("Shanghai")
                .setPostalCode("200040")
                .setCountry("CN")
        );
        member.getAddresses().add(new Address(new DefaultPersistentMap())
                .setAddress1("22 Chelsea Manor St")
                .setCity("London")
                .setPostalCode("SW3 5RL")
                .setCountry("UK")
        );

        return member;
    }

}
