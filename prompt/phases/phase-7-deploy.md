# Phase 7: 최적화 & 배포

> **상태**: ⬜ 대기
> **목표**: 성능 최적화, Docker 프로덕션 빌드, CI/CD, 모니터링 대시보드
> **완료 기준**: GitHub Actions CI 통과 + Docker 프로덕션 배포 + Grafana 대시보드 동작
> **참고**: architecture-plan-v2.md 섹션 2.3 (인프라), 섹션 8 (파일 구조), 섹션 7.2 (보안 체크리스트)

---

## 진행 체크리스트

- [ ] Task 7.1: 성능 최적화 (프론트 + 백엔드)
- [ ] Task 7.2: Docker 프로덕션 배포 설정
- [ ] Task 7.3: OpenAPI 기반 프론트엔드 타입 자동 생성 (CI/CD 연동)
- [ ] Task 7.4: 모니터링 대시보드 (Grafana)

## 검증 기준

- [ ] Redis 캐시 동작 확인 (카테고리 → 1시간, 대시보드 → 5분)
- [ ] Docker 프로덕션 빌드 성공 (Multi-stage)
- [ ] CI/CD 파이프라인 실행 (GitHub Actions)
- [ ] Grafana 대시보드에서 메트릭 표시
- [ ] `make verify` → 전체 통과

---

## Task 7.1: 성능 최적화

### 프론트엔드
- `React.memo`: PostCard, CommentItem, NotificationItem (리렌더링 방지)
- 이미지 lazy loading: `<img loading="lazy">` 또는 IntersectionObserver
- 코드 스플리팅: router.tsx에서 모든 페이지 `React.lazy` + `Suspense` 확인
- PWA 기본 설정: `public/manifest.json` (name, short_name, icons, theme_color, background_color, display: "standalone")
- Bundle 분석: `vite-plugin-visualizer`로 큰 chunk 식별

### 백엔드
- Redis 캐시 (`org.springframework.cache`):
  - `@EnableCaching` in CacheConfig
  - `@Cacheable("categories")` — TTL 1시간 (카테고리 변경 시 `@CacheEvict`)
  - `@Cacheable("dashboard")` — TTL 5분
  - RedisCacheManager 빈: defaultCacheConfiguration + entryTtl + serializeValuesWith(RedisSerializer)
- N+1 점검: PostService.getPosts, CommentService.getComments의 fetchJoin 확인
- 조회수 비동기 처리:
  - `@Async("viewCountExecutor")` + `@EventListener`
  - ViewCountEvent → PostService.incrementViewCount → `@Transactional(propagation = REQUIRES_NEW)`
  - ThreadPoolTaskExecutor 빈 설정 (core: 2, max: 5, queue: 100)
- DOMPurify 또는 서버 사이드 HTML sanitize: 게시글 본문 XSS 방지

### Acceptance Criteria
- [ ] 동일 카테고리 2회 조회 시 2번째는 Redis에서 반환 (로그 확인)
- [ ] PostCard에 React.memo 적용
- [ ] `make verify` 통과

---

## Task 7.2: Docker 프로덕션

### Context
- architecture-plan-v2.md 섹션 8 — apps/web/Dockerfile, apps/api/Dockerfile 구조

### Deliverables
1. `apps/web/Dockerfile` — Multi-stage:
   - Stage 1 (build): `node:22-alpine`, `npm ci`, `npm run build`
   - Stage 2 (serve): `nginx:1.27-alpine`, `dist/` → `/usr/share/nginx/html/`, `nginx.conf` 복사
2. `apps/api/Dockerfile` — Multi-stage:
   - Stage 1 (build): `eclipse-temurin:21-jdk-alpine`, `./gradlew build -x test`
   - Stage 2 (serve): `eclipse-temurin:21-jre-alpine`, `COPY --from=build build/libs/*.jar app.jar`
   - JVM: `-XX:+UseZGC -XX:MaxRAMPercentage=75.0`
3. `infra/docker/nginx/nginx.conf`:
   - `try_files $uri $uri/ /index.html` (SPA)
   - `location /api/ { proxy_pass http://api:8080/; }` — API 프록시
   - gzip: text/html, css, js, json, svg
   - 보안 헤더: X-Frame-Options, X-Content-Type-Options, X-XSS-Protection, Referrer-Policy
   - 정적 파일 캐시: `location ~* \.(js|css|png|jpg|gif|svg|ico)$ { expires 1y; add_header Cache-Control "public, immutable"; }`
4. `docker-compose.prod.yml`:
   - services: web (nginx), api (spring boot), postgres, redis
   - depends_on, healthcheck, restart: unless-stopped
   - 환경변수: `.env.prod` 참조
5. `.env.prod.example` — 프로덕션 환경변수 템플릿

### Acceptance Criteria
- [ ] `docker compose -f docker-compose.prod.yml build` 성공
- [ ] nginx → SPA 서빙 + API 프록시 동작
- [ ] API 컨테이너 `/actuator/health` 정상

---

## Task 7.3: OpenAPI 기반 프론트엔드 타입 자동 생성

### Context
- SpringDoc(Swagger)에서 도출된 API 스펙을 활용하여 프론트엔드의 DTO 타입과 Axios 훅을 자동 생성합니다.

### Deliverables
1. `apps/web/package.json`에 `orval` 또는 `@rtk-query/codegen-openapi` (상황에 맞게 선택, 기본은 `orval`) 추가
2. `orval.config.ts` 설정 파일 생성 (기본 API 엔드포인트: `http://localhost:8080/v3/api-docs`)
3. 프론트엔드 `shared/types` 디렉토리에 자동 생성된 타입 및 쿼리 훅을 반영
4. 기존 클라이언트 코드에서 수동 작성된 타입을 자동 생성된 타입으로 교체
5. `.github/workflows/deploy.yml` 작성 (프로덕션 CD 배포용)

### Acceptance Criteria
- [ ] `npm run generate:api` 스크립트 동작 확인
- [ ] 자동 생성된 타입으로 프론트엔드 빌드 성공
- [ ] CD 파이프라인 정상 배포 동작 확인

---

## Task 7.4: 모니터링 (Grafana)

### Context
- Docker Compose에 Prometheus, Grafana, Loki 이미 설정됨
- Spring Actuator + micrometer 연동

### Deliverables
1. Spring Boot 설정 확인:
   - `spring.actuator.endpoints.web.exposure.include=health,info,metrics,prometheus`
   - `management.metrics.tags.application=community-api`
   - `implementation("io.micrometer:micrometer-registry-prometheus")`
2. `infra/monitoring/grafana/dashboards/spring-boot.json`:
   - JVM 메모리 (heap/non-heap)
   - HTTP 요청 처리 시간 (p50, p95, p99)
   - 활성 DB 커넥션 수
   - Redis 커맨드 수
3. `infra/monitoring/grafana/dashboards/application.json`:
   - 분당 API 요청 수 (엔드포인트별)
   - 에러율 (4xx, 5xx)
   - 가입 추이 (일별)
4. `infra/monitoring/prometheus.yml` — scrape_configs에 spring boot actuator 타겟 확인

### Acceptance Criteria
- [ ] Grafana(localhost:3000)에서 대시보드 자동 로드
- [ ] Spring Boot 메트릭 수집 → 그래프 표시
- [ ] `make dev` 후 Grafana 접근 가능

---

## 구현 이력

> Claude나 AI 에이전트가 이 Phase를 구현할 때, 아래에 계획과 완료 기록을 남깁니다.
