package com.mjc.hotel.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MemberMappingTests {

    private static final String MEMBER_CLASS_NAME = "com.mjc.hotel.member.entity.Member";

    @Test
    void mapsMemberEntityToMembersTable() throws Exception {
        Class<?> memberClass = memberClass();

        assertNotNull(memberClass.getAnnotation(Entity.class));
        assertEquals("members", memberClass.getAnnotation(Table.class).name());
    }

    @Test
    void usesIdentityGeneratedMemberId() throws Exception {
        Field memberId = field("memberId");

        assertNotNull(memberId.getAnnotation(Id.class));
        assertEquals(GenerationType.IDENTITY,
                memberId.getAnnotation(GeneratedValue.class).strategy());
        assertEquals("member_id", memberId.getAnnotation(Column.class).name());
    }

    @Test
    void mapsStringColumnLengthsFromErd() throws Exception {
        assertColumn("name", "name", 50);
        assertColumn("phone", "phone", 20);
        assertColumn("email", "email", 255);
    }

    @Test
    void mapsStatusAndRoleAsStringEnums() throws Exception {
        assertEnumField("status", "status", "MemberStatus");
        assertEnumField("role", "role", "MemberRole");

        assertArrayEquals(new String[]{"ACTIVE", "STOP", "DELETED"}, enumNames("MemberStatus"));
        assertArrayEquals(new String[]{"ADMIN", "USER"}, enumNames("MemberRole"));
    }

    @Test
    void mapsVerificationFlagsAsBooleans() throws Exception {
        assertBooleanColumn("emailVerified", "email_verified");
        assertBooleanColumn("phoneVerified", "phone_verified");
    }

    @Test
    void mapsLifecycleTimestamps() throws Exception {
        Field createdAt = field("createdAt");
        assertEquals(LocalDateTime.class, createdAt.getType());
        assertEquals("created_at", createdAt.getAnnotation(Column.class).name());
        assertFalse(createdAt.getAnnotation(Column.class).updatable());
        assertNotNull(createdAt.getAnnotation(CreationTimestamp.class));

        Field updatedAt = field("updatedAt");
        assertEquals(LocalDateTime.class, updatedAt.getType());
        assertEquals("updated_at", updatedAt.getAnnotation(Column.class).name());
        assertNotNull(updatedAt.getAnnotation(UpdateTimestamp.class));

        Field deletedAt = field("deletedAt");
        assertEquals(LocalDateTime.class, deletedAt.getType());
        assertEquals("deleted_at", deletedAt.getAnnotation(Column.class).name());
    }

    private static Class<?> memberClass() throws ClassNotFoundException {
        return Class.forName(MEMBER_CLASS_NAME);
    }

    private static Field field(String name) throws Exception {
        return memberClass().getDeclaredField(name);
    }

    private static void assertColumn(String fieldName, String columnName, int length) throws Exception {
        Column column = field(fieldName).getAnnotation(Column.class);
        assertEquals(columnName, column.name());
        assertEquals(length, column.length());
    }

    private static void assertEnumField(String fieldName, String columnName, String typeName) throws Exception {
        Field field = field(fieldName);
        assertEquals(typeName, field.getType().getSimpleName());
        assertEquals(columnName, field.getAnnotation(Column.class).name());
        assertEquals(EnumType.STRING, field.getAnnotation(Enumerated.class).value());
    }

    private static String[] enumNames(String simpleName) throws Exception {
        Object[] constants = Class.forName("com.mjc.hotel.member.entity." + simpleName).getEnumConstants();
        String[] names = new String[constants.length];
        for (int index = 0; index < constants.length; index++) {
            names[index] = ((Enum<?>) constants[index]).name();
        }
        return names;
    }

    private static void assertBooleanColumn(String fieldName, String columnName) throws Exception {
        Field field = field(fieldName);
        assertEquals(Boolean.class, field.getType());
        assertEquals(columnName, field.getAnnotation(Column.class).name());
    }
}
