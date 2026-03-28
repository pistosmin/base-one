# Phase 0: 프로젝트 부트스트랩 — 설계 문서

> **작성일**: 2026-03-28
> **상태**: 구현 완료 (Phase 0 완료)
> **참조**: `prompt/phases/phase-0-bootstrap.md`, `prompt/architecture-plan-v2.md`

---

## 1. 개요

커뮤니티 웹앱 개발을 시작할 수 있는 최소한의 프로젝트 골격을 구축한다.
백엔드(Spring Boot) + 프론트엔드(React) + 인프라(Docker Compose)가 각각 독립적으로 실행되며, `make` 명령어 하나로 개발 환경 전체를 시작할 수 있어야 한다.

**완료 기준**:
- `make dev` → Docker 컨테이너 모두 healthy
- `make web` → `localhost:5173` React 앱 표시
- `make api` → `localhost:8080` Swagger UI 접근 가능
- `make lint-api` → 컴파일 성공

---

## 2. 기술 스택 결정 사항

### 2.1 Java 버전 선택

| 옵션 | 근거 |
|------|------|
| Java 21 (LTS) | Spring Boot 4.0 공식 최소 버전 |
| **Java 25 (채택)** | 로컬 환경에 Java 25만 설치되어 있음. Spring Boot 4.0은 Java 17+ 호환이므로 25도 지원됨 |

- Gradle 툴체인(`foojay 0.9.0`)으로 자동 JDK 관리
- `build.gradle.kts`: `JavaLanguageVersion.of(25)`
- `settings.gradle.kts`: foojay 0.9.0 (Gradle 9.x + Java 25 호환)

> **주의**: foojay 0.8.0은 Gradle 9.x와 호환 문제 발생. 반드시 0.9.0 이상 사용.

### 2.2 Spring Boot 버전

| 항목 | 결정값 |
|------|--------|
| Spring Boot | 4.0.3 |
| Spring Security | 7.x (Boot 4.0 포함) |
| JPA/Hibernate | jakarta.* 패키지 (javax.* 완전 제거) |
| Virtual Threads | 활성화 (`spring.threads.virtual.enabled: true`) |
| QueryDSL | 5.1.0 (jakarta classifier 필수) |
| JWT | jjwt 0.12.6 |

> **Spring Boot 4.0 주요 변경사항**: `javax.*` → `jakarta.*`, WebSecurityConfigurerAdapter 삭제, SecurityFilterChain 빈 방식 필수.

### 2.3 React 버전

| 항목 | 결정값 |
|------|--------|
| React | 19.2 |
| Vite | 8.0 |
| TypeScript | 5.9 |
| Node.js | 20.19+ (Vite 8 요구사항) |
| MUI | 7.3 (Grid2 → Grid 승격) |
| React Router | 7.x (`react-router`에서 import, `react-router-dom` 폐기) |
| TanStack Query | 5.90 |
| Zustand | 5.x |

### 2.4 인프라 서비스 선택 근거

| 서비스 | 버전 | 선택 이유 |
|--------|------|-----------|
| PostgreSQL | 18-alpine | uuidv7() 내장, 성능 개선, 최신 LTS |
| Redis | 7.4-alpine | Rate Limiting, JWT 블랙리스트, API 캐시 |
| MinIO | latest | S3 호환, 로컬 개발 비용 없음 |
| Prometheus | v3.2.0 | Spring Actuator 메트릭 수집 |
| Grafana | 11.5.0 | 메트릭 + 로그 통합 시각화 |
| Loki | 3.4.0 | 구조화된 로그 수집 (Grafana와 연동) |
| Nginx | 1.27-alpine | 개발 환경 리버스 프록시 (포트 80 → 5173/8080) |

---

## 3. 모노레포 디렉토리 구조

```
gravity/                          # 프로젝트 루트
├── .env.example                  # 환경변수 예시 (커밋)
├── .env                          # 실제 환경변수 (.gitignore)
├── .gitignore
├── .github/
│   └── workflows/ci.yml          # GitHub Actions CI
├── docker-compose.yml            # 개발 인프라 전체
├── Makefile                      # 개발 편의 명령어
│
├── apps/
│   ├── api/                      # Spring Boot 백엔드
│   │   ├── build.gradle.kts
│   │   ├── settings.gradle.kts
│   │   └── src/main/
│   │       ├── java/com/community/
│   │       │   ├── CommunityApplication.java
│   │       │   └── global/       # 전역 설정, 공통 모듈
│   │       └── resources/
│   │           ├── application.yml
│   │           ├── application-dev.yml
│   │           └── db/migration/ # Flyway SQL 파일
│   │
│   └── web/                      # React 프론트엔드
│       ├── package.json
│       ├── vite.config.ts
│       ├── index.html
│       └── src/
│           ├── app/              # 라우터, 테마, 프로바이더
│           ├── features/         # 기능별 모듈 (FSD)
│           └── shared/           # 공유 컴포넌트/훅
│
├── infra/
│   ├── scripts/init-minio.sh     # MinIO 버킷 초기화
│   ├── docker/nginx/default.conf # Nginx 설정
│   └── monitoring/
│       ├── prometheus/           # prometheus.yml
│       └── grafana/              # datasources, dashboards
│
└── prompt/
    ├── phases/                   # Phase별 개발 계획
    └── architecture-plan-v2.md  # 전체 아키텍처 설계
```

---

## 4. 백엔드 아키텍처

### 4.1 패키지 구조

```
com.community/
├── CommunityApplication.java       # 진입점
└── global/
    ├── config/
    │   ├── JpaConfig.java          # @EnableJpaAuditing
    │   ├── RedisConfig.java        # RedisTemplate<String, Object>
    │   ├── CorsConfig.java         # CorsFilter 빈
    │   └── OpenApiConfig.java      # Swagger/OpenAPI 3.0
    ├── common/
    │   ├── entity/
    │   │   └── BaseTimeEntity.java # createdAt/updatedAt Auditing
    │   ├── response/
    │   │   ├── ApiResponse.java    # 공통 응답 래퍼 record
    │   │   └── ErrorCode.java      # 에러 코드 enum
    │   └── event/
    │       └── DomainEventPublisher.java  # 도메인 이벤트 발행
    ├── security/ (Phase 1에서 구현)
    └── exception/
        ├── BusinessException.java
        └── GlobalExceptionHandler.java
```

### 4.2 공통 API 응답 형식

```json
// 성공
{ "success": true, "data": { ... }, "error": null }

// 실패
{ "success": false, "data": null, "error": { "code": "E011", "message": "이미 사용 중인 이메일입니다" } }

// 페이징
{
  "success": true,
  "data": {
    "content": [...],
    "totalElements": 150,
    "totalPages": 8,
    "page": 0,
    "size": 20,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

### 4.3 에러 코드 체계

| 범위 | 코드 | 설명 |
|------|------|------|
| E001~E009 | 인증/인가 | UNAUTHORIZED, FORBIDDEN, INVALID_CREDENTIALS, EXPIRED_TOKEN |
| E010~E019 | 사용자 | USER_NOT_FOUND, DUPLICATE_EMAIL, DUPLICATE_NICKNAME |
| E020~E029 | 유효성 | VALIDATION_ERROR |
| E500 | 서버 | INTERNAL_SERVER_ERROR |

### 4.4 application.yml 핵심 설정

| 설정 | 값 | 이유 |
|------|-----|------|
| `jpa.open-in-view` | false | Service 계층에서만 영속성 사용, 성능/추적성 |
| `jpa.hibernate.ddl-auto` | validate | Flyway가 스키마 관리, Hibernate는 검증만 |
| `hibernate.default_batch_fetch_size` | 20 | N+1 문제 완화 |
| `threads.virtual.enabled` | true | Virtual Threads로 높은 동시성 |
| `flyway.enabled` | true | 마이그레이션 자동 실행 |

---

## 5. 데이터베이스 스키마 설계

### 5.1 마이그레이션 파일 목록

| 파일 | 내용 |
|------|------|
| V1__create_users_table.sql | 사용자 (인증, 프로필) |
| V2__create_categories_table.sql | 카테고리 + 기본 4개 삽입 |
| V3__create_posts_table.sql | 게시글 (Soft Delete, 비정규화 카운터) |
| V4__create_comments_table.sql | 댓글 (Adjacency List 대댓글) |
| V5__create_votes_bookmarks.sql | 추천/북마크 |
| V6__create_notifications.sql | 알림 |
| V7__create_supporting_tables.sql | 미디어, 신고, 리프레시 토큰 |

### 5.2 설계 결정 사항

**비정규화 카운터 (posts.vote_count, posts.comment_count)**
- 이유: 목록 조회 시 매번 `COUNT(*)` 서브쿼리를 실행하면 성능 저하
- 방식: 추천/댓글 추가·삭제 시 posts 테이블의 카운터를 함께 업데이트
- 트레이드오프: 정합성 유지 책임이 서비스 레이어에 있음

**Soft Delete (posts.is_deleted, comments.is_deleted)**
- 이유: 삭제된 게시글/댓글의 히스토리 보존, 복구 가능성, 신고 처리
- 조건부 인덱스: `WHERE is_deleted = FALSE`로 삭제된 데이터를 인덱스에서 제외

**votes 테이블의 target_type 패턴**
- 게시글과 댓글 추천을 하나의 테이블에 통합
- `UNIQUE(user_id, target_type, target_id)`로 중복 추천 방지

**categories 기본 데이터 삽입**
- V2 마이그레이션에서 자유/질문/정보/리뷰 4개 카테고리를 함께 삽입
- 이유: 애플리케이션 기동 즉시 사용 가능한 상태 보장

---

## 6. 프론트엔드 아키텍처

### 6.1 Feature-Sliced Design (FSD)

```
src/
├── app/          # 앱 전역 설정 (라우터, 프로바이더, 테마)
├── features/     # 기능별 독립 모듈 (auth, post, comment, ...)
│   └── {name}/
│       ├── api/          # Axios API 함수
│       ├── hooks/        # TanStack Query 훅
│       ├── store/        # Zustand 스토어 (필요 시)
│       ├── components/   # UI 컴포넌트
│       ├── pages/        # 페이지
│       └── index.ts      # barrel export
└── shared/       # feature 간 공유 리소스
    ├── api/      # axiosInstance, apiTypes
    ├── components/  # 재사용 UI (layout, feedback, common)
    ├── guards/   # AuthGuard, AdminGuard
    ├── hooks/    # 공통 커스텀 훅
    └── types/    # 타입 정의
```

**feature 간 의존 규칙**: 각 feature는 독립적. 다른 feature를 직접 import 금지. 공유 필요 시 `shared/`로 이동.

### 6.2 디자인 시스템

**브랜드 아이덴티티**: Claude 시그니처 오렌지/브라운(`#D97757`) 기반의 따뜻한 토스 스타일

| 영역 | 결정 |
|------|------|
| 폰트 | Pretendard Variable (CDN, 한국어 최적화) |
| 색상 | tokens.ts에서 중앙 관리, MUI palette에 주입 |
| 간격 | 4px 배수 시스템 (xs~page) |
| 라디우스 | sm(8) ~ full(9999) |
| 그림자 | Apple 스타일 부드러운 그림자 |
| 다크모드 | `darkColor` 토큰으로 별도 정의 |

**tokens.ts 변경 원칙**: 색상·타이포·간격은 모두 `tokens.ts`에서만 변경. MUI 컴포넌트에 직접 값 하드코딩 금지.

### 6.3 Vite 핵심 설정

| 항목 | 값 |
|------|-----|
| `@` 별칭 | `./src` |
| 프록시 `/api` | `http://localhost:8080` |
| 포트 | 5173 |

### 6.4 MUI v7 주요 변경사항

- `Grid2` → `Grid`로 승격 (동일 컴포넌트, 이름 변경)
- `import Grid from '@mui/material/Grid'` 사용
- `Typography` component prop 타입 변경 — `OverridableStringUnion` 방식

---

## 7. 인프라 설계

### 7.1 Docker Compose 서비스 구성

```
┌─────────────────────────────────────────┐
│           community-network             │
│                                         │
│  ┌──────────┐  ┌──────────┐            │
│  │ postgres │  │  redis   │            │
│  │  :5432   │  │  :6379   │            │
│  └──────────┘  └──────────┘            │
│                                         │
│  ┌──────────┐  ┌──────────────────┐    │
│  │  minio   │  │   minio-init     │    │
│  │ :9000    │  │  (일회성, 버킷생성) │    │
│  │ :9001    │  └──────────────────┘    │
│  └──────────┘                           │
│                                         │
│  ┌──────────┐  ┌─────────┐  ┌──────┐  │
│  │prometheus│  │ grafana │  │ loki │  │
│  │  :9090   │  │  :3000  │  │:3100 │  │
│  └──────────┘  └─────────┘  └──────┘  │
│                                         │
│  ┌───────────────────────────────────┐  │
│  │  nginx (:80) → 5173/8080 분기    │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

### 7.2 Nginx 리버스 프록시 라우팅

| 경로 | 대상 | 용도 |
|------|------|------|
| `/api/**` | `host.docker.internal:8080` | Spring Boot API |
| `/**` | `host.docker.internal:5173` | Vite 개발 서버 |

`extra_hosts: host.docker.internal:host-gateway`로 macOS/Linux 모두 호환.

### 7.3 MinIO 버킷 구조

| 버킷 | 접근 정책 | 용도 |
|------|----------|------|
| `community-media` | public (읽기) | 프로필 이미지, 게시글 첨부파일 |

- init-minio.sh가 컨테이너 기동 시 자동 생성
- Phase 5 (미디어)에서 업로드 로직 구현

### 7.4 모니터링 스택

| 도구 | URL | 용도 |
|------|-----|------|
| Prometheus | `http://localhost:9090` | JVM/HTTP 메트릭 수집 |
| Grafana | `http://localhost:3000` | 대시보드 (admin/admin) |
| Loki | `http://localhost:3100` | 로그 수집 |

Spring Boot Actuator → Micrometer → Prometheus 수집 경로: `/actuator/prometheus`

---

## 8. CI/CD 설계

### 8.1 GitHub Actions 파이프라인

```
PR/Push to main
      │
      ├── api-check (병렬)
      │     ├── Java 25 + Gradle 설정
      │     ├── PostgreSQL 서비스 컨테이너
      │     ├── ./gradlew compileJava
      │     └── ./gradlew test
      │
      └── web-check (병렬)
            ├── Node.js 22 + npm ci
            ├── npx tsc --noEmit
            ├── npx eslint src/ --max-warnings 0
            └── npm run build
```

**병렬 실행**: api-check와 web-check는 의존성 없으므로 동시 실행. PR 검증 시간 단축.

---

## 9. 개발 환경 시작 가이드

### 9.1 최초 설정

```bash
git clone https://github.com/pistosmin/base-one.git gravity
cd gravity
cp .env.example .env
make dev          # Docker 인프라 시작
cd apps/web && npm install && cd ../..
```

### 9.2 일상 개발

```bash
make api          # 터미널 1: Spring Boot (localhost:8080)
make web          # 터미널 2: Vite (localhost:5173)
make verify       # 커밋 전: lint + test 전체 검증
```

### 9.3 포트 정리

| 서비스 | 포트 | 접근 URL |
|--------|------|---------|
| Vite 개발 서버 | 5173 | http://localhost:5173 |
| Spring Boot | 8080 | http://localhost:8080/swagger-ui.html |
| Nginx | 80 | http://localhost (프록시) |
| PostgreSQL | 5432 | jdbc:postgresql://localhost:5432/community |
| Redis | 6379 | redis://localhost:6379 |
| MinIO API | 9000 | http://localhost:9000 |
| MinIO 콘솔 | 9001 | http://localhost:9001 |
| Prometheus | 9090 | http://localhost:9090 |
| Grafana | 3000 | http://localhost:3000 |
| Loki | 3100 | http://localhost:3100 |

---

## 10. 알려진 이슈 및 해결책

| 이슈 | 원인 | 해결 |
|------|------|------|
| foojay 툴체인 설치 실패 | 0.8.0 + Gradle 9.x 비호환 | `settings.gradle.kts`에서 0.9.0 이상 사용 |
| `javax.*` import 에러 | Spring Boot 4.0에서 제거됨 | `jakarta.*` 패키지로 교체 |
| MUI Grid 타입 에러 | v7에서 Grid2 → Grid로 변경 | `import Grid from '@mui/material/Grid'` |
| `react-router-dom` not found | v7에서 패키지명 변경 | `import from 'react-router'` |
| CORS 오류 | 프론트(5173)↔백(8080) 도메인 불일치 | CorsConfig에서 origin 추가 또는 Vite proxy 사용 |
