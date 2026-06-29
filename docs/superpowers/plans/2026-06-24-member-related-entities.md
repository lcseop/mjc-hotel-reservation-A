# Member Related Entities Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 세 ERD의 약관, 회원 약관 동의, 로그인 인증 정보 테이블을 기존 `Member`와 연결된 JPA 엔티티로 구현한다.

**Architecture:** `Term`은 독립된 `term.entity` 패키지에 두고, 회원 종속 엔티티는 `member.entity`에 둔다. 외래키는 부모 컬렉션 없이 자식에서만 단방향 LAZY `ManyToOne`으로 매핑한다.

**Tech Stack:** Java 21, Spring Boot 4, Jakarta Persistence, Hibernate ORM, Lombok, JUnit 5, Gradle

---

### Task 1: 약관 엔티티

**Files:**
- Create: `hotel/src/test/java/com/mjc/hotel/term/entity/TermMappingTests.java`
- Create: `hotel/src/main/java/com/mjc/hotel/term/entity/Term.java`

- [x] **Step 1: 약관 매핑 실패 테스트 작성**

```java
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

        Field id = field("sid");
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
```

- [x] **Step 2: 누락된 `Term` 때문에 실패하는지 확인**

Run: `cd hotel && bash gradlew test --tests com.mjc.hotel.term.entity.TermMappingTests`

Expected: FAIL with `ClassNotFoundException: com.mjc.hotel.term.entity.Term`.

- [x] **Step 3: 최소 `Term` 엔티티 구현**

```java
package com.mjc.hotel.term.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "terms")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Term {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "term_id")
    private Long sid;

    @Column(name = "term_type", length = 30)
    private String termType;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "is_required")
    private Boolean isRequired;

    @Column(name = "effective_at")
    private LocalDateTime effectiveAt;
}
```

- [x] **Step 4: 약관 매핑 테스트 통과 확인**

Run: `cd hotel && bash gradlew test --tests com.mjc.hotel.term.entity.TermMappingTests`

Expected: BUILD SUCCESSFUL, 2 tests passed.

- [x] **Step 5: 약관 엔티티만 커밋**

```bash
git add hotel/src/main/java/com/mjc/hotel/term/entity/Term.java hotel/src/test/java/com/mjc/hotel/term/entity/TermMappingTests.java
git commit --only -m "feat: 약관 엔티티 추가" -- hotel/src/main/java/com/mjc/hotel/term/entity/Term.java hotel/src/test/java/com/mjc/hotel/term/entity/TermMappingTests.java
```

### Task 2: 회원 약관 동의 엔티티

**Files:**
- Create: `hotel/src/test/java/com/mjc/hotel/member/entity/MemberTermAgreementMappingTests.java`
- Create: `hotel/src/main/java/com/mjc/hotel/member/entity/MemberTermAgreement.java`

- [x] **Step 1: 회원 약관 동의 매핑 실패 테스트 작성**

```java
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
```

- [x] **Step 2: 누락된 동의 엔티티 때문에 실패하는지 확인**

Run: `cd hotel && bash gradlew test --tests com.mjc.hotel.member.entity.MemberTermAgreementMappingTests`

Expected: FAIL with `ClassNotFoundException: com.mjc.hotel.member.entity.MemberTermAgreement`.

- [x] **Step 3: 최소 `MemberTermAgreement` 엔티티 구현**

```java
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_term_agreements")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MemberTermAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agreement_id")
    private Long agreementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id")
    private Term term;

    @Column(name = "is_agreed")
    private Boolean isAgreed;

    @Column(name = "agreed_at")
    private LocalDateTime agreedAt;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;
}
```

- [x] **Step 4: 회원 약관 동의 테스트 통과 확인**

Run: `cd hotel && bash gradlew test --tests com.mjc.hotel.member.entity.MemberTermAgreementMappingTests`

Expected: BUILD SUCCESSFUL, 3 tests passed.

- [x] **Step 5: 회원 약관 동의 엔티티만 커밋**

```bash
git add hotel/src/main/java/com/mjc/hotel/member/entity/MemberTermAgreement.java hotel/src/test/java/com/mjc/hotel/member/entity/MemberTermAgreementMappingTests.java
git commit --only -m "feat: 회원 약관 동의 엔티티 추가" -- hotel/src/main/java/com/mjc/hotel/member/entity/MemberTermAgreement.java hotel/src/test/java/com/mjc/hotel/member/entity/MemberTermAgreementMappingTests.java
```

### Task 3: 로그인 인증 정보 엔티티

**Files:**
- Create: `hotel/src/test/java/com/mjc/hotel/member/entity/MemberAuthAccountMappingTests.java`
- Create: `hotel/src/main/java/com/mjc/hotel/member/entity/MemberAuthAccount.java`
- Create: `docs/superpowers/plans/2026-06-24-member-related-entities.md`

- [x] **Step 1: 로그인 인증 정보 매핑 실패 테스트 작성**

```java
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
```

- [x] **Step 2: 누락된 인증 계정 엔티티 때문에 실패하는지 확인**

Run: `cd hotel && bash gradlew test --tests com.mjc.hotel.member.entity.MemberAuthAccountMappingTests`

Expected: FAIL with `ClassNotFoundException: com.mjc.hotel.member.entity.MemberAuthAccount`.

- [x] **Step 3: 최소 `MemberAuthAccount` 엔티티 구현**

```java
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_auth_accounts")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MemberAuthAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_account_id")
    private Long authAccountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "provider", length = 20)
    private String provider;

    @Column(name = "provider_user_id", length = 255)
    private String providerUserId;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
```

- [x] **Step 4: 로그인 인증 정보 테스트 통과 확인**

Run: `cd hotel && bash gradlew test --tests com.mjc.hotel.member.entity.MemberAuthAccountMappingTests`

Expected: BUILD SUCCESSFUL, 3 tests passed.

- [x] **Step 5: 전체 회귀 테스트 실행**

Run: `cd hotel && bash gradlew test --rerun-tasks`

Expected: BUILD SUCCESSFUL. 기존 테스트와 신규 매핑 테스트 8개가 모두 통과한다.

- [x] **Step 6: 인증 계정과 실행 계획만 커밋**

```bash
git add hotel/src/main/java/com/mjc/hotel/member/entity/MemberAuthAccount.java hotel/src/test/java/com/mjc/hotel/member/entity/MemberAuthAccountMappingTests.java docs/superpowers/plans/2026-06-24-member-related-entities.md
git commit --only -m "feat: 로그인 인증 정보 엔티티 추가" -- hotel/src/main/java/com/mjc/hotel/member/entity/MemberAuthAccount.java hotel/src/test/java/com/mjc/hotel/member/entity/MemberAuthAccountMappingTests.java docs/superpowers/plans/2026-06-24-member-related-entities.md
```
