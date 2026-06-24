# Member·Term Repository 및 DB 커밋 테스트 설계

## 목표

`Member`와 `Term`에 대한 Spring Data JPA Repository를 추가하고, 테스트에서 저장한 샘플 데이터를 실제 MariaDB에 커밋하여 DBeaver에서 확인할 수 있게 한다.

## 범위

- `com.mjc.hotel.member.repository.MemberRepository`를 추가한다.
- `com.mjc.hotel.term.repository.TermRepository`를 추가한다.
- 두 Repository를 사용하는 Spring Boot 통합 테스트를 추가한다.
- `MemberAuthAccount`와 `MemberTermAgreement` Repository 및 데이터는 이번 작업에서 제외한다.

## Repository 구조

두 Repository는 별도의 사용자 정의 쿼리 없이 `JpaRepository<엔티티, Long>`만 상속한다.

- `MemberRepository extends JpaRepository<Member, Long>`
- `TermRepository extends JpaRepository<Term, Long>`

Spring Data JPA가 두 인터페이스의 Bean과 기본 CRUD 구현을 자동 생성한다.

## 테스트 데이터 흐름

`MemberTermRepositoryTests`는 `@SpringBootTest`, `@Transactional`, `@Commit`을 사용한다.

1. Member 샘플 객체를 생성하고 `MemberRepository.saveAndFlush()`로 저장한다.
2. Term 샘플 객체를 생성하고 `TermRepository.saveAndFlush()`로 저장한다.
3. 두 엔티티의 ID가 생성되었는지 확인한다.
4. `EntityManager.clear()`로 1차 캐시를 비운다.
5. 각 Repository의 `findById()`로 MariaDB에서 다시 읽어 필드값을 검증한다.
6. 테스트 성공 후 트랜잭션을 커밋하여 DBeaver 조회 결과에 데이터를 남긴다.

샘플 Member는 활성 일반 사용자이며 인증 여부는 `false`로 저장한다. 샘플 Term은 필수 서비스 이용약관 버전 `1.0`으로 저장한다.

## 트랜잭션 및 실패 처리

Member와 Term 저장 및 검증을 하나의 테스트 트랜잭션에서 수행한다. 테스트가 정상 종료되면 `@Commit`이 트랜잭션을 커밋한다. 저장 또는 검증 중 예외가 발생하면 테스트가 실패하고 트랜잭션은 커밋되지 않는다.

테스트를 반복 실행하면 샘플 행이 추가로 생성된다. 이는 로컬 DBeaver 확인 목적에 맞춘 의도된 동작이며 자동 정리 로직은 추가하지 않는다.

## 검증

- TDD RED 단계에서는 테스트가 존재하지 않는 Repository 클래스 때문에 실패하는 것을 확인한다.
- GREEN 단계에서는 두 Repository를 추가하고 대상 통합 테스트를 실행한다.
- MariaDB 재조회로 저장값을 검증한다.
- 마지막으로 전체 Gradle 테스트를 실행해 기존 엔티티 매핑 테스트에 회귀가 없는지 확인한다.
