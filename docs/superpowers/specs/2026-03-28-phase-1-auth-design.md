# Phase 1: 인증 시스템 — 설계 문서

> **작성일**: 2026-03-28
> **상태**: 구현 완료 (Phase 1 완료)
> **참조**: `prompt/phases/phase-1-auth.md`, `prompt/architecture-plan-v2.md` 섹션 7

---

## 1. 개요

회원가입, 로그인, JWT 기반 인증, 토큰 자동 갱신, 라우트 가드, 모바일 레이아웃을 구현한다.
백엔드는 Spring Security 7 + JJWT 0.12.x 기반의 Stateless JWT 인증. 프론트엔드는 Zustand 상태관리 + axiosInstance 인터셉터로 토큰을 자동 갱신한다.

**완료 기준**:
- 회원가입 → 로그인 → JWT 토큰으로 보호된 API 접근 가능
- 미인증 사용자의 보호 라우트 접근 시 `/login`으로 리다이렉트

---

## 2. JWT 인증 흐름

```
[클라이언트]                              [서버]

POST /auth/signup (또는 /auth/login)
                    ──────────────────────►
                    ◄──────────────────────
                 accessToken(15분) + refreshToken(7일, DB저장)

API 요청
Authorization: Bearer {accessToken}
                    ──────────────────────►
                    ◄──────────────────────
                         응답 데이터

accessToken 만료 → 401
                    ──────────────────────►
axiosInstance 인터셉터 감지
POST /auth/refresh  ──────────────────────►
                    ◄──────────────────────
                 새 accessToken + 새 refreshToken

원래 요청 재시도
Authorization: Bearer {새 accessToken}
                    ──────────────────────►

refreshToken 만료 → /auth/refresh 401
→ clearAuth() + /login 리다이렉트
```

---

## 3. 기술 결정 사항

### 3.1 토큰 저장 전략

| 토큰 | 저장 위치 | 이유 |
|------|----------|------|
| accessToken | 메모리 (Zustand store) | XSS 공격으로 localStorage 탈취 시 노출 방지 |
| refreshToken | localStorage | 새로고침 후에도 로그인 유지 필요 |
| user 정보 | localStorage (persist) | 새로고침 후 UI 즉시 렌더링 (토큰 갱신 전 표시) |

> **트레이드오프**: accessToken을 메모리에 저장하면 탭 전환/새로고침 시 재발급이 필요하다. 이를 위해 axiosInstance 인터셉터가 401 시 refreshToken으로 자동 갱신한다.

### 3.2 RefreshToken 방식 선택

| 방식 | 채택 여부 | 이유 |
|------|----------|------|
| JWT refreshToken | ❌ | 발급 후 서버 측 무효화 불가 (탈취 대응 불가) |
| **UUID + DB 저장** | ✅ | 로그아웃/계정 정지 시 DB에서 즉시 삭제로 강제 무효화 가능 |

RefreshToken 갱신 시 기존 토큰 삭제 후 새 토큰 발급 (토큰 재사용 공격 방지).

### 3.3 비밀번호 해싱

- BCrypt, cost factor = 12
- cost 12 근거: 일반 서버에서 약 100~300ms 소요 → 브루트포스 공격 어렵게, 로그인 응답성은 허용 범위
- `@Setter` 금지: `User.passwordHash`는 생성 시에만 설정, 이후 변경 불가 (Builder 패턴으로 불변성 보장)

### 3.4 axiosInstance 순환 참조 해결

```
문제: axiosInstance.ts → authStore.ts import → axiosInstance.ts 순환
해결: window.__authStore에 스토어 참조 등록 → axiosInstance에서 window를 통해 접근
```

```typescript
// authStore.ts
window.__authStore = useAuthStore;

// axiosInstance.ts (순환 참조 없음)
const token = (window as any).__authStore?.getState?.()?.accessToken;
```

### 3.5 Spring Security 7 변경사항

| 항목 | 이전 방식 | Spring Boot 4 / Security 7 |
|------|----------|--------------------------|
| 설정 클래스 | `WebSecurityConfigurerAdapter` 상속 | `SecurityFilterChain` 빈 등록 |
| 패키지 | `javax.servlet.*` | `jakarta.servlet.*` |
| CSRF | 기본 활성화 | REST API에서 `disable()` |
| 세션 | 기본 STATEFUL | `STATELESS` 명시 |
| 인증 제외 | `antMatchers()` | `requestMatchers()` |

---

## 4. 백엔드 아키텍처

### 4.1 패키지 구조

```
com.community/
├── domain/
│   ├── user/
│   │   ├── entity/
│   │   │   ├── User.java              # @Entity, BaseTimeEntity 상속, 비즈니스 메서드
│   │   │   └── UserRole.java          # enum: USER, ADMIN
│   │   ├── repository/
│   │   │   └── UserRepository.java    # findByEmail, existsByEmail/Nickname
│   │   ├── dto/response/
│   │   │   ├── UserProfileResponse.java   # record: id, email, nickname, bio, profileImage, role, createdAt
│   │   │   └── UserSummaryResponse.java   # record: id, nickname, profileImage
│   │   └── mapper/
│   │       └── UserMapper.java        # MapStruct, componentModel="spring"
│   │
│   └── auth/
│       ├── entity/
│       │   └── RefreshToken.java      # @Entity, userId + token + expiresAt
│       ├── repository/
│       │   └── RefreshTokenRepository.java  # findByToken, deleteByUserId
│       ├── dto/
│       │   ├── request/
│       │   │   ├── SignupRequest.java      # record + Bean Validation
│       │   │   ├── LoginRequest.java
│       │   │   └── TokenRefreshRequest.java
│       │   └── response/
│       │       └── TokenResponse.java     # record: accessToken, refreshToken, expiresIn, user
│       ├── service/
│       │   └── AuthService.java       # signup/login/refresh/logout + issueTokens
│       └── controller/
│           └── AuthController.java    # @RestController, 4개 엔드포인트
│
└── global/
    ├── security/
    │   ├── jwt/
    │   │   ├── JwtTokenProvider.java  # generateAccessToken, generateRefreshToken, validateToken
    │   │   ├── JwtAuthFilter.java     # OncePerRequestFilter, Bearer 추출 + SecurityContext 설정
    │   │   └── JwtTokenDto.java       # record: accessToken, refreshToken, expiresIn
    │   ├── UserDetailsServiceImpl.java  # loadUserByUsername + loadUserById
    │   └── CustomAuthEntryPoint.java   # 401 시 JSON 응답
    └── config/
        └── SecurityConfig.java        # SecurityFilterChain + BCryptPasswordEncoder 빈
```

### 4.2 AuthService 트랜잭션 경계

| 메서드 | 트랜잭션 설정 | 이유 |
|--------|-------------|------|
| `signup()` | `@Transactional` (기본) | User 저장 + RefreshToken 저장 원자성 보장 |
| `login()` | `@Transactional` | `user.updateLastLogin()` + RefreshToken 저장 원자성 보장 |
| `refresh()` | `@Transactional` | 기존 토큰 삭제 + 새 토큰 저장 원자성 보장 |
| `logout()` | `@Transactional` | RefreshToken 삭제 |

> **클래스 레벨 `@Transactional`**: AuthService 전체에 적용. 개별 메서드에서 `readOnly=true`는 지정하지 않음 (전부 쓰기 작업 포함).

### 4.3 API 엔드포인트

| 메서드 | 경로 | 인증 | 설명 |
|--------|------|------|------|
| POST | `/api/v1/auth/signup` | Public | 회원가입 → 201 + TokenResponse |
| POST | `/api/v1/auth/login` | Public | 로그인 → 200 + TokenResponse |
| POST | `/api/v1/auth/refresh` | Public | 토큰 갱신 → 200 + TokenResponse |
| POST | `/api/v1/auth/logout` | 인증 필요 | 로그아웃 → 200 + Void |

### 4.4 에러 코드

| 상황 | ErrorCode | HTTP 상태 |
|------|----------|----------|
| 이메일 중복 | `DUPLICATE_EMAIL` (E011) | 409 |
| 닉네임 중복 | `DUPLICATE_NICKNAME` (E012) | 409 |
| 이메일/비밀번호 불일치 | `INVALID_CREDENTIALS` (E003) | 401 |
| 비활성 계정 | `INACTIVE_USER` (E013) | 403 |
| 토큰 만료 | `EXPIRED_TOKEN` (E004) | 401 |
| 유효하지 않은 토큰 | `INVALID_TOKEN` (E005) | 401 |
| 사용자 없음 | `USER_NOT_FOUND` (E010) | 404 |

---

## 5. 프론트엔드 아키텍처

### 5.1 상태 관리 구조

```
┌─────────────────────────────────────────┐
│              authStore (Zustand)        │
│  user: User | null   ─── localStorage  │
│  accessToken: string │   (persist)     │
│  isAuthenticated: boolean              │
│  setAuth / clearAuth / updateUser      │
└──────────────┬──────────────────────────┘
               │ window.__authStore
┌──────────────▼──────────────────────────┐
│            axiosInstance (Axios)        │
│  요청 인터셉터: accessToken → Bearer 헤더  │
│  응답 인터셉터: 401 → refreshToken으로 갱신 │
│  동시성 제어: isRefreshing + failedQueue   │
└─────────────────────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         TanStack Query useMutation      │
│  useLogin, useSignup → onSuccess에서    │
│  setAuth + localStorage.refreshToken   │
└─────────────────────────────────────────┘
```

### 5.2 동시성 제어 (axiosInstance)

여러 탭/요청이 동시에 401을 받을 때 refreshToken 요청이 중복 실행되는 문제를 방지:

```
요청1 → 401 감지 → isRefreshing = true → /auth/refresh 요청 시작
요청2 → 401 감지 → isRefreshing = true → failedQueue에 추가 (대기)
요청3 → 401 감지 → isRefreshing = true → failedQueue에 추가 (대기)

/auth/refresh 완료 → processQueue(null, newToken)
  → 요청2, 요청3: 새 토큰으로 재시도
  → isRefreshing = false
```

### 5.3 라우트 구조

```
BrowserRouter
├── <AppLayout>             (TopAppBar + BottomTabBar)
│   ├── /                   PlaceholderPage (Phase 2에서 홈 구현)
│   ├── /posts              PlaceholderPage (Phase 2에서 구현)
│   ├── /posts/:id          PlaceholderPage (Phase 2에서 구현)
│   └── <AuthGuard>         (미인증 → /login)
│       ├── /posts/new      PlaceholderPage
│       ├── /posts/:id/edit PlaceholderPage
│       ├── /notifications  PlaceholderPage
│       └── /profile        PlaceholderPage
│
├── /login                  GuestOnlyRoute → LoginPage
├── /signup                 GuestOnlyRoute → SignupPage
│
└── <AuthGuard> → <AdminGuard> → <AdminLayout>
    ├── /admin/dashboard    PlaceholderPage
    ├── /admin/users        PlaceholderPage
    ├── /admin/posts        PlaceholderPage
    └── /admin/reports      PlaceholderPage
```

**GuestOnlyRoute**: 로그인된 사용자가 `/login`, `/signup` 접근 시 `/`로 리다이렉트.

### 5.4 폼 유효성 검증 (Zod 스키마)

| 필드 | 규칙 |
|------|------|
| email | `z.string().email()` |
| password | `z.string().min(8)` |
| passwordConfirm | `.refine(data => data.password === data.passwordConfirm)` |
| nickname | `z.string().min(2).max(20)` |

에러 처리 계층:
1. Zod 스키마 → TextField의 `error` + `helperText` prop
2. 서버 에러 (ApiError) → 상단 `<Alert severity="error">` 표시
3. API 에러 초기화: 이메일/닉네임 변경 시 에러 상태 자동 초기화 (`watch` 또는 `onChange`)

### 5.5 레이아웃 컴포넌트

| 컴포넌트 | 위치 | 크기 | Z-Index |
|---------|------|------|---------|
| TopAppBar | position: fixed, top 0 | 56px | 1100 |
| BottomTabBar | position: fixed, bottom 0 | 64px | 1000 |
| AppLayout | paddingTop: 56px, paddingBottom: 64px | - | - |

**BottomTabBar 4탭 구성**:

| 탭 | 아이콘 | 경로 | 인증 필요 |
|----|--------|------|---------|
| 홈 | Home | `/` | ❌ |
| 글쓰기 | PenSquare | `/posts/new` | ✅ |
| 알림 | Bell | `/notifications` | ✅ |
| 프로필 | User | `/profile` | ✅ |

---

## 6. 알려진 이슈 및 해결책

| 이슈 | 원인 | 해결 |
|------|------|------|
| Gradle 빌드 실패 | foojay 0.8.0 + Gradle 9.x 비호환 | `settings.gradle.kts`에서 0.9.0 사용 |
| `passwordHash` 필드 없음 오류 | AuthService에서 필드명 오류 | `user.getPassword()` → `user.getPasswordHash()` |
| Zustand 이중 괄호 오류 | Zustand v5 API 변경 | `create<T>()(persist(...))` 형식 |
| axiosInstance 순환 참조 | authStore → axiosInstance → authStore | `window.__authStore` 패턴으로 해결 |
| `react-router-dom` not found | v7에서 패키지명 변경 | `import from 'react-router'` |
| MUI Typography 타입 오류 | MUI v7 component prop 타입 변경 | `OverridableStringUnion` 방식으로 수정 |

---

## 7. 보안 설계 요약

### 7.1 XSS 방지
- accessToken을 메모리에만 저장 (localStorage 미사용)
- refreshToken은 localStorage에 저장 (HttpOnly Cookie 미사용 → Phase 6 이후 개선 가능)

### 7.2 CSRF 방지
- SPA + JWT Stateless 방식에서 CSRF 공격은 세션 쿠키가 없으므로 비해당
- Spring Security CSRF 비활성화

### 7.3 토큰 무효화
- 로그아웃 시 서버 DB에서 RefreshToken 즉시 삭제
- 계정 정지(`ban()`) 시 `isActive = false` → UserDetailsServiceImpl에서 `disabled(true)` 처리

### 7.4 비밀번호 보안
- BCrypt cost=12: 해시 연산 약 250ms
- Salt 자동 생성으로 동일 비밀번호도 다른 해시값
- 비밀번호 원문은 절대 저장하지 않음 (Entity에 `passwordHash`만 존재)

---

## 8. Phase 2와의 인터페이스

Phase 2에서 게시글 기능 구현 시 다음 Phase 1 산출물을 활용:

| 산출물 | Phase 2 활용 |
|--------|-------------|
| `@AuthenticationPrincipal UserDetails` | 게시글 작성 시 현재 사용자 ID 추출 |
| `UserSummaryResponse` | 게시글/댓글 작성자 정보 표시 |
| `axiosInstance` | 모든 Post API 호출에 자동 토큰 첨부 |
| `useIsAuthenticated()` | 비로그인 사용자의 글쓰기 접근 차단 |
| `AuthGuard` | `/posts/new`, `/posts/:id/edit` 라우트 보호 |
| `BaseTimeEntity` | Post 엔티티 상속 (createdAt, updatedAt) |
| `ApiResponse<T>` | PostController 응답 래핑 |
| `ErrorCode` | UNAUTHORIZED_MODIFICATION(E030) 추가 예정 |
