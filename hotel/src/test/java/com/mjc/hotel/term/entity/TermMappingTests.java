package com.mjc.hotel.term.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TermMappingTests {

    private static final String CLASS_NAME = "com.mjc.hotel.term.entity.Term";

    @Test
    void mapsTermsTableAndIdentityPrimaryKey() throws Exception {
        Class<?> type = type();
        assertNotNull(type.getAnnotation(Entity.class));
        assertEquals("terms", type.getAnnotation(Table.class).name());

        Field id = field("termId");
        assertNotNull(id.getAnnotation(Id.class));
        assertEquals(GenerationType.IDENTITY, id.getAnnotation(GeneratedValue.class).strategy());
        assertEquals("term_id", id.getAnnotation(Column.class).name());
    }

    @Test
    void mapsTermColumnsFromErd() throws Exception {
        assertStringColumn("termType", "term_type", 30);
        assertStringColumn("title", "title", 100);
        assertStringColumn("version", "version", 20);
        assertColumn("isRequired", "is_required", Boolean.class);
        assertColumn("effectiveAt", "effective_at", LocalDateTime.class);
    }

    private static Class<?> type() throws ClassNotFoundException {
        return Class.forName(CLASS_NAME);
    }

    private static Field field(String name) throws Exception {
        return type().getDeclaredField(name);
    }

    private static void assertStringColumn(String fieldName, String columnName,
                                           int length) throws Exception {
        Field field = field(fieldName);
        Column column = field.getAnnotation(Column.class);
        assertEquals(String.class, field.getType());
        assertEquals(columnName, column.name());
        assertEquals(length, column.length());
    }

    private static void assertColumn(String fieldName, String columnName,
                                     Class<?> fieldType) throws Exception {
        Field field = field(fieldName);
        assertEquals(fieldType, field.getType());
        assertEquals(columnName, field.getAnnotation(Column.class).name());
    }
}
