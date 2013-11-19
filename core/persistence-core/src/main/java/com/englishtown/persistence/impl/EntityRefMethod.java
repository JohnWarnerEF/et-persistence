package com.englishtown.persistence.impl;

import com.englishtown.persistence.TypeInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * {@link com.englishtown.persistence.EntityRefInfo} implementation for Fields
 */
public class EntityRefMethod extends EntityRefMemberBase {

    private Method method;

    protected EntityRefMethod(String name, Method method, TypeInfo typeInfo) {
        super(name, method, typeInfo);
        this.method = method;
        method.setAccessible(true);
    }

    @Override
    public Object getValue(Object entity) {
        try {
            return method.invoke(entity);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
