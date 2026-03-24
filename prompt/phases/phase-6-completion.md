# Phase 6: 완성

> **상태**: ⬜ 대기
> **목표**: 북마크, 신고, 홈 화면 구성으로 서비스 완성
> **완료 기준**: 홈 화면에서 인기 글 + 최신 글 표시, 북마크/신고 동작
> **참고**: architecture-plan-v2.md 섹션 5.1 (V5 bookmarks, V7 reports 테이블), 섹션 6.1 (API)

---

## 진행 체크리스트

- [ ] Task 6.1: 북마크 (풀스택)
- [ ] Task 6.2: 신고 (풀스택)
- [ ] Task 6.3: 홈 화면 구성

## 검증 기준

- [ ] 북마크 토글 + 내 북마크 목록 동작
- [ ] 신고 접수 + 관리자 신고 처리(해결/무시)
- [ ] 홈 화면: 인기 게시글 가로 스크롤 + 최신 피드
- [ ] `make verify` → 전체 통과

---

## Task 6.1: 북마크 (풀스택)

### 백엔드
- `domain/bookmark/entity/Bookmark.java` — `@Entity`, UNIQUE(user_id, post_id), `@ManyToOne(LAZY)` User + Post, createdAt
- `repository/BookmarkRepository.java` — `Optional findByUserIdAndPostId`, `boolean existsBy...`, `Page<Bookmark> findByUserId(Long, Pageable)`
- `service/BookmarkService.java`: `toggle(userId, postId)` → 존재→삭제, 미존재→추가, `isBookmarked(userId, postId)`, `getMyBookmarks(userId, Pageable)` → PostListResponse 변환
- `controller/BookmarkController.java`:
  - POST `/api/v1/bookmarks` (toggle, body: postId) → 인증
  - GET `/api/v1/bookmarks` (내 목록, 페이징) → 인증

### 프론트엔드
- `shared/types/bookmark.ts`
- `shared/components/common/BookmarkButton.tsx`:
  - lucide-react Bookmark 아이콘 + 토글
  - 활성: fill `tokens.color.primary`
  - Framer Motion scale 애니메이션
- PostDetail에 BookmarkButton 연결 (VoteButton 옆)
- ProfilePage "북마크" 탭에 내 북마크 목록 → PostList 재사용

### Acceptance Criteria
- [ ] 북마크 토글 API 동작 + UI 반영
- [ ] 프로필 > 북마크 탭 목록 동작

---

## Task 6.2: 신고 (풀스택)

### 백엔드
- `domain/report/entity/Report.java` — `@Entity @Table(name = "reports")`
  - 필드: id, `@ManyToOne(LAZY) User reporter`, targetType(String, 'POST'|'COMMENT'|'USER'), targetId(Long), reason(ReportReason enum: SPAM, PROFANITY, INAPPROPRIATE, COPYRIGHT, OTHER), description(VARCHAR 1000, nullable), status(ReportStatus enum: PENDING, RESOLVED, DISMISSED), resolvedBy(Long, nullable), resolvedAt(Timestamp, nullable), createdAt
- `service/ReportService.java`: `createReport(userId, CreateReportRequest)`, 중복 신고 방지 (동일 사용자 + 동일 대상)
- `controller/ReportController.java`: POST `/api/v1/reports` (인증)
- AdminService에 추가: `getReports(status, Pageable)`, `resolveReport(reportId, adminUserId, ReportStatus)`
- AdminController에 추가: GET `/admin/reports`, PATCH `/admin/reports/{id}`

### 프론트엔드
- 게시글/댓글 더보기 메뉴(MUI Menu)에 "🚨 신고" 옵션 추가
- 신고 Dialog:
  - 사유 라디오: 스팸, 욕설/비방, 부적절한 내용, 저작권 침해, 기타
  - "기타" 선택 시 상세 설명 TextField 표시
  - "신고하기" Button → createReport 호출
- `features/admin/components/ReportList.tsx` — MUI Table: 타입, 대상, 사유, 상태, 신고일, 액션(해결/무시)
- `features/admin/pages/AdminReportsPage.tsx`

### Acceptance Criteria
- [ ] 신고 접수 → DB에 PENDING 상태로 저장
- [ ] 동일 사용자 동일 대상 중복 신고 방지
- [ ] 관리자 RESOLVED/DISMISSED 처리 동작

---

## Task 6.3: 홈 화면 구성

### Context
- 디자인 철학: "한 화면 한 목적" — 홈은 콘텐츠 발견에 집중
- 디자인 토큰: `tokens.spacing.section(28px)` 섹션 간 간격, Framer Motion staggered

### Deliverables
1. Backend API 추가:
   - PostService에 `getHotPosts(int count)` → 최근 7일 내 voteCount 상위 N개
   - PostController에 GET `/api/v1/posts/hot?count=5`
2. `features/home/components/HotPosts.tsx`:
   - "이번 주 인기 글 🔥" 섹션 제목 (Typography h6)
   - 가로 스크롤 카드: `display: flex`, `overflowX: auto`, `scrollSnapType: 'x mandatory'`, `scrollSnapAlign: 'center'`
   - 각 카드: width 280px, 제목 + 요약 1줄 + 카테고리 + 추천 수, 그라데이션 배경
   - scrollbar 숨김 CSS
3. `features/home/pages/HomePage.tsx`:
   - 인사말:
     - 로그인: `"안녕하세요, {닉네임}님! 👋"` (h5, textPrimary)
     - 비로그인: `"커뮤니티에 오신 걸 환영합니다! 🎉"` + 로그인 유도 Button
   - HotPosts: 인기 글 가로 스크롤
   - Divider + 섹션 간격 (`tokens.spacing.section`)
   - "최신 글" 섹션 제목 + PostList 재사용 (정렬 LATEST)
   - Framer Motion: staggered fade-in (`staggerChildren: 0.1`)
4. `app/router.tsx` 수정: `/` 경로를 PostListPage 대신 HomePage로 변경

### Google Stitch 2.0 활용 (선택)
- 홈 화면 전체 레이아웃을 Stitch에서 먼저 디자인
- 프롬프트: "토스 스타일 커뮤니티 홈 화면. 인사말 섹션, 인기 글 가로 스크롤 카드, 구분선, 최신 게시글 피드"

### Acceptance Criteria
- [ ] 홈 화면 인기 글 가로 스크롤 (scroll-snap)
- [ ] 최신 피드 무한 스크롤 동작
- [ ] Framer Motion 등장 애니메이션
- [ ] `make typecheck` + `make lint-web` 통과

---

## 구현 이력

> Claude나 AI 에이전트가 이 Phase를 구현할 때, 아래에 계획과 완료 기록을 남깁니다.
