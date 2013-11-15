package com.englishtown.persistence;

import com.google.inject.TypeLiteral;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link TypeInfo}
 */
public class TypeInfoTest {

    @Test
    public void testTypeInfo_Int() throws Exception {

        TypeInfo info = new TypeInfo(int.class);

        assertEquals(int.class, info.getRawType());
        assertEquals(0, info.getTypeArguments().length);

    }

    @Test
    public void testTypeInfo_Integer() throws Exception {

        TypeInfo info = new TypeInfo(Integer.class);

        assertEquals(Integer.class, info.getRawType());
        assertEquals(0, info.getTypeArguments().length);

    }

    @Test
    public void testTypeInfo_String() throws Exception {

        TypeInfo info = new TypeInfo(String.class);

        assertEquals(String.class, info.getRawType());
        assertEquals(0, info.getTypeArguments().length);

    }

    @Test
    public void testTypeInfo_StringList() throws Exception {

        TypeLiteral<List<String>> literal = new TypeLiteral<List<String>>() {
        };
        TypeInfo info = new TypeInfo(literal.getType());

        assertEquals(List.class, info.getRawType());
        assertEquals(1, info.getTypeArguments().length);
        assertEquals(String.class, info.getTypeArguments()[0]);

    }

    @Test
    public void testTypeInfo_StringIntegerMap() throws Exception {

        TypeLiteral<Map<String, Integer>> literal = new TypeLiteral<Map<String, Integer>>() {
        };
        TypeInfo info = new TypeInfo(literal.getType());

        assertEquals(Map.class, info.getRawType());
        assertEquals(2, info.getTypeArguments().length);
        assertEquals(String.class, info.getTypeArguments()[0]);
        assertEquals(Integer.class, info.getTypeArguments()[1]);

    }

}
