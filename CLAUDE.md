# CLAUDE.md — 커뮤니티 웹앱 AI 컨텍스트

## 프로젝트 개요
모바일 퍼스트 커뮤니티 웹앱. 사용자가 게시글을 작성하고 댓글/추천으로 소통.
관리자는 동일 화면에서 서비스를 관리.

## Phase별 개발 가이드

개발 태스크는 Phase별로 독립된 plan 파일로 관리됩니다.

| Phase | 파일 | 상태 |
|-------|------|------|
| 0: 부트스트랩 | `prompt/phases/phase-0-bootstrap.md` | ✅ 완료 |
| 1: 인증 | `prompt/phases/phase-1-auth.md` | ⬜ 대기 |
| 2: 게시글 | `prompt/phases/phase-2-post.md` | ⬜ 대기 |
| 3: 댓글&추천 | `prompt/phases/phase-3-comment-vote.md` | ⬜ 대기 |
| 4: 프로필&관리자 | `prompt/phases/phase-4-profile-admin.md` | ⬜ 대기 |
| 5: 알림&미디어 | `prompt/phases/phase-5-notification-media.md` | ⬜ 대기 |
| 6: 완성 | `prompt/phases/phase-6-completion.md` | ⬜ 대기 |
| 7: 배포 | `prompt/phases/phase-7-deploy.md` | ⬜ 대기 |

### Phase 구현 요청 방법
```
prompt/phases/phase-1-auth.md를 읽고 구현해줘.
```

### AI 에이전트 작업 규칙
1. **시작 전**: 해당 phase 파일을 먼저 읽고 전체 태스크를 파악
2. **계획 수립**: 구현 순서와 예상 사항을 "구현 이력" 섹션에 기록
3. **태스크 완료 시마다**: 진행 체크리스트에 `[x]` 체크
4. **Phase 완료 시**: 검증 기준 체크리스트 모두 확인, 상태를 ✅ 완료로 변경
5. **이 파일(CLAUDE.md)의 Phase 테이블 상태도 함께 업데이트**

### 아키텍처 참조
- 상세 설계: `prompt/architecture-plan-v2.md` (기술 스택, DB 스키마, API 설계)
- 디자인 토큰: `apps/web/src/app/theme/tokens.ts`

## 기술 스택

| 영역 | 기술 | 버전 |
|------|------|------|
| 프론트엔드 | React + TypeScript + Vite | 19.2 + 5.7 + 8.0 |
| UI | MUI (Material UI) | 7.3 |
| 상태 관리 | Zustand + TanStack Query | 5.x + 5.90 |
| 라우팅 | React Router | 7.x |
| 백엔드 | Spring Boot + Java | 4.0.3 + 21 LTS |
| DB | PostgreSQL | 18.3 |
| 캐시 | Redis | 7.4 |
| 스토리지 | MinIO (S3 호환) | latest |
| 빌드 | Gradle Kotlin DSL | 8.12 |

## 프로젝트 구조
```
apps/web/src/       → React SPA (Feature-Sliced Design)
  app/               → 설정 (라우터, 프로바이더, 테마)
  features/          → 기능별 모듈 (auth, post, comment 등)
  shared/            → 공유 컴포넌트, 훅, 유틸

apps/api/src/main/java/com/community/
  global/            → 전역 설정, 보안, 공통 모듈
  domain/            → 도메인별 패키지 (auth, user, post 등)
```

## 코딩 규칙
1. **한국어 주석**: 모든 클래스, 메서드, 함수에 한국어 주석. "무엇을 하는지"뿐 아니라 "왜 이렇게 하는지"도 설명.
2. **파일 상단 설명**: 모든 새 파일의 최상단에 해당 파일의 목적, 의존성, 사용 위치를 한국어로 설명하는 블록 주석 작성.
3. **TypeScript**: `any` 금지, strict mode. `unknown` + 타입 가드로 대체.
4. **Java**: DTO는 record 클래스. Entity에 `@Getter @NoArgsConstructor @Builder`. `@Setter` 금지 (비즈니스 메서드로 상태 변경).
5. **import**: 프론트 `@/shared/...`, 백 `com.community.domain...`
6. **React Router v7**: `from 'react-router'` (NOT `react-router-dom`)
7. **Spring Boot 4.0**: `jakarta.*` 패키지 사용 (`javax.*` 절대 금지)
8. **MUI v7**: `Grid2`가 `Grid`로 승격됨
9. **JPA**: `@ManyToOne(fetch = FetchType.LAZY)` 필수

## 커밋 컨벤션
- **conventional commit**: `feat(post): 게시글 CRUD API 구현` 형식
- **단일 논리 변경 단위**: 한 커밋에 하나의 논리적 변경만 포함. 리네임 + 기능 변경은 별도 커밋
- **커밋 분리 원칙**: 리팩토링 ≠ 기능 추가 ≠ 테스트 추가. 각각 별도 커밋
- **커밋 메시지 언어**: 영어 타입 + 한국어 설명 가능 (예: `fix(auth): 토큰 갱신 시 동시성 문제 해결`)

## Search Before Building 원칙
새 코드를 작성하기 전에 반드시 확인:
1. **기존 패턴 검색**: 프로젝트 내에 이미 비슷한 구현이 있는지 검색
2. **프레임워크 빌트인 확인**: Spring/React/MUI에 내장된 기능인지 확인
3. **공식 문서 참조**: 불확실하면 공식 문서를 먼저 확인
- 바퀴를 재발명하지 않기. 하지만 가끔은 기존 방식에 의문을 제기하는 것이 혁신.

## 테스트 우선 원칙
- 코드 변경 시 관련 테스트도 함께 작성
- AI가 만드는 코드의 한계 비용은 거의 0 — 테스트를 "나중에"로 미루지 않기
- 백엔드: 서비스 레이어 단위 테스트 + 컨트롤러 통합 테스트
- 프론트엔드: 컴포넌트 렌더링 테스트 + 훅 테스트

## 디자인 철학
**"토스의 직관성 + Material Design 3의 체계성 + Apple HIG의 매끄러움"**
- 모바일 퍼스트 (한 화면 한 목적)
- 토스 TDS: 깔끔한 카드 UI, 넉넉한 여백, 토스 컬러 체계
- MUI v7 테마 커스터마이징 (`apps/web/src/app/theme/`)
- Framer Motion: 페이지 전환, 리스트 등장, 터치 피드백
- Pretendard 폰트 (한국어 최적화)

## Google Stitch 2.0 활용 가이드
UI 컴포넌트나 페이지를 구현하기 전에 [Google Stitch](https://stitch.withgoogle.com)에서 먼저 디자인할 수 있습니다.

**활용 방법:**
1. Stitch에서 "토스 스타일 모바일 [페이지명]" 프롬프트로 디자인 생성
2. 생성된 코드(React/HTML)를 참고하여 MUI 컴포넌트로 구현
3. Stitch의 DESIGN.md를 `apps/web/DESIGN.md`에 저장하면 디자인 시스템 일관성 유지
4. 색상/간격은 반드시 `tokens.ts` 값을 사용 (Stitch 출력을 그대로 복붙 X)

**Stitch 프롬프트 예시:**
- "토스 스타일 로그인 페이지. 흰색 배경, 큰 입력, 파란색 풀너비 버튼"
- "모바일 커뮤니티 게시글 카드 리스트. 아바타, 닉네임, 시간, 제목, 요약, 통계"
- "인기 글 가로 스크롤 섹션 + 최신 피드 무한 스크롤 홈 화면"

## 빠른 명령어
```bash
make dev       # Docker 인프라 시작 (PostgreSQL, Redis, MinIO)
make api       # Spring Boot 개발 서버 (localhost:8080)
make web       # Vite 개발 서버 (localhost:5173)
make test      # 전체 테스트
make lint      # 전체 린트 (컴파일 + ESLint + TypeScript)
make verify    # lint + test 통합 검증 (태스크 완료 시 실행)
make reset     # DB/Redis 초기화 후 재시작
```

## 새 기능 추가 순서

### 백엔드
1. `domain/{name}/entity/` — JPA 엔티티
2. `domain/{name}/repository/` — JPA Repository
3. `domain/{name}/dto/request/`, `dto/response/` — record DTO
4. `domain/{name}/mapper/` — MapStruct 매퍼
5. `domain/{name}/service/` — 비즈니스 로직
6. `domain/{name}/controller/` — REST API

### 프론트엔드
1. `features/{name}/api/` — Axios API 함수
2. `features/{name}/hooks/` — TanStack Query 훅
3. `features/{name}/store/` — Zustand 스토어 (필요 시)
4. `features/{name}/components/` — UI 컴포넌트
5. `features/{name}/pages/` — 페이지
6. `app/router.tsx` — 라우트 등록

## 자주 발생하는 에러 & 해결

| 에러 | 원인 | 해결 |
|------|------|------|
| `Cannot find module '@/...'` | 경로 별칭 미설정 | `tsconfig.app.json`의 paths 확인 |
| `javax.persistence.*` | Spring Boot 4.0에서 삭제됨 | `jakarta.persistence.*` 사용 |
| `react-router-dom` import 에러 | v7에서 패키지명 변경 | `react-router`에서 import |
| N+1 쿼리 | `@ManyToOne` 기본 EAGER | `fetch = FetchType.LAZY` 추가 |
| MUI Grid 에러 | v7에서 Grid2 → Grid 승격 | `import Grid from '@mui/material/Grid'` |
| QueryDSL Q클래스 없음 | 어노테이션 프로세서 미실행 | `./gradlew compileJava` 실행 |
| CORS error | 프론트-백 도메인 불일치 | `CorsConfig`에서 origin 추가 |
| 401 Unauthorized | JWT 토큰 만료 | 토큰 갱신 인터셉터 확인 |

## 금지 사항
- ❌ `console.log` 프로덕션 코드
- ❌ API 키/시크릿 하드코딩
- ❌ `SELECT *` 쿼리
- ❌ `alert()` / `window.confirm()` → MUI Dialog 사용
- ❌ inline style → MUI `sx` prop 사용
- ❌ `useEffect`로 데이터 fetching → TanStack Query 사용
- ❌ `@SuppressWarnings` 무분별 사용
- ❌ `javax.*` 패키지 import (Spring Boot 4.0에서 존재하지 않음)

## AI 에이전트 행동 지침
1. 코드 생성 시 한국어 주석 필수 (클래스, 메서드, 주요 로직)
2. 새 파일에 경로와 목적 명시
3. 복잡한 로직은 단계별 설명
4. 에러 처리 명시적 구현
5. 가능하면 테스트 함께 작성
6. 기존 코드 수정 시 영향 범위 설명
7. 파일 삭제/이동 시 의존성 확인

## API 응답 규격
```json
// 성공
{ "success": true, "data": { ... }, "error": null }
// 실패
{ "success": false, "data": null, "error": { "code": "E001", "message": "..." } }
// 페이징
{ "success": true, "data": { "content": [...], "totalElements": 150, "totalPages": 8, "page": 0, "size": 20, "hasNext": true, "hasPrevious": false } }
```

## gstack 스킬 (Claude Code에서 사용)
이 프로젝트에 gstack이 설치되어 있습니다. Claude Code에서 다음 스킬을 사용할 수 있습니다:
- `/review` — PR 코드 리뷰 (자동 수정 + 수동 확인)
- `/ship` — 테스트 실행 + PR 생성 자동화
- `/investigate` — 체계적 루트 코즈 디버깅
- `/qa` — 브라우저 QA 테스트
- `/careful` — 안전 모드 (중요 변경 시 사용)
- `/freeze` / `/unfreeze` — 특정 파일 수정 잠금/해제
- `/retro` — 작업 회고 (커밋 통계)
- `/browse` — 웹 브라우징 (mcp__claude-in-chrome 대신 사용)

gstack 스킬이 작동하지 않으면: `cd .claude/skills/gstack && ./setup`
