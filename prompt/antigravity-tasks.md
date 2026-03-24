# 🎯 Antigravity 태스크 프롬프트 전체 모음

> **사용 방법**: 아래 태스크를 순서대로 하나씩 Antigravity에 복사하여 붙여넣기 합니다.
> 각 태스크는 `---` 구분선으로 분리되어 있습니다.
> 태스크 완료 후 **`make verify`** 또는 **`make lint`** 로 자동 검증한 뒤 다음 태스크로 진행합니다.
>
> **Claude Code 사용 시**: 이 파일 대신 `prompt/phases/phase-N-*.md` 파일을 사용하세요.
> 각 Phase가 독립 파일로 분리되어 있어 컨텍스트 효율이 높습니다.
> 예: `prompt/phases/phase-1-auth.md를 읽고 구현해줘`
>
> **검증 워크플로우** (태스크 완료 시):
> 1. `make lint-api` — 백엔드 컴파일 체크
> 2. `make lint-web` — 프론트엔드 ESLint 체크
> 3. `make typecheck` — TypeScript 타입 체크
> 4. `make test` — 전체 테스트
> 5. 또는 `make verify` — 위 1~4를 한 번에 실행
>
> **모든 태스크 공통 지침** (Antigravity가 암묵적으로 따라야 하는 규칙):
> - 기술 버전: React 19.2, MUI 7.3, Vite 7.3, React Router 7 (패키지: `react-router`), Spring Boot 4.0.3, Java 21, PostgreSQL 18.3
> - Spring Boot 4.0: `jakarta.*` 패키지 사용 (`javax.*` 절대 금지)
> - React Router v7: `from 'react-router'` (`react-router-dom` 아님)
> - MUI v7: `Grid`는 Grid2임. `GridLegacy`가 구 Grid.
> - 모든 코드에 한국어 주석 필수 (클래스, 메서드, 함수, 주요 로직)
> - 파일 상단에 파일 목적 설명 블록 주석 작성
> - TypeScript: `any` 금지, strict mode. `unknown` + 타입 가드로 대체.
> - Java DTO: `record` 클래스 사용. Entity에는 `@Setter` 금지 (비즈니스 메서드로 상태 변경).
> - import 경로: 프론트엔드 `@/` 별칭, 백엔드 `com.community` 패키지

## 📊 진행 현황

| Phase | 태스크 수 | 상태 | 검증 |
|-------|-----------|------|------|
| **0: 부트스트랩** | 7 | ✅ 완료 | `make dev` 동작 확인 |
| **1: 인증** | 7 | ⬜ 대기 | 회원가입→로그인→토큰 API 호출 |
| **2: 게시글** | 6 | ⬜ 대기 | 게시글 CRUD Swagger 확인 |
| **3: 댓글&추천** | 3 | ⬜ 대기 | 댓글 작성 + 추천 토글 확인 |
| **4: 프로필&관리자** | 4 | ⬜ 대기 | 프로필 수정 + 관리자 대시보드 |
| **5: 알림&미디어** | 4 | ⬜ 대기 | 알림 생성 + 이미지 업로드 |
| **6: 완성** | 3 | ⬜ 대기 | 홈 화면 전체 동작 |
| **7: 배포** | 4 | ⬜ 대기 | CI/CD 파이프라인 실행 |

---

# ═══════════════════════════════════════════
# Phase 0: 프로젝트 부트스트랩 (7 Tasks)
# 목표: 빈 프로젝트 골격, 인프라, 테마 설정
# 완료 기준: `make dev`로 인프라 기동, 프론트/백 hello world 확인
#
# 검증 기준 (Phase 완료 시):
# - [x] `make dev` → Docker 컨테이너 모두 healthy
# - [x] `make web` → localhost:5173에서 "Hello Community" 표시
# - [x] `make api` → localhost:8080에서 Swagger UI 접근 가능
# - [x] `make lint-api` → 컴파일 성공
# ═══════════════════════════════════════════

---

## Task 0.1: 프로젝트 루트 구조 & 설정 파일

```
프로젝트 루트 디렉토리를 설정해줘.

이 프로젝트는 React 19.2 + Spring Boot 4.0.3 기반의 모바일 퍼스트 커뮤니티 웹앱이야. Docker Compose로 개발 환경을 구성하고, GitHub Actions로 CI/CD를 할 거야.

[생성할 디렉토리] (내부 파일은 생성하지 말 것. 빈 디렉토리만.)
apps/web/
apps/api/
infra/docker/nginx/
infra/docker/postgres/
infra/monitoring/grafana/provisioning/datasources/
infra/monitoring/grafana/provisioning/dashboards/
infra/monitoring/grafana/dashboards/
infra/monitoring/prometheus/
infra/scripts/
docs/
.github/workflows/

[생성할 파일]

1. .gitignore
내용: Node.js(node_modules, dist, .env, *.log), Java(build/, .gradle/, *.class, *.jar, out/), IDE(.idea/, .vscode/, *.iml, .project, .classpath), OS(.DS_Store, Thumbs.db), Docker(docker-volumes/)

2. .env.example
모든 환경 변수를 예시값과 설명 주석으로:
# ── 데이터베이스 (PostgreSQL 18) ──
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=community_dev
POSTGRES_USER=community
POSTGRES_PASSWORD=community1234

# ── 캐시 (Redis 7.4) ──
REDIS_HOST=localhost
REDIS_PORT=6379

# ── 파일 스토리지 (MinIO S3 호환) ──
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET=community-media

# ── JWT 인증 ──
JWT_SECRET=your-256-bit-secret-key-change-this-in-production
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

# ── 애플리케이션 ──
APP_PORT=8080
APP_ENV=dev

3. Makefile
각 명령어에 한국어 설명 주석:
- make dev: Docker Compose로 인프라 시작 (PostgreSQL, Redis, MinIO, 모니터링)
- make down: 인프라 종료
- make logs: Docker 로그 실시간 확인
- make reset: 볼륨 삭제 후 재시작 (데이터 초기화)
- make web: 프론트엔드 개발 서버 시작 (cd apps/web && npm run dev)
- make api: 백엔드 개발 서버 시작 (cd apps/api && ./gradlew bootRun --args='--spring.profiles.active=dev')
- make test-web: 프론트엔드 테스트 (cd apps/web && npm test)
- make test-api: 백엔드 테스트 (cd apps/api && ./gradlew test)
- make test: 프론트+백 테스트 모두
- make build-web: 프론트엔드 프로덕션 빌드
- make build-api: 백엔드 프로덕션 빌드
- make build: 프론트+백 빌드

4. README.md (한국어)
- 프로젝트명: 커뮤니티 웹앱
- 한 줄 설명: 모바일 퍼스트 커뮤니티 서비스
- 기술 스택 표 (프론트/백/인프라 각각 기술명+버전)
- 사전 요구사항: Node.js 20.19+, Java 21, Docker
- 빠른 시작 가이드: git clone → cp .env.example .env → make dev → make web (새 터미널) → make api (새 터미널)
- 디렉토리 구조 요약
- 주요 URL: 프론트(localhost:5173), 백(localhost:8080), Swagger(localhost:8080/swagger-ui.html), Grafana(localhost:3000), MinIO Console(localhost:9001)
```

---

## Task 0.2: Docker Compose 개발 환경

```
Docker Compose로 개발 인프라를 구성해줘.

`make dev` 한 번으로 PostgreSQL, Redis, MinIO, Prometheus, Grafana, Loki가 모두 시작되어야 해.

[생성할 파일: docker-compose.yml]

서비스 6개 + 초기화 컨테이너 1개:

1. postgres
   이미지: postgres:18-alpine
   포트: ${POSTGRES_PORT:-5432}:5432
   환경변수: POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD (.env에서 로드)
   볼륨: pgdata:/var/lib/postgresql/data
   헬스체크: pg_isready -U ${POSTGRES_USER:-community} -d ${POSTGRES_DB:-community_dev}
   interval: 5s, timeout: 5s, retries: 5

2. redis
   이미지: redis:7.4-alpine
   포트: ${REDIS_PORT:-6379}:6379
   명령어: redis-server --appendonly yes
   볼륨: redis-data:/data
   헬스체크: redis-cli ping | grep PONG
   interval: 5s, timeout: 3s, retries: 5

3. minio
   이미지: minio/minio:latest
   포트: 9000:9000, 9001:9001
   명령어: server /data --console-address ":9001"
   환경변수: MINIO_ROOT_USER=${MINIO_ACCESS_KEY:-minioadmin}, MINIO_ROOT_PASSWORD=${MINIO_SECRET_KEY:-minioadmin}
   볼륨: minio-data:/data
   헬스체크: curl -f http://localhost:9000/minio/health/live
   interval: 10s, timeout: 5s, retries: 3

4. minio-init (일회성)
   이미지: minio/mc:latest
   depends_on: minio (condition: service_healthy)
   entrypoint에서 직접 실행:
   - mc alias set myminio http://minio:9000 minioadmin minioadmin
   - mc mb myminio/community-media --ignore-existing
   - mc anonymous set download myminio/community-media
   restart: "no"

5. prometheus
   이미지: prom/prometheus:v3.2.0
   포트: 9090:9090
   볼륨: ./infra/monitoring/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro

6. loki
   이미지: grafana/loki:3.4.0
   포트: 3100:3100

7. grafana
   이미지: grafana/grafana:11.5.0
   포트: 3000:3000
   depends_on: prometheus, loki
   환경변수:
   - GF_SECURITY_ADMIN_USER=admin
   - GF_SECURITY_ADMIN_PASSWORD=admin
   - GF_AUTH_ANONYMOUS_ENABLED=true
   - GF_AUTH_ANONYMOUS_ORG_ROLE=Viewer
   볼륨:
   - grafana-data:/var/lib/grafana
   - ./infra/monitoring/grafana/provisioning:/etc/grafana/provisioning:ro
   - ./infra/monitoring/grafana/dashboards:/var/lib/grafana/dashboards:ro

네트워크: community-network (bridge)
모든 서비스에 networks: [community-network] 추가.

[추가 생성 파일]

1. infra/monitoring/prometheus/prometheus.yml
global:
  scrape_interval: 15s
scrape_configs:
  - job_name: 'spring-boot'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8080']
    # Spring Boot Actuator가 아직 없으면 에러나지만, Phase 0.5 이후 연결됨

2. infra/monitoring/grafana/provisioning/datasources/datasources.yml
Prometheus(http://prometheus:9090)와 Loki(http://loki:3100) 데이터소스 자동 등록.

3. infra/monitoring/grafana/provisioning/dashboards/dashboards.yml
/var/lib/grafana/dashboards 경로에서 JSON 대시보드 자동 로드 설정.

각 서비스에 한국어 주석으로 목적과 설정 이유를 설명해줘.
```

---

## Task 0.3: React 프로젝트 초기화

```
React 프론트엔드 프로젝트를 초기화해줘.

경로: apps/web/

Vite 7.3 + React 19.2 + TypeScript 5.7 기반이야. 이 프로젝트는 모바일 퍼스트 커뮤니티 웹앱의 프론트엔드야.

[package.json 의존성] (정확한 패키지명과 메이저 버전)

dependencies:
  react: ^19.2.0
  react-dom: ^19.2.0
  react-router: ^7.0.0           ← 주의: react-router-dom이 아님!
  @tanstack/react-query: ^5.90.0
  @tanstack/react-query-devtools: ^5.90.0
  zustand: ^5.0.0
  @mui/material: ^7.3.0
  @emotion/react: ^11.14.0
  @emotion/styled: ^11.14.0
  react-hook-form: ^7.54.0
  @hookform/resolvers: ^4.0.0
  zod: ^3.24.0
  axios: ^1.8.0
  framer-motion: ^12.0.0
  lucide-react: latest
  dayjs: ^1.11.0

devDependencies:
  typescript: ^5.7.0
  vite: ^7.3.0
  @vitejs/plugin-react: latest
  vitest: ^3.0.0
  @testing-library/react: latest
  @testing-library/jest-dom: latest
  jsdom: latest
  eslint: ^9.0.0
  @eslint/js: latest
  typescript-eslint: latest
  prettier: latest

engines: { "node": ">=20.19.0" }

[vite.config.ts]
- react 플러그인
- resolve.alias: { '@': path.resolve(__dirname, './src') }
- server.proxy: { '/api': { target: 'http://localhost:8080', changeOrigin: true } }
- build.rollupOptions.output.manualChunks: vendor(react, react-dom), mui(@mui/material, @emotion), query(@tanstack/react-query)
- 각 설정에 한국어 주석

[tsconfig.json]
- compilerOptions: strict true, target ES2022, module ESNext, moduleResolution bundler
- paths: { "@/*": ["./src/*"] }
- include: ["src"]

[eslint.config.mjs]
- ESLint 9 Flat Config
- @eslint/js recommended + typescript-eslint strict
- rules: no-unused-vars error, no-console warn, @typescript-eslint/no-explicit-any error

[vitest.config.ts]
- environment: jsdom
- globals: true
- setupFiles: ./src/test-setup.ts

[src/test-setup.ts]
- import '@testing-library/jest-dom'

[index.html]
- lang="ko"
- 뷰포트: width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no
- Pretendard Variable 웹폰트 CDN: <link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/variable/pretendardvariable.min.css" />
- <title>커뮤니티</title>
- <div id="root"></div>

[src/main.tsx]
- import React from 'react'
- import ReactDOM from 'react-dom/client'
- import App from '@/app/App'
- ReactDOM.createRoot(document.getElementById('root')!).render(<React.StrictMode><App /></React.StrictMode>)

[src/app/App.tsx]
- 기본 컴포넌트: <div>Hello Community</div> 텍스트만 렌더링
- 한국어 주석: "루트 컴포넌트. 추후 providers.tsx와 router.tsx를 여기서 합성"

[src/app/router.tsx]
- BrowserRouter + Routes 기본 설정
- Route: / → <div>홈</div>, /login → <div>로그인</div>
- 한국어 주석: "SPA 라우터 설정. 각 Route는 React.lazy로 추후 코드 스플리팅 적용"
- 주의: import { BrowserRouter, Routes, Route } from 'react-router'

[src/app/providers.tsx]
- QueryClientProvider 설정 (기본 staleTime: 5분)
- 한국어 주석: "글로벌 프로바이더 합성. ThemeProvider는 Task 0.4에서 추가"

디렉토리 구조 (빈 폴더 + 해당 index.ts에 // TODO 주석):
src/app/theme/
src/features/
src/shared/api/
src/shared/components/layout/
src/shared/components/feedback/
src/shared/components/common/
src/shared/hooks/
src/shared/guards/
src/shared/types/
src/shared/utils/
```

---

## Task 0.4: MUI 테마 & 디자인 토큰 설정

```
토스 스타일의 MUI v7 테마를 설정해줘.

경로: apps/web/src/app/theme/

이 프로젝트의 UI는 한국의 토스(Toss) 앱 스타일을 참고해. 깔끔하고 미니멀하며, 여백을 충분히 활용하고, 부드러운 애니메이션이 특징이야. Material Design 3의 체계를 따르되 토스처럼 커스터마이징해.

[생성할 파일 5개]

1. tokens.ts
컬러(토스 블루 #3182F6 기반), 타이포그래피(Pretendard), 스페이싱(4px 배수), 라디우스, 그림자, 애니메이션, 브레이크포인트, z-index를 모두 정의.
각 토큰 그룹에 한국어 주석으로 "이 값을 변경하면 어디에 영향을 주는지" 설명.
`as const`로 타입 추론. `export type DesignTokens = typeof tokens` 내보내기.

컬러 값:
- primary: '#3182F6' (토스 블루)
- primaryLight: '#EBF3FE'
- primaryDark: '#1B64DA'
- success: '#34C759', warning: '#FF9500', error: '#FF3B30', info: '#007AFF'
- background: '#F2F4F6', surface: '#FFFFFF', surfaceVariant: '#F8F9FA'
- textPrimary: '#191F28', textSecondary: '#8B95A1', textTertiary: '#B0B8C1'
- border: '#E5E8EB', divider: '#F2F4F6'

타이포그래피:
- fontFamily: '"Pretendard Variable", "Pretendard", -apple-system, BlinkMacSystemFont, sans-serif'
- headline1: 26px/700, headline2: 22px/700, title1: 19px/600, title2: 17px/600
- body1: 16px/400, body2: 14px/400, caption: 12px/400

스페이싱: xs:4, sm:8, md:12, lg:16, xl:20, xxl:24, section:28, page:20
라디우스: sm:8, md:12, lg:16, xl:20, full:9999
그림자: sm/md/lg/bottomSheet (부드러운 Apple 스타일)
모션: fast:150ms, normal:250ms, slow:350ms + 이징 함수들
브레이크포인트: mobile:0, tablet:600, desktop:1024

2. palette.ts
tokens.color를 MUI의 createTheme palette 형식으로 변환.
Light 모드 정의. (Dark 모드는 선택사항, 일단 Light만)
- palette.primary.main = tokens.color.primary
- palette.primary.light = tokens.color.primaryLight
- palette.primary.dark = tokens.color.primaryDark
- palette.secondary.main = tokens.color.textPrimary
- palette.error/warning/success/info에 시맨틱 컬러 매핑
- palette.background.default = tokens.color.background
- palette.background.paper = tokens.color.surface
- palette.text.primary/secondary/disabled에 텍스트 컬러 매핑
- palette.divider = tokens.color.divider
각 매핑에 "왜 이 토큰이 이 MUI 속성에 대응하는지" 한국어 주석.

3. typography.ts
tokens.typography를 MUI typography로 변환.
- fontFamily 설정
- h1 = headline1, h2 = headline2, h3 = title1, h4 = title2, h5/h6은 body1 기반
- body1, body2, caption, button, overline 정의
- allVariants에 공통 속성: color tokens.color.textPrimary

4. components.ts
MUI 컴포넌트의 defaultProps와 styleOverrides:

MuiButton:
- defaultProps: disableElevation true, disableRipple false
- styleOverrides.root: height 52px, borderRadius tokens.radius.md, fontWeight 600, fontSize 16px, textTransform 'none' (대문자 변환 비활성화)
- contained 변형: 토스 스타일 (primary 배경, 흰 텍스트, hover 시 primaryDark)
- 한국어 주석: "토스 앱의 하단 CTA 버튼 스타일. 높이 52px, 풀너비 가능"

MuiCard:
- styleOverrides.root: borderRadius tokens.radius.lg, boxShadow tokens.shadow.sm, padding tokens.spacing.lg
- 한국어 주석: "토스 스타일 카드. 둥근 모서리, 미묘한 그림자, 넉넉한 패딩"

MuiTextField:
- defaultProps: variant 'outlined', fullWidth true, size 'medium'
- styleOverrides: borderRadius tokens.radius.md, 배경색 tokens.color.surfaceVariant
- 한국어 주석: "토스 스타일 입력 필드. 부드러운 배경색, 둥근 모서리"

MuiAppBar:
- defaultProps: elevation 0, color 'inherit'
- styleOverrides.root: backgroundColor tokens.color.surface, borderBottom '1px solid' + tokens.color.divider

MuiBottomNavigation:
- styleOverrides.root: boxShadow tokens.shadow.bottomSheet, height 64px
- 한국어 주석: "모바일 하단 탭바. 토스 앱 하단 네비게이션 참고"

MuiChip:
- styleOverrides.root: borderRadius tokens.radius.sm, height 32px

MuiDialog:
- styleOverrides: borderRadius tokens.radius.xl

MuiIconButton:
- styleOverrides: '&:hover' 배경 tokens.color.primaryLight

5. index.ts
createTheme으로 palette + typography + components 합치기.
breakpoints 커스텀 (tokens.breakpoint 사용).
export default theme.

[수정할 파일]
- src/app/providers.tsx에 ThemeProvider와 CssBaseline 추가:
  import { ThemeProvider, CssBaseline } from '@mui/material';
  import theme from '@/app/theme';
  <ThemeProvider theme={theme}><CssBaseline />{children}</ThemeProvider>

- src/app/App.tsx에서 providers.tsx 사용하도록 수정
```

---

## Task 0.5: Spring Boot 프로젝트 초기화

```
Spring Boot 백엔드 프로젝트를 초기화해줘.

경로: apps/api/

Spring Boot 4.0.3 + Java 21 + Gradle Kotlin DSL 프로젝트야. Virtual Threads를 활성화하고, 모든 설정에 한국어 JavaDoc 주석을 달아줘.

주의: Spring Boot 4.0은 Spring Framework 7 기반이고, jakarta.* 패키지를 사용해. javax.* 패키지는 존재하지 않으니 절대 사용하지 마.

[build.gradle.kts]
plugins:
- java
- id("org.springframework.boot") version "4.0.3"
- id("io.spring.dependency-management") version 최신

java: sourceCompatibility = JavaVersion.VERSION_21, toolchain languageVersion 21

dependencies:
implementation:
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-validation
- spring-boot-starter-cache
- spring-boot-starter-actuator
- spring-boot-starter-data-redis
- org.postgresql:postgresql
- io.jsonwebtoken:jjwt-api:0.12.6
- org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.4
- com.querydsl:querydsl-jpa:5.1.0:jakarta (classifier jakarta)
- org.flywaydb:flyway-core:11.3.0
- org.flywaydb:flyway-database-postgresql:11.3.0
- org.mapstruct:mapstruct:1.6.3

runtimeOnly:
- io.jsonwebtoken:jjwt-impl:0.12.6
- io.jsonwebtoken:jjwt-jackson:0.12.6

compileOnly:
- org.projectlombok:lombok

annotationProcessor (순서 중요):
- org.projectlombok:lombok
- org.projectlombok:lombok-mapstruct-binding:0.2.0
- org.mapstruct:mapstruct-processor:1.6.3
- com.querydsl:querydsl-apt:5.1.0:jakarta (classifier jakarta)
- jakarta.annotation:jakarta.annotation-api
- jakarta.persistence:jakarta.persistence-api

testImplementation:
- spring-boot-starter-test
- org.testcontainers:testcontainers:1.20.4
- org.testcontainers:postgresql:1.20.4
- org.testcontainers:junit-jupiter:1.20.4

[settings.gradle.kts]
rootProject.name = "community-api"

[application.yml]
spring:
  application.name: community-api
  threads.virtual.enabled: true       # Virtual Threads 활성화
  jpa:
    open-in-view: false               # OSIV 비활성화 (LazyInitializationException 주의)
    hibernate.ddl-auto: validate      # Flyway가 스키마 관리하므로 validate만
    properties.hibernate:
      default_batch_fetch_size: 100   # N+1 방지: batch fetch
      format_sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration
  servlet.multipart:
    max-file-size: 10MB
    max-request-size: 10MB
server:
  port: ${APP_PORT:8080}
  error.include-message: always
springdoc:
  swagger-ui.path: /swagger-ui.html
  api-docs.path: /api-docs
management:
  endpoints.web.exposure.include: health,info,prometheus,metrics
  metrics.tags.application: ${spring.application.name}

[application-dev.yml]
spring:
  datasource:
    url: jdbc:postgresql://${POSTGRES_HOST:localhost}:${POSTGRES_PORT:5432}/${POSTGRES_DB:community_dev}
    username: ${POSTGRES_USER:community}
    password: ${POSTGRES_PASSWORD:community1234}
    hikari.maximum-pool-size: 10
  data.redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    com.community: DEBUG

[application-prod.yml]
spring:
  datasource:
    url: jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/${POSTGRES_DB}
    username: ${POSTGRES_USER}
    password: ${POSTGRES_PASSWORD}
    hikari.maximum-pool-size: 30
logging:
  level:
    root: WARN
    com.community: INFO

[Java 클래스 생성]

1. com/community/CommunityApplication.java
@SpringBootApplication. main 메서드. JavaDoc: "커뮤니티 웹앱 REST API 서버의 메인 진입점"

2. com/community/global/config/JpaConfig.java
@Configuration @EnableJpaAuditing. JavaDoc: "JPA Auditing 활성화. BaseTimeEntity의 createdAt, updatedAt을 자동 관리"

3. com/community/global/config/RedisConfig.java
@Configuration. RedisTemplate<String, Object> 빈: GenericJackson2JsonRedisSerializer로 JSON 직렬화. StringRedisSerializer로 키 직렬화. JavaDoc: "Redis 연결 및 직렬화 설정. 캐시와 JWT 블랙리스트에 사용"

4. com/community/global/config/CorsConfig.java
@Configuration implements WebMvcConfigurer. addCorsMappings에서 허용: origin "http://localhost:5173", methods GET/POST/PATCH/DELETE, allowCredentials true. JavaDoc: "CORS 설정. 개발 환경에서 프론트엔드(Vite dev server)의 API 호출 허용"

5. com/community/global/config/SwaggerConfig.java
@Configuration. OpenAPI 빈: title "커뮤니티 API", description "모바일 퍼스트 커뮤니티 웹앱 REST API", version "1.0.0". JavaDoc: "Swagger UI 자동 문서 생성 설정. /swagger-ui.html에서 확인"

6. com/community/global/common/response/ApiResponse.java
public record ApiResponse<T>(boolean success, T data, ErrorDetail error). static 팩토리: success(T data), error(ErrorCode code), error(ErrorCode code, String message). 내부 record ErrorDetail(String code, String message). JavaDoc: "모든 API 응답을 감싸는 통합 응답 래퍼. 성공 시 data에 결과, 실패 시 error에 코드와 메시지"

7. com/community/global/common/response/PageResponse.java
public record PageResponse<T>(List<T> content, long totalElements, int totalPages, int page, int size, boolean hasNext, boolean hasPrevious). static 팩토리: of(Page<T> page). JavaDoc: "Spring Data Page를 프론트엔드 친화적 형식으로 변환하는 페이징 응답 래퍼"

8. com/community/global/common/entity/BaseTimeEntity.java
@MappedSuperclass @EntityListeners(AuditingEntityListener.class) @Getter. @CreatedDate LocalDateTime createdAt, @LastModifiedDate LocalDateTime updatedAt. JavaDoc: "모든 엔티티가 상속하는 공통 시간 필드. JPA Auditing이 자동으로 값을 채움"

9. com/community/global/common/exception/ErrorCode.java
public enum ErrorCode. 각 항목: (String code, HttpStatus httpStatus, String message).
값들: INVALID_INPUT("E001", BAD_REQUEST, "입력값이 올바르지 않습니다"), DUPLICATE_EMAIL("E002", CONFLICT, "이미 사용 중인 이메일입니다"), DUPLICATE_NICKNAME("E003", CONFLICT, "이미 사용 중인 닉네임입니다"), INVALID_CREDENTIALS("E004", UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다"), EXPIRED_TOKEN("E005", UNAUTHORIZED, "인증 토큰이 만료되었습니다"), INVALID_TOKEN("E006", UNAUTHORIZED, "유효하지 않은 토큰입니다"), ACCESS_DENIED("E007", FORBIDDEN, "접근 권한이 없습니다"), USER_NOT_FOUND("E008", NOT_FOUND, "사용자를 찾을 수 없습니다"), POST_NOT_FOUND("E009", NOT_FOUND, "게시글을 찾을 수 없습니다"), COMMENT_NOT_FOUND("E010", NOT_FOUND, "댓글을 찾을 수 없습니다"), CATEGORY_NOT_FOUND("E011", NOT_FOUND, "카테고리를 찾을 수 없습니다"), UNAUTHORIZED_MODIFICATION("E012", FORBIDDEN, "수정 권한이 없습니다"), FILE_UPLOAD_FAILED("E013", INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다"), INVALID_FILE_TYPE("E014", BAD_REQUEST, "지원하지 않는 파일 형식입니다"), INTERNAL_ERROR("E999", INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다").
JavaDoc 각 항목에 사용 상황 설명.

10. com/community/global/common/exception/BusinessException.java
extends RuntimeException. @Getter final ErrorCode errorCode. 생성자: BusinessException(ErrorCode). JavaDoc: "비즈니스 로직에서 예상된 예외를 발생시킬 때 사용. throw new BusinessException(ErrorCode.POST_NOT_FOUND)"

11. com/community/global/common/exception/GlobalExceptionHandler.java
@RestControllerAdvice @Slf4j.
@ExceptionHandler(BusinessException.class) → ApiResponse.error(e.getErrorCode()), 상태코드 e.getErrorCode().getHttpStatus()
@ExceptionHandler(MethodArgumentNotValidException.class) → 검증 에러의 첫 번째 필드 에러 메시지 추출, ApiResponse.error(INVALID_INPUT, message)
@ExceptionHandler(Exception.class) → log.error 후 ApiResponse.error(INTERNAL_ERROR)
JavaDoc: "모든 예외를 가로채서 통일된 ApiResponse 형식으로 변환. 클라이언트는 항상 동일한 응답 구조를 받음"

12. com/community/global/util/SecurityUtil.java
static Long getCurrentUserId(): SecurityContextHolder에서 Authentication 가져와서 userId(Long) 반환. 인증 안 됐으면 BusinessException(ACCESS_DENIED). JavaDoc: "현재 로그인한 사용자의 ID를 조회하는 유틸리티. 컨트롤러/서비스에서 현재 사용자 확인용"
```

---

## Task 0.6: Flyway 초기 마이그레이션

```
Flyway 마이그레이션 파일로 데이터베이스 스키마를 생성해줘.

경로: apps/api/src/main/resources/db/migration/

PostgreSQL 18을 사용해. 모든 TIMESTAMP는 WITH TIME ZONE으로 사용하고, CHECK 제약조건으로 유효값을 제한해.

다음 7개 파일을 생성해줘. 각 파일 상단에 SQL 주석으로 목적과 생성되는 테이블을 설명하고, 각 컬럼 옆에 인라인 주석을 달아줘.

1. V1__create_users_table.sql
CREATE TABLE users: id(BIGSERIAL PK), email(VARCHAR 255 NOT NULL UNIQUE), password_hash(VARCHAR 255 NOT NULL), nickname(VARCHAR 50 NOT NULL UNIQUE), bio(VARCHAR 300 nullable), profile_image(VARCHAR 500 nullable), role(VARCHAR 20 NOT NULL DEFAULT 'USER' CHECK IN ('USER','ADMIN')), is_active(BOOLEAN NOT NULL DEFAULT TRUE), last_login_at(TIMESTAMPTZ nullable), created_at(TIMESTAMPTZ NOT NULL DEFAULT NOW()), updated_at(TIMESTAMPTZ NOT NULL DEFAULT NOW()).
인덱스: idx_users_email(email), idx_users_nickname(nickname).

2. V2__create_categories_table.sql
CREATE TABLE categories: id(BIGSERIAL PK), name(VARCHAR 50 NOT NULL UNIQUE), slug(VARCHAR 50 NOT NULL UNIQUE), description(VARCHAR 200 nullable), sort_order(INT NOT NULL DEFAULT 0), is_active(BOOLEAN NOT NULL DEFAULT TRUE), created_at(TIMESTAMPTZ NOT NULL DEFAULT NOW()).
INSERT 4개: ('자유','free','자유롭게 이야기하는 공간',1), ('질문','question','궁금한 것을 물어보는 공간',2), ('정보','info','유용한 정보를 공유하는 공간',3), ('리뷰','review','리뷰와 후기를 나누는 공간',4).

3. V3__create_posts_table.sql
CREATE TABLE posts: id(BIGSERIAL PK), author_id(BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE), category_id(BIGINT REFERENCES categories(id) ON DELETE SET NULL), title(VARCHAR 200 NOT NULL), content(TEXT NOT NULL), summary(VARCHAR 300 nullable), thumbnail_url(VARCHAR 500 nullable), view_count(INT NOT NULL DEFAULT 0), vote_count(INT NOT NULL DEFAULT 0), comment_count(INT NOT NULL DEFAULT 0), is_pinned(BOOLEAN NOT NULL DEFAULT FALSE), is_deleted(BOOLEAN NOT NULL DEFAULT FALSE), created_at(TIMESTAMPTZ NOT NULL DEFAULT NOW()), updated_at(TIMESTAMPTZ NOT NULL DEFAULT NOW()).
인덱스: idx_posts_author(author_id), idx_posts_category(category_id), idx_posts_created(created_at DESC) WHERE is_deleted = FALSE, idx_posts_vote(vote_count DESC) WHERE is_deleted = FALSE.

4. V4__create_comments_table.sql
CREATE TABLE comments: id(BIGSERIAL PK), post_id(BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE), author_id(BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE), parent_id(BIGINT REFERENCES comments(id) ON DELETE CASCADE), content(TEXT NOT NULL), vote_count(INT NOT NULL DEFAULT 0), is_deleted(BOOLEAN NOT NULL DEFAULT FALSE), created_at(TIMESTAMPTZ NOT NULL DEFAULT NOW()), updated_at(TIMESTAMPTZ NOT NULL DEFAULT NOW()).
인덱스: idx_comments_post(post_id), idx_comments_parent(parent_id).

5. V5__create_votes_bookmarks.sql
CREATE TABLE votes: id(BIGSERIAL PK), user_id(BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE), target_type(VARCHAR 20 NOT NULL CHECK IN ('POST','COMMENT')), target_id(BIGINT NOT NULL), created_at(TIMESTAMPTZ NOT NULL DEFAULT NOW()), UNIQUE(user_id, target_type, target_id).
인덱스: idx_votes_target(target_type, target_id).
CREATE TABLE bookmarks: id(BIGSERIAL PK), user_id(BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE), post_id(BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE), created_at(TIMESTAMPTZ NOT NULL DEFAULT NOW()), UNIQUE(user_id, post_id).

6. V6__create_notifications.sql
CREATE TABLE notifications: id(BIGSERIAL PK), recipient_id(BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE), actor_id(BIGINT REFERENCES users(id) ON DELETE SET NULL), type(VARCHAR 30 NOT NULL CHECK IN ('COMMENT','VOTE','FOLLOW','ADMIN_NOTICE')), target_type(VARCHAR 20 nullable), target_id(BIGINT nullable), message(VARCHAR 300 NOT NULL), is_read(BOOLEAN NOT NULL DEFAULT FALSE), created_at(TIMESTAMPTZ NOT NULL DEFAULT NOW()).
인덱스: idx_notifications_recipient(recipient_id, is_read, created_at DESC).

7. V7__create_supporting_tables.sql
CREATE TABLE media: id(BIGSERIAL PK), uploader_id(BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE), original_name(VARCHAR 255 NOT NULL), stored_path(VARCHAR 500 NOT NULL), thumbnail_path(VARCHAR 500 nullable), file_size(BIGINT NOT NULL), mime_type(VARCHAR 100 NOT NULL), width(INT nullable), height(INT nullable), created_at(TIMESTAMPTZ NOT NULL DEFAULT NOW()).
CREATE TABLE reports: id(BIGSERIAL PK), reporter_id(BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE), target_type(VARCHAR 20 NOT NULL CHECK IN ('USER','POST','COMMENT')), target_id(BIGINT NOT NULL), reason(VARCHAR 50 NOT NULL CHECK IN ('SPAM','ABUSE','INAPPROPRIATE','COPYRIGHT','OTHER')), description(VARCHAR 500 nullable), status(VARCHAR 20 NOT NULL DEFAULT 'PENDING' CHECK IN ('PENDING','RESOLVED','DISMISSED')), resolved_by(BIGINT REFERENCES users(id) nullable), resolved_at(TIMESTAMPTZ nullable), created_at(TIMESTAMPTZ NOT NULL DEFAULT NOW()). 인덱스: idx_reports_status(status, created_at DESC).
CREATE TABLE refresh_tokens: id(BIGSERIAL PK), user_id(BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE), token(VARCHAR 500 NOT NULL UNIQUE), expires_at(TIMESTAMPTZ NOT NULL), created_at(TIMESTAMPTZ NOT NULL DEFAULT NOW()). 인덱스: idx_refresh_tokens_user(user_id), idx_refresh_tokens_token(token).
```

---

## Task 0.7: CLAUDE.md (AI 컨텍스트)

```
프로젝트 루트에 CLAUDE.md 파일을 작성해줘.

이 파일은 AI 코딩 어시스턴트가 프로젝트를 이해하기 위한 컨텍스트 문서야. 간결하지만 필수 정보를 모두 담아야 해. 한국어로 작성하되 코드는 영어.

포함 내용:
1. 프로젝트 개요 (3줄 이내)
2. 기술 스택 표 (프론트: React 19.2, Vite 7.3, MUI 7.3, TS 5.7 | 백: Spring Boot 4.0.3, Java 21, PostgreSQL 18.3 | 인프라: Docker, Redis 7.4, MinIO)
3. 디렉토리 구조 (핵심만, 트리 형식)
4. 코딩 규칙: 한국어 주석 필수, any 금지, record DTO, @Setter 금지, import 별칭(@/), jakarta.* 패키지, React Router v7 문법
5. 빠른 명령어: make dev/down/web/api/test
6. 새 기능 추가 순서: ① Flyway 마이그레이션 → ② Entity+Repository → ③ Service → ④ Controller → ⑤ 프론트 types → ⑥ API 함수 → ⑦ TanStack Query 훅 → ⑧ 컴포넌트 → ⑨ 페이지 → ⑩ 라우터 등록
7. 자주 발생하는 에러 & 해결 표: CORS error(CorsConfig), 401(JWT만료→토큰갱신), N+1(Fetch Join/@EntityGraph), QueryDSL Q클래스 없음(./gradlew compileJava)
8. API 응답 규격: ApiResponse<T> { success, data, error }
9. AI 행동 지침: 코드 생성 시 한국어 주석, 새 파일에 경로와 목적 명시, 복잡한 로직 단계별 설명, 에러처리 명시적 구현, 가능하면 테스트 제안
```

---

# ═══════════════════════════════════════════
# Phase 1: 인증 시스템 (7 Tasks)
# 목표: 회원가입, 로그인, JWT 인증, 토큰 갱신
# 완료 기준: 회원가입 → 로그인 → 토큰으로 API 접근 가능
#
# 검증 기준 (Phase 완료 시):
# - [ ] `make lint-api` → 컴파일 성공
# - [ ] POST /api/v1/auth/signup → 201 응답 (Swagger UI)
# - [ ] POST /api/v1/auth/login → JWT 토큰 반환
# - [ ] 프론트엔드 로그인/회원가입 페이지 렌더링 확인
# - [ ] `make lint-web` + `make typecheck` → 통과
# ═══════════════════════════════════════════

---

## Task 1.1: User 엔티티 & Repository

```
사용자(User) 도메인의 엔티티와 리포지토리를 구현해줘.

경로: apps/api/src/main/java/com/community/domain/user/

[생성할 파일]

1. entity/User.java
@Entity @Table(name = "users") @Getter @NoArgsConstructor(access = AccessLevel.PROTECTED) @Builder. BaseTimeEntity 상속.
필드: Long id(@Id @GeneratedValue IDENTITY), String email, String passwordHash, String nickname, String bio, String profileImage, UserRole role(@Enumerated STRING, 기본값 USER), Boolean isActive(기본값 true), LocalDateTime lastLoginAt.
비즈니스 메서드: updateProfile(nickname, bio), updateProfileImage(url), deactivate(), updateLastLogin().
@Setter는 사용하지 않아. 상태 변경은 반드시 비즈니스 메서드를 통해.
각 필드에 인라인 JavaDoc 주석.

2. entity/UserRole.java
public enum UserRole implements GrantedAuthority. 값: USER, ADMIN. getAuthority()는 "ROLE_" + name() 반환.

3. repository/UserRepository.java
extends JpaRepository<User, Long>.
Optional<User> findByEmail(String email);
Optional<User> findByNickname(String nickname);
boolean existsByEmail(String email);
boolean existsByNickname(String nickname);

4. dto/response/UserProfileResponse.java
public record: Long id, String email, String nickname, String bio, String profileImage, UserRole role, LocalDateTime createdAt. JavaDoc: "사용자 프로필 전체 정보 응답. 내 프로필 조회 시 사용"

5. dto/response/UserSummaryResponse.java
public record: Long id, String nickname, String profileImage. JavaDoc: "사용자 요약 정보. 게시글/댓글 목록에서 작성자 정보로 사용"

6. mapper/UserMapper.java
@Mapper(componentModel = "spring"). UserProfileResponse toProfileResponse(User user); UserSummaryResponse toSummaryResponse(User user).
```

---

## Task 1.2: Spring Security & JWT 설정

```
Spring Security와 JWT 인증을 구현해줘.

경로: apps/api/src/main/java/com/community/global/

주의: Spring Boot 4.0에서는 SecurityFilterChain을 @Bean으로 등록해. WebSecurityConfigurerAdapter는 삭제된 클래스이므로 절대 사용하지 마.

[생성할 파일]

1. security/jwt/JwtTokenProvider.java
@Component @Slf4j. @Value로 JWT_SECRET, JWT_ACCESS_EXPIRATION(기본 900000=15분), JWT_REFRESH_EXPIRATION(기본 604800000=7일) 주입.
메서드:
- createAccessToken(Long userId, UserRole role): String → JWT 생성. Claims에 userId, role 포함.
- createRefreshToken(): String → UUID 기반 랜덤 토큰.
- validateToken(String token): boolean → 서명 검증 + 만료 확인.
- getUserIdFromToken(String token): Long → Claims에서 userId 추출.
- getRoleFromToken(String token): UserRole
jjwt 0.12.x API 사용: Jwts.builder(), Jwts.parser().verifyWith(key).build().parseSignedClaims(). 
SecretKey는 Keys.hmacShaKeyFor(secret.getBytes()).
각 메서드에 상세 JavaDoc.

2. security/jwt/JwtAuthFilter.java
extends OncePerRequestFilter. @RequiredArgsConstructor.
doFilterInternal: Authorization 헤더에서 "Bearer " 접두사 제거 → JwtTokenProvider.validateToken → userId/role 추출 → UsernamePasswordAuthenticationToken 생성 → SecurityContextHolder에 설정.
/api/v1/auth/** 경로는 필터 스킵.
JavaDoc: "모든 HTTP 요청에서 JWT 토큰을 검증하는 필터. 유효한 토큰이면 SecurityContext에 인증 정보 설정"

3. security/jwt/JwtTokenDto.java
public record: String accessToken, String refreshToken, long expiresIn.

4. security/UserDetailsServiceImpl.java
implements UserDetailsService. UserRepository를 주입받아 loadUserByUsername(email)으로 User 조회. 없으면 UsernameNotFoundException.

5. security/CustomAuthEntryPoint.java
implements AuthenticationEntryPoint. commence에서 401 JSON 응답: ApiResponse.error(EXPIRED_TOKEN)을 ObjectMapper로 직렬화하여 response에 쓰기.

6. config/SecurityConfig.java
@Configuration @EnableMethodSecurity(prePostEnabled = true). @Bean SecurityFilterChain:
- csrf 비활성화 (JWT stateless)
- sessionManagement STATELESS
- authorizeHttpRequests:
  * permit: POST /api/v1/auth/**, GET /api/v1/posts/**, GET /api/v1/posts, GET /api/v1/categories, /swagger-ui/**, /api-docs/**, /actuator/**
  * 나머지 모두 authenticated
- addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
- exceptionHandling: authenticationEntryPoint(customAuthEntryPoint)
@Bean BCryptPasswordEncoder (strength 12)
각 설정에 "왜 이렇게 설정했는지" 한국어 주석.

7. util/SecurityUtil.java (이미 Task 0.5에서 생성했다면 수정)
getCurrentUserId: SecurityContext에서 Authentication의 principal(Long userId)을 반환. 인증 안 됐으면 BusinessException(ACCESS_DENIED).
```

---

## Task 1.3: 회원가입 & 로그인 API

```
인증(Auth) API를 구현해줘.

경로: apps/api/src/main/java/com/community/domain/auth/

[생성할 파일]

1. dto/request/SignupRequest.java
public record: @Email @NotBlank String email, @NotBlank @Size(min=8, max=50) String password, @NotBlank @Size(min=2, max=20) String nickname. JavaDoc에 검증 규칙 설명.

2. dto/request/LoginRequest.java
public record: @NotBlank String email, @NotBlank String password.

3. dto/request/TokenRefreshRequest.java
public record: @NotBlank String refreshToken.

4. dto/response/TokenResponse.java
public record: String accessToken, String refreshToken, long expiresIn, UserProfileResponse user.

5. entity/RefreshToken.java
@Entity @Table(name = "refresh_tokens") @Getter @NoArgsConstructor(access = AccessLevel.PROTECTED) @Builder. Long id, @ManyToOne(fetch=LAZY) @JoinColumn(name="user_id") User user, String token, LocalDateTime expiresAt, LocalDateTime createdAt(@CreatedDate). 비즈니스 메서드: isExpired().

6. repository/RefreshTokenRepository.java
Optional<RefreshToken> findByToken(String token); void deleteByUser(User user); void deleteByToken(String token).

7. service/AuthService.java
@Service @RequiredArgsConstructor @Transactional. 의존성: UserRepository, RefreshTokenRepository, JwtTokenProvider, BCryptPasswordEncoder, UserMapper.
- signup(SignupRequest): 이메일 중복 확인(existsByEmail → DUPLICATE_EMAIL), 닉네임 중복 확인(existsByNickname → DUPLICATE_NICKNAME), 비밀번호 해싱, User 저장, 토큰 발급, TokenResponse 반환.
- login(LoginRequest): 이메일로 User 조회(없으면 INVALID_CREDENTIALS), 비밀번호 검증(matches 실패 시 INVALID_CREDENTIALS), 기존 리프레시 토큰 삭제, 새 토큰 발급, lastLogin 업데이트, TokenResponse 반환.
- refresh(TokenRefreshRequest): refreshToken으로 RefreshToken 조회, 만료 확인, User 확인, 기존 토큰 삭제, 새 토큰쌍 발급.
- logout(Long userId): User의 모든 RefreshToken 삭제.
각 메서드에 단계별 주석. 에러 케이스마다 어떤 ErrorCode를 던지는지 명시.

8. controller/AuthController.java
@RestController @RequestMapping("/api/v1/auth") @RequiredArgsConstructor @Tag(name = "인증").
POST /signup: @Valid @RequestBody SignupRequest → ApiResponse<TokenResponse>. @Operation(summary = "회원가입")
POST /login: @Valid @RequestBody LoginRequest → ApiResponse<TokenResponse>. @Operation(summary = "로그인")
POST /refresh: @Valid @RequestBody TokenRefreshRequest → ApiResponse<TokenResponse>. @Operation(summary = "토큰 갱신")
POST /logout: @AuthenticationPrincipal Long userId → ApiResponse<Void>. @Operation(summary = "로그아웃")
모든 메서드에 한국어 JavaDoc.
```

---

## Task 1.4: 프론트엔드 Axios & API 공통 설정

```
프론트엔드 API 통신 기반을 설정해줘.

경로: apps/web/src/shared/

[생성할 파일]

1. api/axiosInstance.ts
Axios 인스턴스 생성:
- baseURL: '/api/v1'
- timeout: 10000
- headers: { 'Content-Type': 'application/json' }

요청 인터셉터:
- authStore에서 accessToken 가져와서 있으면 Authorization: Bearer {token} 추가
- 한국어 주석: "모든 API 요청에 자동으로 인증 토큰 추가"

응답 인터셉터 (토큰 갱신 로직):
- 401 에러 시 자동 갱신:
  1) isRefreshing 플래그로 동시 갱신 방지
  2) failedQueue 배열에 대기 중인 요청 추가
  3) /auth/refresh 호출 → 성공 시 새 토큰으로 대기 요청 재시도
  4) 갱신 실패 시 authStore.clearAuth() + /login 리다이렉트
- 각 단계에 상세한 한국어 주석 (이 로직은 복잡하므로 단계별 설명 필수)

2. api/apiTypes.ts
interface ApiResponse<T> { success: boolean; data: T; error: { code: string; message: string } | null; }
interface PageResponse<T> { content: T[]; totalElements: number; totalPages: number; page: number; size: number; hasNext: boolean; hasPrevious: boolean; }
class ApiError extends Error { code: string; constructor(code: string, message: string) }
helper 함수: extractData<T>(response: AxiosResponse<ApiResponse<T>>): T → success가 false면 ApiError throw.

3. types/user.ts
interface User { id: number; email: string; nickname: string; bio: string | null; profileImage: string | null; role: 'USER' | 'ADMIN'; createdAt: string; }
interface UserSummary { id: number; nickname: string; profileImage: string | null; }
interface TokenResponse { accessToken: string; refreshToken: string; expiresIn: number; user: User; }

4. types/common.ts
interface PageRequest { page?: number; size?: number; sort?: string; }
type SortDirection = 'LATEST' | 'POPULAR';

모든 파일에 파일 목적 설명과 한국어 JSDoc 주석.
```

---

## Task 1.5: 인증 스토어 & 훅

```
인증 관련 Zustand 스토어와 커스텀 훅을 구현해줘.

경로: apps/web/src/features/auth/

[생성할 파일]

1. store/authStore.ts
Zustand create 사용 (zustand v5 문법):
상태: user(User | null), accessToken(string | null), isAuthenticated(boolean, computed)
액션: setAuth(user, accessToken), clearAuth(), updateUser(Partial<User>)
persist 미들웨어: localStorage에 user만 저장 (accessToken은 메모리에만)
한국어 주석: 각 상태 필드의 목적, persist 설정의 이유

Zustand v5 정확한 문법:
import { create } from 'zustand'
import { persist } from 'zustand/middleware'
// create<StoreType>()(persist(...))  ← 이중 괄호 주의

2. api/authApi.ts
axiosInstance 사용:
- signup(data: SignupPayload): Promise<TokenResponse> → POST /auth/signup
- login(data: LoginPayload): Promise<TokenResponse> → POST /auth/login  
- refreshToken(refreshToken: string): Promise<TokenResponse> → POST /auth/refresh
- logout(): Promise<void> → POST /auth/logout
- 각 함수에서 extractData 헬퍼 사용

interface SignupPayload { email: string; password: string; nickname: string; }
interface LoginPayload { email: string; password: string; }

3. hooks/useAuth.ts
- useLogin(): useMutation. onSuccess에서 authStore.setAuth(user, accessToken), localStorage에 refreshToken 저장, '/'로 navigate.
- useSignup(): useMutation. onSuccess에서 동일하게 처리.
- useLogout(): authStore.clearAuth(), localStorage에서 refreshToken 제거, queryClient.clear(), '/'로 navigate.
- useCurrentUser(): authStore에서 user 반환.
- useIsAdmin(): user?.role === 'ADMIN'

TanStack Query v5 정확한 문법:
useMutation({ mutationFn: ..., onSuccess: ..., onError: ... })

4. index.ts
barrel export: useLogin, useSignup, useLogout, useCurrentUser, useIsAdmin, authStore

모든 파일에 한국어 주석. 각 훅의 사용 예시를 JSDoc에 포함.
```

---

## Task 1.6: 로그인 & 회원가입 UI

```
로그인과 회원가입 페이지를 구현해줘.

경로: apps/web/src/features/auth/

MUI v7 + React Hook Form + Zod를 사용해. 디자인은 토스 앱의 로그인 화면을 참고: 깨끗한 흰색 배경, 큰 입력 필드, 풀 너비 CTA 버튼, 중앙 정렬.

[생성할 파일]

1. components/LoginForm.tsx
- react-hook-form의 useForm + zodResolver로 Zod 스키마 연결
- Zod 스키마: email(z.string().email("올바른 이메일을 입력하세요")), password(z.string().min(8, "비밀번호는 8자 이상이어야 합니다"))
- MUI TextField 2개: 이메일, 비밀번호(type password)
- MUI Button: "로그인" (풀너비, variant contained)
- 로딩 상태: Button에 disabled + CircularProgress
- 에러 메시지: 각 필드 아래 FormHelperText 또는 TextField error prop
- 서버 에러: Alert 컴포넌트로 상단에 표시
- useLogin 훅 사용
- 하단: "아직 회원이 아니신가요?" + Link to="/signup"
- 한국어 텍스트

2. components/SignupForm.tsx
- Zod 스키마: email, password(8자+), passwordConfirm(password와 일치 검증 .refine()), nickname(2~20자)
- MUI TextField 4개: 이메일, 비밀번호, 비밀번호 확인, 닉네임
- MUI Button: "회원가입"
- useSignup 훅 사용
- 하단: "이미 회원이신가요?" + Link to="/login"

3. pages/LoginPage.tsx
- Box 컴포넌트로 중앙 정렬 (maxWidth 400px, margin auto)
- 상단: 앱 로고 또는 제목 "커뮤니티" (Typography variant h4)
- LoginForm
- Framer Motion: motion.div로 fade-in 애니메이션 (initial opacity 0, animate opacity 1, duration 0.3s)

4. pages/SignupPage.tsx
- LoginPage와 동일한 레이아웃
- SignupForm
- Framer Motion fade-in

5. index.ts
barrel export: LoginPage, SignupPage

[MUI 컴포넌트 사용 시 참고]
- TextField: variant="outlined" fullWidth, sx로 추가 스타일 적용. tokens의 radius, background 사용.
- Button: variant="contained" fullWidth size="large". 높이는 테마에서 이미 52px로 설정됨.
- Typography: 테마의 타이포그래피 자동 적용됨.
- Link: from 'react-router'의 Link 컴포넌트 (MUI Link 아님!)
```

---

## Task 1.7: 라우터 & 레이아웃

```
라우팅 설정과 공통 레이아웃을 구현해줘.

경로: apps/web/src/

React Router v7 사용. 주의: import from 'react-router' (NOT react-router-dom).

[생성/수정할 파일]

1. shared/guards/AuthGuard.tsx
- authStore에서 isAuthenticated 확인
- 미인증 → Navigate to="/login" replace
- 인증됨 → children 렌더링 (Outlet)
- 한국어 주석: "로그인이 필요한 페이지를 보호하는 가드"

2. shared/guards/AdminGuard.tsx
- authStore에서 user?.role === 'ADMIN' 확인
- 비관리자 → Navigate to="/" replace
- 관리자 → children 렌더링

3. shared/components/layout/TopAppBar.tsx
- MUI AppBar + Toolbar
- 왼쪽: 뒤로가기 버튼 (useNavigate) 또는 앱 제목
- 오른쪽: 로그인 상태에 따라 프로필 아바타(UserAvatar) 또는 로그인 버튼
- 높이 56px (모바일 적정)
- 한국어 주석

4. shared/components/layout/BottomTabBar.tsx
- MUI BottomNavigation + BottomNavigationAction
- 탭 4개: 홈(Home아이콘, /), 글쓰기(PenSquare아이콘, /posts/new), 알림(Bell아이콘, /notifications), 프로필(User아이콘, /profile)
- lucide-react 아이콘 사용
- 현재 경로(useLocation)에 따라 활성 탭 하이라이트
- 글쓰기/알림/프로필은 미로그인 시 /login으로 리다이렉트
- position fixed, bottom 0, zIndex tokens.zIndex.bottomTabBar
- 한국어 주석

5. shared/components/layout/AppLayout.tsx
- Box로 감싸기: paddingTop 56px(AppBar), paddingBottom 64px(BottomTab)
- TopAppBar + Outlet + BottomTabBar
- 한국어 주석: "메인 앱 레이아웃. 상단 앱바, 하단 탭바, 가운데 페이지 콘텐츠"

6. shared/components/layout/AdminLayout.tsx
- 데스크톱: 사이드바 네비게이션 (Drawer) + 메인 콘텐츠
- 모바일: 하단 탭 또는 햄버거 메뉴
- 관리자 메뉴: 대시보드, 사용자 관리, 게시글 관리, 신고 관리
- 한국어 주석

7. shared/components/common/UserAvatar.tsx
- MUI Avatar
- props: src(string | null), name(string), size('sm' | 'md' | 'lg')
- 이미지 없으면 이름 첫 글자 표시 (배경색은 이름 해시로 결정)

8. app/router.tsx 수정
React.lazy + Suspense로 코드 스플리팅:
- Layout: AppLayout
  - / → HomePage (placeholder 페이지)
  - /posts → PostListPage (placeholder)
  - /posts/:id → PostDetailPage (placeholder)
  - /posts/new → AuthGuard → PostEditorPage (placeholder)
  - /notifications → AuthGuard → NotificationPage (placeholder)
  - /profile → AuthGuard → ProfilePage (placeholder)
- /login → LoginPage (로그인 상태면 / 리다이렉트)
- /signup → SignupPage (로그인 상태면 / 리다이렉트)
- /admin → AdminGuard → AdminLayout
  - /admin/dashboard → AdminDashboardPage (placeholder)
  - /admin/users → AdminUsersPage (placeholder)

placeholder 페이지: <Box p={4}><Typography>페이지명 (준비 중)</Typography></Box>

9. app/App.tsx 수정: providers + router 합성

import 주의: 모두 from 'react-router' 사용.
```

---

# ═══════════════════════════════════════════
# Phase 2: 게시글 CRUD (6 Tasks)
# 목표: 게시글 작성, 조회, 수정, 삭제, 목록, 검색
# 완료 기준: 로그인 후 게시글 작성 → 목록에서 확인 → 상세 보기
#
# 검증 기준 (Phase 완료 시):
# - [ ] `make lint-api` → 컴파일 성공
# - [ ] CRUD API 모두 Swagger UI에서 동작 확인
# - [ ] 프론트엔드 게시글 목록/상세/에디터 페이지 렌더링
# - [ ] 무한 스크롤 동작 확인
# - [ ] `make verify` → 전체 통과
# ═══════════════════════════════════════════

---

## Task 2.1: Post 엔티티 & Repository (백엔드)

```
게시글(Post) 도메인의 엔티티, 리포지토리, DTO, 매퍼를 구현해줘.

경로: apps/api/src/main/java/com/community/domain/post/

[생성할 파일]

1. entity/Category.java - @Entity @Table(name="categories"), 필드: id, name, slug, description, sortOrder, isActive, createdAt.

2. entity/Post.java - @Entity @Table(name="posts"), BaseTimeEntity 상속.
필드: id, @ManyToOne(fetch=LAZY) @JoinColumn(name="author_id") User author, @ManyToOne(fetch=LAZY) @JoinColumn(name="category_id") Category category, String title, @Column(columnDefinition="TEXT") String content, String summary, String thumbnailUrl, int viewCount(기본0), int voteCount(기본0), int commentCount(기본0), boolean isPinned(기본false), boolean isDeleted(기본false).
비즈니스 메서드: update(title,content,summary,category), softDelete(), pin(), unpin(), increaseViewCount(), increaseVoteCount(), decreaseVoteCount(), increaseCommentCount(), decreaseCommentCount().

3. repository/CategoryRepository.java - JpaRepository<Category, Long>. findAllByIsActiveTrueOrderBySortOrder().

4. repository/PostRepository.java - JpaRepository<Post, Long>. Optional<Post> findByIdAndIsDeletedFalse(Long id).

5. repository/PostRepositoryCustom.java - interface. Page<Post> search(String keyword, Long categoryId, String sort, Pageable pageable).

6. repository/PostRepositoryImpl.java - implements PostRepositoryCustom. QueryDSL 구현:
- QPost, QUser, QCategory 사용
- keyword가 있으면 title.containsIgnoreCase(keyword)
- categoryId가 있으면 category.id.eq(categoryId)
- sort가 "POPULAR"면 voteCount.desc(), 아니면 createdAt.desc()
- isDeleted.isFalse() 항상 적용
- author를 fetchJoin (N+1 방지)
주석: QueryDSL 쿼리 빌딩 과정을 단계별로 설명.

7. dto/request/CreatePostRequest.java - record: @NotBlank @Size(max=200) title, @NotBlank content, Long categoryId(nullable), String thumbnailUrl(nullable).

8. dto/request/UpdatePostRequest.java - record: String title(nullable), String content(nullable), Long categoryId(nullable), String thumbnailUrl(nullable). 모두 Optional (부분 수정).

9. dto/request/PostSearchRequest.java - record: String keyword, Long categoryId, String sort(기본 "LATEST"), int page(기본0), int size(기본20).

10. dto/response/PostDetailResponse.java - record: Long id, UserSummaryResponse author, String categoryName, String categorySlug, String title, String content, int viewCount, int voteCount, int commentCount, boolean isPinned, boolean isVoted, boolean isBookmarked, LocalDateTime createdAt, LocalDateTime updatedAt.

11. dto/response/PostListResponse.java - record: Long id, UserSummaryResponse author, String categoryName, String categorySlug, String title, String summary, String thumbnailUrl, int viewCount, int voteCount, int commentCount, LocalDateTime createdAt.

12. mapper/PostMapper.java - MapStruct @Mapper(componentModel="spring", uses={UserMapper.class}).
```

---

## Task 2.2: Post Service & Controller (백엔드)

```
게시글 Service와 Controller를 구현해줘.

경로: apps/api/src/main/java/com/community/domain/post/

[생성할 파일]

1. service/PostService.java
@Service @RequiredArgsConstructor @Slf4j.
- createPost(Long userId, CreatePostRequest): PostDetailResponse
  → User 조회, Category 조회(있으면), Post 빌드&저장, content에서 summary 추출(첫 300자), 매퍼로 변환
- getPost(Long postId, Long userId): PostDetailResponse
  → findByIdAndIsDeletedFalse 아니면 POST_NOT_FOUND, 조회수 증가, 추천/북마크 여부는 userId가 있으면 조회(없으면 false)
- getPosts(PostSearchRequest): PageResponse<PostListResponse>
  → PostRepositoryCustom.search 사용, 매퍼 변환, PageResponse.of()
- updatePost(Long postId, Long userId, UpdatePostRequest): PostDetailResponse
  → 게시글 조회, 작성자 확인(아니면 UNAUTHORIZED_MODIFICATION), 변경된 필드만 업데이트
- deletePost(Long postId, Long userId): void
  → 게시글 조회, 작성자 또는 ADMIN 확인, softDelete()
읽기 메서드: @Transactional(readOnly = true), 쓰기 메서드: @Transactional.
각 메서드에 JavaDoc + 단계별 인라인 주석.

2. controller/PostController.java
@RestController @RequestMapping("/api/v1/posts") @RequiredArgsConstructor @Tag(name="게시글").
- GET / → getPosts(@ModelAttribute PostSearchRequest). Public.
- POST / → createPost(@AuthenticationPrincipal Long userId, @Valid @RequestBody CreatePostRequest).
- GET /{id} → getPost(@PathVariable Long id, @AuthenticationPrincipal(required=false) Long userId). Public.
- PATCH /{id} → updatePost(@PathVariable Long id, @AuthenticationPrincipal Long userId, @Valid @RequestBody UpdatePostRequest).
- DELETE /{id} → deletePost(@PathVariable Long id, @AuthenticationPrincipal Long userId).
모든 메서드: @Operation 어노테이션 + 한국어 JavaDoc.

추가: controller/CategoryController.java
@RestController @RequestMapping("/api/v1/categories").
- GET / → 활성 카테고리 목록 반환 (public)
```

---

## Task 2.3: 프론트엔드 게시글 API & 훅

```
프론트엔드 게시글 API 호출과 TanStack Query 훅을 구현해줘.

경로: apps/web/src/features/post/

[생성할 파일]

1. shared/types/post.ts (shared에 생성)
interface Post { id: number; author: UserSummary; categoryName: string | null; categorySlug: string | null; title: string; content: string; viewCount: number; voteCount: number; commentCount: number; isPinned: boolean; isVoted: boolean; isBookmarked: boolean; createdAt: string; updatedAt: string; }
interface PostListItem { id: number; author: UserSummary; categoryName: string | null; categorySlug: string | null; title: string; summary: string | null; thumbnailUrl: string | null; viewCount: number; voteCount: number; commentCount: number; createdAt: string; }
interface CreatePostPayload { title: string; content: string; categoryId?: number; thumbnailUrl?: string; }
interface UpdatePostPayload { title?: string; content?: string; categoryId?: number; thumbnailUrl?: string; }
interface PostSearchParams { keyword?: string; categoryId?: number; sort?: 'LATEST' | 'POPULAR'; page?: number; size?: number; }
interface Category { id: number; name: string; slug: string; description: string | null; }

2. features/post/api/postApi.ts
- getPosts(params: PostSearchParams): axiosInstance.get('/posts', {params}) → PageResponse<PostListItem>
- getPost(id: number): axiosInstance.get(`/posts/${id}`) → Post
- createPost(data: CreatePostPayload): axiosInstance.post('/posts', data) → Post
- updatePost(id: number, data: UpdatePostPayload): axiosInstance.patch(`/posts/${id}`, data) → Post
- deletePost(id: number): axiosInstance.delete(`/posts/${id}`) → void
- getCategories(): axiosInstance.get('/categories') → Category[]
모든 함수에서 extractData 사용. 한국어 주석.

3. features/post/hooks/usePosts.ts
useInfiniteQuery로 무한 스크롤:
- queryKey: ['posts', { categoryId, sort }]
- queryFn: ({ pageParam = 0 }) => getPosts({ ...params, page: pageParam })
- getNextPageParam: (lastPage) => lastPage.hasNext ? lastPage.page + 1 : undefined
- 한국어 주석: "게시글 목록을 무한 스크롤로 로딩하는 훅. 카테고리 필터와 정렬 지원"

useCategories(): useQuery({ queryKey: ['categories'], queryFn: getCategories, staleTime: 1000*60*60 })
한국어 주석: "카테고리 목록 조회. 1시간 캐시 (거의 변하지 않는 데이터)"

4. features/post/hooks/usePost.ts
useQuery: queryKey ['post', id], queryFn: () => getPost(id), enabled: !!id
한국어 주석: "단일 게시글 상세 조회"

5. features/post/hooks/usePostMutation.ts
- useCreatePost(): useMutation({ mutationFn: createPost, onSuccess: () => { queryClient.invalidateQueries({queryKey:['posts']}); navigate(`/posts/${data.id}`) } })
- useUpdatePost(): useMutation, 성공 시 ['posts']와 ['post', id] 캐시 무효화
- useDeletePost(): useMutation, 성공 시 navigate('/') 및 캐시 무효화
각 훅에 한국어 JSDoc과 사용 예시.

6. features/post/index.ts - barrel export
```

---

## Task 2.4: 게시글 목록 UI (무한 스크롤)

```
게시글 목록 페이지를 구현해줘.

경로: apps/web/src/features/post/

토스 앱 스타일: 깨끗한 카드 리스트, 카테고리 칩 가로 스크롤, 부드러운 무한 스크롤. 모바일 퍼스트로 한 줄에 카드 1개.

[생성할 파일]

1. shared/hooks/useInfiniteScroll.ts
IntersectionObserver 기반. params: { hasNextPage, fetchNextPage, isFetchingNextPage }. ref를 반환하여 감지 대상 요소에 연결. 한국어 주석.

2. shared/components/feedback/LoadingSpinner.tsx
MUI CircularProgress 래퍼. props: size('sm'|'md'|'lg'). 중앙 정렬.

3. shared/components/feedback/EmptyState.tsx
아이콘 + 메시지 + 선택적 액션 버튼. props: icon(ReactNode), message(string), action?(ReactNode). 중앙 정렬.

4. features/post/components/CategoryFilter.tsx
- useCategories 훅으로 카테고리 목록 로딩
- MUI Chip들을 가로 스크롤 (flexWrap nowrap, overflowX auto, scrollbar 숨김)
- "전체" 칩 + 카테고리별 칩
- 선택된 칩: color="primary" variant="filled", 미선택: variant="outlined"
- props: selectedCategoryId, onSelect(categoryId | null)
- 토스 스타일: 좌우 패딩 tokens.spacing.page, 칩 간격 tokens.spacing.sm

5. features/post/components/PostCard.tsx
MUI Card 기반:
- 상단: UserAvatar(작은) + 닉네임 + "·" + TimeAgo
- 카테고리 칩 (있으면)
- 제목: Typography variant h6, 1줄 ellipsis
- 요약: Typography variant body2, 2줄 ellipsis (line-clamp 2)
- 썸네일 (있으면): CardMedia, 높이 180px, 둥근 모서리
- 하단: 추천수(Heart아이콘) + 댓글수(MessageCircle아이콘) + 조회수(Eye아이콘)
- 클릭: navigate to /posts/{id}
- Framer Motion: whileTap={{ scale: 0.98 }} (토스 스타일 터치 피드백)
- 한국어 주석

6. features/post/components/PostList.tsx
- usePosts 훅 사용
- 로딩 중: 스켈레톤 카드 3개 (MUI Skeleton)
- 데이터 있음: PostCard 목록, 카드 간 간격 tokens.spacing.sm
- 빈 상태: EmptyState "아직 게시글이 없습니다"
- 무한 스크롤: 목록 끝에 ref 요소 배치 (useInfiniteScroll)
- 추가 로딩 중: 하단에 LoadingSpinner

7. features/post/pages/PostListPage.tsx
- 상단: CategoryFilter (position sticky, top 56px, 배경 surface, zIndex 1)
- 정렬 토글: "최신순" | "인기순" (MUI ToggleButtonGroup 또는 Tabs)
- PostList (selectedCategoryId, sort 전달)
- 토스 스타일: 배경 tokens.color.background, 카드들은 surface

router.tsx에서 / 경로에 PostListPage 연결하도록 수정.
```

---

## Task 2.5: 게시글 상세 UI

```
게시글 상세 페이지를 구현해줘.

경로: apps/web/src/features/post/

[생성할 파일]

1. shared/components/common/TimeAgo.tsx
- dayjs relativeTime 플러그인 사용 (dayjs.extend(relativeTime), locale 'ko')
- 1분 미만: "방금 전", 1시간 미만: "N분 전", 24시간 미만: "N시간 전", 7일 미만: "N일 전", 그 이후: "YYYY.MM.DD"
- props: date(string)
- 한국어 주석

2. shared/components/common/VoteButton.tsx
- props: targetType('POST'|'COMMENT'), targetId(number), voteCount(number), isVoted(boolean), onToggle()
- lucide-react Heart 아이콘 + 카운트
- isVoted 시: 아이콘 fill primary, 카운트 primary 색상
- Framer Motion: 추천 시 scale 애니메이션 (1 → 1.2 → 1)
- 미로그인 시: 클릭하면 로그인 페이지로 이동
- 한국어 주석

3. features/post/components/PostDetail.tsx
- usePost 훅으로 데이터 로딩
- 작성자 정보: UserAvatar + 닉네임 + TimeAgo
- 카테고리 칩
- 제목: Typography variant h5
- 본문: dangerouslySetInnerHTML로 HTML 렌더링 (DOMPurify 권장이지만 일단 기본 렌더링)
- 하단 액션바: VoteButton + BookmarkButton(placeholder) + 공유 버튼
- 작성자 본인이면: 더보기 메뉴 (MUI IconButton + Menu) → 수정, 삭제
- 관리자면: 더보기 메뉴에 "삭제" 추가
- 삭제 시: MUI Dialog로 확인 후 useDeletePost
- 수정 시: navigate to /posts/{id}/edit

4. features/post/pages/PostDetailPage.tsx
- URL 파라미터에서 id 추출 (useParams)
- 로딩: 스켈레톤 (제목, 본문, 아바타 영역)
- 에러: 404 처리 → "게시글을 찾을 수 없습니다" + 홈으로 돌아가기 버튼
- PostDetail 컴포넌트 렌더링
- 하단: 댓글 영역 (Phase 3에서 구현, 지금은 <Box>댓글 영역 (준비 중)</Box>)

router.tsx에서 /posts/:id 경로 연결.
```

---

## Task 2.6: 게시글 에디터

```
게시글 작성/수정 에디터를 구현해줘.

경로: apps/web/src/features/post/

package.json에 추가 의존성:
@tiptap/react @tiptap/starter-kit @tiptap/extension-placeholder @tiptap/extension-image @tiptap/extension-link @tiptap/extension-underline

[생성할 파일]

1. features/post/components/PostEditor.tsx
TipTap 에디터 래퍼:
- useEditor with extensions: StarterKit, Placeholder("내용을 입력하세요..."), Image, Link, Underline
- 툴바 (MUI IconButton 가로 배치):
  Bold, Italic, Underline, Heading2, Heading3, BulletList, OrderedList, Blockquote, Code, Link, Image
- 각 버튼: 활성 시 primary 배경색, lucide-react 아이콘 사용
- Image 버튼: 파일 input 트리거 (Phase 5에서 실제 업로드 구현, 지금은 URL 프롬프트)
- 에디터 스타일: min-height 300px, border 1px tokens.color.border, borderRadius tokens.radius.md, padding tokens.spacing.lg
- 한국어 주석: TipTap 설정의 각 옵션 설명

2. features/post/pages/PostEditorPage.tsx
- URL에 :id가 있으면 수정 모드, 없으면 작성 모드
- 수정 모드: usePost로 기존 데이터 로딩 → 폼에 prefill
- 폼 구성:
  * 카테고리 선택: MUI Select (useCategories)
  * 제목 입력: MUI TextField (placeholder "제목을 입력하세요")
  * 본문: PostEditor 컴포넌트
- 하단 버튼: "게시하기" 또는 "수정하기" (useCreatePost / useUpdatePost)
- 뒤로가기: 작성 중인 내용이 있으면 MUI Dialog로 "작성을 취소하시겠습니까?" 확인
- 제출 시 content에서 첫 300자를 summary로 추출 (HTML 태그 제거 후)

router.tsx에서 /posts/new, /posts/:id/edit 경로 연결 (AuthGuard).
```

---

# ═══════════════════════════════════════════
# Phase 3: 댓글 & 추천 (3 Tasks)
# 목표: 댓글 CRUD, 대댓글, 추천 토글
# 완료 기준: 게시글에 댓글 작성 → 대댓글 → 추천 토글
#
# 검증 기준 (Phase 완료 시):
# - [ ] 댓글 트리 구조 API 응답 확인
# - [ ] 추천 토글 → vote_count 증감 확인
# - [ ] 프론트엔드 댓글 UI 렌더링 + VoteButton 동작
# - [ ] `make verify` → 전체 통과
# ═══════════════════════════════════════════

---

## Task 3.1: Comment 엔티티, Service, Controller (백엔드)

```
댓글(Comment) 도메인 전체를 구현해줘 (엔티티 + 리포지토리 + 서비스 + 컨트롤러 + DTO + 매퍼).

경로: apps/api/src/main/java/com/community/domain/comment/

entity/Comment.java: @Entity, BaseTimeEntity 상속. 필드: id, @ManyToOne(LAZY) post, @ManyToOne(LAZY) author, @ManyToOne(LAZY) @JoinColumn(name="parent_id") Comment parent, @Column(columnDefinition="TEXT") content, int voteCount, boolean isDeleted. parent가 null이면 최상위 댓글, 있으면 대댓글.

repository/CommentRepository.java: findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(Long postId).

dto/request/CreateCommentRequest.java: record @NotBlank content, Long parentId(nullable).
dto/request/UpdateCommentRequest.java: record @NotBlank content.
dto/response/CommentResponse.java: record Long id, UserSummaryResponse author, String content, int voteCount, boolean isVoted, boolean isDeleted, LocalDateTime createdAt, List<CommentResponse> children.

service/CommentService.java:
- createComment(Long postId, Long userId, CreateCommentRequest): 게시글 존재 확인, parentId 있으면 부모 댓글 확인, Comment 저장, post.increaseCommentCount().
- updateComment(Long commentId, Long userId, UpdateCommentRequest): 작성자 확인 후 수정.
- deleteComment(Long commentId, Long userId): 작성자 또는 ADMIN 확인, softDelete(), post.decreaseCommentCount().
- getCommentsByPost(Long postId, Long userId): 모든 댓글 조회 → 트리 구조로 변환 (parent가 null인 것이 루트, parent_id로 children 매핑). 트리 변환 로직에 단계별 주석.

controller/CommentController.java:
- GET /api/v1/posts/{postId}/comments → 댓글 트리 (public)
- POST /api/v1/posts/{postId}/comments → 댓글 작성 (인증)
- PATCH /api/v1/comments/{id} → 댓글 수정 (작성자)
- DELETE /api/v1/comments/{id} → 댓글 삭제 (작성자/ADMIN)
```

---

## Task 3.2: Vote 엔티티 & Service (백엔드)

```
추천(Vote) 도메인 전체를 구현해줘.

경로: apps/api/src/main/java/com/community/domain/vote/

entity/Vote.java: @Entity @Table(uniqueConstraints = @UniqueConstraint(columns={"user_id","target_type","target_id"})). 필드: id, @ManyToOne(LAZY) user, String targetType, Long targetId, createdAt.

repository/VoteRepository.java: Optional<Vote> findByUserIdAndTargetTypeAndTargetId(...); boolean existsByUserIdAndTargetTypeAndTargetId(...); long countByTargetTypeAndTargetId(...).

dto/request/VoteRequest.java: record @NotBlank String targetType, @NotNull Long targetId.
dto/response/VoteResponse.java: record boolean isVoted, int voteCount.

service/VoteService.java:
toggleVote(Long userId, VoteRequest): 이미 추천했으면 삭제 + 카운트 감소, 안 했으면 추가 + 카운트 증가.
targetType이 "POST"면 Post의 voteCount 업데이트, "COMMENT"면 Comment의 voteCount 업데이트.
isVoted(Long userId, String targetType, Long targetId): 추천 여부 확인.
// TODO 주석: "MSA 분리 시 NotificationService.createNotification 이벤트 발행으로 전환"

controller/VoteController.java:
POST /api/v1/votes → toggleVote (인증 필요)
```

---

## Task 3.3: 프론트엔드 댓글 UI

```
댓글 기능의 프론트엔드를 구현해줘.

경로: apps/web/src/features/comment/

[생성할 파일]

1. shared/types/comment.ts: Comment interface (id, author:UserSummary, content, voteCount, isVoted, isDeleted, createdAt, children:Comment[]).

2. features/comment/api/commentApi.ts: getComments(postId), createComment(postId, data), updateComment(id, data), deleteComment(id).

3. features/comment/hooks/useComments.ts: useQuery(postId) + useCreateComment, useUpdateComment, useDeleteComment (각 mutation 성공 시 ['comments', postId] 캐시 무효화).

4. features/comment/components/CommentForm.tsx:
- MUI TextField(multiline, 2줄) + 제출 Button
- 답글 모드면: "OOO님에게 답글" 표시 + 취소 버튼
- props: postId, parentId?(답글용), onCancel?
- 한국어 주석

5. features/comment/components/CommentItem.tsx:
- UserAvatar + 닉네임 + TimeAgo
- 본문 텍스트
- VoteButton + "답글" 버튼
- 작성자면: 수정/삭제 메뉴 (IconButton + Menu)
- 삭제된 댓글: "삭제된 댓글입니다" 회색 텍스트
- "답글" 클릭 시: 바로 아래에 CommentForm 토글 (답글 모드)
- 대댓글(children): 왼쪽 paddingLeft 40px + 얇은 borderLeft

6. features/comment/components/CommentList.tsx:
- useComments 훅 사용
- 댓글 없으면: "첫 댓글을 작성해보세요"
- 댓글 목록: CommentItem 재귀 렌더링 (children 포함)
- 하단 고정: CommentForm (새 댓글 작성)
- 한국어 주석

7. features/comment/index.ts

features/post/pages/PostDetailPage.tsx 수정: 하단에 CommentList 컴포넌트 연결.
또한 VoteButton을 PostDetail에 실제 연동 (toggleVote API 호출).
```

---

# ═══════════════════════════════════════════
# Phase 4: 프로필 & 관리자 (4 Tasks)
# 목표: 프로필 조회/수정, 관리자 대시보드, 사용자 관리
# 완료 기준: 프로필 수정 + 관리자 대시보드 접근
#
# 검증 기준 (Phase 완료 시):
# - [ ] PATCH /api/v1/users/me → 닉네임 변경 확인
# - [ ] GET /api/v1/admin/dashboard → 통계 데이터 반환
# - [ ] 관리자 UI 페이지 렌더링
# - [ ] `make verify` → 전체 통과
# ═══════════════════════════════════════════

---

## Task 4.1: 사용자 프로필 API (백엔드)

```
사용자 프로필 관련 API를 구현해줘.

경로: apps/api/src/main/java/com/community/domain/user/

service/UserService.java:
- getMyProfile(Long userId): UserProfileResponse
- updateMyProfile(Long userId, UpdateProfileRequest): UserProfileResponse (닉네임 변경 시 중복 확인)
- getPublicProfile(Long userId): UserProfileResponse (타인 프로필)

dto/request/UpdateProfileRequest.java: record String nickname(@Size 2~20), String bio(@Size max 300).

controller/UserController.java:
- GET /api/v1/users/me → 내 프로필 (인증)
- PATCH /api/v1/users/me → 프로필 수정 (인증)
- GET /api/v1/users/{id} → 공개 프로필 (public)
```

---

## Task 4.2: 프론트엔드 프로필 UI

```
사용자 프로필 페이지를 구현해줘.

경로: apps/web/src/features/user/

api/userApi.ts, hooks/useProfile.ts(useMyProfile, useUpdateProfile, useUserProfile).

components/ProfileCard.tsx: 큰 UserAvatar + 닉네임 + 자기소개 + "프로필 수정" 버튼. 토스 스타일 중앙 정렬.
components/ProfileEditForm.tsx: react-hook-form으로 닉네임, 자기소개 수정. MUI Dialog 형태.
pages/ProfilePage.tsx: 상단 ProfileCard + 탭 3개 (내 글 | 내 댓글 | 북마크). 내 글 탭: usePosts에 authorId 필터. 나머지는 Phase 6에서 구현 (placeholder).

router.tsx에서 /profile 경로 연결.
```

---

## Task 4.3: 관리자 API (백엔드)

```
관리자 API를 구현해줘.

경로: apps/api/src/main/java/com/community/domain/admin/

service/AdminService.java:
- getDashboard(): DashboardResponse (총 사용자, 총 게시글, 오늘 가입 수, 오늘 게시글 수 - JPQL COUNT 쿼리)
- getUsers(Pageable, String keyword): PageResponse<UserManageResponse> (검색: 이메일 또는 닉네임)
- changeUserRole(Long userId, UserRole newRole)
- banUser(Long userId, boolean ban): isActive 토글
- forceDeletePost(Long postId): 관리자 강제 삭제

controller/AdminController.java: 모든 엔드포인트에 @PreAuthorize("hasRole('ADMIN')").
- GET /api/v1/admin/dashboard
- GET /api/v1/admin/users?keyword=&page=&size=
- PATCH /api/v1/admin/users/{id}/role (body: {role: "ADMIN"})
- PATCH /api/v1/admin/users/{id}/ban (body: {ban: true})
- DELETE /api/v1/admin/posts/{id}

dto/response/DashboardResponse.java: record long totalUsers, long totalPosts, long todaySignups, long todayPosts.
dto/response/UserManageResponse.java: record Long id, String email, String nickname, UserRole role, boolean isActive, LocalDateTime createdAt, LocalDateTime lastLoginAt.
```

---

## Task 4.4: 프론트엔드 관리자 UI

```
관리자 대시보드와 관리 페이지를 구현해줘.

경로: apps/web/src/features/admin/

api/adminApi.ts, hooks/useAdmin.ts.

components/Dashboard.tsx:
- 4개 통계 카드 (MUI Card): 총 사용자, 총 게시글, 오늘 가입, 오늘 게시글
- 각 카드: 아이콘 + 숫자(큰 폰트) + 라벨(작은 폰트)
- Grid로 2열 배치 (모바일 1열)

components/UserManageTable.tsx:
- MUI Table (또는 데이터 리스트)
- 컬럼: 닉네임, 이메일, 역할, 상태, 가입일
- 액션: 역할 변경(Select), 정지/해제(Switch), 검색(TextField)
- 페이징: MUI Pagination

pages/AdminDashboardPage.tsx: Dashboard + 최근 신고 목록 (placeholder)
pages/AdminUsersPage.tsx: UserManageTable

AdminLayout에 연결.
```

---

# ═══════════════════════════════════════════
# Phase 5: 알림 & 미디어 (4 Tasks)
# 목표: 인앱 알림, 이미지 업로드, MinIO 연동
# 완료 기준: 댓글 작성 시 알림 생성 + 이미지 업로드
#
# 검증 기준 (Phase 완료 시):
# - [ ] 댓글 작성 → 게시글 작성자에게 알림 생성 확인
# - [ ] POST /api/v1/media/upload → MinIO에 파일 저장 확인
# - [ ] 프론트엔드 알림 목록 + 이미지 업로드 UI
# - [ ] `make verify` → 전체 통과
# ═══════════════════════════════════════════

---

## Task 5.1: 알림 시스템 (백엔드)

```
알림(Notification) 도메인 전체를 구현해줘.

경로: apps/api/src/main/java/com/community/domain/notification/

entity/Notification.java, entity/NotificationType.java(enum: COMMENT, VOTE, FOLLOW, ADMIN_NOTICE).
repository/NotificationRepository.java: findByRecipientIdOrderByCreatedAtDesc(Long, Pageable), countByRecipientIdAndIsReadFalse(Long).

service/NotificationService.java:
- createNotification(Long recipientId, Long actorId, NotificationType, String targetType, Long targetId, String message): 본인에게는 알림 생성 안 함 (recipientId == actorId이면 스킵). // TODO: MSA 분리 시 이벤트 기반으로 전환
- getMyNotifications(Long userId, Pageable): PageResponse
- markAsRead(Long notificationId, Long userId): 본인 알림만 읽음 처리
- markAllAsRead(Long userId)
- getUnreadCount(Long userId): long

controller/NotificationController.java: GET, PATCH /{id}/read, PATCH /read-all, GET /unread-count (모두 인증 필요).

기존 서비스 수정:
- CommentService.createComment에서: notificationService.createNotification(게시글 작성자에게, COMMENT, ...)
- VoteService.toggleVote에서: 추천 추가 시 notificationService.createNotification(대상 작성자에게, VOTE, ...)
```

---

## Task 5.2: 프론트엔드 알림 UI

```
알림 기능 프론트엔드를 구현해줘.

경로: apps/web/src/features/notification/

api/notificationApi.ts, hooks/useNotifications.ts (목록 useInfiniteQuery, unreadCount useQuery refetchInterval 30초).

components/NotificationBadge.tsx: unreadCount > 0이면 MUI Badge 표시. BottomTabBar와 TopAppBar에 연동.
components/NotificationItem.tsx: 타입별 아이콘(COMMENT→MessageCircle, VOTE→Heart), 메시지, TimeAgo, 읽음/안읽음 배경색 차이.
components/NotificationList.tsx: 무한 스크롤 목록. 상단 "모두 읽음" 버튼.
pages/NotificationPage.tsx: NotificationList + 빈 상태("새로운 알림이 없습니다").

BottomTabBar.tsx 수정: 알림 탭에 NotificationBadge 추가.
router.tsx에서 /notifications 연결.
```

---

## Task 5.3: 미디어 업로드 (백엔드)

```
이미지/파일 업로드 기능을 구현해줘.

경로: apps/api/src/main/java/com/community/domain/media/

global/config/MinioConfig.java: MinioClient 빈 등록. @Value로 endpoint, accessKey, secretKey, bucket 주입.

entity/Media.java, repository/MediaRepository.java.

service/ImageResizeService.java: Thumbnailator 라이브러리. 썸네일(400x400 크롭), 본문 이미지(최대 너비 1200px 리사이즈). build.gradle.kts에 net.coobird:thumbnailator:0.4.20 추가.

service/MediaService.java:
- uploadImage(MultipartFile, Long userId): 확장자 검증(jpg,png,gif,webp), MIME 타입 검증, 크기 검증(10MB), UUID 파일명 생성, MinIO에 원본 업로드, 이미지면 썸네일 생성 후 업로드, Media 엔티티 저장, 응답(id, url, thumbnailUrl).

controller/MediaController.java:
POST /api/v1/media/upload (multipart/form-data, 인증 필요) → { id, url, thumbnailUrl }
```

---

## Task 5.4: 프론트엔드 이미지 업로드

```
이미지 업로드 UI를 구현해줘.

경로: apps/web/src/

features/user/components/AvatarUploader.tsx:
- 프로필 이미지 업로드: 아바타 클릭 → 파일 선택 → 미리보기 → 업로드
- MUI Avatar 위에 카메라 아이콘 오버레이
- 업로드 진행 중: Avatar에 CircularProgress 오버레이

PostEditor.tsx 수정:
- 이미지 버튼: 파일 선택 → MediaService 업로드 → 응답 URL을 에디터에 삽입
- TipTap Image extension으로 에디터에 <img> 삽입

shared/api/mediaApi.ts: uploadImage(file: File) → FormData로 POST /media/upload
```

---

# ═══════════════════════════════════════════
# Phase 6: 완성 (3 Tasks)
# 목표: 북마크, 신고, 홈 화면 구성
# 완료 기준: 홈 화면에서 인기 글 + 최신 글 표시
#
# 검증 기준 (Phase 완료 시):
# - [ ] 북마크 토글 API + 내 북마크 목록
# - [ ] 신고 접수 + 관리자 신고 처리
# - [ ] 홈 화면: 인기 게시글 가로 스크롤 + 최신 피드
# - [ ] `make verify` → 전체 통과
# ═══════════════════════════════════════════

---

## Task 6.1: 북마크 (풀스택)

```
북마크 기능을 풀스택으로 구현해줘.

백엔드 (apps/api): domain/bookmark/ 전체 (Entity, Repository, Service - 토글 + 목록, Controller - POST /api/v1/bookmarks 토글, GET /api/v1/bookmarks 목록).

프론트엔드 (apps/web): shared/components/common/BookmarkButton.tsx (Bookmark아이콘, 토글, isBookmarked 상태). features/post/components/PostDetail.tsx에 BookmarkButton 추가. features/user/pages/ProfilePage.tsx의 "북마크" 탭에 내 북마크 목록 연동.
```

---

## Task 6.2: 신고 (풀스택)

```
신고 기능을 풀스택으로 구현해줘.

백엔드: domain/report/ 전체 (Entity, Repository, Service, Controller POST /api/v1/reports). AdminService에 getReports(Pageable), resolveReport(Long id, String status) 추가. AdminController에 GET /api/v1/admin/reports, PATCH /api/v1/admin/reports/{id} 추가.

프론트엔드: 게시글/댓글 더보기 메뉴에 "신고" 옵션 추가. 신고 다이얼로그 (MUI Dialog): 사유 선택 라디오(스팸, 욕설, 부적절, 저작권, 기타) + 상세 설명 TextField + 제출 버튼. features/admin/components/ReportList.tsx: 신고 목록 테이블 + 처리/무시 액션.
```

---

## Task 6.3: 홈 화면 구성

```
홈 화면을 완성해줘.

경로: apps/web/src/features/home/

components/HotPosts.tsx:
- 인기 게시글 TOP 5 (voteCount 상위, usePosts sort=POPULAR size=5)
- 가로 스크롤 작은 카드들 (MUI Card, width 280px)
- 스크롤 snap (scroll-snap-type: x mandatory)
- 토스 스타일: 섹션 타이틀 "인기 게시글" + "더보기" 링크

pages/HomePage.tsx:
- 상단: 인사말 ("안녕하세요, {닉네임}님!" 로그인 시, 아니면 "커뮤니티에 오신 걸 환영합니다!")
- HotPosts 가로 스크롤
- 구분선
- 최신 게시글 피드 (PostList 재사용, sort LATEST)
- Framer Motion: staggered fade-in (각 섹션이 순차적으로 나타남)

router.tsx에서 / 경로를 HomePage로 변경.
```

---

# ═══════════════════════════════════════════
# Phase 7: 최적화 & 배포 (4 Tasks)
# 목표: 성능 최적화, Docker 프로덕션, CI/CD, 모니터링
# 완료 기준: GitHub Actions CI 통과 + Grafana 대시보드 동작
#
# 검증 기준 (Phase 완료 시):
# - [ ] Redis 캐시 동작 확인 (카테고리, 대시보드)
# - [ ] Docker 프로덕션 빌드 성공
# - [ ] CI/CD 파이프라인 실행 (GitHub Actions)
# - [ ] Grafana 대시보드에서 메트릭 표시
# - [ ] `make verify` → 전체 통과
# ═══════════════════════════════════════════

---

## Task 7.1: 성능 최적화

```
프론트엔드와 백엔드의 성능을 최적화해줘.

[프론트엔드]
1. React.memo: PostCard, CommentItem, NotificationItem (목록 항목 리렌더링 방지)
2. 이미지 lazy loading: PostCard 썸네일에 loading="lazy"
3. 코드 스플리팅 확인: router.tsx의 모든 페이지가 React.lazy인지
4. PWA 기본 설정: manifest.json (name, short_name, start_url, display standalone, theme_color, icons)

[백엔드]
5. Redis 캐시:
   - @Cacheable("categories") on CategoryRepository.findAll → TTL 1시간
   - @Cacheable("dashboard") on AdminService.getDashboard → TTL 5분
   - 각 캐시에 @CacheEvict 설정 (데이터 변경 시)
6. N+1 쿼리 점검:
   - PostService.getPosts: author fetch join 확인
   - CommentService.getCommentsByPost: author fetch join 확인
7. 조회수 증가 비동기화: @Async + @EventListener 패턴으로 조회수 증가를 비동기 처리 (응답 시간 단축)

한국어 주석으로 "왜 이 최적화를 적용했는지" 설명.
```

---

## Task 7.2: Docker 프로덕션 배포

```
프로덕션 배포를 위한 Docker 설정을 만들어줘.

[생성할 파일]

1. apps/web/Dockerfile (Multi-stage)
   Stage 1: node:22-alpine로 빌드 (npm ci, npm run build)
   Stage 2: nginx:1.27-alpine로 서빙 (빌드 결과물 /usr/share/nginx/html에 복사)

2. apps/api/Dockerfile (Multi-stage)
   Stage 1: eclipse-temurin:21-jdk-alpine로 빌드 (./gradlew bootJar)
   Stage 2: eclipse-temurin:21-jre-alpine로 실행 (java -jar app.jar)
   JVM 옵션: -XX:+UseZGC -XX:MaxRAMPercentage=75

3. infra/docker/nginx/nginx.conf
   - React SPA: try_files $uri $uri/ /index.html
   - /api/ → proxy_pass http://api:8080
   - gzip on (js, css, json, html, svg)
   - 보안 헤더: X-Frame-Options DENY, X-Content-Type-Options nosniff, CSP, HSTS
   - 캐시: 정적 파일 30일, index.html no-cache

4. docker-compose.prod.yml
   서비스: nginx(빌드), api(빌드), postgres, redis, minio, prometheus, grafana, loki
   .env.prod에서 환경변수 로드
   api에 healthcheck: wget -q --spider http://localhost:8080/actuator/health
```

---

## Task 7.3: GitHub Actions CI/CD

```
GitHub Actions CI/CD 파이프라인을 구성해줘.

경로: .github/workflows/

1. ci.yml (on: push, pull_request to main)
jobs:
  frontend:
    runs-on: ubuntu-latest
    steps: checkout → setup node 22 → npm ci → npm run lint → npx tsc --noEmit → npm test → npm run build

  backend:
    runs-on: ubuntu-latest
    services: postgres(18), redis(7.4)
    steps: checkout → setup java 21 → gradle test → gradle build

  둘 다 병렬 실행.

2. deploy.yml (on: push to main, ci 성공 후)
jobs:
  deploy:
    steps: checkout → Docker 이미지 빌드 → SSH로 서버 접속 → docker-compose.prod.yml pull & up → 헬스체크 확인

각 스텝에 한국어 주석.
```

---

## Task 7.4: 모니터링 대시보드

```
Grafana 모니터링 대시보드를 설정해줘.

경로: infra/monitoring/

1. grafana/dashboards/spring-boot.json
Grafana 대시보드 JSON:
- 패널: JVM 메모리 사용량, CPU 사용률, 활성 스레드 수, HTTP 요청 수(초당), HTTP 응답 시간(p50/p95/p99), HTTP 에러율(4xx/5xx), DB 커넥션 풀 사용량
- 각 패널에 title 한국어로

2. grafana/dashboards/application.json
커스텀 비즈니스 메트릭:
- API 엔드포인트별 응답 시간 Top 10
- 에러율 타임라인
- 동시 접속자 수 (활성 세션)

Spring Boot Actuator 설정 확인: management.endpoints.web.exposure.include에 prometheus 포함 확인. micrometer-registry-prometheus 의존성이 starter-actuator에 포함되어 있는지 확인.
```

---

# 📋 전체 태스크 체크리스트

| # | Phase | Task | 설명 |
|---|-------|------|------|
| 1 | 0 | 0.1 | 프로젝트 루트 구조 |
| 2 | 0 | 0.2 | Docker Compose |
| 3 | 0 | 0.3 | React 초기화 |
| 4 | 0 | 0.4 | MUI 테마 |
| 5 | 0 | 0.5 | Spring Boot 초기화 |
| 6 | 0 | 0.6 | Flyway 스키마 |
| 7 | 0 | 0.7 | CLAUDE.md |
| 8 | 1 | 1.1 | User 엔티티 |
| 9 | 1 | 1.2 | Security & JWT |
| 10 | 1 | 1.3 | Auth API |
| 11 | 1 | 1.4 | Axios 설정 |
| 12 | 1 | 1.5 | 인증 스토어 |
| 13 | 1 | 1.6 | 로그인/회원가입 UI |
| 14 | 1 | 1.7 | 라우터 & 레이아웃 |
| 15 | 2 | 2.1 | Post 엔티티 |
| 16 | 2 | 2.2 | Post Service |
| 17 | 2 | 2.3 | Post API 훅 |
| 18 | 2 | 2.4 | 게시글 목록 UI |
| 19 | 2 | 2.5 | 게시글 상세 UI |
| 20 | 2 | 2.6 | 게시글 에디터 |
| 21 | 3 | 3.1 | Comment 전체 |
| 22 | 3 | 3.2 | Vote 전체 |
| 23 | 3 | 3.3 | 댓글 UI |
| 24 | 4 | 4.1 | 프로필 API |
| 25 | 4 | 4.2 | 프로필 UI |
| 26 | 4 | 4.3 | 관리자 API |
| 27 | 4 | 4.4 | 관리자 UI |
| 28 | 5 | 5.1 | 알림 시스템 |
| 29 | 5 | 5.2 | 알림 UI |
| 30 | 5 | 5.3 | 미디어 업로드 |
| 31 | 5 | 5.4 | 이미지 UI |
| 32 | 6 | 6.1 | 북마크 |
| 33 | 6 | 6.2 | 신고 |
| 34 | 6 | 6.3 | 홈 화면 |
| 35 | 7 | 7.1 | 성능 최적화 |
| 36 | 7 | 7.2 | Docker 프로덕션 |
| 37 | 7 | 7.3 | CI/CD |
| 38 | 7 | 7.4 | 모니터링 |
