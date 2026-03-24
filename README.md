# 🌐 커뮤니티 웹앱

사용자가 게시글을 작성하고, 댓글과 추천을 통해 소통하는 **모바일 퍼스트 커뮤니티 웹앱**입니다.
관리자는 동일 화면에서 추가 권한으로 서비스를 관리할 수 있습니다.

## 📋 기술 스택

### 프론트엔드 (모바일 퍼스트 웹앱)

| 구분 | 기술 | 버전 |
|------|------|------|
| 프레임워크 | React + TypeScript | 19.2.x + 5.7.x |
| 빌드 도구 | Vite | 7.3.x |
| UI 프레임워크 | MUI (Material UI) | 7.3.x |
| 상태 관리 | Zustand | 5.x |
| 서버 상태 | TanStack Query | 5.90.x |
| 라우팅 | React Router | 7.x |
| 폼 관리 | React Hook Form + Zod | 7.x + 3.x |
| HTTP 클라이언트 | Axios | 1.8.x |
| 애니메이션 | Framer Motion | 12.x |

### 백엔드

| 구분 | 기술 | 버전 |
|------|------|------|
| 프레임워크 | Spring Boot | 4.0.3 |
| 언어 | Java | 21 LTS |
| 보안 | Spring Security | 7.x |
| ORM | Spring Data JPA + QueryDSL | 4.0.x + 5.1.x |
| DB | PostgreSQL | 18.3 |
| 캐시 | Redis | 7.4.x |
| 마이그레이션 | Flyway | 11.x |
| 빌드 | Gradle (Kotlin DSL) | 8.12.x |

### 인프라

| 구분 | 기술 | 용도 |
|------|------|------|
| 컨테이너 | Docker + Docker Compose | 개발/배포 환경 통일 |
| 리버스 프록시 | Nginx | HTTPS, 정적 파일 서빙 |
| 오브젝트 스토리지 | MinIO | 이미지/파일 저장 (S3 호환) |
| 모니터링 | Grafana + Prometheus + Loki | 메트릭/로그/대시보드 |

## 🚀 빠른 시작

### 1. 프로젝트 클론 & 환경 설정

```bash
git clone <repository-url>
cd gravity

# 환경 변수 설정 (예시값이 설정되어 있으므로 바로 사용 가능)
cp .env.example .env
```

### 2. 개발 인프라 시작 (Docker)

```bash
# PostgreSQL, Redis, MinIO, 모니터링 스택 시작
make dev

# 서비스 상태 확인
docker compose ps
```

### 3. 백엔드 서버 시작

```bash
# Spring Boot 개발 서버 (http://localhost:8080)
make api
```

### 4. 프론트엔드 서버 시작

```bash
# 프론트엔드 의존성 설치 (최초 1회)
cd apps/web && npm install && cd ../..

# Vite 개발 서버 (http://localhost:5173)
make web
```

### 5. 접속

| 서비스 | URL | 설명 |
|--------|-----|------|
| 프론트엔드 | http://localhost:5173 | React 웹앱 |
| 백엔드 API | http://localhost:8080 | REST API |
| Swagger UI | http://localhost:8080/swagger-ui.html | API 문서 |
| MinIO Console | http://localhost:9001 | 파일 스토리지 관리 |
| Grafana | http://localhost:3000 | 모니터링 대시보드 |
| Prometheus | http://localhost:9090 | 메트릭 조회 |

## 📁 디렉토리 구조

```
gravity/
├── apps/
│   ├── web/                    # React 프론트엔드 (Vite 7 + MUI 7)
│   │   └── src/
│   │       ├── app/            # 앱 설정 (라우터, 프로바이더, 테마)
│   │       ├── features/       # 기능별 모듈 (auth, post, comment 등)
│   │       └── shared/         # 공유 컴포넌트, 훅, 유틸
│   └── api/                    # Spring Boot 백엔드
│       └── src/main/java/com/community/
│           ├── global/         # 전역 설정, 보안, 공통 모듈
│           └── domain/         # 도메인별 패키지 (auth, user, post 등)
├── infra/
│   ├── docker/                 # Docker 관련 설정 (Nginx, PostgreSQL)
│   ├── monitoring/             # Grafana, Prometheus 설정
│   └── scripts/                # 초기화 스크립트
├── docs/                       # 프로젝트 문서
├── prompt/                     # AI 에이전트용 설계서, 프롬프트
├── docker-compose.yml          # 개발 환경 Docker Compose
├── Makefile                    # 원클릭 명령어 모음
└── .env.example                # 환경 변수 템플릿
```

## 🛠️ 주요 명령어

```bash
make dev       # 개발 인프라 시작 (Docker)
make down      # 개발 인프라 종료
make logs      # 서비스 로그 확인
make reset     # 인프라 초기화 (데이터 삭제 후 재시작)
make web       # 프론트엔드 개발 서버 시작
make api       # 백엔드 개발 서버 시작
make test      # 전체 테스트 실행
make build     # 프로덕션 빌드
make clean     # 빌드 산출물 정리
```

## 📝 라이선스

Private — 비공개 프로젝트
