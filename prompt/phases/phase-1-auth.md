# Phase 1: 인증 시스템

> **상태**: ✅ 완료
> **목표**: 회원가입, 로그인, JWT 인증, 토큰 갱신
> **완료 기준**: 회원가입 → 로그인 → JWT 토큰으로 인증 API 접근 가능
> **참고**: architecture-plan-v2.md 섹션 7 (보안 설계), 섹션 6.1 (인증 API), 섹션 3.2 (백엔드 모듈 구조)

---

## 진행 체크리스트

- [x] Task 1.1: User 엔티티 & Repository (백엔드)
- [x] Task 1.2: Spring Security & JWT 설정 (백엔드)
- [x] Task 1.3: Auth Service & Controller (백엔드)
- [x] Task 1.4: Axios 인스턴스 & 공유 타입 (프론트)
- [x] Task 1.5: 인증 스토어 & 훅 (프론트)
- [x] Task 1.6: 로그인 & 회원가입 UI (프론트)
- [x] Task 1.7: 라우터 & 레이아웃 (프론트)

## 검증 기준

- [ ] `make lint-api` → 컴파일 성공
- [ ] POST /api/v1/auth/signup → 201 응답 (Swagger UI)
- [ ] POST /api/v1/auth/login → JWT 토큰 반환
- [ ] Authorization: Bearer {token} 으로 보호된 API 접근 성공
- [ ] 프론트엔드 로그인/회원가입 페이지 렌더링 확인
- [ ] `make lint-web` + `make typecheck` → 통과
- [ ] 토큰 갱신 인터셉터 동작 확인

---

## Task 1.1: User 엔티티 & Repository

### Context
- DB 스키마: architecture-plan-v2.md 섹션 5.1 — `users` 테이블 (V1 마이그레이션)
- 패키지: `com.community.domain.user`

### Deliverables
1. `entity/User.java` — `@Entity @Table(name = "users")`, BaseTimeEntity 상속
   - 필드: id(Long, IDENTITY), email(VARCHAR 255, UNIQUE), passwordHash(VARCHAR 255), nickname(VARCHAR 50, UNIQUE), bio(VARCHAR 300, nullable), profileImage(VARCHAR 500, nullable), role(UserRole, default USER), isActive(boolean, default true), lastLoginAt(Timestamp, nullable)
   - 비즈니스 메서드: `updateProfile(nickname, bio)`, `updateProfileImage(url)`, `updateLastLogin()`, `ban()`, `unban()`, `changeRole(role)`
2. `entity/UserRole.java` — enum: USER, ADMIN
3. `repository/UserRepository.java` — `Optional<User> findByEmail(String email)`, `boolean existsByEmail(String email)`, `boolean existsByNickname(String nickname)`
4. `dto/response/UserProfileResponse.java` — record: id, email, nickname, bio, profileImage, role, createdAt
5. `dto/response/UserSummaryResponse.java` — record: id, nickname, profileImage (게시글/댓글에서 작성자 표시용)
6. `mapper/UserMapper.java` — MapStruct `@Mapper(componentModel = "spring")`

### Constraints
- `@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED) @Builder`
- `@Setter` 금지
- Lombok 어노테이션: `@AllArgsConstructor` 사용 시 Builder와 충돌 주의

### Acceptance Criteria
- [ ] `make lint-api` 통과
- [ ] Entity 필드가 DB 스키마(V1__create_users_table.sql)와 정확히 일치

---

## Task 1.2: Spring Security & JWT 설정

### Context
- architecture-plan-v2.md 섹션 7.1 (JWT 인증 흐름)
- architecture-plan-v2.md 섹션 3.2 — `global/security/` 패키지 구조
- Spring Boot 4.0 → Spring Security 7 → `SecurityFilterChain` 빈 방식

### JWT 인증 흐름 (architecture-plan-v2.md 섹션 7.1)
```
로그인 → AccessToken(15분) + RefreshToken(7일, DB저장) 발급
→ 클라이언트: AccessToken은 메모리/localStorage, RefreshToken은 httpOnly cookie 또는 localStorage
→ API 요청 시 Authorization: Bearer {accessToken}
→ 401 시 /auth/refresh → 새 AccessToken → 실패한 요청 재시도
→ RefreshToken도 만료 → 로그아웃 + /login 리다이렉트
```

### Deliverables
1. `global/security/jwt/JwtTokenProvider.java`
   - 토큰 생성: HS256, userId/role 포함, AccessToken 15분, RefreshToken은 UUID 랜덤
   - 토큰 검증: 만료/서명 검증, Claims 추출
   - jjwt 0.12.x (`io.jsonwebtoken:jjwt-api/impl/jackson`)
2. `global/security/jwt/JwtAuthFilter.java`
   - OncePerRequestFilter 구현
   - Authorization 헤더에서 "Bearer " 접두어 제거 후 토큰 추출
   - 유효하면 SecurityContextHolder에 Authentication 설정
3. `global/security/jwt/JwtTokenDto.java` — record: accessToken, refreshToken, expiresIn
4. `global/security/UserDetailsServiceImpl.java` — UserDetailsService 구현, UserRepository.findByEmail 사용
5. `global/security/CustomAuthEntryPoint.java` — 401 시 `ApiResponse.error(UNAUTHORIZED)` JSON 응답
6. `global/config/SecurityConfig.java`
   - SecurityFilterChain 빈
   - CSRF 비활성화 (JWT Stateless)
   - 인증 제외 경로: `/api/v1/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/actuator/**`
   - JwtAuthFilter를 `UsernamePasswordAuthenticationFilter` 앞에 추가
   - 세션 정책: STATELESS
   - BCryptPasswordEncoder 빈

### Constraints
- `jakarta.servlet.*` 사용 (`javax.servlet` 절대 금지)
- `WebSecurityConfigurerAdapter`는 삭제된 클래스 — 사용 금지
- @Value로 JWT secret, expiration 주입 (application.yml에서)
- Virtual Threads 환경: synchronized 대신 ReentrantLock 권장

### Acceptance Criteria
- [ ] SecurityConfig에 `SecurityFilterChain` 빈이 존재하고 STATELESS 설정
- [ ] JWT 토큰 생성 → 검증 → Claims 추출 로직 동작
- [ ] 인증 제외 경로에서 토큰 없이 접근 가능
- [ ] `make lint-api` 통과

---

## Task 1.3: Auth Service & Controller

### Context
- API: architecture-plan-v2.md 섹션 6.1 — 인증 API 4개 엔드포인트
- DB: `refresh_tokens` 테이블 (V7__create_supporting_tables.sql)
- 보안: architecture-plan-v2.md 섹션 7.2 — BCrypt cost factor 12, Rate Limiting 로그인 5회/분

### Deliverables
1. `domain/auth/dto/request/SignupRequest.java` — record: `@Email @NotBlank String email`, `@NotBlank @Size(min=8) String password`, `@NotBlank @Size(min=2, max=20) String nickname`
2. `domain/auth/dto/request/LoginRequest.java` — record: `@Email @NotBlank String email`, `@NotBlank String password`
3. `domain/auth/dto/request/TokenRefreshRequest.java` — record: `@NotBlank String refreshToken`
4. `domain/auth/dto/response/TokenResponse.java` — record: accessToken, refreshToken, expiresIn(long), UserProfileResponse user
5. `domain/auth/repository/RefreshTokenRepository.java` — `Optional findByToken(String)`, `void deleteByUserId(Long)`, `void deleteByToken(String)`
6. `domain/auth/service/AuthService.java`
   - `signup(SignupRequest)` → 이메일/닉네임 중복 확인 → BCrypt 해싱 → User 저장 → JWT 발급
   - `login(LoginRequest)` → User 조회 → BCrypt.matches → lastLoginAt 업데이트 → JWT 발급
   - `refresh(TokenRefreshRequest)` → RefreshToken DB 조회 → 만료 확인 → 새 AccessToken 발급
   - `logout(Long userId)` → RefreshToken DB에서 삭제
7. `domain/auth/controller/AuthController.java`
   - `@RestController @RequestMapping("/api/v1/auth") @Tag(name = "인증")`
   - POST /signup → 회원가입 (Public)
   - POST /login → 로그인 (Public)
   - POST /refresh → 토큰 갱신 (Public)
   - POST /logout → 로그아웃 (인증 필요, @AuthenticationPrincipal)

### Constraints
- 이메일 중복 → `ErrorCode.DUPLICATE_EMAIL` (409)
- 닉네임 중복 → `ErrorCode.DUPLICATE_NICKNAME` (409)
- 잘못된 로그인 → `ErrorCode.INVALID_CREDENTIALS` (401)
- 만료된 리프레시 토큰 → `ErrorCode.EXPIRED_TOKEN` (401)
- 로그아웃 시 해당 userId의 RefreshToken DB에서 삭제
- `@Operation` 어노테이션 (SpringDoc) + 한국어 JavaDoc

### Acceptance Criteria
- [ ] Swagger UI에서 4개 엔드포인트 모두 동작
- [ ] 회원가입 → 로그인 → 토큰 반환 → 보호된 API 접근 flow 테스트
- [ ] 중복 이메일/닉네임 시 409 반환
- [ ] `make lint-api` 통과

---

## Task 1.4: Axios 인스턴스 & 공유 타입

### Context
- architecture-plan-v2.md 섹션 7.1 — JWT 클라이언트 측 흐름 (토큰 갱신 인터셉터)
- architecture-plan-v2.md 섹션 6 — API 응답 형식 (`ApiResponse<T>`, `PageResponse<T>`)
- 경로: `apps/web/src/shared/`

### Deliverables
1. `api/axiosInstance.ts`
   - baseURL: `/api/v1`
   - timeout: 10000
   - headers: `{ 'Content-Type': 'application/json' }`
   - **요청 인터셉터**: authStore에서 accessToken 가져와서 `Authorization: Bearer {token}` 추가
   - **응답 인터셉터 (토큰 갱신)**: 
     - `isRefreshing` 플래그로 동시 갱신 방지
     - `failedQueue` 배열에 대기 중인 요청 축적
     - 401 시 → `/auth/refresh` 호출 → 성공 시 새 토큰으로 대기 요청 모두 재시도
     - 갱신 실패 → `authStore.clearAuth()` + `/login` 리다이렉트
   - 각 단계에 상세한 한국어 주석 (이 로직은 복잡하므로 단계별 설명)
2. `api/apiTypes.ts`
   - `interface ApiResponse<T> { success: boolean; data: T; error: { code: string; message: string } | null }`
   - `interface PageResponse<T> { content: T[]; totalElements: number; totalPages: number; page: number; size: number; hasNext: boolean; hasPrevious: boolean }`
   - `class ApiError extends Error { code: string; ... }`
   - `extractData<T>(response): T` — success가 false면 ApiError throw
3. `types/user.ts`
   - `interface User { id: number; email: string; nickname: string; bio: string | null; profileImage: string | null; role: 'USER' | 'ADMIN'; createdAt: string }`
   - `interface UserSummary { id: number; nickname: string; profileImage: string | null }`
   - `interface TokenResponse { accessToken: string; refreshToken: string; expiresIn: number; user: User }`
4. `types/common.ts`
   - `interface PageRequest { page?: number; size?: number; sort?: string }`
   - `type SortDirection = 'LATEST' | 'POPULAR'`

### Acceptance Criteria
- [ ] `make typecheck` 통과
- [ ] axiosInstance에 요청/응답 인터셉터 2개 설정
- [ ] extractData 헬퍼가 error 시 ApiError throw

---

## Task 1.5: 인증 스토어 & 훅

### Context
- 경로: `apps/web/src/features/auth/`
- Zustand v5 + TanStack Query v5 사용

### Deliverables
1. `store/authStore.ts`
   - Zustand v5: `create<AuthStore>()(persist(...))`  (이중 괄호 주의!)
   - 상태: `user: User | null`, `accessToken: string | null`, `isAuthenticated` (computed: `!!user && !!accessToken`)
   - 액션: `setAuth(user, accessToken)`, `clearAuth()`, `updateUser(Partial<User>)`
   - persist: localStorage에 **user만** 저장 (accessToken은 보안상 메모리에만)
2. `api/authApi.ts`
   - `signup(data: SignupPayload): Promise<TokenResponse>` → POST /auth/signup
   - `login(data: LoginPayload): Promise<TokenResponse>` → POST /auth/login
   - `refreshToken(refreshToken: string): Promise<TokenResponse>` → POST /auth/refresh
   - `logout(): Promise<void>` → POST /auth/logout
   - 각 함수에서 `extractData` 헬퍼 사용
   - SignupPayload: `{ email, password, nickname }`
   - LoginPayload: `{ email, password }`
3. `hooks/useAuth.ts`
   - `useLogin()`: useMutation → onSuccess: setAuth + localStorage refreshToken 저장 + navigate('/')
   - `useSignup()`: useMutation → onSuccess: 동일
   - `useLogout()`: clearAuth + localStorage refreshToken 삭제 + queryClient.clear() + navigate('/')
   - `useCurrentUser()`: authStore에서 user 반환
   - `useIsAdmin()`: `user?.role === 'ADMIN'`
   - TanStack Query v5: `useMutation({ mutationFn, onSuccess, onError })`
4. `index.ts` — barrel export

### Acceptance Criteria
- [ ] `make typecheck` 통과
- [ ] useLogin 훅에서 성공 시 스토어 업데이트 + navigate

---

## Task 1.6: 로그인 & 회원가입 UI

### Context
- 디자인 철학 (architecture-plan-v2.md 섹션 4.1): **토스의 직관성 + MD3 체계성 + Apple HIG 매끄러움**
- 디자인 토큰: `apps/web/src/app/theme/tokens.ts` — 컬러, 타이포, 스페이싱, 라디우스 참조
- 경로: `apps/web/src/features/auth/`

### 디자인 상세 (토스 앱 로그인 참고)
- 깨끗한 흰색 배경 (`tokens.color.surface`)
- 큰 입력 필드: MUI TextField `variant="outlined"` `fullWidth`, 배경 `tokens.color.surfaceVariant`, 테두리 없음
- 풀 너비 CTA 버튼: MUI Button `variant="contained"` `fullWidth` `size="large"` (높이 52px, 테마에서 설정됨)
- 중앙 정렬: `maxWidth: 400px`, `margin: 'auto'`
- 폰트: Pretendard Variable (index.html CDN 로딩)

### Deliverables
1. `components/LoginForm.tsx`
   - react-hook-form `useForm` + `zodResolver` 연결
   - Zod 스키마: `email: z.string().email("올바른 이메일을 입력하세요")`, `password: z.string().min(8, "비밀번호는 8자 이상이어야 합니다")`
   - MUI TextField 2개: 이메일, 비밀번호(`type="password"`)
   - MUI Button: "로그인" (풀너비, variant contained)
   - 로딩: Button에 `disabled` + `CircularProgress`
   - 에러: TextField의 `error`/`helperText` prop + 상단 Alert
   - useLogin 훅 사용
   - 하단: "아직 회원이 아니신가요?" + `Link to="/signup"` (from 'react-router')
2. `components/SignupForm.tsx`
   - Zod 스키마: email, password(8자+), passwordConfirm(`.refine()` password와 일치), nickname(2~20자)
   - MUI TextField 4개
   - useSignup 훅 사용
   - 하단: "이미 회원이신가요?" + `Link to="/login"`
3. `pages/LoginPage.tsx`
   - Box 중앙 정렬 (`maxWidth: 400px`)
   - 상단 앱 제목 "커뮤니티" (`Typography variant="h4"`)
   - Framer Motion: `motion.div` fade-in (`initial={{ opacity: 0 }}, animate={{ opacity: 1 }}, transition={{ duration: 0.3 }}`)
4. `pages/SignupPage.tsx` — 동일 레이아웃
5. `index.ts` — barrel export: LoginPage, SignupPage

### Google Stitch 2.0 활용 (선택)
- 로그인/회원가입 화면 디자인을 Stitch에서 먼저 생성 가능
- 프롬프트 예시: "토스 스타일의 모바일 로그인 화면. 흰색 배경, 큰 입력 필드, 파란색 풀 너비 버튼"
- Stitch에서 생성한 DESIGN.md를 `apps/web/DESIGN.md`로 저장하여 일관성 유지

### Acceptance Criteria
- [ ] 로그인/회원가입 페이지 브라우저에서 렌더링 확인
- [ ] 폼 유효성 검증 동작 (빈 필드, 짧은 비밀번호, 비밀번호 불일치 등)
- [ ] Framer Motion fade-in 애니메이션 동작
- [ ] `make typecheck` + `make lint-web` 통과

---

## Task 1.7: 라우터 & 레이아웃

### Context
- architecture-plan-v2.md 섹션 3.3 (프론트엔드 FSD 구조)
- 디자인 토큰: zIndex, spacing, shadow 참조
- 경로: `apps/web/src/`

### Deliverables
1. `shared/guards/AuthGuard.tsx`
   - authStore에서 isAuthenticated 확인 → 미인증: `<Navigate to="/login" replace />`
   - 인증됨: `<Outlet />` 렌더링
2. `shared/guards/AdminGuard.tsx`
   - `user?.role === 'ADMIN'` 확인 → 비관리자: `<Navigate to="/" replace />`
3. `shared/components/layout/TopAppBar.tsx`
   - MUI AppBar + Toolbar, 높이 56px
   - 왼쪽: 뒤로가기 (`useNavigate(-1)`) 또는 앱 제목
   - 오른쪽: 로그인 상태 → UserAvatar, 비로그인 → 로그인 버튼
   - 스타일: 그림자 없음, 배경 `tokens.color.surface`, 텍스트 `tokens.color.textPrimary`
4. `shared/components/layout/BottomTabBar.tsx`
   - MUI BottomNavigation + BottomNavigationAction
   - 4탭: 홈(Home), 글쓰기(PenSquare), 알림(Bell), 프로필(User) — lucide-react 아이콘
   - 현재 경로(`useLocation`)에 따라 활성 탭 하이라이트
   - 미로그인 시 글쓰기/알림/프로필 클릭 → `/login` 리다이렉트
   - 스타일: `position: fixed`, bottom 0, `zIndex: tokens.zIndex.bottomTabBar(1000)`, `shadow: tokens.shadow.bottomSheet`
5. `shared/components/layout/AppLayout.tsx`
   - Box: `paddingTop: 56px` (AppBar), `paddingBottom: 64px` (BottomTab)
   - `<TopAppBar />` + `<Outlet />` + `<BottomTabBar />`
6. `shared/components/layout/AdminLayout.tsx`
   - 데스크톱: MUI Drawer 사이드바 + 메인 콘텐츠
   - 모바일: 햄버거 메뉴
   - 관리자 메뉴: 대시보드, 사용자 관리, 게시글 관리, 신고 관리
7. `shared/components/common/UserAvatar.tsx`
   - MUI Avatar: src 있으면 이미지, 없으면 이름 첫 글자
   - 배경색: 이름 해시로 결정 (일관된 컬러)
   - size prop: `'sm'(32px) | 'md'(40px) | 'lg'(64px)`
8. `app/router.tsx` — React.lazy + Suspense 코드 스플리팅:
   ```
   Layout: AppLayout
     / → HomePage (placeholder)
     /posts → PostListPage (placeholder)
     /posts/:id → PostDetailPage (placeholder)
     /posts/new → AuthGuard → PostEditorPage (placeholder)
     /posts/:id/edit → AuthGuard → PostEditorPage (placeholder)
     /notifications → AuthGuard → NotificationPage (placeholder)
     /profile → AuthGuard → ProfilePage (placeholder)
   /login → LoginPage (로그인 상태면 / 리다이렉트)
   /signup → SignupPage (로그인 상태면 / 리다이렉트)
   /admin → AdminGuard → AdminLayout
     /admin/dashboard → AdminDashboardPage (placeholder)
     /admin/users → AdminUsersPage (placeholder)
   ```
9. `app/App.tsx` 수정: providers + router 합성

### Constraints
- import: 모두 `from 'react-router'` (NOT `react-router-dom`)
- placeholder 페이지: `<Box p={4}><Typography>페이지명 (준비 중)</Typography></Box>`
- BottomTabBar/TopAppBar 스타일은 `tokens.*` 참조

### Acceptance Criteria
- [ ] 모든 라우트에서 적절한 레이아웃 렌더링
- [ ] AuthGuard가 미인증 사용자를 /login으로 리다이렉트
- [ ] BottomTabBar 현재 경로에 따라 활성 탭 표시
- [ ] AdminGuard가 비관리자를 / 로 리다이렉트
- [ ] `make lint-web` + `make typecheck` 통과

---

## 구현 이력

### 2026-03-27: Phase 1 구현 완료

**구현 전략:**
- Batch 1 (병렬): Task 1.1 + Task 1.2 + Task 1.4 동시 실행
- Batch 2 (병렬): Task 1.3 + Task 1.5 동시 실행
- Batch 3: Task 1.6 순차 실행
- Batch 4: Task 1.7 순차 실행

**특이사항:**
- Gradle 9.4.1 + foojay 0.8.0 호환성 문제 → foojay 0.9.0으로 업그레이드
- 로컬 Java 25 설치 환경 (Java 21 없음) → build.gradle.kts 툴체인을 Java 25로 변경
- AuthService의 필드명 오류 (`password` → `passwordHash`) 수정
- axiosInstance에서 authStore 순환 참조 방지 → `window.__authStore` 패턴 사용

**검증 결과:**
- `./gradlew compileJava` → BUILD SUCCESSFUL
- `npx tsc --noEmit` → 에러 없음
- `npx eslint src/ --max-warnings 0` → 경고 없음

**생성된 백엔드 파일:**
- `domain/user/entity/User.java`, `UserRole.java`
- `domain/user/repository/UserRepository.java`
- `domain/user/dto/response/UserProfileResponse.java`, `UserSummaryResponse.java`
- `domain/user/mapper/UserMapper.java`
- `global/security/jwt/JwtTokenProvider.java`, `JwtAuthFilter.java`, `JwtTokenDto.java`
- `global/security/UserDetailsServiceImpl.java`, `CustomAuthEntryPoint.java`
- `global/config/SecurityConfig.java`
- `domain/auth/entity/RefreshToken.java`
- `domain/auth/repository/RefreshTokenRepository.java`
- `domain/auth/dto/request/SignupRequest.java`, `LoginRequest.java`, `TokenRefreshRequest.java`
- `domain/auth/dto/response/TokenResponse.java`
- `domain/auth/service/AuthService.java`
- `domain/auth/controller/AuthController.java`

**생성된 프론트엔드 파일:**
- `shared/types/user.ts`, `common.ts`
- `shared/api/apiTypes.ts`, `axiosInstance.ts`
- `features/auth/store/authStore.ts`
- `features/auth/api/authApi.ts`
- `features/auth/hooks/useAuth.ts`
- `features/auth/components/LoginForm.tsx`, `SignupForm.tsx`
- `features/auth/pages/LoginPage.tsx`, `SignupPage.tsx`
- `features/auth/index.ts`
- `shared/guards/AuthGuard.tsx`, `AdminGuard.tsx`
- `shared/components/common/UserAvatar.tsx`
- `shared/components/layout/TopAppBar.tsx`, `BottomTabBar.tsx`, `AppLayout.tsx`, `AdminLayout.tsx`
- `app/router.tsx` (전체 교체)
