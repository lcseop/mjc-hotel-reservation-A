# Member Entity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `회원.png` ERD의 `members` 테이블을 정확히 나타내는 JPA 엔티티와 enum을 추가한다.

**Architecture:** `com.mjc.hotel.member.entity` 패키지가 회원 영속성 모델을 소유한다. `Member`는 테이블과 컬럼 매핑을 명시하고, 상태 및 권한 값은 각각 독립 enum으로 제한하며, Hibernate가 생성·수정 시각을 관리한다.

**Tech Stack:** Java 21, Spring Boot 4, Jakarta Persistence, Hibernate ORM, Lombok, JUnit 5, Gradle

---

### Task 1: 회원 엔티티 매핑

**Files:**
- Create: `hotel/src/test/java/com/mjc/hotel/member/entity/MemberMappingTests.java`
- Create: `hotel/src/main/java/com/mjc/hotel/member/entity/Member.java`
- Create: `hotel/src/main/java/com/mjc/hotel/member/entity/MemberStatus.java`
- Create: `hotel/src/main/java/com/mjc/hotel/member/entity/MemberRole.java`

- [x] **Step 1: 매핑 요구사항을 검증하는 실패 테스트 작성**

```java
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
```

- [x] **Step 2: 테스트가 누락된 엔티티 때문에 실패하는지 확인**

Run: `cd hotel && ./gradlew test --tests com.mjc.hotel.member.entity.MemberMappingTests`

Expected: FAIL. `mapsMemberEntityToMembersTable()`에서 `ClassNotFoundException: com.mjc.hotel.member.entity.Member`가 발생한다.

- [x] **Step 3: 상태 및 권한 enum 구현**

`MemberStatus.java`:

```java
package com.mjc.hotel.member.entity;

public enum MemberStatus {
    ACTIVE,
    STOP,
    DELETED
}
```

`MemberRole.java`:

```java
package com.mjc.hotel.member.entity;

public enum MemberRole {
    ADMIN,
    USER
}
```

- [x] **Step 4: 최소 회원 엔티티 구현**

```java
package com.mjc.hotel.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MemberStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private MemberRole role;

    @Column(name = "email_verified")
    private Boolean emailVerified;

    @Column(name = "phone_verified")
    private Boolean phoneVerified;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
```

- [x] **Step 5: 대상 테스트 통과 확인**

Run: `cd hotel && ./gradlew test --tests com.mjc.hotel.member.entity.MemberMappingTests`

Expected: BUILD SUCCESSFUL, 6 tests passed.

- [x] **Step 6: 전체 회귀 테스트 실행**

Run: `cd hotel && ./gradlew test`

Expected: BUILD SUCCESSFUL. 기존 테스트와 `MemberMappingTests`가 모두 통과한다.

- [x] **Step 7: 회원 엔티티 변경만 커밋**

```bash
git add hotel/src/main/java/com/mjc/hotel/member/entity/Member.java \
        hotel/src/main/java/com/mjc/hotel/member/entity/MemberStatus.java \
        hotel/src/main/java/com/mjc/hotel/member/entity/MemberRole.java \
        hotel/src/test/java/com/mjc/hotel/member/entity/MemberMappingTests.java \
        docs/superpowers/plans/2026-06-24-member-entity.md
git commit -m "feat: 회원 엔티티 추가"
```
