# 회원 엔티티 설계

## 목표

`hotel/erd_images/회원.png`의 `members` 테이블 정의를 Spring Data JPA 엔티티로 구현한다.

## 범위

- `com.mjc.hotel.member.entity` 패키지에 `Member` 엔티티를 추가한다.
- 회원 상태와 권한을 각각 `MemberStatus`, `MemberRole` enum으로 분리한다.
- 저장소, 서비스, 컨트롤러, 회원 가입 및 탈퇴 로직은 이번 작업에서 제외한다.

## 엔티티 매핑

`Member`는 `members` 테이블에 명시적으로 매핑한다. 필드와 컬럼은 다음과 같다.

| Java 필드 | DB 컬럼 | Java 타입 | 제약 및 매핑 |
|---|---|---|---|
| `memberId` | `member_id` | `Long` | 기본키, `IDENTITY` 자동 증가 |
| `name` | `name` | `String` | 최대 50자 |
| `phone` | `phone` | `String` | 최대 20자 |
| `email` | `email` | `String` | 최대 255자 |
| `status` | `status` | `MemberStatus` | 문자열 enum: `ACTIVE`, `STOP`, `DELETED` |
| `role` | `role` | `MemberRole` | 문자열 enum: `ADMIN`, `USER` |
| `emailVerified` | `email_verified` | `Boolean` | `TINYINT(1)` 대응 |
| `phoneVerified` | `phone_verified` | `Boolean` | `TINYINT(1)` 대응 |
| `createdAt` | `created_at` | `LocalDateTime` | 생성 시각, 생성 후 변경 불가 |
| `updatedAt` | `updated_at` | `LocalDateTime` | 수정 시각 |
| `deletedAt` | `deleted_at` | `LocalDateTime` | nullable 탈퇴 시각 |

ERD에 nullable 여부와 기본값이 표시되어 있지 않으므로 이번 엔티티에서는 임의의 `nullable = false`나 기본값을 추가하지 않는다. DB `ENUM` 전용 `columnDefinition`도 사용하지 않고 `EnumType.STRING`으로 값만 안정적으로 보존한다.

## 생명주기와 데이터 흐름

Hibernate의 `@CreationTimestamp`와 `@UpdateTimestamp`가 생성 및 수정 시각을 관리한다. `deletedAt`은 단순 nullable 필드로 두며, 이후 탈퇴 기능이 구현될 때 애플리케이션 계층에서 설정한다.

Lombok은 기존 프로젝트 관례에 맞춰 기본 생성자, 전체 필드 생성자, 빌더와 접근자를 생성한다.

## 오류 처리

enum에 정의되지 않은 상태나 권한은 Java 코드에서 생성할 수 없으며, DB에서 알 수 없는 문자열을 읽으면 JPA 변환 오류가 발생한다. 이번 작업은 엔티티 정의만 다루므로 별도의 예외 변환은 추가하지 않는다.

## 검증

데이터베이스 연결 없이 실행되는 단위 테스트에서 리플렉션으로 다음 사항을 검증한다.

- 엔티티와 `members` 테이블 매핑
- `member_id`의 기본키 및 `IDENTITY` 전략
- 문자열 컬럼 길이
- enum의 `EnumType.STRING` 매핑과 허용 값
- 인증 여부 필드의 `Boolean` 타입
- 생성/수정 타임스탬프 어노테이션과 `created_at`의 변경 불가 설정

마지막으로 전체 Gradle 테스트를 실행해 기존 기능에 회귀가 없는지 확인한다.
