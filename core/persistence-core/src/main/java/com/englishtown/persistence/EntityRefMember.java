package com.englishtown.persistence;

import java.lang.reflect.Member;

/**
 * Represents an entity ref member (field or getter method)
 */
public interface EntityRefMember {

    String getName();

    Member getMember();

    TypeInfo getTypeInfo();

    Object getValue(Object entity);

}
