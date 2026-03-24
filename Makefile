# ====================================================
# 커뮤니티 웹앱 - Makefile
# 자주 사용하는 명령어를 간편하게 실행하기 위한 원클릭 명령어 모음
# 사용법: make <명령어> (예: make dev)
# ====================================================

.PHONY: dev down logs reset web api test test-web test-api lint lint-api lint-web typecheck verify build build-web build-api clean

# ──────────────────────────────────────────────────────
# Docker Compose (개발 인프라)
# ──────────────────────────────────────────────────────

## 개발 인프라 시작 (PostgreSQL, Redis, MinIO, Prometheus, Grafana, Loki)
## 백그라운드로 실행되며, 최초 실행 시 이미지 다운로드에 시간이 걸릴 수 있음
dev:
	docker compose up -d

## 개발 인프라 종료 (컨테이너만 정지, 데이터는 유지)
down:
	docker compose down

## 모든 서비스의 로그를 실시간으로 확인 (Ctrl+C로 종료)
logs:
	docker compose logs -f

## 개발 인프라 완전 초기화 (볼륨 데이터 삭제 후 재시작)
## ⚠️ 주의: 데이터베이스, Redis, MinIO의 모든 데이터가 삭제됩니다!
reset:
	docker compose down -v && docker compose up -d

# ──────────────────────────────────────────────────────
# 프론트엔드 (React + Vite)
# ──────────────────────────────────────────────────────

## 프론트엔드 개발 서버 시작 (http://localhost:5173)
## Hot Module Replacement(HMR) 활성화 — 코드 수정 시 자동 반영
web:
	cd apps/web && npm run dev

# ──────────────────────────────────────────────────────
# 백엔드 (Spring Boot + Gradle)
# ──────────────────────────────────────────────────────

## 백엔드 개발 서버 시작 (http://localhost:8080)
## dev 프로필로 실행 (로컬 Docker의 PostgreSQL, Redis 연결)
api:
	cd apps/api && ./gradlew bootRun --args='--spring.profiles.active=dev'

# ──────────────────────────────────────────────────────
# 테스트
# ──────────────────────────────────────────────────────

## 프론트엔드 + 백엔드 테스트 모두 실행
test:
	@echo "🧪 프론트엔드 테스트 실행..."
	cd apps/web && npm test -- --run
	@echo ""
	@echo "🧪 백엔드 테스트 실행..."
	cd apps/api && ./gradlew test

## 프론트엔드 테스트만 실행
test-web:
	cd apps/web && npm test -- --run

## 백엔드 테스트만 실행
test-api:
	cd apps/api && ./gradlew test

# ──────────────────────────────────────────────────────
# 린트 & 타입 체크
# ──────────────────────────────────────────────────────

## 백엔드 린트 (컴파일 체크 — QueryDSL Q클래스 생성 포함)
lint-api:
	cd apps/api && ./gradlew compileJava

## 프론트엔드 린트 (ESLint)
lint-web:
	cd apps/web && npx eslint src/ --max-warnings 0

## 프론트엔드 TypeScript 타입 체크
typecheck:
	cd apps/web && npx tsc --noEmit

## 전체 린트 (백엔드 컴파일 + 프론트 ESLint + 타입 체크)
## CI 파이프라인과 동일한 검증을 로컬에서 실행
lint: lint-api lint-web typecheck

## 통합 검증 (린트 + 테스트 모두 실행)
## 태스크 완료 시 이 명령어로 전체 검증
verify: lint test
	@echo ""
	@echo "✅ 전체 검증 통과!"

# ──────────────────────────────────────────────────────
# 빌드
# ──────────────────────────────────────────────────────

## 프론트엔드 + 백엔드 프로덕션 빌드
build:
	@echo "📦 프론트엔드 빌드..."
	cd apps/web && npm run build
	@echo ""
	@echo "📦 백엔드 빌드..."
	cd apps/api && ./gradlew build -x test

## 프론트엔드 빌드만
build-web:
	cd apps/web && npm run build

## 백엔드 빌드만
build-api:
	cd apps/api && ./gradlew build -x test

# ──────────────────────────────────────────────────────
# 유틸리티
# ──────────────────────────────────────────────────────

## 빌드 산출물 정리 (node_modules, build 디렉토리 삭제)
clean:
	rm -rf apps/web/node_modules apps/web/dist
	cd apps/api && ./gradlew clean

