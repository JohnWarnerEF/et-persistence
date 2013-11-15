package com.englishtown.persistence.impl;

import com.englishtown.persistence.EntityRefInfo;
import com.englishtown.persistence.ReflectionUtils;
import com.englishtown.persistence.TypeInfo;

import javax.persistence.Table;
import java.lang.reflect.Member;

/**
 * Base implementation of {@link com.englishtown.persistence.EntityRefInfo}
 */
public abstract class EntityRefInfoBase implements EntityRefInfo {

    private String name;
    private String table;
    private String schema;
    private Member member;
    private TypeInfo typeInfo;

    protected EntityRefInfoBase(String name, Member member, TypeInfo typeInfo) {
        this.name = name;
        this.member = member;
        this.typeInfo = typeInfo;
        init();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTable() {
        return table;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public TypeInfo getTypeInfo() {
        return typeInfo;
    }

    protected Member getMember() {
        return member;
    }

    private void init() {

        Table table;

        if (typeInfo.getTypeArguments().length == 1) {
            Class<?> clazz = (Class<?>) typeInfo.getTypeArguments()[0];
            table = ReflectionUtils.getAnnotation(Table.class, clazz);
        } else {
            table = ReflectionUtils.getAnnotation(Table.class, typeInfo.getRawType());
        }


        if (table != null) {
            this.table = table.name();
            this.schema = table.schema();
        }
    }

}
