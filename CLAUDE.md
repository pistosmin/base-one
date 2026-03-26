# CLAUDE.md — 커뮤니티 웹앱 AI 컨텍스트

## 프로젝트 개요
모바일 퍼스트 커뮤니티 웹앱. 사용자가 게시글을 작성하고 댓글/추천으로 소통.
관리자는 동일 화면에서 서비스를 관리.

## Phase별 개발 가이드

개발 태스크는 Phase별로 독립된 plan 파일로 관리됩니다.

| Phase | 파일 | 상태 |
|-------|------|------|
| 0: 부트스트랩 | `prompt/phases/phase-0-bootstrap.md` | ✅ 완료 |
| 1: 인증 | `prompt/phases/phase-1-auth.md` | ✅ 완료 |
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
   - **이벤트 패턴**: 서비스 간 강결합을 방지하기 위해 `DomainEventPublisher`를 활용한 이벤트 주도 통신 권장.
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
make proxy     # Nginx 프록시 포함 인프라 및 네트워크 전체 시작
make all       # 인프라 + 프록시 + 백엔드 + 프론트엔드 한 번에 실행
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

## AI 서브에이전트 라우팅 전략

서브에이전트 정의 파일은 `.claude/agents/` 디렉토리에 위치합니다.
Claude Code의 Agent tool로 `subagent_type`을 지정하여 호출합니다.

### 에이전트 역할 분담

| 에이전트 | 모델 | 담당 영역 | 정의 파일 |
|---------|------|---------|----------|
| Orchestrator | Opus | 페이즈 계획, 아키텍처 결정, 복잡한 태스크 분해 | `.claude/agents/orchestrator.md` |
| Implementer | Sonnet | 코드 작성, 파일 생성/수정 | `.claude/agents/implementer.md` |
| Reviewer | Opus | 비즈니스 로직 검토, 코드 품질, 보안 리뷰 | `.claude/agents/reviewer.md` |
| Tech Advisor | Opus | 트랜잭션/동시성/정합성/성능 설계 자문 | `.claude/agents/tech-advisor.md` |
| Verifier | Sonnet | 빌드/컴파일/린트 검증 | `.claude/agents/verifier.md` |
| Tester | Sonnet | 테스트 작성, 실행, 결과 분석 | `.claude/agents/tester.md` |
| Explorer | Sonnet | 코드베이스 탐색, 기존 패턴 파악 | `.claude/agents/explorer.md` |

### 실행 모드 결정 규칙

| 실행 모드 | 조건 | 예시 |
|----------|------|------|
| **병렬 (Parallel)** | 태스크 간 의존성 없음 | 프론트엔드 API 함수 탐색 + 백엔드 엔티티 패턴 탐색 |
| **순차 (Sequential)** | 이전 태스크의 산출물이 다음 태스크의 입력 | 엔티티 -> Repository -> Service -> Controller |
| **백그라운드 (Background)** | 결과가 당장 필요 없는 오래 걸리는 작업 | `make verify` 전체 테스트 스위트 실행 |
| **포어그라운드 (Foreground)** | 결과가 다음 단계의 의사결정에 즉시 필요 | 기존 패턴 탐색 후 구현 방식 결정 |

**병렬 실행 판단 기준:**
- 서로 다른 파일을 생성/수정하는 태스크는 병렬 가능
- 같은 파일을 수정하는 태스크는 순차 필수
- 탐색(Explorer) 태스크는 거의 항상 병렬 가능
- 검증(Verifier) + 테스트(Tester)는 구현(Implementer) 완료 후에만 실행

### 페이즈 구현 표준 흐름

```
1. Orchestrator: Phase 파일을 읽고 태스크 분해 및 실행 계획 수립
       |
2. Explorer (병렬): 백엔드 기존 패턴 탐색  |  프론트엔드 기존 패턴 탐색
       |
3. Implementer (순차): 엔티티 -> Repository -> DTO -> Service -> Controller
   Implementer (순차): API 함수 -> 훅 -> 스토어 -> 컴포넌트 -> 페이지
   (백엔드와 프론트엔드는 서로 병렬 가능)
       |
4. Verifier (백그라운드): make lint / make verify 실행
       |
5. Tester: 테스트 작성 및 실행, 실패 원인 분석
       |
6. Reviewer: 전체 코드 리뷰 (비즈니스 로직, 보안, 품질)
       |
7. Implementer: 리뷰 피드백 반영 수정 (필요 시)
```

### 서브에이전트 트리거 조건

| 상황 | 호출할 에이전트 | 이유 |
|------|--------------|------|
| "phase-N을 구현해줘" | Orchestrator -> Explorer -> Implementer -> ... | 전체 파이프라인 실행 |
| 단일 파일/기능 수정 요청 | Implementer 단독 | Orchestrator 불필요 (단순 태스크) |
| "이 코드 리뷰해줘" | Reviewer | 코드 품질 검토 |
| "빌드 되는지 확인해줘" | Verifier | 컴파일/린트 검증 |
| "테스트 작성/실행해줘" | Tester | 테스트 전문 |
| "기존 코드에서 X 패턴 찾아줘" | Explorer | 탐색 전문 |
| 커밋 전 최종 확인 | Verifier + Tester (병렬) | 빌드 + 테스트 동시 검증 |
| 아키텍처 결정이 필요한 복잡한 변경 | Orchestrator | 분석 후 판단 필요 |
| 트랜잭션/동시성/정합성/성능 설계 결정 | Tech Advisor | 기술 설계 자문 |
| Implementer가 self-check에서 에스컬레이션 판단 | Tech Advisor | 복잡한 기술 시나리오 |

### Opus 에스컬레이션 규칙

Sonnet(Implementer)이 구현 중 아래 시나리오를 만나면 **즉시 멈추고** Opus(Orchestrator 또는 Tech Advisor)에게 에스컬레이션합니다.

#### 에스컬레이션 트리거 목록

| 카테고리 | 트리거 시나리오 | 에스컬레이션 대상 |
|---------|--------------|----------------|
| **트랜잭션 경계** | `@Transactional` 전파 레벨 선택이 필요한 경우 (REQUIRED vs REQUIRES_NEW 등) | Tech Advisor |
| **트랜잭션 경계** | 트랜잭션 내에서 외부 API 호출(메일, 알림 등)이 포함된 경우 | Tech Advisor |
| **트랜잭션 경계** | 서비스 간 메서드 호출에서 롤백 범위 결정이 필요한 경우 | Tech Advisor |
| **동시성** | 좋아요 수, 조회수 등 카운터 동시 업데이트 로직 구현 | Tech Advisor |
| **동시성** | 낙관적 락 vs 비관적 락 선택이 필요한 경우 | Tech Advisor |
| **동시성** | `@Async` 메서드가 `@Transactional` 메서드를 호출하거나 그 반대인 경우 | Tech Advisor |
| **동시성** | Redis 분산 락이 필요한 비즈니스 로직 (중복 요청 방지 등) | Tech Advisor |
| **데이터 정합성** | Cascade 삭제 전략 결정 (soft delete + 연관 엔티티 처리) | Tech Advisor |
| **데이터 정합성** | 양방향 연관관계에서 편의 메서드 설계 | Tech Advisor |
| **데이터 정합성** | 대용량 페이징에서 offset vs cursor 기반 결정 | Tech Advisor |
| **보안** | JWT 토큰 재발급 시 동시 요청 처리 (토큰 무효화 경쟁) | Tech Advisor |
| **보안** | 권한 검사 레이어 설계 (Service vs Controller vs AOP) | Tech Advisor |
| **성능** | Redis 캐시 무효화 전략 선택 (Cache-Aside vs Write-Through) | Tech Advisor |
| **성능** | 복합 인덱스 설계 및 컬럼 순서 결정 | Tech Advisor |
| **성능** | N+1 해결에서 fetch join vs `@BatchSize` vs `@EntityGraph` 선택 | Tech Advisor |
| **아키텍처** | 2개 이상의 도메인이 얽힌 비즈니스 로직 설계 | Orchestrator |
| **아키텍처** | 기존 패턴과 상충하는 새로운 패턴 도입이 필요한 경우 | Orchestrator |

#### 에스컬레이션 프로토콜

Implementer가 트리거 시나리오를 감지하면:

1. **즉시 코드 작성을 멈춤** — 추측으로 구현하지 않음
2. **현재 상태를 정리**하여 Opus에게 전달:
   ```
   ## 에스컬레이션 요청
   - 트리거: [어떤 시나리오에 해당하는지]
   - 컨텍스트: [구현 중인 기능, 관련 엔티티/서비스]
   - 선택지: [Implementer가 고려한 옵션들 (있다면)]
   - 관련 코드: [이미 작성된 관련 코드 파일 경로]
   ```
3. **Opus(Tech Advisor 또는 Orchestrator)가 설계 결정을 내림**
4. **Implementer가 결정에 따라 구현을 재개**

#### Opus 산출물 규격

Opus는 코드를 직접 작성하지 않고, 아래 형식으로 설계 결정과 가이드라인을 제공합니다:

```markdown
## 설계 결정

### 결정 사항
- [선택한 방식과 그 이유]

### 구현 가이드라인
- [Implementer가 따라야 할 구체적 지침]
- [사용할 어노테이션, 패턴, 클래스 구조]

### 주의사항
- [이 결정에서 흔히 발생하는 실수]

### 검증 방법
- [구현 후 올바르게 동작하는지 확인하는 방법]
```

단, 구현 가이드라인만으로 Sonnet이 올바르게 작성하기 어려운 경우(트랜잭션 전파 조합, 분산 락 패턴 등)에는 **핵심 코드 스니펫을 직접 제공**할 수 있습니다.

#### Implementer Self-Check 체크리스트

Implementer는 코드 작성 전 아래 질문을 스스로 확인합니다. 하나라도 "예"이면 에스컬레이션을 고려합니다:

- [ ] 이 메서드에서 `@Transactional`의 전파 레벨을 기본값(REQUIRED) 외로 바꿔야 하는가?
- [ ] 트랜잭션 안에서 외부 시스템(메일, 알림, 외부 API)을 호출하는가?
- [ ] 두 명 이상의 사용자가 동시에 같은 데이터를 수정할 수 있는가?
- [ ] `@Async`와 `@Transactional`이 같은 호출 체인에 있는가?
- [ ] 엔티티 삭제 시 연관된 다른 엔티티에 영향이 있는가?
- [ ] 이 쿼리가 10만 건 이상의 데이터에서도 성능이 괜찮은가?
- [ ] 캐시를 도입/무효화해야 하는가?
- [ ] JWT 토큰 관련 동시 요청 시나리오가 있는가?
- [ ] 2개 이상의 도메인 서비스를 조합해야 하는가?

**에스컬레이션 과잉 방지 원칙:**
- 단순 CRUD, 단일 엔티티 저장/조회, 기본 페이징은 에스컬레이션 불필요
- 프로젝트 내에 이미 동일한 패턴이 구현되어 있으면 그 패턴을 따름 (에스컬레이션 불필요)
- 확신이 80% 이상이면 구현 후 Reviewer에게 검증 요청 (에스컬레이션 대신 리뷰)

### 서브에이전트를 쓰는 기준

**사용해야 할 때:**
- Context window가 커질 것으로 예상될 때 (Phase 전체 구현 등)
- 독립적인 검증이 필요할 때 (만든 에이전트와 검증 에이전트 분리 -> 맹점 방지)
- 병렬화로 속도 향상이 가능할 때 (탐색 2건 동시 실행 등)
- 전문화된 프롬프트가 품질에 영향을 줄 때 (보안 리뷰, 테스트 작성 등)

**사용하지 않아도 될 때:**
- 단일 파일 수정, 간단한 버그 수정 등 단순 태스크
- 이미 context에 충분한 정보가 있는 경우
- 사용자가 빠른 응답을 기대하는 간단한 질문

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

## Git 관리 전략

### 리포지토리 정보
- **Remote**: `origin` → `https://github.com/pistosmin/base-one.git`
- **기본 브랜치**: `main`

### 브랜치 전략 (GitHub Flow)
```
main ─────────────────────────── 안정 버전, 항상 배포 가능
  └── feature/phase-1-auth ───── Phase별 기능 브랜치
  └── fix/login-token-refresh ── 버그 수정 브랜치
  └── chore/update-deps ──────── 유지보수 브랜치
```

- **main**: 안정적인 코드만. 직접 푸시 대신 feature 브랜치에서 병합
- **feature/**: 새 기능 개발 (`feature/phase-N-기능명`)
- **fix/**: 버그 수정 (`fix/이슈명`)
- **chore/**: 의존성 업데이트, 설정 변경 등

### AI 에이전트 Git 작업 규칙

1. **커밋 전 항상 검증**:
   ```bash
   make lint      # 최소한 린트는 통과
   # 가능하면 make verify (린트 + 테스트 전체)
   ```

2. **커밋 단위**: 한 커밋에 하나의 논리적 변경만
   ```bash
   # ✅ 좋은 예
   git commit -m "feat(auth): User 엔티티 및 Repository 구현"
   git commit -m "feat(auth): Spring Security JWT 설정"
   
   # ❌ 나쁜 예
   git commit -m "feat: Phase 1 전체 구현"  # 너무 큼
   ```

3. **커밋 메시지 형식** (Conventional Commit):
   ```
   <type>(<scope>): <한국어 또는 영어 설명>
   
   타입: feat, fix, refactor, test, chore, docs, style
   스코프: auth, post, comment, vote, user, admin, notification, media, infra
   ```

4. **Phase 작업 시 Git 흐름**:
   ```bash
   # 1. feature 브랜치 생성
   git checkout -b feature/phase-1-auth
   
   # 2. Task별로 커밋 (Task 1.1, 1.2, ...)
   git add <관련 파일>
   git commit -m "feat(auth): User 엔티티 및 Repository 구현"
   
   # 3. Phase 완료 후 push
   git push origin feature/phase-1-auth
   
   # 4. main에 병합 (또는 PR 생성)
   git checkout main && git merge feature/phase-1-auth
   git push origin main
   ```

5. **푸시 타이밍**:
   - Task 완료 시: 커밋 (로컬)
   - Phase 완료 시: `git push` (원격)
   - 중요 구조 변경 시: 즉시 `git push` (백업)

6. **plan/문서 변경도 커밋**:
   ```bash
   git commit -m "docs(phase-1): 구현 이력 업데이트 — Task 1.1~1.3 완료"
   ```

7. **금지 사항**:
   - ❌ `git push --force` (히스토리 파괴)
   - ❌ `.env`, 시크릿, API 키 커밋
   - ❌ `node_modules/`, `build/`, `dist/` 커밋 (.gitignore에 의해 방지됨)
   - ❌ 빌드 실패 상태로 main에 병합

### 다른 PC에서 프로젝트 시작하기
```bash
# 1. 클론
git clone https://github.com/pistosmin/base-one.git gravity
cd gravity

# 2. 환경 설정
cp .env.example .env   # .env 파일 생성 후 값 수정

# 3. 인프라 시작
make dev               # Docker Compose (PostgreSQL, Redis, MinIO 등)

# 4. 프론트엔드 의존성
cd apps/web && npm install && cd ../..

# 5. 개발 서버 시작
make api               # 터미널 1: Spring Boot (localhost:8080)
make web               # 터미널 2: Vite (localhost:5173)
```
