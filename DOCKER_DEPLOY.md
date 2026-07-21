# StayNow Docker Deploy

## 1. 환경 파일 준비

```bash
cp docker.env.example .env
```

`.env`에서 Docker Hub ID, DB/Redis 비밀번호, Gmail 앱 비밀번호, Toss 키와 Google OAuth 정보를 서버 환경에 맞게 수정합니다.

Google Cloud Console에는 `https://<배포-도메인>/login/oauth2/code/google`을 승인된 리다이렉트 URI로 등록합니다. `.env`의 `OAUTH2_FRONTEND_CALLBACK_URL`은 `https://<배포-도메인>/oauth-callback.html`로 설정합니다.

## 2. 로컬에서 빌드 및 실행

```bash
docker compose up -d --build
```

API 확인:

```bash
curl http://localhost:33000/swagger-ui/index.html
```

## 3. Docker Hub에 앱/프론트 이미지 올리기

```bash
docker login
docker compose build app frontend
docker compose push app frontend
```

이미지 이름은 `.env`의 값 기준으로 아래처럼 생성됩니다.

- `${DOCKERHUB_USERNAME}/staynow-hotel-api:${APP_IMAGE_TAG}`
- `${DOCKERHUB_USERNAME}/staynow-hotel-frontend:${FRONT_IMAGE_TAG}`

## 4. AWS 서버에서 실행

AWS 서버에 `docker-compose.yml`과 `.env`를 올린 뒤 실행합니다.

```bash
docker compose pull app frontend
docker compose up -d
```

MariaDB와 Redis는 공식 이미지를 사용하고, Spring Boot 앱과 프론트엔드 이미지는 Docker Hub에서 내려받습니다.

프론트엔드는 Nginx로 정적 파일을 제공하고 `/api/**`, `/images/**` 요청을 Spring Boot 컨테이너로 프록시합니다. 그래서 배포 환경에서는 프론트 JavaScript가 같은 도메인의 `/api`를 호출합니다.

## 5. 로그 확인

```bash
docker compose logs -f app
docker compose logs -f mariadb
docker compose logs -f redis
```

## 참고

- 앱 포트는 기본 `33000`입니다.
- MariaDB 컨테이너 내부 포트는 `3306`, 호스트 노출 포트는 기본 `33306`입니다.
- 업로드 파일은 Docker volume `staynow-upload-data`에 저장됩니다.
- DB 데이터는 Docker volume `staynow-mariadb-data`에 저장됩니다.
