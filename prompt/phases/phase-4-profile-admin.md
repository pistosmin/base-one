# Phase 4: 프로필 & 관리자

> **상태**: ⬜ 대기
> **목표**: 사용자 프로필 조회/수정, 관리자 대시보드, 사용자 관리
> **완료 기준**: 프로필 수정 + 관리자 대시보드 접근 + 사용자 역할/정지 관리
> **참고**: architecture-plan-v2.md 섹션 5.1 (users 테이블), 섹션 6.1 (사용자/관리자 API)

---

## 진행 체크리스트

- [ ] Task 4.1: 사용자 프로필 API (백엔드)
- [ ] Task 4.2: 프론트엔드 프로필 UI
- [ ] Task 4.3: 관리자 API (백엔드)
- [ ] Task 4.4: 프론트엔드 관리자 UI

## 검증 기준

- [ ] PATCH /api/v1/users/me → 닉네임/자기소개 변경 확인
- [ ] GET /api/v1/admin/dashboard → 통계 데이터 반환
- [ ] 관리자: 사용자 역할 변경, 정지/해제, 게시글 강제 삭제
- [ ] `make verify` → 전체 통과

---

## Task 4.1: 프로필 API (백엔드)

### Context
- 패키지: `com.community.domain.user`
- users 테이블: email(UNIQUE), nickname(UNIQUE), bio(300자), profile_image(500자)

### Deliverables
1. `domain/user/service/UserService.java`:
   - `getMyProfile(Long userId)` → UserProfileResponse
   - `updateMyProfile(Long userId, UpdateProfileRequest)` → 닉네임 변경 시 중복 확인, User.updateProfile() 호출
   - `getPublicProfile(Long userId)` → UserPublicProfileResponse (email 미포함, 게시글 수 포함)
2. `domain/user/dto/request/UpdateProfileRequest.java` — record: `@Size(min=2,max=20) String nickname (nullable)`, `@Size(max=300) String bio (nullable)`
3. `domain/user/dto/response/UserPublicProfileResponse.java` — record: id, nickname, bio, profileImage, createdAt, postCount(long), commentCount(long)
4. `domain/user/controller/UserController.java`:
   - GET `/api/v1/users/me` → 내 프로필 (인증)
   - PATCH `/api/v1/users/me` → 프로필 수정 (인증)
   - GET `/api/v1/users/{id}` → 공개 프로필 (Public)

### Acceptance Criteria
- [ ] 닉네임 중복 시 409
- [ ] 공개 프로필에 email 미포함 + postCount 포함
- [ ] `make lint-api` 통과

---

## Task 4.2: 프로필 UI (프론트)

### Context
- 디자인 토큰: `tokens.spacing.*`, `tokens.color.*`, `tokens.radius.*`
- 경로: `apps/web/src/features/user/`

### Deliverables
1. `features/user/api/userApi.ts` — getMyProfile, updateMyProfile, getPublicProfile
2. `features/user/hooks/useProfile.ts` — useMyProfile, useUpdateProfile, usePublicProfile
3. `features/user/components/ProfileCard.tsx`:
   - 큰 UserAvatar(size='lg', 64px) + 닉네임(h5) + 자기소개(body2, textSecondary)
   - 통계: 게시글 수 | 댓글 수 (Divider로 분리)
   - 본인: "프로필 수정" Button
   - 전체 Card: `tokens.radius.lg`, `tokens.shadow.sm`
4. `features/user/components/ProfileEditForm.tsx`:
   - MUI Dialog (fullScreen on mobile)
   - react-hook-form + Zod: nickname(2~20자), bio(max 300)
   - useUpdateProfile 훅
5. `features/user/pages/ProfilePage.tsx`:
   - `:id` 있으면 타인 프로필, 없으면 내 프로필
   - ProfileCard + MUI Tabs: "내 글" | "내 댓글" | "북마크" (Phase 6)
   - "내 글" 탭: usePosts(authorId) → PostList 재사용
   - "내 댓글" 탭: 내가 작성한 댓글 목록 (CommentItem 재사용)

### Google Stitch 2.0 활용 (선택)
- 프로필 페이지 디자인: "토스 스타일 프로필 페이지. 큰 아바타, 닉네임, 자기소개, 통계, 하단 탭"

### Acceptance Criteria
- [ ] 프로필 수정 Dialog 동작 + 반영 확인
- [ ] "내 글" 탭 PostList 렌더링
- [ ] `make typecheck` + `make lint-web` 통과

---

## Task 4.3: 관리자 API (백엔드)

### Context
- architecture-plan-v2.md 섹션 6.1 — 관리자 API
- `@PreAuthorize("hasRole('ADMIN')")` 전체 적용

### Deliverables
1. `domain/admin/service/AdminService.java`:
   - `getDashboard()` → DashboardResponse: 총 사용자 수, 오늘 가입 수, 총 게시글 수, 오늘 생성 게시글 수, 총 댓글 수, 신고 대기 수 (COUNT 쿼리)
   - `getUsers(String search, Pageable)` → PageResponse<UserManageResponse>: 검색(이메일/닉네임), status/role 필터
   - `changeUserRole(Long userId, UserRole)` → 역할 변경
   - `banUser(Long userId)` → User.ban(), 이유 기록 (향후)
   - `unbanUser(Long userId)` → User.unban()
   - `forceDeletePost(Long postId)` → Post.softDelete() (관리자 권한)
2. `domain/admin/controller/AdminController.java` — `@RequestMapping("/api/v1/admin")`
   - GET `/dashboard` → 대시보드 통계
   - GET `/users` → 사용자 목록 (검색+페이징)
   - PATCH `/users/{id}/role` → 역할 변경
   - PATCH `/users/{id}/ban` → 정지
   - PATCH `/users/{id}/unban` → 정지 해제
   - DELETE `/posts/{id}` → 관리자 게시글 삭제
3. DTO:
   - `DashboardResponse` — record: totalUsers, todayNewUsers, totalPosts, todayNewPosts, totalComments, pendingReports
   - `UserManageResponse` — record: id, email, nickname, role, isActive, createdAt, lastLoginAt, postCount
   - `ChangeRoleRequest` — record: `@NotNull UserRole role`

### Acceptance Criteria
- [ ] 비관리자 → 403
- [ ] 대시보드 통계 정확성 (COUNT 쿼리 결과와 일치)
- [ ] `make lint-api` 통과

---

## Task 4.4: 관리자 UI (프론트)

### Context
- 디자인: 데스크톱 위주 (관리자는 PC 사용), 토스 파트너 어드민 스타일 참고
- 경로: `apps/web/src/features/admin/`

### Deliverables
1. `features/admin/api/adminApi.ts`, `hooks/useAdmin.ts` (useDashboard, useAdminUsers, useChangeRole, useBanUser, useForceDeletePost)
2. `features/admin/components/Dashboard.tsx`:
   - MUI Grid 2열: 4개 통계 카드
   - 카드: 라벨(body2, textSecondary) + 숫자(h4, textPrimary) + 트렌드 화살표
   - Framer Motion: staggered fade-in 등장
3. `features/admin/components/UserManageTable.tsx`:
   - MUI Table: Avatar+닉네임, 이메일, 역할(Select), 상태(Switch), 가입일
   - 상단: 검색 TextField + 역할 필터 (Select)
   - 페이지네이션: MUI TablePagination
   - 역할 변경: MUI Select → onChangeRole 즉시 호출
   - 정지/해제: Switch → ban/unban 즉시 호출
4. `features/admin/pages/AdminDashboardPage.tsx` — Dashboard
5. `features/admin/pages/AdminUsersPage.tsx` — UserManageTable

### Acceptance Criteria
- [ ] 관리자 로그인 후 대시보드 접근 + 통계 표시
- [ ] 사용자 검색 + 역할 변경 + 정지/해제 UI 동작
- [ ] `make typecheck` + `make lint-web` 통과

---

## 구현 이력

> Claude나 AI 에이전트가 이 Phase를 구현할 때, 아래에 계획과 완료 기록을 남깁니다.
