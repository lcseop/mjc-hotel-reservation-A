# Member Term Repositories Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Member와 Term용 Spring Data JPA Repository를 추가하고 실제 MariaDB에 샘플 데이터를 커밋하는 통합 테스트를 제공한다.

**Architecture:** 각 도메인 패키지의 `repository` 하위에 `JpaRepository` 인터페이스를 둔다. Repository 타입 계약을 RED→GREEN으로 구현한 후 Spring Boot 통합 테스트가 저장, flush, 영속성 컨텍스트 초기화, DB 재조회와 커밋을 수행한다.

**Tech Stack:** Java 21, Spring Boot 4, Spring Data JPA, Hibernate ORM, JUnit 5, MariaDB, Gradle

---

### Task 1: Member 및 Term Repository

**Files:**
- Create: `hotel/src/test/java/com/mjc/hotel/repository/RepositoryTypeTests.java`
- Create: `hotel/src/main/java/com/mjc/hotel/member/repository/MemberRepository.java`
- Create: `hotel/src/main/java/com/mjc/hotel/term/repository/TermRepository.java`

- [x] **Step 1: Repository 타입 계약 실패 테스트 작성**

```java
package com.mjc.hotel.repository;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.term.entity.Term;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryTypeTests {

    @Test
    void memberRepositoryUsesMemberAndLongTypes() throws Exception {
        assertRepositoryTypes(
                "com.mjc.hotel.member.repository.MemberRepository",
                Member.class
        );
    }

    @Test
    void termRepositoryUsesTermAndLongTypes() throws Exception {
        assertRepositoryTypes(
                "com.mjc.hotel.term.repository.TermRepository",
                Term.class
        );
    }

    private static void assertRepositoryTypes(String className,
                                              Class<?> entityType) throws Exception {
        Class<?> repositoryType = Class.forName(className);
        assertTrue(repositoryType.isInterface());
        assertTrue(JpaRepository.class.isAssignableFrom(repositoryType));

        Type genericInterface = repositoryType.getGenericInterfaces()[0];
        ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
        assertEquals(JpaRepository.class, parameterizedType.getRawType());
        assertEquals(entityType, parameterizedType.getActualTypeArguments()[0]);
        assertEquals(Long.class, parameterizedType.getActualTypeArguments()[1]);
    }
}
```

- [x] **Step 2: 누락된 Repository 때문에 실패하는지 확인**

Run: `cd hotel && bash gradlew test --tests com.mjc.hotel.repository.RepositoryTypeTests`

Expected: FAIL with `ClassNotFoundException: com.mjc.hotel.member.repository.MemberRepository` and `ClassNotFoundException: com.mjc.hotel.term.repository.TermRepository`.

- [x] **Step 3: 최소 Repository 인터페이스 구현**

`MemberRepository.java`:

```java
package com.mjc.hotel.member.repository;

import com.mjc.hotel.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
```

`TermRepository.java`:

```java
package com.mjc.hotel.term.repository;

import com.mjc.hotel.term.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<Term, Long> {
}
```

- [x] **Step 4: Repository 타입 계약 테스트 통과 확인**

Run: `cd hotel && bash gradlew test --tests com.mjc.hotel.repository.RepositoryTypeTests`

Expected: BUILD SUCCESSFUL, 2 tests passed.

### Task 2: MariaDB 저장 및 커밋 통합 테스트

**Files:**
- Create: `hotel/src/test/java/com/mjc/hotel/member/MemberTermRepositoryTests.java`
- Create: `docs/superpowers/plans/2026-06-24-member-term-repositories.md`

- [x] **Step 1: 실제 저장·재조회 통합 테스트 작성**

```java
package com.mjc.hotel.member;

import com.mjc.hotel.member.entity.Member;
import com.mjc.hotel.member.entity.MemberRole;
import com.mjc.hotel.member.entity.MemberStatus;
import com.mjc.hotel.member.repository.MemberRepository;
import com.mjc.hotel.term.entity.Term;
import com.mjc.hotel.term.repository.TermRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@Commit
class MemberTermRepositoryTests {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TermRepository termRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void savesMemberAndTermToMariaDb() {
        Member member = memberRepository.saveAndFlush(
                Member.builder()
                        .name("Repository Test User")
                        .phone("010-0000-0000")
                        .email("repository-test@example.com")
                        .status(MemberStatus.ACTIVE)
                        .role(MemberRole.USER)
                        .emailVerified(false)
                        .phoneVerified(false)
                        .build()
        );

        Term term = termRepository.saveAndFlush(
                Term.builder()
                        .termType("SERVICE")
                        .title("Repository Test Service Terms")
                        .version("1.0")
                        .isRequired(true)
                        .effectiveAt(LocalDateTime.now())
                        .build()
        );

        assertNotNull(member.getSid());
        assertNotNull(term.getSid());

        Long sid = member.getSid();
        Long sid = term.getSid();
        entityManager.clear();

        Member savedMember = memberRepository.findById(sid).orElseThrow();
        Term savedTerm = termRepository.findById(sid).orElseThrow();

        assertEquals("repository-test@example.com", savedMember.getEmail());
        assertEquals(MemberStatus.ACTIVE, savedMember.getStatus());
        assertFalse(savedMember.getEmailVerified());
        assertEquals("Repository Test Service Terms", savedTerm.getTitle());
        assertEquals("1.0", savedTerm.getVersion());
        assertTrue(savedTerm.getIsRequired());
    }
}
```

- [x] **Step 2: 대상 통합 테스트 실행 및 DB 커밋 확인**

Run: `cd hotel && bash gradlew test --tests com.mjc.hotel.member.MemberTermRepositoryTests`

Expected: BUILD SUCCESSFUL, 1 test passed. 성공한 테스트 트랜잭션이 MariaDB의 `members`, `terms` 테이블에 각각 한 행을 커밋한다.

- [x] **Step 3: 전체 회귀 테스트 실행**

Run: `cd hotel && bash gradlew test --rerun-tasks`

Expected: BUILD SUCCESSFUL. 기존 테스트와 신규 Repository 테스트가 모두 통과한다.

- [x] **Step 4: Repository와 테스트만 커밋**

```bash
git add hotel/src/main/java/com/mjc/hotel/member/repository/MemberRepository.java hotel/src/main/java/com/mjc/hotel/term/repository/TermRepository.java hotel/src/test/java/com/mjc/hotel/repository/RepositoryTypeTests.java hotel/src/test/java/com/mjc/hotel/member/MemberTermRepositoryTests.java docs/superpowers/plans/2026-06-24-member-term-repositories.md
git commit --only -m "test: Member Term Repository 저장 검증" -- hotel/src/main/java/com/mjc/hotel/member/repository/MemberRepository.java hotel/src/main/java/com/mjc/hotel/term/repository/TermRepository.java hotel/src/test/java/com/mjc/hotel/repository/RepositoryTypeTests.java hotel/src/test/java/com/mjc/hotel/member/MemberTermRepositoryTests.java docs/superpowers/plans/2026-06-24-member-term-repositories.md
```
