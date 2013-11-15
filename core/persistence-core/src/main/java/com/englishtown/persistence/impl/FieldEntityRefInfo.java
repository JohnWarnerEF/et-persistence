package com.englishtown.persistence.impl;

import com.englishtown.persistence.TypeInfo;

import java.lang.reflect.Field;

/**
 * {@link com.englishtown.persistence.EntityRefInfo} implementation for Fields
 */
public class FieldEntityRefInfo extends EntityRefInfoBase {

    private Field field;

    protected FieldEntityRefInfo(String name, Field field, TypeInfo typeInfo) {
        super(name, field, typeInfo);
        this.field = field;
        field.setAccessible(true);
    }

    @Override
    public Object getValue(Object entity) {
        try {
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
