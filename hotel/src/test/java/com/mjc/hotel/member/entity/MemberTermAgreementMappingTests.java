package com.mjc.hotel.member.entity;

import com.mjc.hotel.term.entity.Term;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MemberTermAgreementMappingTests {

    private static final String CLASS_NAME = "com.mjc.hotel.member.entity.MemberTermAgreement";

    @Test
    void mapsAgreementTableAndIdentityPrimaryKey() throws Exception {
        Class<?> type = type();
        assertNotNull(type.getAnnotation(Entity.class));
        assertEquals("member_term_agreements", type.getAnnotation(Table.class).name());
        Field id = field("agreementId");
        assertNotNull(id.getAnnotation(Id.class));
        assertEquals(GenerationType.IDENTITY, id.getAnnotation(GeneratedValue.class).strategy());
        assertEquals("agreement_id", id.getAnnotation(Column.class).name());
    }

    @Test
    void mapsMemberAndTermAsLazyManyToOne() throws Exception {
        assertAssociation("member", Member.class, "member_id");
        assertAssociation("term", Term.class, "term_id");
    }

    @Test
    void mapsAgreementValueColumns() throws Exception {
        assertColumn("isAgreed", "is_agreed", Boolean.class);
        assertColumn("agreedAt", "agreed_at", LocalDateTime.class);
        assertColumn("withdrawnAt", "withdrawn_at", LocalDateTime.class);
    }

    private static Class<?> type() throws ClassNotFoundException {
        return Class.forName(CLASS_NAME);
    }

    private static Field field(String name) throws Exception {
        return type().getDeclaredField(name);
    }

    private static void assertAssociation(String fieldName, Class<?> fieldType,
                                          String joinColumn) throws Exception {
        Field field = field(fieldName);
        assertEquals(fieldType, field.getType());
        assertEquals(FetchType.LAZY, field.getAnnotation(ManyToOne.class).fetch());
        assertEquals(joinColumn, field.getAnnotation(JoinColumn.class).name());
    }

    private static void assertColumn(String fieldName, String columnName,
                                     Class<?> fieldType) throws Exception {
        Field field = field(fieldName);
        assertEquals(fieldType, field.getType());
        assertEquals(columnName, field.getAnnotation(Column.class).name());
    }
}
