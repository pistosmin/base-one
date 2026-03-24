# 🏗️ 커뮤니티 웹앱 서비스 - 아키텍처 설계서 & AI 에이전트 개발 플랜 v2

> **작성 목적**: Google Antigravity로 개발하고 Claude Code로 검증하는 워크플로우에 최적화된 설계 문서
> **작성일**: 2026-03-14 (v2 - 최신 버전 반영 & AI 에이전트 가이드 보강)
> **설계 원칙**: 모바일 퍼스트 웹앱 | 토스 스타일 UI/UX | MSA 확장 가능한 모놀리스

---

## ⚠️ AI 에이전트를 위한 전역 지침 (모든 태스크에 적용)

> **이 섹션은 모든 코드 생성 태스크에서 반드시 준수해야 하는 규칙입니다.**
> **Google Antigravity나 Claude Code가 이 프로젝트의 어떤 태스크를 수행하든, 아래 규칙을 항상 따르세요.**

### 코딩 컨벤션
1. **한국어 주석 필수**: 모든 클래스, 메서드, 함수, 컴포넌트에 한국어 주석을 작성합니다. 코드의 "무엇을 하는지"뿐 아니라 "왜 이렇게 하는지"도 설명합니다.
2. **파일 상단 설명**: 모든 새 파일의 최상단에 해당 파일의 목적, 의존성, 사용 위치를 한국어로 설명하는 블록 주석을 작성합니다.
3. **TypeScript strict mode**: `any` 타입 사용 금지. 모든 변수, 함수 파라미터, 반환값에 명시적 타입을 부여합니다. `unknown`으로 대체 후 타입 가드를 사용합니다.
4. **Java record 클래스**: DTO는 가능한 한 Java `record` 클래스로 작성합니다. 불변성과 간결함을 위함입니다.
5. **Lombok 사용**: Entity 클래스에는 `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`, `@Builder`를 사용합니다. `@Setter`는 사용하지 않습니다. 대신 비즈니스 메서드로 상태를 변경합니다.
6. **Git 커밋 메시지**: `feat(post): 게시글 CRUD API 구현` 형식의 conventional commit을 따릅니다.

### 에러 방지 규칙 (AI가 자주 실수하는 부분)
1. **import 경로**: React 프로젝트에서 `@/` 별칭을 사용합니다 (예: `@/shared/api/axiosInstance`). 상대 경로 `../../` 대신 별칭을 사용하세요.
2. **Spring Boot 패키지**: 패키지명은 반드시 `com.community`로 시작합니다. 다른 패키지명을 임의로 사용하지 마세요.
3. **MUI import**: MUI v7에서는 `@mui/material`에서 직접 import합니다. `@mui/material/Button` 같은 깊은 import도 가능하지만 일관성을 위해 배럴 import를 사용합니다.
4. **Spring Security**: Spring Boot 4.0에서 `SecurityFilterChain`을 `@Bean`으로 등록합니다. `WebSecurityConfigurerAdapter`는 **이미 삭제된 클래스**이므로 절대 사용하지 마세요.
5. **JPA 연관관계**: `@ManyToOne`에는 반드시 `fetch = FetchType.LAZY`를 명시합니다. 기본값인 EAGER는 N+1 문제를 유발합니다.
6. **React Hook Form + Zod**: `@hookform/resolvers/zod`를 사용하여 Zod 스키마와 연결합니다. `yup`이나 `joi`는 사용하지 마세요.
7. **Zustand store**: `create()` 함수로 스토어를 생성합니다. `createStore()`가 아닙니다. persist 미들웨어 사용 시 `import { persist } from 'zustand/middleware'`입니다.
8. **TanStack Query v5**: `useQuery({ queryKey: [...], queryFn: ... })` 형식입니다. v4의 `useQuery(key, fn)` 형식은 **더 이상 지원되지 않습니다**.
9. **Vite 프록시**: `vite.config.ts`에서 `server.proxy`로 백엔드 API를 프록시합니다. CORS 문제를 개발 환경에서 해결합니다.
10. **PostgreSQL 18**: `uuidv7()` 함수가 내장되었습니다. UUID 생성이 필요하면 이를 활용할 수 있습니다.

### 금지 사항 (절대 하지 말 것)
- ❌ `console.log`를 프로덕션 코드에 남기지 않습니다 (개발용 디버그 제외)
- ❌ API 키, 비밀번호, 시크릿을 소스 코드에 하드코딩하지 않습니다
- ❌ `@SuppressWarnings`를 무분별하게 사용하지 않습니다
- ❌ `SELECT *` 쿼리를 작성하지 않습니다 (QueryDSL로 필요한 컬럼만 선택)
- ❌ `alert()`이나 `window.confirm()`을 사용하지 않습니다 (MUI Dialog 사용)
- ❌ inline style을 직접 작성하지 않습니다 (MUI `sx` prop 또는 테마 사용)
- ❌ `useEffect` 안에서 데이터 fetching을 하지 않습니다 (TanStack Query 사용)

---

## 1. 프로젝트 개요

### 1.1 서비스 요약
사용자가 게시글을 작성하고, 댓글과 추천을 통해 소통하는 **모바일 퍼스트 커뮤니티 웹앱**.
관리자는 동일 화면에서 추가 권한으로 서비스를 관리할 수 있음.

### 1.2 핵심 기능 목록

| 카테고리 | 기능 | 우선순위 | 관련 Phase |
|----------|------|----------|------------|
| 인증 | 회원가입, 로그인, 로그아웃, 토큰 갱신 | P0 | Phase 1 |
| 프로필 | 프로필 조회/수정, 프로필 이미지 업로드 | P0 | Phase 4 |
| 게시글 | CRUD, 목록/검색, 카테고리 분류 | P0 | Phase 2 |
| 상호작용 | 추천(좋아요), 댓글 CRUD, 대댓글 | P0 | Phase 3 |
| 관리자 | 사용자 관리, 게시글 관리, 대시보드 | P1 | Phase 4 |
| 알림 | 인앱 알림 (댓글, 추천) | P1 | Phase 5 |
| 미디어 | 이미지 업로드/리사이징, 첨부파일 | P2 | Phase 5 |
| 북마크 | 게시글 저장/목록 | P2 | Phase 6 |
| 신고 | 게시글/댓글/사용자 신고 | P3 | Phase 6 |

### 1.3 개발 워크플로우

```
[이 설계서를 읽고 이해]
       ↓
[Phase별 태스크 프롬프트를 Antigravity에 전달]
       ↓
[Antigravity가 코드 생성]
       ↓
[Claude Code에 검증 프롬프트 전달]
  - 코드 리뷰 (컨벤션, 보안, 성능)
  - 테스트 실행
  - 누락된 부분 확인
       ↓
[문제 발견 시 → Antigravity에 수정 요청]
[문제 없으면 → Git commit & 다음 태스크]
```

---

## 2. 기술 스택 (2026년 3월 기준 최신 안정 버전)

### 2.1 프론트엔드 (모바일 퍼스트 웹앱)

| 구분 | 기술 | 버전 | 선정 이유 | 설치 명령어 |
|------|------|------|-----------|------------|
| 프레임워크 | React + TypeScript | **19.2.x** + **5.7.x** | 생태계 최대, AI 학습 데이터 풍부 | `npm create vite@latest` |
| 빌드 도구 | Vite | **7.3.x** | ESM 네이티브, Rolldown 기반, 매우 빠름 | Vite 7은 Node 20.19+ 필요 |
| UI 프레임워크 | MUI (Material UI) | **7.3.x** | MD3 기반, 토스 스타일 커스터마이징 가능, ESM/CJS 개선 | `npm i @mui/material @emotion/react @emotion/styled` |
| 상태 관리 | Zustand | **5.x** | 가볍고 직관적, 보일러플레이트 최소 | `npm i zustand` |
| 서버 상태 | TanStack Query | **5.90.x** | 캐싱, 무한 스크롤, 낙관적 업데이트 | `npm i @tanstack/react-query` |
| 라우팅 | React Router | **7.x** | 표준, 레이지 로딩, Data API 지원 | `npm i react-router` (v7부터 패키지명 변경) |
| 폼 관리 | React Hook Form + Zod | **7.x** + **3.x** | 타입 안전 유효성 검증 통합 | `npm i react-hook-form zod @hookform/resolvers` |
| HTTP 클라이언트 | Axios | **1.8.x** | 인터셉터, 토큰 갱신 자동화 | `npm i axios` |
| 에디터 | TipTap | **2.x** | 마크다운 + WYSIWYG, 확장성 | `npm i @tiptap/react @tiptap/starter-kit` |
| 아이콘 | Lucide React | **latest** | 가볍고 일관된 아이콘셋 | `npm i lucide-react` |
| 애니메이션 | Framer Motion | **12.x** | 토스 스타일 부드러운 전환 | `npm i framer-motion` |
| 날짜 처리 | Day.js | **1.x** | 가볍고 국제화 지원 | `npm i dayjs` |
| 테스트 | Vitest + Testing Library | **3.x** + **latest** | Vite 네이티브 테스트 | `npm i -D vitest @testing-library/react` |

> **⚠️ AI 에이전트 주의사항 - React Router v7**:
> React Router v7부터 패키지명이 `react-router-dom`에서 `react-router`로 변경되었습니다.
> ```typescript
> // ✅ 올바른 import (v7)
> import { BrowserRouter, Routes, Route, Link } from 'react-router';
> // ❌ 잘못된 import (v6 이하)
> import { BrowserRouter } from 'react-router-dom';
> ```

> **⚠️ AI 에이전트 주의사항 - MUI v7**:
> MUI v7에서 `Grid` 컴포넌트가 `GridLegacy`로 이름이 변경되었고, `Grid2`가 `Grid`로 승격되었습니다.
> ```typescript
> // ✅ MUI v7에서의 Grid 사용
> import Grid from '@mui/material/Grid';  // 이것은 Grid2입니다
> // ❌ 구버전 Grid API 사용 금지
> ```

> **⚠️ AI 에이전트 주의사항 - Vite 7**:
> Vite 7은 Node.js 20.19+ 또는 22.12+이 필요합니다. Node 18은 지원하지 않습니다.
> 기본 브라우저 타겟이 `'modules'`에서 `'baseline-widely-available'`로 변경되었습니다.

### 2.2 백엔드 (Spring Boot)

| 구분 | 기술 | 버전 | 선정 이유 | 비고 |
|------|------|------|-----------|------|
| 프레임워크 | Spring Boot | **4.0.3** | 최신 안정, Spring Framework 7 기반 | 2025년 11월 GA 릴리스 |
| 언어 | Java | **21 LTS** | Virtual Threads, Pattern Matching, Record | Spring Boot 4.0은 Java 17~25 지원 |
| 보안 | Spring Security | **7.x** | Spring Boot 4.0 번들 버전 | JWT + RBAC |
| ORM | Spring Data JPA | **4.0.x** | Spring Boot 4.0 번들 버전 | JSpecify null-safety 지원 |
| QueryDSL | QueryDSL JPA | **5.1.x** (jakarta) | 타입 안전 동적 쿼리 | jakarta classifier 필수 |
| DB | PostgreSQL | **18.3** | AIO 서브시스템 (3배 성능향상), uuidv7(), 가상 생성 컬럼 | 2025년 9월 GA |
| 캐시 | Redis | **7.4.x** | 세션, 캐시, Rate Limiting | 최신 안정 |
| 마이그레이션 | Flyway | **11.x** | SQL 기반 버전 관리 | `flyway-database-postgresql` 모듈 필요 |
| 검증 | Hibernate Validator | **8.x** | Bean Validation 3.0 | Spring Boot 4.0 번들 |
| API 문서 | SpringDoc OpenAPI | **2.8.x** | Swagger UI 자동 생성 | Spring Boot 4.0 호환 확인 필요 |
| 매핑 | MapStruct | **1.6.x** | 컴파일 타임 DTO 매핑 | 어노테이션 프로세서 설정 필요 |
| 빌드 | Gradle (Kotlin DSL) | **8.12.x** | 빌드 캐싱, 유연한 설정 | `build.gradle.kts` |
| 테스트 | JUnit 5 + Mockito + Testcontainers | **5.11.x** + **5.x** + **1.20.x** | 통합 테스트 포함 | |

> **⚠️ AI 에이전트 주의사항 - Spring Boot 4.0 주요 변경사항**:
> 1. **Java 17 최소 요구**: Java 17~25 지원. Java 11, 16은 지원하지 않습니다.
> 2. **Spring Framework 7 기반**: 이전의 Spring Framework 6이 아닙니다.
> 3. **JSpecify null-safety**: `@Nullable`, `@NonNull` 어노테이션이 JSpecify로 통합되었습니다.
> 4. **모듈화 개선**: 더 작고 집중된 JAR로 분리되었습니다.
> 5. **Jakarta EE**: `javax.*` 패키지가 아닌 `jakarta.*` 패키지를 사용합니다. **`javax.persistence`는 존재하지 않습니다. `jakarta.persistence`를 사용하세요.**
>
> ```java
> // ✅ 올바른 import (Spring Boot 4.0)
> import jakarta.persistence.Entity;
> import jakarta.persistence.Id;
> import jakarta.validation.constraints.NotBlank;
>
> // ❌ 잘못된 import (절대 사용 금지)
> import javax.persistence.Entity;  // 존재하지 않음!
> ```

> **⚠️ AI 에이전트 주의사항 - PostgreSQL 18 주요 신기능**:
> 1. **비동기 I/O (AIO)**: 순차 스캔, 비트맵 힙 스캔 등에서 최대 3배 성능 향상
> 2. **`uuidv7()`**: 타임스탬프 정렬 UUID 내장 함수 (외부 라이브러리 불필요)
> 3. **가상 생성 컬럼**: `GENERATED ALWAYS AS (...) VIRTUAL` - 쿼리 시점에 계산
> 4. **Skip Scan**: 멀티컬럼 B-tree 인덱스 활용 확대
> 5. **프로토콜 3.2**: 2003년 이후 첫 와이어 프로토콜 업그레이드

### 2.3 인프라 & DevOps

| 구분 | 기술 | 버전 | 용도 |
|------|------|------|------|
| 컨테이너 | Docker + Docker Compose | **27.x** + **v2** | 개발/배포 환경 통일 |
| CI/CD | GitHub Actions | latest | 자동화 빌드/테스트/배포 |
| 모니터링 | Grafana + Prometheus + Loki | **11.x** + **3.x** + **3.x** | 메트릭/로그/대시보드 |
| 리버스 프록시 | Nginx | **1.27.x** | HTTPS 터미네이션, 정적 파일 서빙 |
| 오브젝트 스토리지 | MinIO | **latest** | 이미지/파일 저장 (S3 호환 API) |

---

## 3. 시스템 아키텍처

### 3.1 전체 아키텍처 (텍스트 다이어그램)

```
┌─────────────────────────────────────────────────────────────────┐
│                     클라이언트 (모바일 브라우저)                     │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  React 19.2 SPA (PWA 대응)                               │    │
│  │  - MUI 7 컴포넌트 (토스 스타일 디자인 토큰)                  │    │
│  │  - Zustand 5 (클라이언트 상태)                             │    │
│  │  - TanStack Query 5 (서버 상태 + 캐싱)                    │    │
│  │  - React Router 7 (SPA 라우팅 + 코드 스플리팅)             │    │
│  └─────────────────────────────────────────────────────────┘    │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTPS (REST API + JSON)
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Nginx 1.27 (리버스 프록시)                    │
│  - TLS 1.3 터미네이션 (Let's Encrypt)                            │
│  - 정적 파일 서빙 (React 빌드 결과물)                              │
│  - /api/** → Spring Boot 포워딩                                 │
│  - Rate Limiting (요청 제한)                                     │
│  - Brotli/Gzip 압축                                             │
│  - 보안 헤더 (CSP, HSTS, X-Frame-Options 등)                    │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTP (내부 통신)
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│              Spring Boot 4.0.3 (메인 애플리케이션)                  │
│              Java 21 + Virtual Threads 활성화                     │
│                                                                  │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐          │
│  │   Auth    │ │   User   │ │   Post   │ │ Comment  │          │
│  │  Module   │ │  Module  │ │  Module  │ │  Module  │          │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐  │
│  │  Admin   │ │  Media   │ │ Bookmark │ │  Notification    │  │
│  │  Module  │ │  Module  │ │  Module  │ │  Module ★MSA후보  │  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────────────┘  │
│                                                                  │
│  ┌────────────────────────────────────────────────────────┐     │
│  │ 횡단 관심사 (Cross-Cutting Concerns)                      │     │
│  │ Spring Security 7 + JWT | GlobalExceptionHandler       │     │
│  │ AOP 로깅 | 캐시 추상화 | 입력값 검증                       │     │
│  └────────────────────────────────────────────────────────┘     │
└────────┬──────────────────┬──────────────────┬──────────────────┘
         │                  │                  │
         ▼                  ▼                  ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────────┐
│ PostgreSQL   │  │    Redis     │  │  MinIO (S3 호환)  │
│   18.3       │  │    7.4       │  │  파일/이미지 저장  │
│              │  │              │  │                    │
│ - AIO 활성화 │  │ - JWT 블랙리스│  │ - 프로필 이미지    │
│ - uuidv7()   │  │   트           │  │ - 게시글 첨부파일  │
│ - 가상 생성   │  │ - API 캐시    │  │ - 자동 썸네일      │
│   컬럼        │  │ - Rate Limit │  │                    │
└──────────────┘  └──────────────┘  └──────────────────┘
         │
         ▼
┌──────────────────────────────────────────────────────────────┐
│        모니터링 스택 (Grafana + Prometheus + Loki)             │
│  - Spring Actuator 메트릭 수집 (JVM, HTTP, DB)                │
│  - 애플리케이션 로그 수집 및 시각화                              │
│  - 커스텀 대시보드 (응답시간, 에러율, 동시접속)                   │
└──────────────────────────────────────────────────────────────┘
```

### 3.2 백엔드 모듈 구조 (모듈러 모놀리스)

> **AI 에이전트 참고**: 모든 도메인 패키지는 동일한 내부 구조를 따릅니다.
> 새 도메인을 추가할 때 아래 구조를 템플릿으로 사용하세요.

```
com.community/
├── CommunityApplication.java          # 메인 진입점 (@SpringBootApplication)
│
├── global/                             # ── 전역 설정 및 공통 모듈 ──
│   ├── config/
│   │   ├── SecurityConfig.java         # Spring Security 설정 (SecurityFilterChain 빈)
│   │   ├── JpaConfig.java              # JPA Auditing 활성화 (@EnableJpaAuditing)
│   │   ├── RedisConfig.java            # RedisTemplate 직렬화 설정
│   │   ├── CorsConfig.java             # CORS 허용 오리진 설정
│   │   ├── SwaggerConfig.java          # OpenAPI 3.0 문서 기본 정보
│   │   └── MinioConfig.java            # MinIO S3 클라이언트 빈 등록
│   │
│   ├── security/
│   │   ├── jwt/
│   │   │   ├── JwtTokenProvider.java   # 토큰 생성/검증/파싱
│   │   │   ├── JwtAuthFilter.java      # OncePerRequestFilter 구현
│   │   │   └── JwtTokenDto.java        # record: accessToken, refreshToken, expiresIn
│   │   ├── UserDetailsServiceImpl.java # UserDetailsService 구현
│   │   └── CustomAuthEntryPoint.java   # 401 JSON 응답 처리
│   │
│   ├── common/
│   │   ├── response/
│   │   │   ├── ApiResponse.java        # 통합 응답 래퍼: { success, data, error }
│   │   │   └── PageResponse.java       # 페이징 응답: { content, totalElements, totalPages, ... }
│   │   ├── entity/
│   │   │   └── BaseTimeEntity.java     # @MappedSuperclass (createdAt, updatedAt)
│   │   └── exception/
│   │       ├── ErrorCode.java          # enum (코드, HTTP 상태, 메시지)
│   │       ├── BusinessException.java  # 비즈니스 예외 (ErrorCode 포함)
│   │       └── GlobalExceptionHandler.java  # @RestControllerAdvice
│   │
│   └── util/
│       ├── SecurityUtil.java           # 현재 로그인 사용자 ID 조회
│       └── SlugUtil.java               # URL 슬러그 생성 유틸리티
│
├── domain/                             # ── 도메인별 패키지 ──
│   │
│   ├── auth/                           # [인증 도메인]
│   │   ├── controller/AuthController.java
│   │   ├── service/AuthService.java
│   │   ├── dto/
│   │   │   ├── request/                # SignupRequest, LoginRequest, TokenRefreshRequest
│   │   │   └── response/              # TokenResponse
│   │   └── repository/RefreshTokenRepository.java
│   │
│   ├── user/                           # [사용자 도메인]
│   │   ├── controller/UserController.java
│   │   ├── service/UserService.java
│   │   ├── entity/
│   │   │   ├── User.java              # @Entity, BaseTimeEntity 상속
│   │   │   └── UserRole.java          # enum: USER, ADMIN
│   │   ├── repository/UserRepository.java
│   │   ├── dto/
│   │   │   ├── request/               # UpdateProfileRequest
│   │   │   └── response/             # UserProfileResponse, UserSummaryResponse
│   │   └── mapper/UserMapper.java     # MapStruct @Mapper
│   │
│   ├── post/                           # [게시글 도메인]
│   │   ├── controller/PostController.java
│   │   ├── service/PostService.java
│   │   ├── entity/
│   │   │   ├── Post.java
│   │   │   └── Category.java
│   │   ├── repository/
│   │   │   ├── PostRepository.java
│   │   │   ├── PostRepositoryCustom.java      # QueryDSL 인터페이스
│   │   │   ├── PostRepositoryImpl.java        # QueryDSL 구현
│   │   │   └── CategoryRepository.java
│   │   ├── dto/
│   │   │   ├── request/               # CreatePostRequest, UpdatePostRequest, PostSearchRequest
│   │   │   └── response/             # PostDetailResponse, PostListResponse
│   │   └── mapper/PostMapper.java
│   │
│   ├── comment/                        # [댓글 도메인]
│   │   ├── controller/CommentController.java
│   │   ├── service/CommentService.java
│   │   ├── entity/Comment.java        # parentId로 대댓글 지원 (Adjacency List)
│   │   ├── repository/CommentRepository.java
│   │   ├── dto/
│   │   │   ├── request/               # CreateCommentRequest, UpdateCommentRequest
│   │   │   └── response/             # CommentResponse (children 필드로 트리 구조)
│   │   └── mapper/CommentMapper.java
│   │
│   ├── vote/                           # [추천 도메인]
│   │   ├── controller/VoteController.java
│   │   ├── service/VoteService.java
│   │   ├── entity/Vote.java           # targetType(POST/COMMENT) + targetId (다형적)
│   │   ├── repository/VoteRepository.java
│   │   └── dto/                       # VoteRequest, VoteResponse
│   │
│   ├── bookmark/                       # [북마크 도메인]
│   │   ├── controller/BookmarkController.java
│   │   ├── service/BookmarkService.java
│   │   ├── entity/Bookmark.java
│   │   └── repository/BookmarkRepository.java
│   │
│   ├── notification/                   # [알림 도메인] ★ MSA 분리 1순위
│   │   ├── controller/NotificationController.java
│   │   ├── service/NotificationService.java
│   │   ├── entity/
│   │   │   ├── Notification.java
│   │   │   └── NotificationType.java  # enum: COMMENT, VOTE, FOLLOW, ADMIN_NOTICE
│   │   └── repository/NotificationRepository.java
│   │
│   ├── media/                          # [미디어 도메인] ★ MSA 분리 2순위
│   │   ├── controller/MediaController.java
│   │   ├── service/MediaService.java
│   │   ├── service/ImageResizeService.java  # Thumbnailator 기반
│   │   ├── entity/Media.java
│   │   └── repository/MediaRepository.java
│   │
│   ├── report/                         # [신고 도메인]
│   │   ├── controller/ReportController.java
│   │   ├── service/ReportService.java
│   │   ├── entity/Report.java
│   │   ├── repository/ReportRepository.java
│   │   └── dto/                       # CreateReportRequest
│   │
│   └── admin/                          # [관리자 도메인]
│       ├── controller/AdminController.java
│       ├── service/AdminService.java
│       └── dto/response/             # DashboardResponse, UserManageResponse
```

### 3.3 프론트엔드 구조 (Feature-Sliced Design)

> **AI 에이전트 참고**: 프론트엔드도 백엔드와 마찬가지로 기능(feature)별로 폴더를 분리합니다.
> 각 feature 폴더는 독립적으로 동작할 수 있어야 하며, 다른 feature를 직접 import하지 않습니다.
> feature 간 공유가 필요한 코드는 반드시 `shared/` 폴더에 배치합니다.

```
apps/web/src/
├── app/                           # 앱 설정 (라우터, 프로바이더, 테마)
│   ├── App.tsx                    # 루트 컴포넌트
│   ├── router.tsx                 # 라우터 설정 (React.lazy + Suspense)
│   ├── providers.tsx              # QueryClient, ThemeProvider 등 프로바이더 합성
│   └── theme/                     # 토스 스타일 MUI 테마
│       ├── index.ts               # createTheme() 호출 및 내보내기
│       ├── palette.ts             # 라이트/다크 컬러 팔레트
│       ├── typography.ts          # Pretendard 폰트 기반 타이포그래피
│       ├── components.ts          # MUI 컴포넌트 기본 스타일 오버라이드
│       └── tokens.ts              # 디자인 토큰 (AI에게 수정 지시 시 참조점)
│
├── features/                      # 기능별 모듈 (각 feature는 독립적)
│   ├── auth/                      # [인증 기능]
│   │   ├── api/authApi.ts         # Axios 호출 함수 (login, signup, refresh, logout)
│   │   ├── hooks/useAuth.ts       # useMutation 기반 인증 훅
│   │   ├── store/authStore.ts     # Zustand: user, accessToken, isAuthenticated
│   │   ├── components/
│   │   │   ├── LoginForm.tsx      # 로그인 폼 (react-hook-form + zod)
│   │   │   └── SignupForm.tsx     # 회원가입 폼
│   │   ├── pages/
│   │   │   ├── LoginPage.tsx
│   │   │   └── SignupPage.tsx
│   │   └── index.ts               # 모듈 공개 API (barrel export)
│   │
│   ├── post/                      # [게시글 기능]
│   │   ├── api/postApi.ts
│   │   ├── hooks/
│   │   │   ├── usePosts.ts        # useInfiniteQuery (무한 스크롤)
│   │   │   ├── usePost.ts         # useQuery (단일 게시글)
│   │   │   └── usePostMutation.ts # useMutation (생성/수정/삭제)
│   │   ├── components/
│   │   │   ├── PostCard.tsx        # 목록용 카드 컴포넌트
│   │   │   ├── PostDetail.tsx      # 상세 뷰
│   │   │   ├── PostEditor.tsx      # TipTap 에디터 래퍼
│   │   │   ├── PostList.tsx        # 무한 스크롤 목록
│   │   │   └── CategoryFilter.tsx  # 카테고리 칩 필터 (가로 스크롤)
│   │   ├── pages/
│   │   │   ├── PostListPage.tsx
│   │   │   ├── PostDetailPage.tsx
│   │   │   └── PostEditorPage.tsx
│   │   └── index.ts
│   │
│   ├── comment/                   # [댓글 기능]
│   ├── user/                      # [사용자 프로필 기능]
│   ├── notification/              # [알림 기능]
│   ├── admin/                     # [관리자 기능]
│   └── home/                      # [홈 화면 기능]
│
├── shared/                        # 공유 리소스 (feature 간 공용)
│   ├── api/
│   │   ├── axiosInstance.ts       # Axios 인스턴스 + 인터셉터 (토큰 갱신 로직)
│   │   └── apiTypes.ts            # ApiResponse<T>, PageResponse<T>, ApiError
│   ├── components/
│   │   ├── layout/
│   │   │   ├── AppLayout.tsx      # Outlet + TopAppBar + BottomTabBar
│   │   │   ├── BottomTabBar.tsx   # 모바일 하단 탭 (홈/글쓰기/알림/프로필)
│   │   │   ├── TopAppBar.tsx      # 상단 앱바 (제목 + 액션)
│   │   │   └── AdminLayout.tsx    # 관리자 전용 레이아웃
│   │   ├── feedback/
│   │   │   ├── LoadingSpinner.tsx
│   │   │   ├── EmptyState.tsx
│   │   │   ├── ErrorFallback.tsx
│   │   │   └── Toast.tsx
│   │   └── common/
│   │       ├── VoteButton.tsx     # 추천 토글 (게시글/댓글 공용)
│   │       ├── UserAvatar.tsx     # 프로필 이미지 표시
│   │       └── TimeAgo.tsx        # 상대 시간 ("5분 전")
│   ├── hooks/
│   │   ├── useInfiniteScroll.ts   # IntersectionObserver 기반
│   │   ├── useDebounce.ts
│   │   └── useMediaQuery.ts
│   ├── guards/
│   │   ├── AuthGuard.tsx          # 미로그인 → /login 리다이렉트
│   │   └── AdminGuard.tsx         # 비관리자 → 접근 거부
│   ├── types/                     # 공유 TypeScript 타입
│   │   ├── user.ts
│   │   ├── post.ts
│   │   ├── comment.ts
│   │   └── common.ts
│   └── utils/
│       ├── format.ts              # 날짜/숫자 포맷 함수
│       ├── validation.ts          # 공용 Zod 스키마
│       └── storage.ts             # localStorage 래퍼 (타입 안전)
│
└── main.tsx                       # 엔트리 포인트 (ReactDOM.createRoot)
```

---

## 4. 디자인 시스템 전략

### 4.1 디자인 철학

**"토스의 직관성 + Material Design 3의 체계성 + Apple HIG의 매끄러움"**

| 참조 | 적용 포인트 | 구체적 예시 |
|------|-------------|------------|
| **토스 (TDS)** | 깔끔한 카드 UI, 여백 활용, 한 화면 한 목적 | 게시글 카드: 둥근 모서리, 넉넉한 패딩, 제목 + 요약 2줄 |
| **Material Design 3** | 컬러 시스템, 타이포그래피 스케일, 컴포넌트 규격 | MUI v7 테마 커스터마이징으로 구현 |
| **Apple HIG** | 제스처, 부드러운 애니메이션, 가독성 | Framer Motion으로 페이지 전환, 리스트 아이템 등장 |

### 4.2 디자인 토큰 체계

> **AI 에이전트를 위한 핵심 설명**:
> 디자인 토큰은 앱의 시각적 속성을 중앙에서 관리하는 "변수"입니다.
> 이 토큰을 변경하면 앱 전체에 일관되게 반영됩니다.
> 사용자가 "메인 색상을 파란색에서 초록색으로 바꿔줘"라고 요청하면,
> `tokens.color.primary` 값만 변경하면 됩니다.
>
> **MUI 테마와의 관계**: `tokens.ts`에 정의된 값을 `palette.ts`, `typography.ts`, `components.ts`에서
> MUI 테마 포맷으로 변환하여 `createTheme()`에 전달합니다.

```typescript
// apps/web/src/app/theme/tokens.ts
// ──────────────────────────────────────────────────────
// 디자인 토큰 정의 파일
// 이 파일의 값을 변경하면 앱 전체의 시각적 스타일이 바뀝니다.
// AI에게 디자인 수정을 요청할 때 이 파일의 키를 참조하세요.
// 예: "tokens.color.primary를 #10B981로 변경해줘"
// ──────────────────────────────────────────────────────

export const tokens = {
  // === 컬러 팔레트 ===
  // 토스 앱의 컬러 시스템을 참고하되, Material Design 3의 체계를 따릅니다
  color: {
    // 메인 브랜드 색상 (토스의 시그니처 블루)
    primary: '#3182F6',
    primaryLight: '#EBF3FE',     // primary의 10% 불투명도 버전 (배경용)
    primaryDark: '#1B64DA',      // hover/active 상태용

    // 시맨틱 컬러 (Apple HIG의 시스템 컬러 참고)
    success: '#34C759',          // 성공, 완료
    warning: '#FF9500',          // 경고, 주의
    error: '#FF3B30',            // 에러, 삭제, 위험
    info: '#007AFF',             // 정보성 알림

    // 배경 컬러 (토스의 회색 계열)
    background: '#F2F4F6',       // 페이지 전체 배경 (연한 회색)
    surface: '#FFFFFF',          // 카드, 모달 등 표면 배경
    surfaceVariant: '#F8F9FA',   // 입력 필드 배경 등 약간 다른 표면

    // 텍스트 컬러 (토스의 텍스트 계층)
    textPrimary: '#191F28',      // 본문, 제목 (거의 검정)
    textSecondary: '#8B95A1',    // 보조 텍스트, 날짜, 메타 정보
    textTertiary: '#B0B8C1',     // 비활성 텍스트, 플레이스홀더
    textOnPrimary: '#FFFFFF',    // primary 배경 위의 텍스트 (흰색)

    // 구분선 & 테두리
    border: '#E5E8EB',           // 카드 테두리, 구분선
    divider: '#F2F4F6',          // 리스트 아이템 사이 얇은 구분선
  },

  // === 타이포그래피 ===
  // Pretendard 폰트 사용 (한국어 최적화, 가변 폰트)
  // index.html에 <link> 태그로 Pretendard 웹폰트를 로딩해야 합니다
  typography: {
    fontFamily: '"Pretendard Variable", "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',

    // 각 스타일의 이름은 토스 TDS의 명명 규칙을 참고
    headline1: { fontSize: '26px', fontWeight: 700, lineHeight: 1.35, letterSpacing: '-0.02em' },
    headline2: { fontSize: '22px', fontWeight: 700, lineHeight: 1.36, letterSpacing: '-0.02em' },
    title1:    { fontSize: '19px', fontWeight: 600, lineHeight: 1.42 },
    title2:    { fontSize: '17px', fontWeight: 600, lineHeight: 1.41 },
    body1:     { fontSize: '16px', fontWeight: 400, lineHeight: 1.5 },
    body2:     { fontSize: '14px', fontWeight: 400, lineHeight: 1.43 },
    caption:   { fontSize: '12px', fontWeight: 400, lineHeight: 1.33 },
    button:    { fontSize: '16px', fontWeight: 600, lineHeight: 1.5 },
  },

  // === 간격 시스템 (4px 배수) ===
  // 토스 앱의 간격 체계를 참고합니다
  spacing: {
    xs: 4,        // 아이콘과 텍스트 사이 등 최소 간격
    sm: 8,        // 칩 사이, 작은 요소 간 간격
    md: 12,       // 카드 내부 요소 간 간격
    lg: 16,       // 카드 패딩, 리스트 아이템 패딩
    xl: 20,       // 섹션 내 요소 간 간격
    xxl: 24,      // 섹션 간 간격
    section: 28,  // 큰 섹션 구분 (예: 홈 화면의 "인기 글" ↔ "최신 글")
    page: 20,     // 화면 좌우 패딩 (모바일 기준)
  },

  // === 모서리 둥글기 ===
  radius: {
    sm: 8,        // 태그, 뱃지, 작은 칩
    md: 12,       // 버튼, 입력 필드
    lg: 16,       // 카드, 이미지
    xl: 20,       // 바텀시트, 모달
    full: 9999,   // 완전한 원형 (아바타, FAB)
  },

  // === 그림자 (Apple 스타일 부드러운 그림자) ===
  shadow: {
    sm: '0 1px 3px rgba(0,0,0,0.04), 0 1px 2px rgba(0,0,0,0.06)',
    md: '0 4px 12px rgba(0,0,0,0.08)',
    lg: '0 8px 24px rgba(0,0,0,0.12)',
    bottomSheet: '0 -4px 24px rgba(0,0,0,0.12)',
  },

  // === 애니메이션 (Framer Motion에서 사용) ===
  motion: {
    duration: {
      fast: 150,     // 밀리초. 버튼 피드백, 토글 등 즉각적 반응
      normal: 250,   // 페이지 전환, 카드 등장
      slow: 350,     // 바텀시트, 모달 등장
    },
    easing: {
      standard: [0.4, 0.0, 0.2, 1],       // Material 표준 이징
      decelerate: [0.0, 0.0, 0.2, 1],     // 요소가 화면에 진입할 때
      accelerate: [0.4, 0.0, 1, 1],       // 요소가 화면에서 퇴장할 때
      spring: { type: 'spring', stiffness: 300, damping: 24 }, // 토스 스타일 탄성
    },
  },

  // === 반응형 브레이크포인트 (모바일 퍼스트) ===
  breakpoint: {
    mobile: 0,       // 0 ~ 599px  (기본, 모바일 우선 설계)
    tablet: 600,     // 600 ~ 1023px
    desktop: 1024,   // 1024px ~ (관리자 대시보드 등)
  },

  // === Z-Index 계층 ===
  zIndex: {
    bottomTabBar: 1000,
    topAppBar: 1100,
    modal: 1300,
    toast: 1400,
  },
} as const;

// 타입 내보내기 (다른 파일에서 참조용)
export type DesignTokens = typeof tokens;
```

### 4.3 AI 디자인 수정 가이드

디자인 수정 요청 시 아래 형식을 사용하면 AI가 정확히 수정 가능합니다:

```
✅ 좋은 수정 요청 예시:
- "tokens.color.primary를 #10B981로 변경하고, primaryLight도 맞춰서 조정해줘"
- "PostCard 컴포넌트의 border-radius를 tokens.radius.xl (20px)로 변경해줘"
- "전체 body1 fontSize를 15px로, lineHeight를 1.6으로 조정해줘"
- "BottomTabBar의 그림자를 tokens.shadow.lg로 변경해줘"
- "페이지 좌우 패딩(tokens.spacing.page)을 16px로 줄여줘"

❌ 나쁜 수정 요청 예시:
- "색상 좀 바꿔줘" → 어떤 색상을 어디에서 무엇으로 바꿀지 불명확
- "글씨가 좀 큰 것 같아" → 어느 텍스트 스타일의 fontSize를 몇으로 바꿀지 불명확
```

---

## 5. 데이터베이스 설계

> **AI 에이전트 참고**: 아래 SQL은 Flyway 마이그레이션 파일로 작성됩니다.
> 파일 경로: `apps/api/src/main/resources/db/migration/`
> 파일명 규칙: `V{번호}__{설명}.sql` (언더바 2개)
> 예: `V1__create_users_table.sql`
>
> **PostgreSQL 18 전용 기능 활용**:
> - `uuidv7()` 함수를 PK 기본값으로 사용 가능 (선택사항, 이 프로젝트에서는 BIGSERIAL 사용)
> - 가상 생성 컬럼: 자주 계산되는 파생 값에 활용 가능

### 5.1 테이블 스키마

```sql
-- =============================================
-- V1__create_users_table.sql
-- 사용자 테이블
-- 서비스의 핵심 엔티티. 인증, 게시글 작성, 관리자 기능의 기반.
-- =============================================
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,       -- BCrypt 해싱된 비밀번호
    nickname        VARCHAR(50) NOT NULL UNIQUE,  -- 2~20자, 고유
    bio             VARCHAR(300),                 -- 자기소개 (선택)
    profile_image   VARCHAR(500),                 -- MinIO URL (선택)
    role            VARCHAR(20) NOT NULL DEFAULT 'USER',  -- USER 또는 ADMIN
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,        -- 계정 활성 상태 (정지 시 FALSE)
    last_login_at   TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 이메일 검색 (로그인 시 사용)
CREATE INDEX idx_users_email ON users(email);
-- 닉네임 검색 (중복 확인, 프로필 조회)
CREATE INDEX idx_users_nickname ON users(nickname);

-- =============================================
-- V2__create_categories_table.sql
-- 게시글 카테고리
-- 정적 데이터. 관리자가 추가/수정 가능.
-- =============================================
CREATE TABLE categories (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50) NOT NULL UNIQUE,  -- 표시명 (예: "자유")
    slug            VARCHAR(50) NOT NULL UNIQUE,  -- URL용 (예: "free")
    description     VARCHAR(200),
    sort_order      INT NOT NULL DEFAULT 0,       -- 표시 순서 (오름차순)
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 기본 카테고리 삽입
INSERT INTO categories (name, slug, description, sort_order) VALUES
    ('자유', 'free', '자유롭게 이야기하는 공간', 1),
    ('질문', 'question', '궁금한 것을 물어보는 공간', 2),
    ('정보', 'info', '유용한 정보를 공유하는 공간', 3),
    ('리뷰', 'review', '리뷰와 후기를 나누는 공간', 4);

-- =============================================
-- V3__create_posts_table.sql
-- 게시글 테이블
-- vote_count, comment_count는 비정규화 컬럼 (성능 최적화)
-- =============================================
CREATE TABLE posts (
    id              BIGSERIAL PRIMARY KEY,
    author_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id     BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    title           VARCHAR(200) NOT NULL,
    content         TEXT NOT NULL,                 -- TipTap 에디터의 HTML 출력
    summary         VARCHAR(300),                  -- 목록 미리보기용 (content에서 추출)
    thumbnail_url   VARCHAR(500),                  -- 대표 이미지 URL (선택)
    view_count      INT NOT NULL DEFAULT 0,
    vote_count      INT NOT NULL DEFAULT 0,        -- 비정규화: 추천 수 (매번 COUNT 쿼리 방지)
    comment_count   INT NOT NULL DEFAULT 0,        -- 비정규화: 댓글 수
    is_pinned       BOOLEAN NOT NULL DEFAULT FALSE, -- 관리자 공지 고정
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE, -- Soft Delete
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 작성자별 게시글 조회
CREATE INDEX idx_posts_author ON posts(author_id);
-- 카테고리별 필터링
CREATE INDEX idx_posts_category ON posts(category_id);
-- 최신순 정렬 (기본 목록)
CREATE INDEX idx_posts_created ON posts(created_at DESC) WHERE is_deleted = FALSE;
-- 인기순 정렬
CREATE INDEX idx_posts_vote ON posts(vote_count DESC) WHERE is_deleted = FALSE;

-- =============================================
-- V4__create_comments_table.sql
-- 댓글 테이블 (Adjacency List 패턴으로 대댓글 지원)
-- parent_id가 NULL이면 최상위 댓글, 값이 있으면 대댓글
-- =============================================
CREATE TABLE comments (
    id              BIGSERIAL PRIMARY KEY,
    post_id         BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    author_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    parent_id       BIGINT REFERENCES comments(id) ON DELETE CASCADE,
    content         TEXT NOT NULL,
    vote_count      INT NOT NULL DEFAULT 0,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_comments_post ON comments(post_id);
CREATE INDEX idx_comments_parent ON comments(parent_id);

-- =============================================
-- V5__create_votes_bookmarks_table.sql
-- 추천과 북마크
-- =============================================

-- 추천 (게시글/댓글 통합)
-- UNIQUE 제약으로 동일 사용자의 중복 추천 방지
CREATE TABLE votes (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_type     VARCHAR(20) NOT NULL CHECK (target_type IN ('POST', 'COMMENT')),
    target_id       BIGINT NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, target_type, target_id)
);

CREATE INDEX idx_votes_target ON votes(target_type, target_id);

-- 북마크
CREATE TABLE bookmarks (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id         BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, post_id)
);

-- =============================================
-- V6__create_notifications_table.sql
-- 알림
-- recipient_id에 복합 인덱스: 안읽은 알림을 빠르게 조회
-- =============================================
CREATE TABLE notifications (
    id              BIGSERIAL PRIMARY KEY,
    recipient_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    actor_id        BIGINT REFERENCES users(id) ON DELETE SET NULL,
    type            VARCHAR(30) NOT NULL CHECK (type IN ('COMMENT', 'VOTE', 'FOLLOW', 'ADMIN_NOTICE')),
    target_type     VARCHAR(20),
    target_id       BIGINT,
    message         VARCHAR(300) NOT NULL,
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 내 알림 목록 조회 (안읽은 것 우선, 최신순)
CREATE INDEX idx_notifications_recipient ON notifications(recipient_id, is_read, created_at DESC);

-- =============================================
-- V7__create_media_reports_tokens_table.sql
-- 미디어, 신고, 리프레시 토큰
-- =============================================

CREATE TABLE media (
    id              BIGSERIAL PRIMARY KEY,
    uploader_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    original_name   VARCHAR(255) NOT NULL,        -- 원본 파일명
    stored_path     VARCHAR(500) NOT NULL,         -- MinIO 저장 경로
    thumbnail_path  VARCHAR(500),                  -- 썸네일 경로 (이미지인 경우)
    file_size       BIGINT NOT NULL,               -- 바이트 단위
    mime_type       VARCHAR(100) NOT NULL,
    width           INT,                           -- 이미지 너비 (이미지인 경우)
    height          INT,                           -- 이미지 높이
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE reports (
    id              BIGSERIAL PRIMARY KEY,
    reporter_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_type     VARCHAR(20) NOT NULL CHECK (target_type IN ('USER', 'POST', 'COMMENT')),
    target_id       BIGINT NOT NULL,
    reason          VARCHAR(50) NOT NULL CHECK (reason IN ('SPAM', 'ABUSE', 'INAPPROPRIATE', 'COPYRIGHT', 'OTHER')),
    description     VARCHAR(500),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'RESOLVED', 'DISMISSED')),
    resolved_by     BIGINT REFERENCES users(id),
    resolved_at     TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reports_status ON reports(status, created_at DESC);

CREATE TABLE refresh_tokens (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token           VARCHAR(500) NOT NULL UNIQUE,
    expires_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
```

---

## 6. API 설계 (RESTful)

> **AI 에이전트 참고**: 모든 API는 `/api/v1/` 접두사를 사용합니다.
> 응답 형식은 항상 `ApiResponse<T>` 래퍼를 사용합니다:
> ```json
> // 성공 시
> { "success": true, "data": { ... }, "error": null }
>
> // 실패 시
> { "success": false, "data": null, "error": { "code": "POST_NOT_FOUND", "message": "게시글을 찾을 수 없습니다." } }
> ```
>
> **페이징 응답**은 `PageResponse<T>`를 사용합니다:
> ```json
> {
>   "success": true,
>   "data": {
>     "content": [ ... ],
>     "totalElements": 150,
>     "totalPages": 8,
>     "page": 0,
>     "size": 20,
>     "hasNext": true,
>     "hasPrevious": false
>   }
> }
> ```

### 6.1 전체 API 엔드포인트 목록

#### 인증 API (`/api/v1/auth`)
| Method | Endpoint | 설명 | 인증 | 요청 Body |
|--------|----------|------|------|-----------|
| POST | /signup | 회원가입 | ✗ | `{ email, password, nickname }` |
| POST | /login | 로그인 | ✗ | `{ email, password }` |
| POST | /refresh | 토큰 갱신 | ✗ | `{ refreshToken }` |
| POST | /logout | 로그아웃 | ✓ | (없음, Authorization 헤더만) |

#### 사용자 API (`/api/v1/users`)
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | /me | 내 프로필 조회 | ✓ |
| PATCH | /me | 내 프로필 수정 | ✓ |
| GET | /{id} | 공개 프로필 조회 | ✗ |
| POST | /me/avatar | 프로필 이미지 업로드 | ✓ |

#### 게시글 API (`/api/v1/posts`)
| Method | Endpoint | 설명 | 인증 | 비고 |
|--------|----------|------|------|------|
| GET | / | 게시글 목록 | ✗ | 쿼리파라미터: page, size, categoryId, sort |
| POST | / | 게시글 작성 | ✓ | |
| GET | /{id} | 게시글 상세 | ✗ | 조회수 증가 |
| PATCH | /{id} | 게시글 수정 | ✓ | 작성자 또는 ADMIN만 |
| DELETE | /{id} | 게시글 삭제 | ✓ | Soft Delete. 작성자 또는 ADMIN만 |
| GET | /search | 게시글 검색 | ✗ | 쿼리파라미터: keyword |

#### 댓글 API
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | /api/v1/posts/{postId}/comments | 댓글 목록 (트리) | ✗ |
| POST | /api/v1/posts/{postId}/comments | 댓글 작성 | ✓ |
| PATCH | /api/v1/comments/{id} | 댓글 수정 | ✓ (작성자) |
| DELETE | /api/v1/comments/{id} | 댓글 삭제 | ✓ (작성자/ADMIN) |

#### 추천/북마크 API
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | /api/v1/votes | 추천 토글 | ✓ |
| POST | /api/v1/bookmarks | 북마크 토글 | ✓ |
| GET | /api/v1/bookmarks | 내 북마크 목록 | ✓ |

#### 알림 API (`/api/v1/notifications`)
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | / | 알림 목록 (페이징) | ✓ |
| PATCH | /{id}/read | 읽음 처리 | ✓ |
| PATCH | /read-all | 전체 읽음 | ✓ |
| GET | /unread-count | 안읽은 알림 수 | ✓ |

#### 미디어 API
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | /api/v1/media/upload | 이미지 업로드 | ✓ |

#### 신고 API
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | /api/v1/reports | 신고 접수 | ✓ |

#### 관리자 API (`/api/v1/admin`) - 모두 ADMIN 역할 필요
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /dashboard | 통계 대시보드 |
| GET | /users | 사용자 관리 목록 |
| PATCH | /users/{id}/role | 역할 변경 |
| PATCH | /users/{id}/ban | 정지/해제 |
| DELETE | /posts/{id} | 게시글 강제 삭제 |
| GET | /reports | 신고 목록 |
| PATCH | /reports/{id} | 신고 처리 |

---

## 7. 보안 설계

### 7.1 JWT 인증 흐름

```
[1. 로그인 요청] POST /api/v1/auth/login { email, password }
       ↓
[2. 서버 검증] 이메일로 User 조회 → BCrypt.matches(password, hash)
       ↓
[3. 토큰 발급]
   - Access Token: JWT (만료 15분, body에 userId/role 포함)
   - Refresh Token: 랜덤 UUID (만료 7일, DB에 저장)
       ↓
[4. 클라이언트 저장]
   - Access Token → Zustand store (메모리) + localStorage
   - Refresh Token → httpOnly secure cookie (또는 localStorage)
       ↓
[5. API 요청 시] Axios 인터셉터가 자동으로 Authorization: Bearer {accessToken} 추가
       ↓
[6. Access Token 만료 (401 응답)]
   → Axios 인터셉터가 /auth/refresh 자동 호출
   → 새 Access Token 발급
   → 실패한 요청 자동 재시도
   → 갱신 중 다른 요청은 큐에 대기 (동시 갱신 방지)
       ↓
[7. Refresh Token도 만료]
   → 로그아웃 처리
   → 로그인 페이지로 리다이렉트
```

> **⚠️ AI 에이전트 주의사항 - Axios 인터셉터 구현 시**:
> - 토큰 갱신 중 다른 요청이 동시에 401을 받으면, 갱신 요청을 한 번만 보내야 합니다.
> - `isRefreshing` 플래그와 `failedQueue` 배열로 동시 갱신을 방지합니다.
> - 이 패턴은 "Axios Refresh Token Interceptor"로 검색하면 많은 레퍼런스가 있습니다.

### 7.2 보안 체크리스트

| 영역 | 대응 | 구현 위치 |
|------|------|-----------|
| 비밀번호 | BCrypt 해싱 (cost factor 12) | AuthService |
| SQL Injection | JPA 파라미터 바인딩 + QueryDSL | Repository 계층 |
| XSS | React 기본 이스케이핑 + DOMPurify (에디터 콘텐츠) | PostDetail 컴포넌트 |
| CSRF | JWT Stateless → CSRF 토큰 불필요 | SecurityConfig |
| Rate Limiting | Redis 기반 (로그인: 5회/분, API: 100회/분) | 커스텀 필터 |
| CORS | 허용 오리진 명시 (개발: localhost:5173) | CorsConfig |
| 파일 업로드 | MIME 검증 + 확장자 화이트리스트 + 10MB 제한 | MediaService |
| 권한 검증 | `@PreAuthorize` + 서비스 레이어 소유권 검증 | Controller + Service |
| HTTPS | Nginx SSL 터미네이션 | nginx.conf |
| 민감정보 | 환경 변수로 관리 (.env), 소스에 절대 하드코딩 금지 | application.yml |

---

## 8. 프로젝트 파일 구조

```
project-root/
├── apps/
│   ├── web/                      # React 프론트엔드
│   │   ├── public/
│   │   │   ├── manifest.json     # PWA 매니페스트
│   │   │   └── favicon.svg
│   │   ├── src/                  # (섹션 3.3 구조 참조)
│   │   ├── index.html            # Pretendard 폰트 <link> 포함
│   │   ├── vite.config.ts        # 별칭(@/), 프록시(/api → :8080)
│   │   ├── tsconfig.json         # strict: true, 별칭 paths
│   │   ├── eslint.config.mjs     # ESLint 9 Flat Config
│   │   ├── vitest.config.ts
│   │   └── package.json
│   │
│   └── api/                      # Spring Boot 백엔드
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/com/community/  # (섹션 3.2 구조 참조)
│       │   │   └── resources/
│       │   │       ├── application.yml
│       │   │       ├── application-dev.yml
│       │   │       ├── application-prod.yml
│       │   │       └── db/migration/     # Flyway SQL 파일들
│       │   └── test/java/com/community/
│       ├── build.gradle.kts
│       └── Dockerfile
│
├── infra/
│   ├── docker/
│   │   ├── nginx/
│   │   │   ├── nginx.conf
│   │   │   └── Dockerfile
│   │   └── postgres/
│   │       └── init.sql
│   ├── monitoring/
│   │   ├── grafana/
│   │   │   ├── provisioning/     # 데이터소스 & 대시보드 자동 설정
│   │   │   └── dashboards/       # JSON 대시보드 파일
│   │   └── prometheus/
│   │       └── prometheus.yml
│   └── scripts/
│       └── init-minio.sh         # MinIO 초기 버킷 생성 스크립트
│
├── docs/
│   ├── ARCHITECTURE.md            # 이 문서
│   ├── API.md                     # API 상세 명세 (Swagger와 별도)
│   └── DESIGN-TOKENS.md           # 디자인 토큰 가이드
│
├── docker-compose.yml             # 개발 환경 (PostgreSQL, Redis, MinIO, 모니터링)
├── docker-compose.prod.yml        # 프로덕션 환경 (전체 서비스)
├── Makefile                       # 원클릭 명령어 (make dev, make test 등)
├── CLAUDE.md                      # AI 어시스턴트 컨텍스트 (프로젝트 요약)
├── .env.example                   # 환경 변수 템플릿
├── .gitignore
└── README.md                      # 프로젝트 소개 & 시작 가이드
```

---

## 9. AI 에이전트 최적화 개발 플랜

### 9.1 플랜 사용 방법

1. **한 번에 하나의 태스크**만 Antigravity에 전달합니다.
2. 각 태스크 프롬프트를 **그대로 복사하여** Antigravity에 붙여넣기 합니다.
3. 태스크 완료 후 **Claude Code에 검증 프롬프트**를 전달합니다.
4. 문제 없으면 Git commit 후 다음 태스크로 진행합니다.

### 9.2 검증 프롬프트 (Claude Code용)

각 태스크 완료 후 Claude Code에 전달할 표준 검증 프롬프트:

```
[검증 요청]
방금 완성된 코드를 아래 기준으로 검증해주세요:

1. 코딩 컨벤션
   - 모든 클래스, 메서드, 함수에 한국어 주석이 있는가?
   - 파일 상단에 파일 목적 설명이 있는가?
   - TypeScript에서 any 타입을 사용하지 않았는가?

2. 정확성
   - import 경로가 올바른가? (@/ 별칭 사용, 패키지명 정확)
   - Spring Boot 4.0 호환 코드인가? (jakarta.* 패키지 사용)
   - MUI v7 API를 올바르게 사용했는가?
   - React Router v7 문법을 사용했는가?

3. 보안
   - SQL 인젝션 가능성이 없는가?
   - XSS 취약점이 없는가?
   - 인증/인가 누락이 없는가?
   - 민감 정보가 하드코딩되지 않았는가?

4. 에러 처리
   - 모든 API 호출에 에러 처리가 있는가?
   - BusinessException 사용이 적절한가?
   - 사용자에게 의미 있는 에러 메시지를 제공하는가?

5. 성능
   - JPA N+1 쿼리 문제가 없는가?
   - 불필요한 리렌더링 요소가 없는가?
   - 적절한 인덱스가 설정되었는가?

6. 누락
   - 이 태스크에서 요구한 모든 파일이 생성되었는가?
   - 기존 파일과의 연결(import, 라우트 등)이 누락되지 않았는가?

문제를 발견하면 수정 코드와 함께 알려주세요.
```

---

## Phase 0: 프로젝트 부트스트랩

> **이 Phase의 목표**: 개발을 시작할 수 있는 빈 프로젝트 골격을 만드는 것.
> 비즈니스 로직은 포함하지 않으며, 인프라 설정과 프로젝트 구조만 생성합니다.
> Phase 0 완료 후 `make dev`로 PostgreSQL, Redis, MinIO가 정상 기동되어야 합니다.

### Task 0.1: 프로젝트 루트 구조 & 설정 파일

```
프로젝트 루트 디렉토리를 설정해줘.

[배경 설명]
이 프로젝트는 React + Spring Boot 기반의 커뮤니티 웹앱이야.
모바일 퍼스트로 설계하고, Docker Compose로 개발 환경을 구성해.

[생성할 디렉토리]
- apps/web/         (React 프론트엔드 - 내부는 다음 태스크에서 생성)
- apps/api/         (Spring Boot 백엔드 - 내부는 다음 태스크에서 생성)
- infra/docker/nginx/
- infra/docker/postgres/
- infra/monitoring/grafana/provisioning/
- infra/monitoring/grafana/dashboards/
- infra/monitoring/prometheus/
- infra/scripts/
- docs/

[생성할 파일]
1. .gitignore
   - Node.js: node_modules, dist, .env
   - Java: build/, .gradle/, *.class, *.jar
   - IDE: .idea/, .vscode/, *.iml
   - OS: .DS_Store, Thumbs.db
   - Docker: 빈 볼륨 디렉토리

2. .env.example (모든 환경 변수를 예시값과 함께)
   - DB: POSTGRES_HOST, POSTGRES_PORT, POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD
   - Redis: REDIS_HOST, REDIS_PORT
   - MinIO: MINIO_ENDPOINT, MINIO_ACCESS_KEY, MINIO_SECRET_KEY, MINIO_BUCKET
   - JWT: JWT_SECRET, JWT_ACCESS_EXPIRATION, JWT_REFRESH_EXPIRATION
   - App: APP_PORT, APP_ENV

3. Makefile (명령어별 한국어 설명 주석 필수)
   - make dev       → docker compose up -d
   - make down      → docker compose down
   - make logs      → docker compose logs -f
   - make reset     → docker compose down -v && docker compose up -d
   - make web       → cd apps/web && npm run dev
   - make api       → cd apps/api && ./gradlew bootRun
   - make test      → 프론트+백 테스트 모두 실행
   - make build     → 프론트+백 프로덕션 빌드

4. README.md
   - 프로젝트 소개 (한국어)
   - 기술 스택 표 (프론트/백/인프라)
   - 빠른 시작 가이드 (git clone → make dev → make web / make api)
   - 디렉토리 구조 설명

[주의사항]
- apps/web/, apps/api/ 내부에는 아무 파일도 생성하지 마세요. 빈 디렉토리만.
- 각 파일에 한국어 주석을 상세하게 작성해주세요.
- .env.example의 예시값은 로컬 Docker 기준으로 작성해주세요.
```

### Task 0.2: Docker Compose 개발 환경

```
Docker Compose로 개발 인프라를 구성해줘.

[배경 설명]
이 파일은 개발 시 필요한 데이터베이스, 캐시, 파일 스토리지, 모니터링 서비스를 한 번에 시작시켜.
`make dev` (= docker compose up -d) 한 번으로 모든 인프라가 준비되어야 해.

[생성할 파일: docker-compose.yml]

포함할 서비스 (총 6개):

1. postgres
   - 이미지: postgres:18-alpine (최신 PostgreSQL 18)
   - 포트: 5432:5432
   - 환경변수: .env에서 로드
   - 볼륨: pgdata:/var/lib/postgresql/data
   - 헬스체크: pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}
   - 한국어 주석: "메인 데이터베이스. 사용자, 게시글, 댓글 등 모든 데이터 저장"

2. redis
   - 이미지: redis:7.4-alpine
   - 포트: 6379:6379
   - 볼륨: redis-data:/data
   - 헬스체크: redis-cli ping
   - 한국어 주석: "캐시 및 세션 저장소. JWT 블랙리스트, API 응답 캐시, Rate Limiting"

3. minio
   - 이미지: minio/minio:latest
   - 포트: 9000:9000 (API), 9001:9001 (Console)
   - 명령어: server /data --console-address ":9001"
   - 볼륨: minio-data:/data
   - 환경변수: MINIO_ROOT_USER, MINIO_ROOT_PASSWORD
   - 한국어 주석: "S3 호환 오브젝트 스토리지. 이미지, 파일 업로드 저장"

4. minio-init (일회성 초기화 컨테이너)
   - 이미지: minio/mc:latest
   - depends_on: minio (healthy)
   - entrypoint: infra/scripts/init-minio.sh를 실행
   - 한국어 주석: "MinIO 초기 버킷(community-media)을 자동 생성하는 일회성 컨테이너"

5. prometheus
   - 이미지: prom/prometheus:v3.2.0
   - 포트: 9090:9090
   - 볼륨: infra/monitoring/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml

6. grafana
   - 이미지: grafana/grafana:11.5.0
   - 포트: 3000:3000
   - 환경변수: GF_SECURITY_ADMIN_PASSWORD=admin, GF_AUTH_ANONYMOUS_ENABLED=true
   - 볼륨: grafana-data, provisioning 디렉토리 마운트

7. loki
   - 이미지: grafana/loki:3.4.0
   - 포트: 3100:3100

[추가 생성 파일]

1. infra/scripts/init-minio.sh
   - mc alias set myminio http://minio:9000 ...
   - mc mb myminio/community-media --ignore-existing
   - mc anonymous set download myminio/community-media
   - 실행 권한 설정 필요 (chmod +x)

2. infra/monitoring/prometheus/prometheus.yml
   - scrape 대상: Spring Boot Actuator (host.docker.internal:8080/actuator/prometheus)
   - scrape_interval: 15s

[요구사항]
- 모든 서비스가 community-network라는 Docker 네트워크에 연결
- depends_on에 condition: service_healthy 사용
- 각 서비스에 한국어 주석으로 목적 설명
- .env 파일에서 환경변수 로드
- Makefile 업데이트: make dev, make down, make logs가 이 docker-compose.yml 사용하도록
```

### Task 0.3: React 프로젝트 초기화

```
React 프론트엔드 프로젝트를 초기화해줘.

[배경 설명]
모바일 퍼스트 커뮤니티 웹앱의 프론트엔드야.
React 19.2 + TypeScript + Vite 7을 사용하고,
MUI v7 테마를 토스 스타일로 커스터마이징할 거야.

[경로] apps/web/

[기술 버전 - 이 버전을 반드시 사용]
- React: 19.2.x
- TypeScript: 5.7.x
- Vite: 7.3.x (Node.js 20.19+ 필요)
- MUI: 7.3.x (@mui/material)
- React Router: 7.x (패키지명: react-router, NOT react-router-dom)
- TanStack Query: 5.90.x (@tanstack/react-query)
- Zustand: 5.x
- Axios: 1.8.x
- React Hook Form: 7.x
- Zod: 3.x
- Framer Motion: 12.x
- Lucide React: latest
- Day.js: 1.x
- Vitest: 3.x

[설정]
1. Vite 설정 (vite.config.ts)
   - 별칭: '@' → './src' (모든 import에서 @/ 사용)
   - 프록시: '/api' → 'http://localhost:8080' (백엔드 포워딩)
   - 빌드 최적화: rollupOptions에서 vendor 청크 분리
   - 각 설정 옵션에 한국어 주석

2. TypeScript 설정 (tsconfig.json)
   - strict: true
   - paths: { "@/*": ["./src/*"] }
   - target: ES2022
   - module: ESNext

3. ESLint 9 Flat Config (eslint.config.mjs)
   - @eslint/js recommended
   - typescript-eslint strict
   - react hooks 규칙
   - no-unused-vars: error
   - no-console: warn

4. Vitest 설정 (vitest.config.ts)
   - react 환경 (jsdom)
   - 커버리지: v8 provider

[디렉토리 구조 생성]
src/
├── app/
│   ├── App.tsx           → <BrowserRouter><Routes>...</Routes></BrowserRouter>
│   ├── router.tsx        → 기본 라우트: / (홈), /login, /signup
│   ├── providers.tsx     → QueryClientProvider + ThemeProvider 합성
│   └── theme/            → 빈 폴더 (다음 태스크에서 생성)
├── features/             → 빈 폴더
├── shared/               → 빈 폴더
└── main.tsx              → createRoot + <App />

index.html:
- Pretendard Variable 웹폰트 CDN 링크 추가
- 뷰포트 메타태그: width=device-width, initial-scale=1, maximum-scale=1
- PWA 관련 메타태그

[주의사항]
- React Router v7: `from 'react-router'`로 import. `react-router-dom` 아님!
- MUI v7: `@mui/material` 패키지. `@mui/material/Grid`는 Grid2임.
- Vite 7: Node.js 20.19+ 필요. package.json에 engines 필드 추가.
- 모든 설정 파일에 각 옵션이 무엇인지 한국어 주석 달기
```

### Task 0.4: MUI 테마 & 디자인 토큰 설정

```
토스 스타일의 MUI v7 테마를 설정해줘.

[배경 설명]
이 프로젝트의 UI는 토스 앱의 깔끔한 스타일을 참고해.
Material Design 3의 체계를 따르되, 토스처럼 미니멀하고 모바일 친화적으로 커스터마이징해.
Apple HIG의 부드러운 애니메이션과 가독성 원칙도 반영해.

모든 디자인 속성은 "디자인 토큰"으로 중앙 관리해서,
나중에 AI에게 "tokens.color.primary를 #10B981로 바꿔줘"라고 하면
토큰 값만 수정하면 앱 전체가 바뀌도록 설계해.

[경로] apps/web/src/app/theme/

[생성할 파일]

1. tokens.ts
   - 이 문서의 "4.2 디자인 토큰 체계" 섹션의 코드를 그대로 사용
   - `as const`로 타입 추론 최적화
   - DesignTokens 타입 내보내기

2. palette.ts
   - tokens.color를 MUI createTheme의 palette로 변환
   - Light 모드와 Dark 모드 모두 정의
   - MUI의 primary, secondary, error, warning, success, info, background, text에 매핑
   - 각 매핑에 "왜 이 토큰이 이 MUI 속성에 대응하는지" 주석

3. typography.ts
   - tokens.typography를 MUI createTheme의 typography로 변환
   - fontFamily: tokens.typography.fontFamily
   - h1~h6, body1, body2, caption, button 모두 정의
   - 한국어 최적화: letterSpacing, lineHeight 조정

4. components.ts
   - MUI 컴포넌트의 기본 스타일 오버라이드 (styleOverrides)
   - 오버라이드할 컴포넌트 목록:
     * MuiButton: 높이 52px, 풀 너비 옵션, radius.md, 토스 스타일
     * MuiCard: radius.lg, shadow.sm, 패딩 spacing.lg
     * MuiTextField: radius.md, 배경 surfaceVariant, 테두리 없음 (outlined 변형)
     * MuiAppBar: 그림자 없음, 배경 surface, 텍스트 textPrimary
     * MuiBottomNavigation: shadow.bottomSheet, 배경 surface
     * MuiChip: radius.sm, 작은 크기
     * MuiDialog: radius.xl
     * MuiIconButton: hover 시 배경 primaryLight
   - 각 오버라이드에 "토스/Apple에서 어떤 부분을 참고했는지" 주석

5. index.ts
   - createTheme()으로 palette + typography + components 합치기
   - 생성된 테마 내보내기 (export default theme)

[수정할 파일]
- providers.tsx: ThemeProvider에 생성된 테마 적용
- CssBaseline 추가 (MUI 기본 CSS 리셋)

[MUI v7 주의사항]
- MUI v7에서 Grid가 Grid2로 변경됨 (components.ts에서 MuiGrid2로 오버라이드)
- CSS layers 지원: StyledEngineProvider에 enableCssLayer 옵션 가능
- slot 패턴 표준화됨

[주의사항]
- Pretendard 폰트가 로딩되지 않았을 때의 폴백 폰트 체인 확인
- Dark 모드에서도 가독성이 보장되는 컬러 선택
- 모든 파일에 상세한 한국어 주석 필수
```

### Task 0.5: Spring Boot 프로젝트 초기화

```
Spring Boot 백엔드 프로젝트를 초기화해줘.

[배경 설명]
커뮤니티 웹앱의 REST API 서버야.
Spring Boot 4.0.3 + Java 21 + Gradle Kotlin DSL을 사용해.
Virtual Threads를 활성화해서 동시 요청 처리 성능을 높여.

[경로] apps/api/

[기술 버전 - 이 버전을 반드시 사용]
- Spring Boot: 4.0.3 (Spring Framework 7 기반)
- Java: 21 LTS
- Gradle: 8.12.x (Kotlin DSL)
- Spring Security: 7.x (Spring Boot 4.0 번들)
- Spring Data JPA: 4.0.x (Spring Boot 4.0 번들)
- QueryDSL: 5.1.x (jakarta classifier)
- Flyway: 11.x + flyway-database-postgresql 모듈
- PostgreSQL JDBC: 42.7.x
- jjwt: 0.12.x (jjwt-api, jjwt-impl, jjwt-jackson)
- SpringDoc OpenAPI: 2.8.x
- MapStruct: 1.6.x
- Lombok: latest
- Testcontainers: 1.20.x

[Gradle 설정 (build.gradle.kts)]
- Spring Boot 4.0.3 플러그인
- Java 21 타겟
- QueryDSL 어노테이션 프로세서 (jakarta classifier)
- MapStruct 어노테이션 프로세서 (Lombok과 함께 사용 시 순서 중요)
- Lombok 어노테이션 프로세서
- 의존성 그룹:
  * implementation: starter-web, starter-data-jpa, starter-security, starter-validation,
    starter-cache, starter-actuator, starter-data-redis, postgresql, jjwt, springdoc,
    querydsl-jpa(jakarta), flyway-core, flyway-database-postgresql, mapstruct
  * compileOnly: lombok
  * annotationProcessor: lombok, mapstruct-processor, querydsl-apt(jakarta)
  * testImplementation: starter-test, testcontainers

[애플리케이션 설정]

1. application.yml (공통)
   - spring.application.name: community-api
   - spring.threads.virtual.enabled: true (Virtual Threads 활성화)
   - spring.jpa.open-in-view: false (성능, OSIV 비활성화)
   - spring.jpa.hibernate.ddl-auto: validate (Flyway가 스키마 관리)
   - spring.flyway.enabled: true
   - springdoc.swagger-ui.path: /swagger-ui.html
   - server.error.include-message: always
   - 한국어 주석으로 각 설정의 의미 설명

2. application-dev.yml
   - spring.datasource: docker compose의 PostgreSQL 연결 정보
   - spring.data.redis: docker compose의 Redis 연결 정보
   - logging.level.org.hibernate.SQL: DEBUG
   - logging.level.com.community: DEBUG

3. application-prod.yml
   - 환경변수로 DB/Redis 접속 정보 주입
   - logging.level: WARN (프로덕션에서는 불필요한 로그 제거)

[기본 패키지 구조 생성]

com.community/
├── CommunityApplication.java
│   - @SpringBootApplication
│   - main 메서드
│   - JavaDoc: "커뮤니티 웹앱 메인 애플리케이션 진입점"
│
├── global/config/
│   ├── JpaConfig.java
│   │   - @Configuration + @EnableJpaAuditing
│   │   - JavaDoc: "JPA 감사(Auditing) 기능 활성화. createdAt, updatedAt 자동 관리"
│   │
│   ├── RedisConfig.java
│   │   - @Configuration
│   │   - RedisTemplate<String, Object> 빈 (JSON 직렬화)
│   │   - JavaDoc: "Redis 연결 및 직렬화 설정"
│   │
│   ├── CorsConfig.java
│   │   - @Configuration + WebMvcConfigurer
│   │   - 개발: localhost:5173 허용
│   │   - JavaDoc: "CORS 정책 설정. 프론트엔드에서의 API 호출 허용"
│   │
│   └── SwaggerConfig.java
│       - @Configuration
│       - OpenAPI 기본 정보 (제목, 설명, 버전)
│       - JavaDoc: "Swagger UI API 문서 자동 생성 설정"
│
├── global/common/
│   ├── response/ApiResponse.java
│   │   - 제네릭 record: success(boolean), data(T), error(ErrorDetail)
│   │   - 정적 팩토리: ApiResponse.success(data), ApiResponse.error(errorCode)
│   │   - JavaDoc: "모든 API 응답을 감싸는 통합 응답 래퍼"
│   │
│   ├── response/PageResponse.java
│   │   - Page<T>를 받아서 { content, totalElements, totalPages, page, size, hasNext } 반환
│   │   - JavaDoc: "페이징 응답을 위한 래퍼. Spring Data의 Page를 프론트엔드 친화적으로 변환"
│   │
│   ├── entity/BaseTimeEntity.java
│   │   - @MappedSuperclass + @EntityListeners(AuditingEntityListener.class)
│   │   - @CreatedDate createdAt, @LastModifiedDate updatedAt
│   │   - JavaDoc: "모든 엔티티의 공통 시간 필드. JPA Auditing으로 자동 관리"
│   │
│   └── exception/
│       ├── ErrorCode.java
│       │   - enum: 각 항목에 code(String), httpStatus(HttpStatus), message(String) 포함
│       │   - 값: INVALID_INPUT(400), DUPLICATE_EMAIL(409), INVALID_CREDENTIALS(401),
│       │         EXPIRED_TOKEN(401), POST_NOT_FOUND(404), UNAUTHORIZED(403),
│       │         INTERNAL_ERROR(500) 등
│       │   - JavaDoc: "모든 에러 코드를 중앙에서 관리하는 enum"
│       │
│       ├── BusinessException.java
│       │   - RuntimeException 확장
│       │   - ErrorCode 필드 포함
│       │   - JavaDoc: "비즈니스 로직에서 발생하는 예외. ErrorCode와 함께 던짐"
│       │
│       └── GlobalExceptionHandler.java
│           - @RestControllerAdvice
│           - BusinessException → ApiResponse.error(e.getErrorCode())
│           - MethodArgumentNotValidException → 검증 에러 처리
│           - Exception → 500 내부 서버 에러
│           - JavaDoc: "모든 예외를 가로채서 통일된 ApiResponse 형식으로 변환"

[주의사항 - Spring Boot 4.0 관련]
- jakarta.* 패키지 사용 (javax.* 절대 금지)
- WebSecurityConfigurerAdapter는 존재하지 않음 (SecurityConfig는 Phase 1에서 생성)
- Spring Framework 7 기반이므로, 6.x 문서 참고 시 주의
- Virtual Threads 활성화 시 synchronized 블록 주의 (ReentrantLock 권장)
- Gradle에서 MapStruct + Lombok 함께 사용 시 어노테이션 프로세서 순서:
  annotationProcessor("org.projectlombok:lombok")
  annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
  annotationProcessor("org.mapstruct:mapstruct-processor:1.6.x")

[생성하지 말 것]
- SecurityConfig.java (Phase 1에서 생성)
- domain/ 하위 모든 패키지 (각 Phase에서 생성)
```

### Task 0.6: Flyway 초기 마이그레이션 (DB 스키마)

```
Flyway 마이그레이션 파일로 데이터베이스 스키마를 생성해줘.

[배경 설명]
이 프로젝트의 전체 DB 스키마를 Flyway SQL 마이그레이션으로 생성해.
PostgreSQL 18을 사용하므로, TIMESTAMP WITH TIME ZONE을 사용해.
이 문서의 섹션 5.1의 SQL을 기반으로 하되, 파일별로 분리해줘.

[경로] apps/api/src/main/resources/db/migration/

[파일 목록]
1. V1__create_users_table.sql      → users 테이블 + 인덱스
2. V2__create_categories_table.sql → categories 테이블 + 기본 데이터 INSERT
3. V3__create_posts_table.sql      → posts 테이블 + 인덱스 (조건부 인덱스 포함)
4. V4__create_comments_table.sql   → comments 테이블 + 인덱스
5. V5__create_votes_bookmarks.sql  → votes, bookmarks 테이블
6. V6__create_notifications.sql    → notifications 테이블 + 복합 인덱스
7. V7__create_supporting_tables.sql → media, reports, refresh_tokens 테이블

[각 파일에 포함할 주석]
- 파일 상단: 이 마이그레이션의 목적, 생성되는 테이블 이름
- 각 테이블 위: 테이블의 역할 설명
- 각 컬럼 옆: 해당 컬럼의 의미 (한 줄 SQL 주석)
- 각 인덱스 위: 이 인덱스가 어떤 쿼리를 최적화하는지 설명

[주의사항]
- 모든 TIMESTAMP는 WITH TIME ZONE 사용 (PostgreSQL 권장사항)
- CHECK 제약조건으로 유효값 제한 (role, target_type, status, reason 등)
- ON DELETE CASCADE 적절히 사용 (사용자 삭제 시 관련 데이터도 삭제)
- 조건부 인덱스 사용 (WHERE is_deleted = FALSE 등)
- 이 문서의 섹션 5.1 SQL을 참고하되, 주석을 더 상세하게 작성
```

### Task 0.7: CLAUDE.md (AI 어시스턴트 컨텍스트)

```
프로젝트 루트에 CLAUDE.md 파일을 작성해줘.

[배경 설명]
이 파일은 AI 코딩 어시스턴트(Claude Code, Cursor 등)가 프로젝트를 이해하기 위한 컨텍스트 문서야.
프로젝트 루트에 배치하면 AI가 자동으로 참조해.
이 문서에 있는 핵심 정보를 간결하게 요약해서 담아줘.

[포함 내용]
1. 프로젝트 개요 (2~3줄)
2. 기술 스택 표 (최신 버전 명시)
   - 프론트: React 19.2, Vite 7.3, MUI 7.3, TypeScript 5.7
   - 백: Spring Boot 4.0.3, Java 21, PostgreSQL 18.3
   - 인프라: Docker, Redis 7.4, MinIO
3. 프로젝트 구조 (핵심 디렉토리만)
4. 코딩 규칙 (이 문서의 "전역 지침" 요약)
5. 빠른 명령어 (make dev, make test 등)
6. 새 기능 추가 가이드 (백엔드 순서 → 프론트 순서)
7. 자주 발생하는 에러 & 해결법 표
8. AI 어시스턴트 행동 지침 (한국어 주석, 테스트 작성 등)

[한국어로 작성, 코드 예시는 영어]
```

---

## Phase 1~7: 이후 개발 태스크

> 이후 Phase의 태스크 프롬프트는 이전 버전(v1)의 내용을 기반으로 하되,
> 위의 "전역 지침"과 "최신 버전 정보"를 반영하여 사용합니다.
>
> **각 태스크에 공통으로 추가할 안내문** (Antigravity에 전달 시 태스크 프롬프트 상단에 붙여넣기):
>
> ```
> [공통 지침 - 모든 태스크에 적용]
> - 기술 버전: React 19.2, MUI 7.3, Vite 7.3, Spring Boot 4.0.3, Java 21, PostgreSQL 18.3
> - React Router v7: `from 'react-router'` (react-router-dom 아님)
> - Spring Boot 4.0: jakarta.* 패키지, SecurityFilterChain 빈 방식
> - 모든 코드에 한국어 주석 필수 (클래스, 메서드, 주요 로직)
> - TypeScript: any 금지, strict mode
> - Java DTO: record 클래스 사용
> - import 경로: 프론트 @/ 별칭, 백 com.community 패키지
> ```

### Phase 요약 (33개 태스크)

| Phase | 태스크 수 | 핵심 내용 |
|-------|-----------|-----------|
| **0: 부트스트랩** | 7 | 프로젝트 골격, Docker, DB 스키마, 테마 |
| **1: 인증** | 7 | User 엔티티, JWT, 로그인/회원가입, Axios 설정, 인증 UI, 라우터 |
| **2: 게시글 CRUD** | 6 | Post 엔티티, Service, Controller, API 훅, 목록 UI, 상세 UI, 에디터 |
| **3: 댓글 & 추천** | 3 | Comment, Vote, 프론트엔드 댓글 UI |
| **4: 프로필 & 관리자** | 4 | 프로필 API/UI, 관리자 API/UI |
| **5: 알림 & 미디어** | 4 | 알림 시스템, 알림 UI, 미디어 업로드, 이미지 UI |
| **6: 완성** | 3 | 북마크, 신고, 홈 화면 구성 |
| **7: 배포** | 4 | 성능 최적화, Docker 프로덕션, CI/CD, 모니터링 |

---

## 10. MSA 확장 로드맵 (향후)

현재는 모듈러 모놀리스로 개발하되, 아래 모듈은 MSA 분리 후보입니다.
각 모듈의 Service 클래스에 `// TODO: MSA 분리 시 이벤트 기반으로 전환` 주석을 남깁니다.

| 우선순위 | 모듈 | 분리 이유 | 통신 방식 |
|----------|------|-----------|-----------|
| 1순위 | Notification | 비동기 처리, 외부 서비스 연동 (이메일, 푸시) | RabbitMQ 이벤트 |
| 2순위 | Media | CPU 집약적 (이미지 리사이징), 독립 스토리지 | REST + S3 |
| 3순위 | Search | 전문 검색 엔진 도입 시 | Elasticsearch + 이벤트 |

분리 시 추가 인프라:
- API Gateway (Spring Cloud Gateway)
- 서비스 디스커버리 (Kubernetes DNS 또는 Eureka)
- 메시지 브로커 (RabbitMQ)
- 분산 추적 (OpenTelemetry + Jaeger)

---

*이 문서는 살아있는 문서입니다. 개발 진행에 따라 업데이트됩니다.*
*최종 수정: 2026-03-14 v2*
