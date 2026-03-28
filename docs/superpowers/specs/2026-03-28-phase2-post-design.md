# Phase 2: 게시글 CRUD — 설계 문서

> **작성일**: 2026-03-28
> **상태**: 승인됨 (구현 대기)
> **참조**: `prompt/phases/phase-2-post.md`, `prompt/architecture-plan-v2.md`

---

## 1. 개요

로그인한 사용자가 게시글을 작성·조회·수정·삭제할 수 있는 커뮤니티 핵심 기능 구현.
카테고리 필터링, 무한 스크롤 목록, TipTap 기반 리치 텍스트 에디터를 포함한다.

**완료 기준**: 로그인 → 게시글 작성 → 목록 확인 → 상세 보기 → 수정/삭제

---

## 2. 기술 결정 사항

### 2.1 isVoted / isBookmarked 처리
- Phase 2에서 Vote/Bookmark 테이블은 구현하지 않음 (Phase 3/6 담당)
- `PostDetailResponse`의 `isVoted`, `isBookmarked` 필드는 **`false` 하드코딩** 반환
- Phase 3 구현 시 `PostService.getPost()`에서 실제 조회로 교체

### 2.2 TipTap 버전
- 현재 설치: `@tiptap/react@3.x`, `@tiptap/starter-kit@3.x`
- 추가 설치 필요: `@tiptap/extension-placeholder`, `@tiptap/extension-image`, `@tiptap/extension-link`, `@tiptap/extension-underline`
- TipTap 3.x API 기준으로 구현

### 2.3 summary 추출
- 백엔드: content HTML에서 태그 제거 후 첫 300자 추출 (정규식 기반)
- 프론트엔드: 에디터 submit 시 동일 로직으로 추출하여 `CreatePostPayload`에 포함

### 2.4 조회수 증가
- `getPost()` 호출 시 즉시 DB 업데이트 (`post.increaseViewCount()`)
- 동시성 이슈는 Phase 2에서 허용 (낮은 트래픽 가정), 추후 Redis 캐싱으로 개선 가능

### 2.5 이미지 삽입 (Phase 2)
- Phase 5 전까지 URL 직접 입력 방식
- `window.prompt` 대신 **MUI Dialog**로 URL 입력받음

---

## 3. 백엔드 아키텍처

### 3.1 패키지 구조
```
com.community.domain.post/
├── entity/
│   ├── Category.java
│   └── Post.java
├── repository/
│   ├── CategoryRepository.java
│   ├── PostRepository.java
│   ├── PostRepositoryCustom.java      ← QueryDSL 인터페이스
│   └── PostRepositoryImpl.java        ← QueryDSL 구현
├── dto/
│   ├── request/
│   │   ├── CreatePostRequest.java
│   │   ├── UpdatePostRequest.java
│   │   └── PostSearchRequest.java
│   └── response/
│       ├── PostDetailResponse.java
│       └── PostListResponse.java
├── mapper/
│   └── PostMapper.java
├── service/
│   └── PostService.java
└── controller/
    ├── PostController.java
    └── CategoryController.java
```

### 3.2 엔티티 설계

**Category**
```
id | name(UNIQUE) | slug(UNIQUE) | description | sort_order | is_active | created_at
```
- `findAllByIsActiveTrueOrderBySortOrder()` 메서드로 활성 카테고리 정렬 조회

**Post** (BaseTimeEntity 상속)
```
id | author(LAZY FK→users) | category(LAZY FK→categories, nullable) |
title | content(TEXT) | summary | thumbnail_url |
view_count | vote_count | comment_count |
is_pinned | is_deleted | created_at | updated_at
```

비즈니스 메서드:
- `update(title, content, summary, category)` — 변경 필드만 반영
- `softDelete()` — `is_deleted = true`
- `increaseViewCount()`, `increaseVoteCount()`, `decreaseVoteCount()`
- `increaseCommentCount()`, `decreaseCommentCount()`
- `pin()`, `unpin()`

### 3.3 QueryDSL 검색

```java
// PostRepositoryImpl — search() 조건
WHERE is_deleted = false
  AND (keyword IS NULL OR title ILIKE '%keyword%')
  AND (categoryId IS NULL OR category.id = categoryId)
ORDER BY (sort="POPULAR") ? vote_count DESC : created_at DESC
JOIN FETCH author  // N+1 방지
```

### 3.4 API 엔드포인트

| Method | Path | 인증 | 설명 |
|--------|------|------|------|
| GET | `/api/v1/posts` | ✗ | 게시글 목록 (페이징) |
| POST | `/api/v1/posts` | ✓ | 게시글 작성 |
| GET | `/api/v1/posts/{id}` | optional | 게시글 상세 + 조회수 증가 |
| PATCH | `/api/v1/posts/{id}` | ✓ 작성자/ADMIN | 게시글 수정 |
| DELETE | `/api/v1/posts/{id}` | ✓ 작성자/ADMIN | Soft Delete |
| GET | `/api/v1/posts/search` | ✗ | 키워드 검색 |
| GET | `/api/v1/categories` | ✗ | 활성 카테고리 목록 |

---

## 4. 프론트엔드 아키텍처

### 4.1 파일 구조
```
apps/web/src/
├── shared/
│   ├── types/
│   │   └── post.ts                    ← Post, PostListItem, Category 타입
│   ├── hooks/
│   │   └── useInfiniteScroll.ts       ← IntersectionObserver 기반
│   └── components/
│       ├── feedback/
│       │   ├── LoadingSpinner.tsx
│       │   └── EmptyState.tsx
│       └── common/
│           ├── TimeAgo.tsx            ← dayjs + relativeTime, 한국어
│           └── VoteButton.tsx         ← 게시글/댓글 공용 추천 버튼
└── features/
    └── post/
        ├── api/
        │   └── postApi.ts
        ├── hooks/
        │   ├── usePosts.ts            ← useInfiniteQuery
        │   ├── usePost.ts             ← useQuery
        │   └── usePostMutation.ts     ← create/update/delete
        ├── components/
        │   ├── CategoryFilter.tsx     ← 가로 스크롤 Chip 필터
        │   ├── PostCard.tsx           ← 목록 카드 (Framer Motion)
        │   ├── PostList.tsx           ← 스켈레톤 + 무한 스크롤
        │   ├── PostDetail.tsx         ← 상세 + 수정/삭제 메뉴
        │   └── PostEditor.tsx         ← TipTap 에디터 + 툴바
        ├── pages/
        │   ├── PostListPage.tsx
        │   ├── PostDetailPage.tsx
        │   └── PostEditorPage.tsx     ← 작성/수정 통합
        └── index.ts                   ← barrel export
```

### 4.2 타입 정의 (post.ts)

```typescript
Post { id, author: UserSummary, categoryName, categorySlug, title, content,
       viewCount, voteCount, commentCount, isPinned, isVoted, isBookmarked,
       createdAt, updatedAt }

PostListItem { id, author: UserSummary, categoryName, categorySlug, title,
               summary, thumbnailUrl, viewCount, voteCount, commentCount, createdAt }

CreatePostPayload { title, content, categoryId?, thumbnailUrl?, summary }
UpdatePostPayload { title?, content?, categoryId?, thumbnailUrl?, summary? }
PostSearchParams { keyword?, categoryId?, sort?: 'LATEST'|'POPULAR', page?, size? }
Category { id, name, slug, description }
```

### 4.3 TanStack Query 캐시 전략

| Query Key | staleTime | 설명 |
|-----------|-----------|------|
| `['posts', {categoryId, sort}]` | 30초 | 무한 스크롤 목록 |
| `['post', id]` | 60초 | 게시글 상세 |
| `['categories']` | 1시간 | 카테고리 목록 (거의 변하지 않음) |

Mutation 성공 시:
- createPost → `['posts']` invalidate + navigate(`/posts/{id}`)
- updatePost → `['posts']`, `['post', id]` invalidate
- deletePost → navigate('/') + `['posts']` invalidate

### 4.4 UI 설계 원칙

- **모바일 퍼스트**: 최대 너비 600px, 카드 1열
- **토스 스타일**: 둥근 모서리(`tokens.radius.lg`), 넉넉한 여백, 부드러운 터치 피드백
- **Framer Motion**: PostCard `whileTap={{ scale: 0.98 }}`, VoteButton 추천 애니메이션 (`scale 1→1.2→1`)
- **CategoryFilter**: sticky top, 가로 스크롤 숨김 (`overflow-x: auto`, scrollbar hidden)
- **로딩 상태**: MUI Skeleton으로 스켈레톤 카드 3개
- **빈 상태**: EmptyState 컴포넌트 ("아직 게시글이 없습니다")

### 4.5 PostEditor (TipTap 3.x)

Extensions: `StarterKit`, `Placeholder`, `Image`, `Link`, `Underline`
툴바 버튼: Bold, Italic, Underline, H2, H3, BulletList, OrderedList, Blockquote, Code, Link, Image
이미지 삽입: MUI Dialog로 URL 입력 (Phase 5에서 MinIO 업로드로 교체)

---

## 5. 라우팅 추가

```typescript
// app/router.tsx에 추가
/posts              → PostListPage (공개)
/posts/new          → PostEditorPage (인증 필요)
/posts/:id          → PostDetailPage (공개)
/posts/:id/edit     → PostEditorPage (인증 + 작성자)
```

---

## 6. 추가 패키지 설치

**프론트엔드** (npm):
```
@tiptap/extension-placeholder
@tiptap/extension-image
@tiptap/extension-link
@tiptap/extension-underline
```

---

## 7. 검증 기준

- [ ] `make lint-api` → 컴파일 성공 (QueryDSL Q클래스 포함)
- [ ] CRUD API Swagger UI에서 모두 동작
- [ ] 권한 없는 수정/삭제 시 403 반환
- [ ] 게시글 목록 + 카테고리 필터 + 정렬 동작
- [ ] 무한 스크롤 하단 도달 시 다음 페이지 로딩
- [ ] 스켈레톤 로딩 표시
- [ ] 상세 페이지 작성자 수정/삭제 메뉴
- [ ] TimeAgo 한국어 형식
- [ ] TipTap 에디터 작성 → 제출 → 목록 확인
- [ ] 수정 모드 기존 데이터 prefill
- [ ] `make verify` 전체 통과
