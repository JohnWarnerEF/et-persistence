package com.englishtown.persistence.impl;

import com.englishtown.persistence.EntityRefMember;
import com.englishtown.persistence.TypeInfo;

import java.lang.reflect.Member;

/**
 * Base implementation of {@link com.englishtown.persistence.EntityRefInfo}
 */
public abstract class EntityRefMemberBase implements EntityRefMember {

    private String name;
    private Member member;
    private TypeInfo typeInfo;

    protected EntityRefMemberBase(String name, Member member, TypeInfo typeInfo) {
        this.name = name;
        this.member = member;
        this.typeInfo = typeInfo;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TypeInfo getTypeInfo() {
        return typeInfo;
    }

    @Override
    public Member getMember() {
        return member;
    }

}
