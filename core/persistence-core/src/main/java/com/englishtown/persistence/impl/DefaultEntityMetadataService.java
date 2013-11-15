package com.englishtown.persistence.impl;

import com.englishtown.persistence.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link EntityMetadataService}
 */
public class DefaultEntityMetadataService implements EntityMetadataService {

    private Map<Class<?>, EntityMetadata> metadataMap;

    public DefaultEntityMetadataService() {
        this.metadataMap = new HashMap<>();
    }

    @Override
    public EntityMetadata get(Class<? extends PersistentMap> clazz) {

        EntityMetadata result = metadataMap.get(clazz);

        if (result == null) {
            result = readMetadata(clazz);
            metadataMap.put(clazz, result);
        }

        return result;
    }

    protected EntityMetadata readMetadata(Class<?> clazz) {

        EntityMetadata metadata = new DefaultEntityMetadata();

        Entity entity = ReflectionUtils.getAnnotation(Entity.class, clazz);
        if (entity != null && entity.name() != null && !entity.name().isEmpty()) {
            metadata.setType(entity.name());
        } else {
            metadata.setType(clazz.getName());
        }

        Table table = ReflectionUtils.getAnnotation(Table.class, clazz);
        if (table != null) {
            metadata.setTable(table.name())
                    .setSchema(table.schema());
        }

        Map<String, TypeInfo> schema = metadata.getFields();
        List<Class<?>> hierarchy = getHierarchy(clazz);

        // Go in reverse order to allow overrides to win
        for (int i = hierarchy.size() - 1; i >= 0; i--) {
            Class<?> c = hierarchy.get(i);
            readMethodAnnotations(metadata, schema, c);
            readFieldAnnotations(metadata, schema, c);
        }

        return metadata;
    }

    private void readMethodAnnotations(EntityMetadata metadata, Map<String, TypeInfo> schema, Class<?> c) {
        for (Method method : c.getDeclaredMethods()) {
            Column column = method.getAnnotation(Column.class);
            EntityRef ref = method.getAnnotation(EntityRef.class);

            if (column == null && ref == null) {
                continue;
            }

            TypeInfo typeInfo = new TypeInfo(method.getGenericReturnType());

            if (column != null) {
                checkName(column.name(), method, column);
                schema.put(column.name(), typeInfo);
            }

            if (ref != null) {
                checkName(ref.name(), method, ref);
                EntityRefInfo refInfo = new MethodEntityRefInfo(ref.name(), method, typeInfo);
                addEntityRef(metadata, refInfo);
            }
        }
    }

    private void readFieldAnnotations(EntityMetadata metadata, Map<String, TypeInfo> schema, Class<?> c) {
        for (Field field : c.getDeclaredFields()) {
            Column column = field.getAnnotation(Column.class);
            EntityRef ref = field.getAnnotation(EntityRef.class);

            if (column == null && ref == null) {
                continue;
            }

            TypeInfo typeInfo = new TypeInfo(field.getGenericType());

            if (column != null) {
                checkName(column.name(), field, column);
                schema.put(column.name(), typeInfo);
            }

            if (ref != null) {
                checkName(ref.name(), field, ref);
                EntityRefInfo refInfo = new FieldEntityRefInfo(ref.name(), field, typeInfo);
                addEntityRef(metadata, refInfo);
            }
        }
    }

    private List<Class<?>> getHierarchy(Class<?> clazz) {
        List<Class<?>> list = new ArrayList<>();
        Class<?> current = clazz;
        while (current != Object.class) {
            list.add(current);
            current = current.getSuperclass();
        }
        return list;
    }

    private void addEntityRef(EntityMetadata metadata, EntityRefInfo ref) {
        metadata.getEntityRefs().put(ref.getName(), ref);
    }

    private void checkName(String name, Member member, Annotation annotation) {
        if (name == null || name.isEmpty()) {
            throw new IllegalStateException(member.getDeclaringClass().getName() + " has member " + member.getName()
                    + " with annotation " + annotation.annotationType().getName() + ", but it is missing a name.");
        }
    }

}
