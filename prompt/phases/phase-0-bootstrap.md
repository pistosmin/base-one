# Phase 0: 프로젝트 부트스트랩

> **상태**: ✅ 완료
> **목표**: 개발을 시작할 수 있는 빈 프로젝트 골격 생성
> **완료 기준**: `make dev`로 인프라 기동, 프론트/백 개발 서버 정상 동작

---

## 진행 체크리스트

- [x] Task 0.1: 프로젝트 루트 구조 & 설정 파일 (.gitignore, .env.example, Makefile, README.md)
- [x] Task 0.2: Docker Compose 개발 환경 (PostgreSQL 18, Redis 7.4, MinIO, Prometheus, Grafana, Loki)
- [x] Task 0.3: React 프로젝트 초기화 (React 19.2 + Vite 7.3 + TypeScript 5.7)
- [x] Task 0.4: MUI 테마 & 디자인 토큰 설정 (토스 스타일)
- [x] Task 0.5: Spring Boot 프로젝트 초기화 (Spring Boot 4.0.3 + Java 21 + Gradle)
- [x] Task 0.6: Flyway 초기 마이그레이션 (전체 DB 스키마)
- [x] Task 0.7: CLAUDE.md (AI 어시스턴트 컨텍스트)

## 검증 기준

- [x] `make dev` → Docker 컨테이너 모두 healthy
- [x] `make web` → localhost:5173에서 React 앱 표시
- [x] `make api` → localhost:8080에서 Swagger UI 접근 가능
- [x] `make lint-api` → 컴파일 성공

---

## 구현 이력

> Claude나 AI 에이전트가 이 Phase를 구현할 때, 아래에 계획과 완료 기록을 남깁니다.

### 2026-03-14: Phase 0 초기 구현 완료
- 전체 프로젝트 골격 생성 (apps/web, apps/api, infra/, docs/)
- Docker Compose: 7개 서비스 (postgres, redis, minio, minio-init, prometheus, grafana, loki)
- React + Vite + MUI 테마 설정 완료
- Spring Boot + Gradle + 전역 설정 (JpaConfig, RedisConfig, CorsConfig 등)
- Flyway 마이그레이션 V1~V7 생성
- CLAUDE.md 작성

### 2026-03-23: Claude Code 최적화 적용
- gstack 설치 (.claude/skills/gstack/)
- CLAUDE.md 계층 구조 (루트 + api + web)
- Claude Code hooks 설정 (.claude/settings.json)
- Makefile 검증 명령어 추가 (lint, verify 등)

### 2026-03-26: 아키텍처 개선 적용 (Phase 0+)
- Nginx Reverse Proxy 설정 완료 (docker-compose, default.conf)
- 도메인 결합도 완화를 위한 Spring 이벤트 기반 구성 (DomainEventPublisher)
- GitHub Actions 병렬 검증 CI 파일 생성 (ci.yml)
- Makefile 편의 명령어 추가 (make proxy, make all)