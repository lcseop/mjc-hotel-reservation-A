# StayNow 
http://hotel.seoplee.site/

> 숙소 탐색부터 예약·결제·체크인까지 하나의 흐름으로 연결한 호텔 예약 웹 서비스

StayNow는 사용자가 조건에 맞는 숙소와 객실을 찾고, 예약과 결제를 완료한 뒤 예약 내역을 관리할 수 있도록 만든 웹 애플리케이션입니다. 운영자는 숙소·객실·프로모션·회원·예약을 관리하고 QR 기반 체크인을 처리할 수 있습니다.

## 프로젝트 한눈에 보기

| 구분 | 내용 |
| --- | --- |
| 서비스 영역 | 호텔·리조트 예약 플랫폼 |
| 백엔드 | Java 21, Spring Boot 4, Spring Security, JPA, QueryDSL, MyBatis |
| 프런트엔드 | HTML, CSS, JavaScript, jQuery, Nginx |
| 데이터·인프라 | MariaDB, Redis Session, Docker Compose |
| 외부 연동 | Google·Kakao·Naver OAuth 2.0, Toss Payments, 공공데이터포털 관광 API, SMTP 메일 |

## 팀 소개

| 팀원 이름 | 역할 | 담당 기능 및 기여 |
| --- | --- | --- |
| 이충섭 | 팀장 / 프로젝트 구조 전반 설계 / 백엔드 호텔, 객실 담당 / 프론트엔드 | 프로젝트 구조 설계, 호텔, 객실 테이블의 구조 담당, Codex를 활용해 프론트엔드 전반 관리 |
| 이승협 | 백엔드 리뷰 담당 | 리뷰 테이블의 구조 담당 |
| 이재원 | 백엔드 회원, 약관 담당 / JWT 담당 / Spring Security 담당 | 회원, 약관 테이블의 구조 담당, JWT 및 Spring Security 구현 및 적용, 인증·인가 구현 |
| 한환희 | 백엔드 예약 담당 | 예약 테이블의 구조 담당 |
| 홍새한 | 백엔드 프로모션, 쿠폰 담당 | 프로모션, 쿠폰 테이블의 구조 담당 |

### 협업 방식

- GitHub Issue와 Pull Request를 중심으로 기능 단위 작업을 관리했습니다.
- 브랜치를 분리해 개발하고, 코드 리뷰 후 `main` 브랜치에 병합했습니다.
- API 명세는 Swagger UI와 Postman 컬렉션으로 공유했습니다.
- 본 프로젝트에는 Codex, Claude가 활용되었습니다.

## 주요 기능

### 고객 서비스

- 숙소·객실 검색, 상세 조회, 유형·태그·편의시설 기반 탐색
- 회원가입, 이메일 인증, 비밀번호 재설정
- 일반 로그인 및 Google·Kakao·Naver 소셜 로그인
- JWT 액세스 토큰 갱신과 Redis 기반 세션 관리
- 예약 생성·조회·취소, 결제 승인 및 환불 처리
- 찜 목록, 포인트 내역, 리뷰·사진·반응 관리
- 내 정보, 약관 동의, 연결된 소셜 계정 관리

### 관리자 서비스

- 숙소·객실·사진·편의시설·태그 CRUD
- 회원, 예약, 매출, 프로모션·쿠폰, 리뷰 관리 화면
- QR 체크인과 체크아웃 처리
- 공공데이터포털 관광 API를 통한 숙소 데이터 미리보기·선택 가져오기

## 아키텍처

```text
Browser
  │
  ▼
Nginx (static frontend, /api proxy)
  │
  ▼
Spring Boot API ───── MariaDB
  │       │
  │       └────────── Redis (session)
  │
  ├── OAuth providers
  ├── Toss Payments
  ├── SMTP
  └── Tourism API
```

프런트엔드는 Nginx가 정적 파일을 제공하고, `/api`, OAuth, 이미지 요청은 API 서버로 프록시합니다. API는 역할 기반 접근 제어로 고객 기능과 관리자 기능을 분리합니다.

## 디렉터리 구조

```text
.
├── hotel/                 # Spring Boot API
│   └── src/main/java/     # 도메인별 controller · service · repository
├── hotel-frontend/        # 정적 프런트엔드와 Nginx 설정
├── docs/                  # SQL 및 Redis Compose 참고 자료
├── json/                  # Postman API 컬렉션
└── docker-compose.yml     # API · frontend · MariaDB · Redis 실행 구성
```

## 시작하기

### 사전 요구사항

- Docker 및 Docker Compose
- 또는 Java 21, MariaDB, Redis

### Docker Compose로 실행

프로젝트 루트에서 `.env` 파일을 만들고 아래 필수 값을 설정합니다. 실제 키와 비밀번호는 Git에 커밋하지 마세요.

```env
MARIADB_ROOT_PASSWORD=change-me
REDIS_PASSWORD=change-me
JWT_SECRET=use-a-long-random-secret
TOSS_SECRET_KEY=your-toss-secret-key
OAUTH2_FRONTEND_CALLBACK_URL=https://your-domain.example/oauth-callback.html
CORS_ALLOWED_ORIGINS=https://your-domain.example
```

실행합니다.

```bash
docker compose up -d --build
```

- 프런트엔드: `http://localhost`
- API Swagger UI: `http://localhost:33000/swagger-ui/index.html`

배포용 Compose는 기본적으로 `oauth,prod` 프로필을 사용합니다. `prod` 프로필은 HTTPS 쿠키를 활성화하고 JPA 스키마 자동 변경을 허용하지 않으므로, 배포 전 스키마를 마이그레이션으로 준비해야 합니다. 자세한 내용은 [DOCKER_DEPLOY.md](DOCKER_DEPLOY.md)를 참고하세요.

### 테스트

```bash
cd hotel
./gradlew test
```

Windows에서는 `gradlew.bat test`를 사용합니다.

## 보안 및 운영 고려사항

- 민감 정보는 환경변수로 주입하고, SonarQube 토큰은 `SONAR_TOKEN`으로 전달합니다.
- CORS는 `CORS_ALLOWED_ORIGINS`에 등록한 출처만 허용합니다.
- 운영 프로필에서는 보안 쿠키를 사용하고 SQL 로그 및 자동 DDL 변경을 비활성화합니다.
- 권한이 필요한 API는 Spring Security와 JWT로 보호하며, 관리 기능은 `ADMIN` 역할로 제한합니다.

## API 문서

실행 후 Swagger UI에서 API 명세를 확인할 수 있습니다.

- `http://localhost:33000/swagger-ui/index.html`
- [Postman 컬렉션](json/Hotel_Reservation.postman_collection.json)

## 향후 개선 방향

- 예약 가능 객실 조회의 동시성 제어 강화
- 프론트엔드 프레임워크 전환 및 E2E 테스트 확대

---

본 프로젝트는 호텔 예약 도메인의 고객 여정과 운영 관리 흐름을 구현한 포트폴리오 프로젝트입니다.
