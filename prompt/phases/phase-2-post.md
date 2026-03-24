# Phase 2: 게시글 CRUD

> **상태**: ⬜ 대기
> **목표**: 게시글 작성, 조회, 수정, 삭제, 목록(무한스크롤), 검색
> **완료 기준**: 로그인 후 게시글 작성 → 목록에서 확인 → 상세 보기 → 수정/삭제
> **참고**: architecture-plan-v2.md 섹션 3.2 (백엔드 모듈), 섹션 5.1 (posts/categories 테이블), 섹션 6.1 (게시글 API)

---

## 진행 체크리스트

- [ ] Task 2.1: Post 엔티티 & Repository + Category (백엔드)
- [ ] Task 2.2: Post Service & Controller (백엔드)
- [ ] Task 2.3: 프론트엔드 게시글 API & TanStack Query 훅
- [ ] Task 2.4: 게시글 목록 UI (무한 스크롤 + 카테고리 필터)
- [ ] Task 2.5: 게시글 상세 UI
- [ ] Task 2.6: 게시글 에디터 (TipTap)

## 검증 기준

- [ ] `make lint-api` → 컴파일 성공
- [ ] CRUD API 모두 Swagger UI에서 동작 확인
- [ ] 프론트엔드 게시글 목록/상세/에디터 페이지 렌더링
- [ ] 무한 스크롤 동작 확인
- [ ] `make verify` → 전체 통과

---

## Task 2.1: Post 엔티티 & Repository

### Context
- DB: architecture-plan-v2.md 섹션 5.1 — posts(V3), categories(V2) 테이블
- 패키지: `com.community.domain.post`

### Deliverables
1. `entity/Category.java` — `@Entity @Table(name = "categories")`
   - 필드: id, name(VARCHAR 50, UNIQUE), slug(VARCHAR 50, UNIQUE), description(VARCHAR 200), sortOrder(int, default 0), isActive(boolean, default true), createdAt
2. `entity/Post.java` — `@Entity @Table(name = "posts")`, BaseTimeEntity 상속
   - 필드: id, `@ManyToOne(fetch=LAZY) @JoinColumn(name="author_id") User author`, `@ManyToOne(fetch=LAZY) @JoinColumn(name="category_id") Category category`, title(VARCHAR 200), `@Column(columnDefinition="TEXT") String content`, summary(VARCHAR 300), thumbnailUrl(VARCHAR 500), viewCount(int, default 0), voteCount(int, default 0), commentCount(int, default 0), isPinned(boolean, default false), isDeleted(boolean, default false)
   - 비즈니스 메서드: `update(title, content, summary, category)`, `softDelete()`, `pin()`, `unpin()`, `increaseViewCount()`, `increaseVoteCount()`, `decreaseVoteCount()`, `increaseCommentCount()`, `decreaseCommentCount()`
3. `repository/CategoryRepository.java` — `List<Category> findAllByIsActiveTrueOrderBySortOrder()`
4. `repository/PostRepository.java` — `Optional<Post> findByIdAndIsDeletedFalse(Long id)`
5. `repository/PostRepositoryCustom.java` — interface: `Page<Post> search(String keyword, Long categoryId, String sort, Pageable pageable)`
6. `repository/PostRepositoryImpl.java` — QueryDSL 구현:
   - QPost, QUser, QCategory 사용
   - `keyword != null` → `title.containsIgnoreCase(keyword)`
   - `categoryId != null` → `category.id.eq(categoryId)`
   - sort "POPULAR" → `voteCount.desc()`, else → `createdAt.desc()`
   - `isDeleted.isFalse()` 항상 적용
   - `.join(post.author).fetchJoin()` (N+1 방지)
7. DTO:
   - `CreatePostRequest` — record: `@NotBlank @Size(max=200) title`, `@NotBlank content`, `Long categoryId`(nullable), `String thumbnailUrl`(nullable)
   - `UpdatePostRequest` — record: `String title`, `String content`, `Long categoryId`, `String thumbnailUrl` (모두 nullable, 부분 수정)
   - `PostSearchRequest` — record: keyword, categoryId, sort(기본 "LATEST"), page(기본 0), size(기본 20)
   - `PostDetailResponse` — record: id, UserSummaryResponse author, categoryName, categorySlug, title, content, viewCount, voteCount, commentCount, isPinned, isVoted, isBookmarked, createdAt, updatedAt
   - `PostListResponse` — record: id, UserSummaryResponse author, categoryName, categorySlug, title, summary, thumbnailUrl, viewCount, voteCount, commentCount, createdAt
8. `mapper/PostMapper.java` — `@Mapper(componentModel = "spring", uses = {UserMapper.class})`

### Constraints
- summary: content에서 HTML 태그 제거 후 첫 300자 추출
- QueryDSL Q클래스: `./gradlew compileJava`로 생성 필요
- `@ManyToOne(fetch = FetchType.LAZY)` 필수

### Acceptance Criteria
- [ ] `make lint-api` 통과
- [ ] Entity 필드가 DB 스키마(V3, V2)와 정확히 일치

---

## Task 2.2: Post Service & Controller

### Context
- API: architecture-plan-v2.md 섹션 6.1 (게시글 API 엔드포인트)
- 응답 형식: `ApiResponse<T>`, `PageResponse<T>`

### Deliverables
1. `service/PostService.java` — `@Service @RequiredArgsConstructor @Slf4j`
   - `createPost(Long userId, CreatePostRequest)` → User 조회, Category 조회(있으면), Post 빌드&저장, summary 추출, 매퍼 변환
   - `getPost(Long postId, Long userId)` → findByIdAndIsDeletedFalse → POST_NOT_FOUND, 조회수 증가, isVoted/isBookmarked는 userId 있으면 조회
   - `getPosts(PostSearchRequest)` → PostRepositoryCustom.search, PageResponse.of()
   - `updatePost(Long postId, Long userId, UpdatePostRequest)` → 작성자 확인 → 변경 필드만 업데이트
   - `deletePost(Long postId, Long userId)` → 작성자 또는 ADMIN 확인 → softDelete()
   - 읽기: `@Transactional(readOnly=true)`, 쓰기: `@Transactional`
2. `controller/PostController.java` — `@RestController @RequestMapping("/api/v1/posts") @Tag(name = "게시글")`
   - GET `/` → getPosts(`@ModelAttribute PostSearchRequest`) — Public
   - POST `/` → createPost(`@AuthenticationPrincipal`, `@Valid @RequestBody`) — 인증
   - GET `/{id}` → getPost(`@PathVariable`, `@AuthenticationPrincipal(required=false)`) — Public
   - PATCH `/{id}` → updatePost — 인증
   - DELETE `/{id}` → deletePost — 인증
   - GET `/search` → 게시글 검색 — Public
3. `controller/CategoryController.java` — GET `/api/v1/categories` → 활성 카테고리 목록 (Public)

### Acceptance Criteria
- [ ] Swagger UI에서 CRUD + 카테고리 목록 모두 동작
- [ ] 권한 없는 수정/삭제 시 403 또는 UNAUTHORIZED_MODIFICATION 반환
- [ ] 삭제 후 목록에서 미노출 확인

---

## Task 2.3: 프론트엔드 게시글 API & 훅

### Context
- 경로: `apps/web/src/features/post/`, `shared/types/post.ts`
- TanStack Query v5 사용

### Deliverables
1. `shared/types/post.ts`:
   - `Post { id, author: UserSummary, categoryName, categorySlug, title, content, viewCount, voteCount, commentCount, isPinned, isVoted, isBookmarked, createdAt, updatedAt }`
   - `PostListItem { id, author: UserSummary, categoryName, categorySlug, title, summary, thumbnailUrl, viewCount, voteCount, commentCount, createdAt }`
   - `CreatePostPayload { title, content, categoryId?, thumbnailUrl? }`
   - `UpdatePostPayload { title?, content?, categoryId?, thumbnailUrl? }`
   - `PostSearchParams { keyword?, categoryId?, sort?: 'LATEST'|'POPULAR', page?, size? }`
   - `Category { id, name, slug, description }`
2. `features/post/api/postApi.ts` — getPosts, getPost, createPost, updatePost, deletePost, getCategories (extractData 사용)
3. `features/post/hooks/usePosts.ts`:
   - `usePosts()`: `useInfiniteQuery({ queryKey: ['posts', { categoryId, sort }], queryFn: ({pageParam=0}) => ..., getNextPageParam: (lastPage) => lastPage.hasNext ? lastPage.page+1 : undefined })`
   - `useCategories()`: `useQuery({ queryKey: ['categories'], queryFn: getCategories, staleTime: 1000*60*60 })` — 1시간 캐시
4. `features/post/hooks/usePost.ts` — `useQuery({ queryKey: ['post', id], queryFn: () => getPost(id), enabled: !!id })`
5. `features/post/hooks/usePostMutation.ts`:
   - `useCreatePost()` — onSuccess: invalidate `['posts']` + navigate
   - `useUpdatePost()` — onSuccess: invalidate `['posts']` + `['post', id]`
   - `useDeletePost()` — onSuccess: navigate('/') + invalidate
6. `features/post/index.ts` — barrel export

### Acceptance Criteria
- [ ] `make typecheck` 통과
- [ ] TanStack Query 훅에 queryKey 올바르게 설정

---

## Task 2.4: 게시글 목록 UI (무한 스크롤)

### Context
- 디자인: 토스 앱 스타일 카드 리스트, 모바일 퍼스트 (한 줄에 카드 1개)
- 디자인 토큰 참조: `tokens.spacing.*`, `tokens.radius.*`, `tokens.color.*`, `tokens.shadow.*`

### Deliverables
1. `shared/hooks/useInfiniteScroll.ts` — IntersectionObserver 기반, ref 반환
2. `shared/components/feedback/LoadingSpinner.tsx` — MUI CircularProgress 중앙 정렬, size prop('sm'|'md'|'lg')
3. `shared/components/feedback/EmptyState.tsx` — icon + message + action? 중앙 정렬
4. `features/post/components/CategoryFilter.tsx`:
   - useCategories 훅으로 로딩
   - MUI Chip 가로 스크롤: `flexWrap: 'nowrap'`, `overflowX: 'auto'`, scrollbar 숨김
   - "전체" 칩 + 카테고리별 칩
   - 선택됨: `color="primary" variant="filled"`, 미선택: `variant="outlined"`
   - 좌우 패딩 `tokens.spacing.page`, 칩 간격 `tokens.spacing.sm`
5. `features/post/components/PostCard.tsx`:
   - MUI Card (`tokens.radius.lg`, `tokens.shadow.sm`)
   - 상단: UserAvatar(작은) + 닉네임 + "·" + TimeAgo
   - 카테고리 칩 (있으면)
   - 제목: Typography h6, 1줄 ellipsis
   - 요약: Typography body2, 2줄 ellipsis (`-webkit-line-clamp: 2`)
   - 썸네일 (있으면): CardMedia, 높이 180px, `tokens.radius.lg`
   - 하단: 추천(Heart) + 댓글(MessageCircle) + 조회(Eye) — lucide-react 아이콘
   - Framer Motion: `whileTap={{ scale: 0.98 }}` (토스 터치 피드백)
   - 클릭: `navigate(/posts/${id})`
6. `features/post/components/PostList.tsx`:
   - 로딩: 스켈레톤 카드 3개 (MUI Skeleton)
   - 데이터: PostCard 목록, 간격 `tokens.spacing.sm`
   - 빈 상태: EmptyState "아직 게시글이 없습니다"
   - 무한 스크롤: 목록 끝에 ref + LoadingSpinner
7. `features/post/pages/PostListPage.tsx`:
   - CategoryFilter: position sticky, top 56px, 배경 `tokens.color.surface`, zIndex 1
   - 정렬 토글: "최신순" | "인기순" (ToggleButtonGroup 또는 Tabs)
   - PostList
   - 배경 `tokens.color.background`, 카드는 `tokens.color.surface`

### Google Stitch 2.0 활용 (선택)
- PostCard, PostList 레이아웃을 Stitch에서 먼저 디자인 가능
- 프롬프트: "토스 스타일 게시글 카드 목록. 모바일 퍼스트, 아바타+닉네임+시간, 제목, 요약 2줄, 하단 통계"

### Acceptance Criteria
- [ ] 게시글 목록 페이지 렌더링 + 토스 스타일 확인
- [ ] 카테고리 칩 클릭 시 필터링 동작
- [ ] 무한 스크롤 하단 도달 시 다음 페이지 자동 로딩
- [ ] 스켈레톤 로딩 표시

---

## Task 2.5: 게시글 상세 UI

### Deliverables
1. `shared/components/common/TimeAgo.tsx`:
   - dayjs + relativeTime 플러그인, locale 'ko'
   - 1분 미만: "방금 전", 1시간 미만: "N분 전", 24시간 미만: "N시간 전", 7일 미만: "N일 전", 이후: "YYYY.MM.DD"
2. `shared/components/common/VoteButton.tsx`:
   - props: targetType('POST'|'COMMENT'), targetId, voteCount, isVoted, onToggle
   - lucide-react Heart 아이콘 + 카운트
   - isVoted: fill primary, 카운트 primary 색상
   - Framer Motion: 추천 시 scale `1 → 1.2 → 1` (`tokens.motion.duration.fast`)
   - 미로그인 시 → /login 리다이렉트
3. `features/post/components/PostDetail.tsx`:
   - 작성자: UserAvatar + 닉네임 + TimeAgo
   - 카테고리 칩
   - 제목: Typography h5
   - 본문: `dangerouslySetInnerHTML` (향후 DOMPurify 추가)
   - 하단 액션바: VoteButton + BookmarkButton(placeholder) + 공유
   - 작성자 본인: 더보기 MUI IconButton+Menu → 수정, 삭제
   - ADMIN: 더보기에 "삭제" 추가
   - 삭제: MUI Dialog 확인 후 useDeletePost
4. `features/post/pages/PostDetailPage.tsx`:
   - `useParams`로 id 추출
   - 로딩: 스켈레톤 (제목+본문+아바타)
   - 404: "게시글을 찾을 수 없습니다" + 홈으로 돌아가기
   - 하단: 댓글 영역 placeholder (Phase 3)

### Acceptance Criteria
- [ ] 상세 페이지 렌더링 + 작성자 메뉴 동작
- [ ] TimeAgo "N분 전" 한국어 형식
- [ ] VoteButton 애니메이션

---

## Task 2.6: 게시글 에디터 (TipTap)

### Deliverables
1. `features/post/components/PostEditor.tsx`:
   - TipTap extensions: StarterKit, Placeholder("내용을 입력하세요..."), Image, Link, Underline
   - 툴바: Bold, Italic, Underline, Heading2, Heading3, BulletList, OrderedList, Blockquote, Code, Link, Image — lucide-react 아이콘
   - 활성 버튼: `tokens.color.primary` 배경
   - 에디터 영역: min-height 300px, border `tokens.color.border`, borderRadius `tokens.radius.md`, padding `tokens.spacing.lg`
   - 이미지: Phase 5 전까지 URL 직접 입력 (window.prompt가 아닌 MUI Dialog)
2. `features/post/pages/PostEditorPage.tsx`:
   - `:id` 있으면 수정 모드, 없으면 작성 모드
   - 수정 모드: usePost 데이터 prefill
   - 폼: 카테고리 MUI Select + 제목 TextField + PostEditor
   - 버튼: "게시하기" | "수정하기"
   - 뒤로가기: 내용 있으면 Dialog "작성을 취소하시겠습니까?"
   - summary 자동 추출: content HTML 태그 제거 후 첫 300자
   - 추가 패키지: `@tiptap/react @tiptap/starter-kit @tiptap/extension-placeholder @tiptap/extension-image @tiptap/extension-link @tiptap/extension-underline`

### Acceptance Criteria
- [ ] 에디터에서 글 작성 → 제출 → 생성 확인
- [ ] 수정 모드: 기존 데이터 prefill + 수정 성공
- [ ] 툴바 버튼 활성/비활성 스타일 동작

---

## 구현 이력

> Claude나 AI 에이전트가 이 Phase를 구현할 때, 아래에 계획과 완료 기록을 남깁니다.
