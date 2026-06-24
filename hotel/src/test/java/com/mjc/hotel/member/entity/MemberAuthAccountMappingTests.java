package com.mjc.hotel.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MemberAuthAccountMappingTests {

    private static final String CLASS_NAME = "com.mjc.hotel.member.entity.MemberAuthAccount";

    @Test
    void mapsAuthAccountTableAndIdentityPrimaryKey() throws Exception {
        Class<?> type = type();
        assertNotNull(type.getAnnotation(Entity.class));
        assertEquals("member_auth_accounts", type.getAnnotation(Table.class).name());
        Field id = field("authAccountId");
        assertNotNull(id.getAnnotation(Id.class));
        assertEquals(GenerationType.IDENTITY, id.getAnnotation(GeneratedValue.class).strategy());
        assertEquals("auth_account_id", id.getAnnotation(Column.class).name());
    }

    @Test
    void mapsMemberAsLazyManyToOne() throws Exception {
        Field member = field("member");
        assertEquals(Member.class, member.getType());
        assertEquals(FetchType.LAZY, member.getAnnotation(ManyToOne.class).fetch());
        assertEquals("member_id", member.getAnnotation(JoinColumn.class).name());
    }

    @Test
    void mapsAuthAccountValueColumns() throws Exception {
        assertStringColumn("provider", "provider", 20);
        assertStringColumn("providerUserId", "provider_user_id", 255);
        assertStringColumn("passwordHash", "password_hash", 255);
        assertColumn("lastLoginAt", "last_login_at", LocalDateTime.class);

        Field createdAt = field("createdAt");
        assertEquals(LocalDateTime.class, createdAt.getType());
        assertEquals("created_at", createdAt.getAnnotation(Column.class).name());
        assertFalse(createdAt.getAnnotation(Column.class).updatable());
        assertNotNull(createdAt.getAnnotation(CreationTimestamp.class));
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
