# Phase 3: 댓글 & 추천

> **상태**: ⬜ 대기
> **목표**: 댓글 CRUD, 대댓글(트리 구조), 추천 토글
> **완료 기준**: 게시글에 댓글 작성 → 대댓글 → 추천 토글 동작
> **참고**: architecture-plan-v2.md 섹션 5.1 (V4 comments, V5 votes 테이블), 섹션 6.1 (댓글/추천 API)

---

## 진행 체크리스트

- [ ] Task 3.1: Comment 엔티티, Service, Controller (백엔드 전체)
- [ ] Task 3.2: Vote 엔티티 & Service (백엔드 전체)
- [ ] Task 3.3: 프론트엔드 댓글 UI

## 검증 기준

- [ ] 댓글 트리 구조 API 응답 확인 (children 배열)
- [ ] 추천 토글 → vote_count 증감 확인
- [ ] 프론트엔드 댓글 UI 렌더링 + VoteButton 연동
- [ ] `make verify` → 전체 통과

---

## Task 3.1: Comment 전체 (백엔드)

### Context
- DB: V4__create_comments_table.sql — Adjacency List 패턴 (parent_id)
- 패키지: `com.community.domain.comment`

### Deliverables
1. `entity/Comment.java` — `@Entity @Table(name = "comments")`, BaseTimeEntity 상속
   - 필드: id, `@ManyToOne(LAZY) @JoinColumn(name="post_id") Post post`, `@ManyToOne(LAZY) @JoinColumn(name="author_id") User author`, `@ManyToOne(LAZY) @JoinColumn(name="parent_id") Comment parent`, `@Column(columnDefinition="TEXT") String content`, voteCount(int, default 0), isDeleted(boolean, default false)
   - 비즈니스 메서드: `update(content)`, `softDelete()`, `increaseVoteCount()`, `decreaseVoteCount()`
2. `repository/CommentRepository.java` — `List<Comment> findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(Long postId)`
3. DTO:
   - `CreateCommentRequest` — record: `@NotBlank String content`, `Long parentId` (nullable: null이면 최상위, 값이면 대댓글)
   - `UpdateCommentRequest` — record: `@NotBlank String content`
   - `CommentResponse` — record: id, UserSummaryResponse author, content, voteCount, isVoted(boolean), isDeleted(boolean), createdAt, `List<CommentResponse> children`
4. `service/CommentService.java`:
   - `createComment(Long postId, Long userId, CreateCommentRequest)` → 게시글 존재 확인, parentId 있으면 부모 댓글 존재 확인, Comment 저장, `post.increaseCommentCount()`
   - `updateComment(Long commentId, Long userId, UpdateCommentRequest)` → 작성자 확인 후 수정
   - `deleteComment(Long commentId, Long userId)` → 작성자 또는 ADMIN 확인, `softDelete()`, `post.decreaseCommentCount()`
   - `getCommentsByPost(Long postId, Long userId)` → 모든 댓글 조회 → **트리 구조 변환**: parent == null인 것이 루트, parentId로 children 매핑. 트리 변환 로직에 단계별 주석 필수
5. `controller/CommentController.java`:
   - GET `/api/v1/posts/{postId}/comments` → 댓글 트리 (Public)
   - POST `/api/v1/posts/{postId}/comments` → 댓글 작성 (인증)
   - PATCH `/api/v1/comments/{id}` → 댓글 수정 (작성자)
   - DELETE `/api/v1/comments/{id}` → 댓글 삭제 (작성자/ADMIN)

### Constraints
- 삭제된 댓글: "삭제된 댓글입니다" 표시하되 children은 유지 (트리 구조 보존)
- author fetchJoin 필수 (N+1 방지)

### Acceptance Criteria
- [ ] 트리 구조 API 응답: 최상위 댓글 → children 배열 포함
- [ ] 대댓글 작성 시 parentId 올바르게 설정
- [ ] softDelete 후에도 children 댓글 보존
- [ ] `make lint-api` 통과

---

## Task 3.2: Vote 전체 (백엔드)

### Context
- DB: V5__create_votes_bookmarks.sql — UNIQUE(user_id, target_type, target_id)
- 패키지: `com.community.domain.vote`

### Deliverables
1. `entity/Vote.java` — `@Entity @Table(uniqueConstraints = @UniqueConstraint(columns = {"user_id", "target_type", "target_id"}))`
   - 필드: id, `@ManyToOne(LAZY) User user`, `String targetType` (CHECK: 'POST', 'COMMENT'), `Long targetId`, createdAt
2. `repository/VoteRepository.java`:
   - `Optional<Vote> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId)`
   - `boolean existsByUserIdAndTargetTypeAndTargetId(...)`
   - `long countByTargetTypeAndTargetId(String targetType, Long targetId)`
3. DTO:
   - `VoteRequest` — record: `@NotBlank String targetType`, `@NotNull Long targetId`
   - `VoteResponse` — record: `boolean isVoted`, `int voteCount`
4. `service/VoteService.java`:
   - `toggleVote(Long userId, VoteRequest)` → 이미 추천 → 삭제 + 카운트 감소, 미추천 → 추가 + 카운트 증가
   - targetType "POST" → `Post.voteCount` 업데이트, "COMMENT" → `Comment.voteCount` 업데이트
   - `isVoted(Long userId, String targetType, Long targetId)` → boolean
   - `// TODO: MSA 분리 시 NotificationService.createNotification 이벤트 발행으로 전환`
5. `controller/VoteController.java`: POST `/api/v1/votes` (인증 필요)

### Acceptance Criteria
- [ ] 추천 토글: 처음 → isVoted:true + voteCount+1, 다시 → isVoted:false + voteCount-1
- [ ] DB UNIQUE 제약으로 동일 사용자 중복 방지
- [ ] `make lint-api` 통과

---

## Task 3.3: 댓글 UI (프론트)

### Context
- 디자인 토큰: `tokens.spacing.*`, `tokens.color.*` 참조
- 경로: `apps/web/src/features/comment/`

### Deliverables
1. `shared/types/comment.ts` — `Comment { id, author: UserSummary, content, voteCount, isVoted, isDeleted, createdAt, children: Comment[] }`
2. `features/comment/api/commentApi.ts` — getComments(postId), createComment(postId, data), updateComment(id, data), deleteComment(id)
3. `features/comment/hooks/useComments.ts` — useQuery + useCreateComment, useUpdateComment, useDeleteComment (각 mutation 성공 시 `['comments', postId]` 캐시 무효화)
4. `features/comment/components/CommentForm.tsx`:
   - MUI TextField(multiline, 2줄) + 제출 Button
   - 답글 모드: "OOO님에게 답글" 표시 + 취소 버튼
   - props: postId, parentId?, onCancel?
5. `features/comment/components/CommentItem.tsx`:
   - UserAvatar + 닉네임 + TimeAgo
   - 본문 텍스트
   - VoteButton + "답글" 버튼
   - 작성자: 수정/삭제 메뉴 (MUI IconButton + Menu)
   - 삭제된 댓글: "삭제된 댓글입니다" 회색 텍스트 (`tokens.color.textTertiary`)
   - "답글" 클릭 → 바로 아래에 CommentForm 토글
   - **대댓글**: `paddingLeft: 40px` + 얇은 `borderLeft` (`tokens.color.border`)
6. `features/comment/components/CommentList.tsx`:
   - useComments 훅, 댓글 없으면 "첫 댓글을 작성해보세요"
   - CommentItem **재귀 렌더링** (children 포함)
   - 하단 고정: 새 댓글 CommentForm
7. `features/comment/index.ts` — barrel export
8. PostDetailPage.tsx 수정: 하단에 CommentList 연결, VoteButton 실제 toggleVote API 호출

### Google Stitch 2.0 활용 (선택)
- 댓글 트리 UI를 Stitch에서 디자인: "모바일 댓글 트리 UI. 대댓글은 왼쪽 indent. 답글 버튼, 추천 하트 아이콘"

### Acceptance Criteria
- [ ] 댓글 목록 (대댓글 indent 포함) 렌더링
- [ ] 댓글 작성/답글/수정/삭제 UI 동작
- [ ] VoteButton 클릭 시 API 호출 + UI 반영
- [ ] `make typecheck` + `make lint-web` 통과

---

## 구현 이력

> Claude나 AI 에이전트가 이 Phase를 구현할 때, 아래에 계획과 완료 기록을 남깁니다.
