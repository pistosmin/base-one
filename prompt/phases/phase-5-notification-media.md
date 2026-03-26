# Phase 5: 알림 & 미디어

> **상태**: ⬜ 대기
> **목표**: 인앱 알림 시스템, 이미지/파일 업로드 (MinIO)
> **완료 기준**: 댓글 작성 시 알림 생성 + 이미지 업로드 + 프로필 이미지 변경
> **참고**: architecture-plan-v2.md 섹션 5.1 (V6 notifications, V7 media 테이블), 섹션 6.1 (알림/미디어 API)

---

## 진행 체크리스트

- [ ] Task 5.1: 알림 시스템 (백엔드)
- [ ] Task 5.2: 프론트엔드 알림 UI
- [ ] Task 5.3: 미디어 업로드 (백엔드 — MinIO)
- [ ] Task 5.4: 프론트엔드 이미지 업로드 UI

## 검증 기준

- [ ] 댓글 작성 → 게시글 작성자에게 알림 (본인 제외)
- [ ] 추천 → 대상 작성자에게 알림 (본인 제외)
- [ ] POST /api/v1/media/upload → MinIO에 파일 저장 + URL 반환
- [ ] 알림 읽음 처리 + 전체 읽음
- [ ] `make verify` → 전체 통과

---

## Task 5.1: 알림 시스템 (백엔드)

### Context
- DB: V6__create_notifications_table.sql
- 패키지: `com.community.domain.notification`
- architecture-plan-v2.md: 알림 서비스는 **MSA 분리 1순위** → 인터페이스 깔끔하게 설계

### Deliverables
1. `entity/Notification.java` — `@Entity @Table(name = "notifications")`
   - 필드: id, `@ManyToOne(LAZY) User recipient`, `@ManyToOne(LAZY) User actor`, type(NotificationType), targetType(String, 'POST'|'COMMENT'), targetId(Long), content(VARCHAR 500), isRead(boolean, default false), createdAt
2. `entity/NotificationType.java` — enum: COMMENT("댓글"), VOTE("추천"), FOLLOW("팔로우"), ADMIN_NOTICE("관리자 공지")
3. `repository/NotificationRepository.java`:
   - `Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable)`
   - `long countByRecipientIdAndIsReadFalse(Long recipientId)`
   - `List<Notification> findByRecipientIdAndIsReadFalse(Long recipientId)`
4. `service/NotificationService.java`:
   - `createNotification(Long recipientId, Long actorId, NotificationType, String targetType, Long targetId, String content)` — **recipientId == actorId이면 스킵** (자기 자신에게 알림 X)
   - `getMyNotifications(Long userId, Pageable)` — PageResponse 반환
   - `markAsRead(Long notificationId, Long userId)` — 본인 확인 후 isRead=true
   - `markAllAsRead(Long userId)` — 해당 사용자의 모든 미읽은 알림 읽음 처리 (벌크 업데이트)
   - `getUnreadCount(Long userId)` → long
5. `controller/NotificationController.java`:
   - GET `/api/v1/notifications` → 내 알림 목록 (페이징)
   - PATCH `/api/v1/notifications/{id}/read` → 단일 읽음
   - PATCH `/api/v1/notifications/read-all` → 전체 읽음
   - GET `/api/v1/notifications/unread-count` → 미읽은 수
6. **이벤트 리스너 구현** (기존 서비스 직접 수정 불필요):
   - `NotificationEventListener.java` 생성 (`@Component`)
   - `@Async("eventTaskExecutor") @EventListener` 메서드로 `CommentCreatedEvent` 수신 → `notificationService.createNotification(...)` 호출
   - 동일하게 `VoteEvent` 수신 → 알림 생성

### Acceptance Criteria
- [ ] 댓글 → 게시글 작성자 알림 (본인 댓글은 알림 X)
- [ ] 추천 → 대상 작성자 알림 (본인 추천은 알림 X)
- [ ] 전체 읽음 처리 동작
- [ ] `make lint-api` 통과

---

## Task 5.2: 알림 UI (프론트)

### Context
- 경로: `apps/web/src/features/notification/`
- 디자인 토큰: background vs surface로 읽음/안읽음 구분

### Deliverables
1. `shared/types/notification.ts` — Notification { id, actor: UserSummary, type, targetType, targetId, content, isRead, createdAt }
2. `features/notification/api/notificationApi.ts` — getNotifications(page), markAsRead(id), markAllAsRead(), getUnreadCount()
3. `features/notification/hooks/useNotifications.ts`:
   - `useNotifications()` — useInfiniteQuery (무한 스크롤)
   - `useUnreadCount()` — useQuery + `refetchInterval: 30000` (30초 폴링)
   - useMarkAsRead, useMarkAllAsRead 뮤테이션
4. `features/notification/components/NotificationBadge.tsx` — MUI Badge: unreadCount > 0이면 빨간 뱃지, BottomTabBar/TopAppBar에서 사용
5. `features/notification/components/NotificationItem.tsx`:
   - 타입별 아이콘 (COMMENT→MessageCircle, VOTE→Heart, ADMIN→Megaphone — lucide-react)
   - 안읽음: `background: tokens.color.primaryLight`, 읽음: `background: tokens.color.surface`
   - actor 아바타 + content + TimeAgo
   - 클릭: 해당 게시글로 이동 + markAsRead
6. `features/notification/components/NotificationList.tsx` — 무한 스크롤 + 상단 "모두 읽음" Button
7. `features/notification/pages/NotificationPage.tsx`
8. BottomTabBar.tsx 수정: 알림 탭에 NotificationBadge 추가

### Acceptance Criteria
- [ ] 알림 페이지 렌더링 + 무한 스크롤
- [ ] 30초마다 unreadCount 자동 갱신
- [ ] 뱃지 BottomTabBar에 표시
- [ ] `make typecheck` + `make lint-web` 통과

---

## Task 5.3: 미디어 업로드 (백엔드 — MinIO)

### Context
- architecture-plan-v2.md 섹션 2.3 — MinIO(S3 호환) + Docker Compose에 이미 설정됨
- DB: V7__create_supporting_tables.sql — media 테이블
- architecture-plan-v2.md: 미디어 서비스는 **MSA 분리 2순위**

### Deliverables
1. `global/config/MinioConfig.java`:
   - `@Configuration`, `@Value("${app.minio.*}")` (endpoint, accessKey, secretKey, bucket)
   - `MinioClient` 빈 등록 (`io.minio:minio`)
   - `@PostConstruct`: 버킷 미존재 시 자동 생성
2. `domain/media/entity/Media.java` — `@Entity @Table(name = "media")`
   - 필드: id, `@ManyToOne(LAZY) User uploader`, originalFilename(VARCHAR 255), storedFilename(VARCHAR 255), mimeType(VARCHAR 50), fileSize(Long), url(VARCHAR 500), thumbnailUrl(VARCHAR 500, nullable), createdAt
3. `domain/media/repository/MediaRepository.java`
4. `domain/media/service/ImageResizeService.java`:
   - Thumbnailator (`net.coobird:thumbnailator:0.4.20`)
   - `resizeSmart(InputStream, int maxWidth)` → 원본이 maxWidth보다 작으면 리사이징 안 함
   - 썸네일: 400x400 crop
   - 본문 이미지: maxWidth 1200px
5. `domain/media/service/MediaService.java`:
   - `uploadImage(Long userId, MultipartFile)`:
     - 확장자 검증: jpg, jpeg, png, gif, webp → 나머지 `ErrorCode.INVALID_FILE_TYPE`
     - MIME 검증: `image/*` → 확장자와 불일치 시 에러
     - 크기 검증: 10MB 초과 → `ErrorCode.FILE_SIZE_EXCEEDED`
     - 파일명: `UUID.randomUUID() + "." + ext`
     - MinIO 업로드: 원본 + 썸네일(별도 경로)
     - Media 엔티티 저장 + 응답
6. `domain/media/dto/response/MediaResponse.java` — record: id, url, thumbnailUrl, originalFilename, mimeType, fileSize
7. `domain/media/controller/MediaController.java`:
   - POST `/api/v1/media/upload` — `@RequestParam("file") MultipartFile` (인증 필요)
   - application.yml에 `spring.servlet.multipart.max-file-size: 10MB` 설정 확인

### Constraints
- build.gradle.kts에 추가: `implementation("io.minio:minio:8.5.14")`, `implementation("net.coobird:thumbnailator:0.4.20")`
- MinIO 개발 환경: docker-compose에서 localhost:9000 (API) / localhost:9001 (Console)

### Acceptance Criteria
- [ ] Swagger에서 이미지 업로드 → MinIO Console(localhost:9001)에서 파일 확인
- [ ] 썸네일 자동 생성 + 별도 URL 반환
- [ ] 잘못된 확장자/크기 업로드 시 에러 응답
- [ ] `make lint-api` 통과

---

## Task 5.4: 이미지 업로드 UI (프론트)

### Deliverables
1. `shared/api/mediaApi.ts` — `uploadImage(file: File): Promise<MediaResponse>` (FormData POST, Content-Type 자동 설정)
2. `features/user/components/AvatarUploader.tsx`:
   - 현재 아바타 클릭 → 숨겨진 `<input type="file" accept="image/*" />` 트리거
   - 선택 → 미리보기 (URL.createObjectURL) → 업로드 (로딩 표시) → 프로필 이미지 URL 업데이트 API 호출
   - 크기 제한: 클라이언트에서도 10MB 사전 검증
3. PostEditorPage 수정:
   - 에디터 툴바 이미지 버튼: 클릭 → 파일 선택 → 업로드 → 반환된 URL로 TipTap `editor.chain().setImage({ src: url })` 삽입
   - 업로드 중: 로딩 표시 (Button disabled + CircularProgress)
4. UserController에 PATCH `/api/v1/users/me/profile-image` 추가 (선택적으로 updateMyProfile에 포함 가능)

### Acceptance Criteria
- [ ] 프로필 이미지 업로드 + 반영 (아바타 즉시 변경)
- [ ] 에디터에서 이미지 삽입 동작
- [ ] 10MB 초과 파일 선택 시 경고
- [ ] `make typecheck` + `make lint-web` 통과

---

## 구현 이력

> Claude나 AI 에이전트가 이 Phase를 구현할 때, 아래에 계획과 완료 기록을 남깁니다.
