# 회원 관련 엔티티 설계

## 목표

다음 ERD 이미지의 세 테이블을 기존 `Member` 엔티티와 연결된 JPA 엔티티로 구현한다.

- `hotel/erd_images/회원_약관_동의.png`
- `hotel/erd_images/약관.png`
- `hotel/erd_images/로그인_인증_정보.png`

## 범위

- `terms` 테이블을 나타내는 `Term` 엔티티를 추가한다.
- `member_term_agreements` 테이블을 나타내는 `MemberTermAgreement` 엔티티를 추가한다.
- `member_auth_accounts` 테이블을 나타내는 `MemberAuthAccount` 엔티티를 추가한다.
- 외래키는 단방향 LAZY `@ManyToOne` 연관관계로 매핑한다.
- 저장소, 서비스, 컨트롤러와 약관 동의·로그인 업무 로직은 이번 작업에서 제외한다.

## 패키지 및 관계 구조

`Term`은 독립적인 약관 도메인이므로 `com.mjc.hotel.term.entity`에 둔다. 회원에 종속된 동의 이력과 인증 계정은 기존 회원 모델과 함께 `com.mjc.hotel.member.entity`에 둔다.

- `MemberTermAgreement` → `Member`: 다대일, 단방향, LAZY
- `MemberTermAgreement` → `Term`: 다대일, 단방향, LAZY
- `MemberAuthAccount` → `Member`: 다대일, 단방향, LAZY

`Member`와 `Term`에는 `@OneToMany` 컬렉션을 추가하지 않는다. ERD에 없는 cascade와 orphan removal도 설정하지 않는다. 따라서 연관 엔티티의 생명주기는 각각 독립적으로 관리된다.

## 테이블 매핑

### 약관

`Term`은 `terms` 테이블에 매핑한다.

| Java 필드 | DB 컬럼 | Java 타입 | 제약 및 매핑 |
|---|---|---|---|
| `termId` | `term_id` | `Long` | 기본키, `IDENTITY` 자동 증가 |
| `termType` | `term_type` | `String` | 최대 30자 |
| `title` | `title` | `String` | 최대 100자 |
| `version` | `version` | `String` | 최대 20자 |
| `isRequired` | `is_required` | `Boolean` | `TINYINT(1)` 대응 |
| `effectiveAt` | `effective_at` | `LocalDateTime` | 약관 시행 일시 |

`term_type`은 ERD에서 `VARCHAR(30)`으로 정의되므로 enum으로 제한하지 않는다.

### 회원 약관 동의

`MemberTermAgreement`는 `member_term_agreements` 테이블에 매핑한다.

| Java 필드 | DB 컬럼 | Java 타입 | 제약 및 매핑 |
|---|---|---|---|
| `agreementId` | `agreement_id` | `Long` | 기본키, `IDENTITY` 자동 증가 |
| `member` | `member_id` | `Member` | LAZY `@ManyToOne` 외래키 |
| `term` | `term_id` | `Term` | LAZY `@ManyToOne` 외래키 |
| `isAgreed` | `is_agreed` | `Boolean` | `TINYINT(1)` 대응 |
| `agreedAt` | `agreed_at` | `LocalDateTime` | 동의 일시 |
| `withdrawnAt` | `withdrawn_at` | `LocalDateTime` | nullable 철회 일시 |

`agreedAt`과 `withdrawnAt`은 업무 이벤트가 발생할 때 애플리케이션 계층에서 설정하며 Hibernate 타임스탬프 자동 생성 어노테이션을 사용하지 않는다.

### 로그인 인증 정보

`MemberAuthAccount`는 `member_auth_accounts` 테이블에 매핑한다.

| Java 필드 | DB 컬럼 | Java 타입 | 제약 및 매핑 |
|---|---|---|---|
| `authAccountId` | `auth_account_id` | `Long` | 기본키, `IDENTITY` 자동 증가 |
| `member` | `member_id` | `Member` | LAZY `@ManyToOne` 외래키 |
| `provider` | `provider` | `String` | 최대 20자 |
| `providerUserId` | `provider_user_id` | `String` | 최대 255자 |
| `passwordHash` | `password_hash` | `String` | 최대 255자 |
| `lastLoginAt` | `last_login_at` | `LocalDateTime` | 마지막 로그인 일시 |
| `createdAt` | `created_at` | `LocalDateTime` | 생성 일시, 생성 후 변경 불가 |

`provider`는 ERD에서 `VARCHAR(20)`으로 정의되므로 enum으로 제한하지 않는다. 한 회원이 여러 인증 제공자 계정을 가질 수 있도록 ERD의 다대일 구조를 그대로 유지한다. `createdAt`은 `@CreationTimestamp`로 관리하고 `lastLoginAt`은 로그인 성공 시 애플리케이션 계층에서 설정한다.

## 공통 매핑 원칙

- 모든 테이블명과 컬럼명을 명시한다.
- 모든 `BIGINT` 기본키는 기존 `Member`와 동일하게 `GenerationType.IDENTITY`를 사용한다.
- `TINYINT(1)` 필드는 nullable 가능성을 보존하기 위해 `Boolean`으로 매핑한다.
- ERD에 표시되지 않은 `nullable`, `unique`, 기본값, 인덱스 제약은 추가하지 않는다.
- 기존 프로젝트 관례에 따라 Lombok으로 기본 생성자, 전체 필드 생성자, 빌더와 접근자를 생성한다.

## 오류 및 생명주기 처리

엔티티 정의 단계에서는 별도의 예외 변환을 추가하지 않는다. 존재하지 않는 회원이나 약관을 참조하는 데이터는 데이터베이스 외래키 제약에서 거부된다. 연관 엔티티 저장·삭제 순서와 트랜잭션 처리는 이후 서비스 계층의 책임으로 둔다.

## 검증

데이터베이스 연결 없이 실행되는 리플렉션 단위 테스트로 다음 사항을 검증한다.

- 세 엔티티의 테이블명
- 각 기본키의 컬럼명과 `IDENTITY` 생성 전략
- 문자열 컬럼 길이와 Boolean·`LocalDateTime` 타입
- 각 외래키의 `@ManyToOne(fetch = LAZY)` 및 `@JoinColumn` 설정
- 인증 계정 `createdAt`의 `@CreationTimestamp`와 변경 불가 설정

마지막으로 전체 Gradle 테스트를 재실행해 기존 회원 및 학생 기능에 회귀가 없는지 확인한다.
