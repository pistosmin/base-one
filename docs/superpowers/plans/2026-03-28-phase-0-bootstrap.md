# Phase 0: 프로젝트 부트스트랩 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 개발을 시작할 수 있는 빈 프로젝트 골격 생성 — `make dev`로 인프라 기동, 프론트/백 개발 서버 정상 동작

**Architecture:** 모노레포 구조 (`apps/web` + `apps/api`). 인프라는 Docker Compose로 일괄 관리. 프론트엔드는 React 19.2 + Vite + MUI 7, 백엔드는 Spring Boot 4.0.3 + Java 25 + Gradle Kotlin DSL. Flyway로 DB 스키마 버전 관리.

**Tech Stack:** React 19.2 + Vite 8.0 + TypeScript 5.9 + MUI 7.3 (프론트) / Spring Boot 4.0.3 + Java 25 + Gradle 8.12 + PostgreSQL 18 + Redis 7.4 + MinIO (백엔드/인프라)

---

## File Structure

### 루트 레벨 신규 파일
```
.gitignore
.env.example
Makefile
docker-compose.yml
infra/scripts/init-minio.sh
infra/docker/nginx/default.conf
infra/monitoring/prometheus/prometheus.yml
infra/monitoring/grafana/provisioning/datasources/datasources.yml
.github/workflows/ci.yml
```

### 백엔드 신규 파일
```
apps/api/build.gradle.kts
apps/api/settings.gradle.kts
apps/api/gradle/wrapper/gradle-wrapper.properties
apps/api/src/main/java/com/community/CommunityApplication.java
apps/api/src/main/java/com/community/global/config/JpaConfig.java
apps/api/src/main/java/com/community/global/config/RedisConfig.java
apps/api/src/main/java/com/community/global/config/CorsConfig.java
apps/api/src/main/java/com/community/global/config/OpenApiConfig.java
apps/api/src/main/java/com/community/global/common/response/ApiResponse.java
apps/api/src/main/java/com/community/global/common/response/ErrorCode.java
apps/api/src/main/java/com/community/global/common/entity/BaseTimeEntity.java
apps/api/src/main/java/com/community/global/common/event/DomainEventPublisher.java
apps/api/src/main/java/com/community/global/exception/BusinessException.java
apps/api/src/main/java/com/community/global/exception/GlobalExceptionHandler.java
apps/api/src/main/resources/application.yml
apps/api/src/main/resources/application-dev.yml
apps/api/src/main/resources/application-prod.yml
apps/api/src/main/resources/db/migration/V1__create_users_table.sql
apps/api/src/main/resources/db/migration/V2__create_categories_table.sql
apps/api/src/main/resources/db/migration/V3__create_posts_table.sql
apps/api/src/main/resources/db/migration/V4__create_comments_table.sql
apps/api/src/main/resources/db/migration/V5__create_votes_bookmarks.sql
apps/api/src/main/resources/db/migration/V6__create_notifications.sql
apps/api/src/main/resources/db/migration/V7__create_supporting_tables.sql
```

### 프론트엔드 신규 파일
```
apps/web/package.json
apps/web/vite.config.ts
apps/web/tsconfig.json
apps/web/tsconfig.app.json
apps/web/index.html
apps/web/src/main.tsx
apps/web/src/app/App.tsx
apps/web/src/app/providers.tsx
apps/web/src/app/theme/tokens.ts
apps/web/src/app/theme/palette.ts
apps/web/src/app/theme/typography.ts
apps/web/src/app/theme/components.ts
apps/web/src/app/theme/index.ts
```

---

## Task 1: 프로젝트 루트 구조

**Files:**
- Create: `.gitignore`
- Create: `.env.example`
- Create: `Makefile`

- [ ] **Step 1: .gitignore 작성**

```gitignore
# Node
node_modules/
dist/
.vite/
*.local

# Java
apps/api/build/
apps/api/.gradle/
apps/api/out/
*.class

# 환경 변수 (절대 커밋 금지)
.env

# IDE
.idea/
*.iml
.vscode/
*.swp

# OS
.DS_Store
Thumbs.db

# Docker 볼륨 데이터
pgdata/
redis-data/
minio-data/
```

- [ ] **Step 2: .env.example 작성**

```bash
# ====================================================
# 환경 변수 예시 파일
# 이 파일을 .env로 복사하고 실제 값을 입력하세요
# cp .env.example .env
# ====================================================

# PostgreSQL
POSTGRES_DB=community
POSTGRES_USER=community
POSTGRES_PASSWORD=community1234
POSTGRES_PORT=5432

# Redis
REDIS_PORT=6379

# MinIO (S3 호환 스토리지)
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin

# JWT 서명 키 (운영: 32자 이상 랜덤 문자열)
JWT_SECRET=community-jwt-secret-key-for-development-only-minimum-256-bits

# Spring 프로필
SPRING_PROFILES_ACTIVE=dev
```

- [ ] **Step 3: Makefile 작성**

```makefile
.PHONY: dev proxy all api web test lint lint-api lint-web typecheck verify reset clean

# ──────────────────────────────────────────────────
# 인프라 명령어
# ──────────────────────────────────────────────────

## Docker 인프라 시작 (PostgreSQL, Redis, MinIO, Prometheus, Grafana, Loki)
dev:
	docker compose up -d postgres redis minio minio-init prometheus grafana loki

## Nginx 프록시 포함 전체 인프라 시작
proxy:
	docker compose up -d

## 인프라 + 백엔드 + 프론트엔드 한 번에 실행
all:
	$(MAKE) proxy
	$(MAKE) -j2 api web

## 인프라 중단
stop:
	docker compose down

## DB/Redis 초기화 후 재시작 (볼륨 삭제)
reset:
	docker compose down -v
	$(MAKE) dev

# ──────────────────────────────────────────────────
# 개발 서버
# ──────────────────────────────────────────────────

## Spring Boot 개발 서버 (localhost:8080)
api:
	cd apps/api && ./gradlew bootRun --args='--spring.profiles.active=dev'

## Vite 개발 서버 (localhost:5173)
web:
	cd apps/web && npm run dev

# ──────────────────────────────────────────────────
# 테스트 & 검증
# ──────────────────────────────────────────────────

## 전체 테스트
test:
	cd apps/api && ./gradlew test
	cd apps/web && npm test -- --run

## API 린트 (컴파일 + 정적 분석)
lint-api:
	cd apps/api && ./gradlew compileJava

## 웹 린트 (ESLint)
lint-web:
	cd apps/web && npx eslint src/ --max-warnings 0

## TypeScript 타입 체크
typecheck:
	cd apps/web && npx tsc --noEmit

## 전체 린트 (컴파일 + ESLint + TypeScript)
lint: lint-api lint-web typecheck

## lint + test 통합 검증 (태스크 완료 시 실행)
verify: lint test

# ──────────────────────────────────────────────────
# 정리
# ──────────────────────────────────────────────────

## 빌드 산출물 정리
clean:
	cd apps/api && ./gradlew clean
	cd apps/web && rm -rf dist
```

- [ ] **Step 4: 커밋**

```bash
git init
git add .gitignore .env.example Makefile
git commit -m "chore: 프로젝트 루트 구조 및 Makefile 초기 설정"
```

---

## Task 2: Docker Compose 개발 환경

**Files:**
- Create: `docker-compose.yml`
- Create: `infra/scripts/init-minio.sh`
- Create: `infra/docker/nginx/default.conf`
- Create: `infra/monitoring/prometheus/prometheus.yml`
- Create: `infra/monitoring/grafana/provisioning/datasources/datasources.yml`

- [ ] **Step 1: docker-compose.yml 작성**

```yaml
# ====================================================
# 커뮤니티 웹앱 - Docker Compose 개발 환경 설정
# 포함 서비스: PostgreSQL, Redis, MinIO, Prometheus, Grafana, Loki, Nginx
# ====================================================
services:

  postgres:
    image: postgres:18-alpine
    container_name: community-postgres
    ports:
      - "${POSTGRES_PORT:-5432}:5432"
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-community}
      POSTGRES_USER: ${POSTGRES_USER:-community}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-community1234}
      TZ: Asia/Seoul
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER:-community} -d ${POSTGRES_DB:-community}"]
      interval: 5s
      timeout: 5s
      retries: 5
    networks:
      - community-network
    restart: unless-stopped

  redis:
    image: redis:7.4-alpine
    container_name: community-redis
    ports:
      - "${REDIS_PORT:-6379}:6379"
    volumes:
      - redis-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 5s
      retries: 5
    networks:
      - community-network
    restart: unless-stopped

  minio:
    image: minio/minio:latest
    container_name: community-minio
    ports:
      - "9000:9000"
      - "9001:9001"
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: ${MINIO_ACCESS_KEY:-minioadmin}
      MINIO_ROOT_PASSWORD: ${MINIO_SECRET_KEY:-minioadmin}
    volumes:
      - minio-data:/data
    healthcheck:
      test: ["CMD", "mc", "ready", "local"]
      interval: 5s
      timeout: 5s
      retries: 5
    networks:
      - community-network
    restart: unless-stopped

  minio-init:
    image: minio/mc:latest
    container_name: community-minio-init
    depends_on:
      minio:
        condition: service_healthy
    entrypoint: /bin/sh
    command: /scripts/init-minio.sh
    environment:
      MINIO_ACCESS_KEY: ${MINIO_ACCESS_KEY:-minioadmin}
      MINIO_SECRET_KEY: ${MINIO_SECRET_KEY:-minioadmin}
    volumes:
      - ./infra/scripts:/scripts:ro
    networks:
      - community-network

  prometheus:
    image: prom/prometheus:v3.2.0
    container_name: community-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./infra/monitoring/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro
    networks:
      - community-network
    restart: unless-stopped

  grafana:
    image: grafana/grafana:11.5.0
    container_name: community-grafana
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin
      GF_AUTH_ANONYMOUS_ENABLED: "true"
    volumes:
      - grafana-data:/var/lib/grafana
      - ./infra/monitoring/grafana/provisioning:/etc/grafana/provisioning:ro
    depends_on:
      - prometheus
      - loki
    networks:
      - community-network
    restart: unless-stopped

  loki:
    image: grafana/loki:3.4.0
    container_name: community-loki
    ports:
      - "3100:3100"
    networks:
      - community-network
    restart: unless-stopped

  nginx:
    image: nginx:1.27-alpine
    container_name: community-nginx
    ports:
      - "80:80"
    volumes:
      - ./infra/docker/nginx/default.conf:/etc/nginx/conf.d/default.conf:ro
    extra_hosts:
      - "host.docker.internal:host-gateway"
    networks:
      - community-network
    restart: unless-stopped

volumes:
  pgdata:
  redis-data:
  minio-data:
  grafana-data:

networks:
  community-network:
    driver: bridge
```

- [ ] **Step 2: MinIO 초기화 스크립트 작성**

```bash
# infra/scripts/init-minio.sh
#!/bin/sh
# MinIO 초기 버킷 생성 스크립트
# 컨테이너 시작 시 1회만 실행 (minio-init 서비스)

set -e

echo "MinIO 초기화 시작..."

# MinIO 클라이언트 설정
mc alias set local http://minio:9000 "$MINIO_ACCESS_KEY" "$MINIO_SECRET_KEY"

# community-media 버킷 생성 (이미 존재하면 무시)
mc mb local/community-media --ignore-existing

# 버킷을 공개 읽기 가능으로 설정 (프로필 이미지, 게시글 이미지 등 공개 접근 필요)
mc anonymous set public local/community-media

echo "MinIO 초기화 완료: community-media 버킷 생성"
```

```bash
chmod +x infra/scripts/init-minio.sh
```

- [ ] **Step 3: Nginx 설정 작성**

```nginx
# infra/docker/nginx/default.conf
# Nginx Reverse Proxy 개발 환경 설정
# 포트 80 → 프론트(5173) / 백(8080) 분기

server {
    listen 80;
    server_name localhost;

    # /api/** → Spring Boot (8080)
    location /api/ {
        proxy_pass http://host.docker.internal:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # 나머지 → Vite (5173)
    location / {
        proxy_pass http://host.docker.internal:5173;
        proxy_set_header Host $host;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

- [ ] **Step 4: Prometheus 설정 작성**

```yaml
# infra/monitoring/prometheus/prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'community-api'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8080']
```

- [ ] **Step 5: Grafana datasources 설정 작성**

```yaml
# infra/monitoring/grafana/provisioning/datasources/datasources.yml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    url: http://prometheus:9090
    isDefault: true

  - name: Loki
    type: loki
    url: http://loki:3100
```

- [ ] **Step 6: 인프라 기동 확인**

```bash
cp .env.example .env
make dev
```

Expected: 6개 컨테이너(postgres, redis, minio, minio-init, prometheus, grafana, loki) healthy

```bash
docker compose ps
```

Expected: STATUS가 `Up` 또는 `healthy`

- [ ] **Step 7: 커밋**

```bash
git add docker-compose.yml infra/
git commit -m "chore(infra): Docker Compose 개발 환경 설정 (PostgreSQL, Redis, MinIO, 모니터링)"
```

---

## Task 3: Spring Boot 프로젝트 초기화

**Files:**
- Create: `apps/api/settings.gradle.kts`
- Create: `apps/api/build.gradle.kts`
- Create: `apps/api/src/main/java/com/community/CommunityApplication.java`
- Create: `apps/api/src/main/resources/application.yml`
- Create: `apps/api/src/main/resources/application-dev.yml`

- [ ] **Step 1: settings.gradle.kts 작성**

```kotlin
// apps/api/settings.gradle.kts
rootProject.name = "community-api"

// JVM 툴체인 자동 설치 (foojay)
// foojay 0.9.0: Gradle 9.x + Java 25 호환
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}
```

- [ ] **Step 2: build.gradle.kts 작성**

```kotlin
// apps/api/build.gradle.kts
plugins {
    java
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.community"
version = "0.0.1-SNAPSHOT"

// Java 25 사용 (로컬 환경에 Java 21 없음, Spring Boot 4.0은 Java 17+ 지원)
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
}

val querydslVersion = "5.1.0"
val mapstructVersion = "1.6.3"
val jjwtVersion = "0.12.6"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // QueryDSL — jakarta classifier 필수 (Jakarta EE 호환)
    implementation("com.querydsl:querydsl-jpa:${querydslVersion}:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:${querydslVersion}:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    implementation("io.jsonwebtoken:jjwt-api:${jjwtVersion}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${jjwtVersion}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${jjwtVersion}")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")
    implementation("org.mapstruct:mapstruct:${mapstructVersion}")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    compileOnly("org.projectlombok:lombok")
    // MapStruct + Lombok 동시 사용: mapstruct-processor가 lombok-processor 이후에 실행되어야 함
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

- [ ] **Step 3: CommunityApplication.java 작성**

```java
/**
 * 커뮤니티 웹앱 Spring Boot 진입점
 * Virtual Threads 활성화로 높은 동시성 처리 가능
 */
package com.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommunityApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }
}
```

- [ ] **Step 4: application.yml 작성**

```yaml
# apps/api/src/main/resources/application.yml
spring:
  application:
    name: community-api

  # Virtual Threads 활성화 (Java 21+)
  threads:
    virtual:
      enabled: true

  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
        default_batch_fetch_size: 20

  flyway:
    enabled: true

server:
  error:
    include-message: always

springdoc:
  api-docs:
    enabled: false
    path: /v3/api-docs
  swagger-ui:
    enabled: false

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  metrics:
    tags:
      application: ${spring.application.name}

jwt:
  secret: ${JWT_SECRET:community-jwt-secret-key-for-development-only-minimum-256-bits}
  access-token-expiration: 900000
  refresh-token-expiration: 604800
```

- [ ] **Step 5: application-dev.yml 작성**

```yaml
# apps/api/src/main/resources/application-dev.yml
# 개발 환경 전용 설정 (make api 실행 시 자동 적용)

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/community
    username: community
    password: community1234
    driver-class-name: org.postgresql.Driver

  data:
    redis:
      host: localhost
      port: 6379

  flyway:
    locations: classpath:db/migration
    baseline-on-migrate: true

  jpa:
    show-sql: true

server:
  port: 8080

springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
    path: /swagger-ui.html

logging:
  level:
    com.community: DEBUG
    org.springframework.security: DEBUG
```

- [ ] **Step 6: 전역 공통 클래스 작성**

`apps/api/src/main/java/com/community/global/common/entity/BaseTimeEntity.java`:

```java
/**
 * 생성/수정 시각 공통 엔티티
 * 모든 엔티티가 상속받아 created_at, updated_at을 자동 관리
 */
package com.community.global.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

`apps/api/src/main/java/com/community/global/common/response/ApiResponse.java`:

```java
/**
 * 모든 API 응답의 공통 래퍼
 * { "success": true, "data": {...}, "error": null } 형식으로 통일
 */
package com.community.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorDetail error
) {
    public record ErrorDetail(String code, String message) {}

    /** 성공 응답 (데이터 있음) */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /** 성공 응답 (데이터 없음, 204 등) */
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, null, null);
    }

    /** 에러 응답 */
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, null,
                new ErrorDetail(errorCode.getCode(), errorCode.getMessage()));
    }
}
```

`apps/api/src/main/java/com/community/global/common/response/ErrorCode.java`:

```java
/**
 * 에러 코드 열거형
 * HTTP 상태 코드와 비즈니스 에러 코드를 함께 관리
 */
package com.community.global.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 인증/인가
    UNAUTHORIZED("E001", "인증이 필요합니다", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("E002", "접근 권한이 없습니다", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS("E003", "이메일 또는 비밀번호가 올바르지 않습니다", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("E004", "토큰이 만료되었습니다", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("E005", "유효하지 않은 토큰입니다", HttpStatus.UNAUTHORIZED),

    // 사용자
    USER_NOT_FOUND("E010", "사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    DUPLICATE_EMAIL("E011", "이미 사용 중인 이메일입니다", HttpStatus.CONFLICT),
    DUPLICATE_NICKNAME("E012", "이미 사용 중인 닉네임입니다", HttpStatus.CONFLICT),
    INACTIVE_USER("E013", "비활성화된 계정입니다", HttpStatus.FORBIDDEN),

    // 유효성 검사
    VALIDATION_ERROR("E020", "입력값이 올바르지 않습니다", HttpStatus.BAD_REQUEST),

    // 서버 에러
    INTERNAL_SERVER_ERROR("E500", "서버 내부 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
```

`apps/api/src/main/java/com/community/global/exception/BusinessException.java`:

```java
/**
 * 비즈니스 로직 예외
 * ErrorCode를 포함하여 GlobalExceptionHandler에서 일관된 에러 응답 생성
 */
package com.community.global.exception;

import com.community.global.common.response.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
```

`apps/api/src/main/java/com/community/global/exception/GlobalExceptionHandler.java`:

```java
/**
 * 전역 예외 처리기
 * BusinessException과 ValidationException을 잡아 ApiResponse 형식으로 응답
 */
package com.community.global.exception;

import com.community.global.common.response.ApiResponse;
import com.community.global.common.response.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 비즈니스 예외 처리 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.error(errorCode));
    }

    /** Bean Validation 예외 처리 (@Valid 실패) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("입력값이 올바르지 않습니다");
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, null,
                        new ApiResponse.ErrorDetail(ErrorCode.VALIDATION_ERROR.getCode(), message)));
    }

    /** 기타 예외 처리 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected exception", e);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
```

- [ ] **Step 7: JPA, Redis, CORS, OpenAPI 설정 작성**

`apps/api/src/main/java/com/community/global/config/JpaConfig.java`:

```java
/**
 * JPA 설정
 * Auditing 활성화로 BaseTimeEntity의 createdAt/updatedAt 자동 주입
 */
package com.community.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {}
```

`apps/api/src/main/java/com/community/global/config/RedisConfig.java`:

```java
/**
 * Redis 설정
 * ObjectMapper를 통해 직렬화/역직렬화를 JSON 형식으로 처리
 */
package com.community.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        return template;
    }
}
```

`apps/api/src/main/java/com/community/global/config/CorsConfig.java`:

```java
/**
 * CORS 설정
 * 프론트엔드(localhost:5173)에서 백엔드(localhost:8080)로의 요청 허용
 */
package com.community.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",   // Vite 개발 서버
                "http://localhost:80",     // Nginx
                "http://localhost"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }
}
```

`apps/api/src/main/java/com/community/global/config/OpenApiConfig.java`:

```java
/**
 * Swagger/OpenAPI 3.0 설정
 * 개발 환경에서만 활성화 (application-dev.yml에서 springdoc.swagger-ui.enabled=true)
 */
package com.community.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        String securitySchemeName = "BearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("커뮤니티 API")
                        .description("커뮤니티 웹앱 REST API 문서")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
```

- [ ] **Step 8: DomainEventPublisher 작성**

```java
/**
 * 도메인 이벤트 발행자
 * 서비스 간 강결합을 방지하기 위한 이벤트 주도 통신 유틸리티
 * Spring의 ApplicationEventPublisher를 래핑하여 도메인 계층에서 이벤트를 쉽게 발행
 */
package com.community.global.common.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /** 도메인 이벤트를 발행합니다 */
    public void publish(Object event) {
        eventPublisher.publishEvent(event);
    }
}
```

- [ ] **Step 9: 백엔드 컴파일 확인**

```bash
cd apps/api && ./gradlew compileJava
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 10: 커밋**

```bash
git add apps/api/
git commit -m "feat(api): Spring Boot 4.0 프로젝트 초기화 및 전역 설정"
```

---

## Task 4: React 프론트엔드 초기화

**Files:**
- Create: `apps/web/package.json`
- Create: `apps/web/vite.config.ts`
- Create: `apps/web/tsconfig.json`
- Create: `apps/web/tsconfig.app.json`
- Create: `apps/web/index.html`
- Create: `apps/web/src/main.tsx`

- [ ] **Step 1: apps/web 디렉토리 생성 및 package.json 작성**

```json
{
  "name": "community-web",
  "version": "0.1.0",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc -b && vite build",
    "preview": "vite preview",
    "test": "vitest",
    "lint": "eslint src/ --max-warnings 0",
    "typecheck": "tsc --noEmit"
  },
  "dependencies": {
    "@emotion/react": "^11.14.0",
    "@emotion/styled": "^11.14.0",
    "@hookform/resolvers": "^4.1.3",
    "@mui/icons-material": "^7.3.0",
    "@mui/material": "^7.3.0",
    "@tanstack/react-query": "^5.90.0",
    "@tanstack/react-query-devtools": "^5.90.0",
    "@tiptap/extension-image": "^3.0.7",
    "@tiptap/extension-link": "^3.0.7",
    "@tiptap/extension-placeholder": "^3.0.7",
    "@tiptap/extension-underline": "^3.0.7",
    "@tiptap/react": "^3.0.7",
    "@tiptap/starter-kit": "^3.0.7",
    "axios": "^1.9.0",
    "dayjs": "^1.11.13",
    "dompurify": "^3.2.6",
    "framer-motion": "^12.18.0",
    "lucide-react": "^0.511.0",
    "react": "^19.2.0",
    "react-dom": "^19.2.0",
    "react-hook-form": "^7.56.4",
    "react-router": "^7.6.0",
    "zustand": "^5.0.3",
    "zod": "^3.24.4"
  },
  "devDependencies": {
    "@eslint/js": "^9.25.0",
    "@testing-library/jest-dom": "^6.6.3",
    "@testing-library/react": "^16.3.0",
    "@types/dompurify": "^3.0.5",
    "@types/node": "^22.15.21",
    "@types/react": "^19.1.2",
    "@types/react-dom": "^19.1.2",
    "@vitejs/plugin-react": "^4.4.1",
    "eslint": "^9.25.0",
    "eslint-plugin-react-hooks": "^5.2.0",
    "jsdom": "^26.1.0",
    "typescript": "^5.9.0",
    "typescript-eslint": "^8.29.1",
    "vite": "^8.0.0",
    "vitest": "^3.2.0"
  }
}
```

- [ ] **Step 2: 패키지 설치**

```bash
cd apps/web && npm install
```

Expected: node_modules 생성, 에러 없음

- [ ] **Step 3: vite.config.ts 작성**

```typescript
// apps/web/vite.config.ts
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    // @/... 경로 별칭 설정 (import '@/shared/...' 등에서 사용)
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      // /api/** → Spring Boot로 프록시 (개발 시 CORS 문제 없음)
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

- [ ] **Step 4: tsconfig.json, tsconfig.app.json 작성**

`apps/web/tsconfig.json`:
```json
{
  "files": [],
  "references": [
    { "path": "./tsconfig.app.json" }
  ]
}
```

`apps/web/tsconfig.app.json`:
```json
{
  "compilerOptions": {
    "tsBuildInfoFile": "./node_modules/.tmp/tsconfig.app.tsbuildinfo",
    "target": "ES2022",
    "useDefineForClassFields": true,
    "lib": ["ES2022", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "isolatedModules": true,
    "moduleDetection": "force",
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "noUncheckedSideEffectImports": true,
    "paths": {
      "@/*": ["./src/*"]
    }
  },
  "include": ["src"]
}
```

- [ ] **Step 5: index.html 작성**

```html
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>커뮤니티</title>
    <!-- Pretendard 가변 폰트 (한국어 최적화) -->
    <link
      rel="stylesheet"
      href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/variable/pretendardvariable-dynamic-subset.min.css"
    />
    <!-- 기본 box-sizing 및 배경색 -->
    <style>
      *, *::before, *::after { box-sizing: border-box; }
      html, body { margin: 0; padding: 0; background: #F7F5F2; }
    </style>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>
```

- [ ] **Step 6: main.tsx 작성**

```typescript
// apps/web/src/main.tsx
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './app/App'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
```

- [ ] **Step 7: 타입 체크 확인**

```bash
cd apps/web && npx tsc --noEmit
```

Expected: 에러 없음

- [ ] **Step 8: 커밋**

```bash
git add apps/web/
git commit -m "feat(web): React 19.2 + Vite 8 + TypeScript 5.9 프론트엔드 초기화"
```

---

## Task 5: MUI 테마 & 디자인 토큰

**Files:**
- Create: `apps/web/src/app/theme/tokens.ts`
- Create: `apps/web/src/app/theme/palette.ts`
- Create: `apps/web/src/app/theme/typography.ts`
- Create: `apps/web/src/app/theme/components.ts`
- Create: `apps/web/src/app/theme/index.ts`
- Create: `apps/web/src/app/providers.tsx`
- Create: `apps/web/src/app/App.tsx`

- [ ] **Step 1: tokens.ts 작성 (디자인 토큰 원천)**

```typescript
// apps/web/src/app/theme/tokens.ts
/**
 * 디자인 토큰 정의 파일
 * 모든 시각적 속성을 중앙 관리. 변경 시 앱 전체에 반영됨.
 * 브랜드 컬러: Claude 시그니처 오렌지/브라운 (#D97757)
 * 폰트: Pretendard (한국어 최적화 가변폰트)
 */
export const tokens = {
  color: {
    primary: '#D97757',
    primaryLight: '#F9EBE6',
    primaryDark: '#B85B3F',
    success: '#34C759',
    warning: '#FF9500',
    error: '#FF3B30',
    info: '#4A7299',
    background: '#F7F5F2',
    surface: '#FFFFFF',
    surfaceVariant: '#F2EFEA',
    textPrimary: '#2D2926',
    textSecondary: '#8C8681',
    textTertiary: '#B5AFA6',
    textOnPrimary: '#FFFFFF',
    border: '#E8E4DF',
    divider: '#F7F5F2',
  },
  darkColor: {
    primary: '#E29578',
    primaryLight: '#3A231C',
    primaryDark: '#D97757',
    success: '#30D158',
    warning: '#FFD60A',
    error: '#FF453A',
    info: '#6B96C3',
    background: '#1A1918',
    surface: '#252422',
    surfaceVariant: '#312F2D',
    textPrimary: '#EDEBE9',
    textSecondary: '#9E9A96',
    textTertiary: '#625E5A',
    textOnPrimary: '#FFFFFF',
    border: '#423E3B',
    divider: '#312F2D',
  },
  typography: {
    fontFamily: '"Pretendard Variable", "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
    headline1: { fontSize: '26px', fontWeight: 700, lineHeight: 1.35, letterSpacing: '-0.02em' },
    headline2: { fontSize: '22px', fontWeight: 700, lineHeight: 1.36, letterSpacing: '-0.02em' },
    title1:    { fontSize: '19px', fontWeight: 600, lineHeight: 1.42 },
    title2:    { fontSize: '17px', fontWeight: 600, lineHeight: 1.41 },
    body1:     { fontSize: '16px', fontWeight: 400, lineHeight: 1.5 },
    body2:     { fontSize: '14px', fontWeight: 400, lineHeight: 1.43 },
    caption:   { fontSize: '12px', fontWeight: 400, lineHeight: 1.33 },
    button:    { fontSize: '16px', fontWeight: 600, lineHeight: 1.5 },
  },
  spacing: {
    xs: 4, sm: 8, md: 12, lg: 16, xl: 20, xxl: 24, section: 28, page: 20,
  },
  radius: {
    sm: 8, md: 12, lg: 16, xl: 20, full: 9999,
  },
  shadow: {
    sm: '0 1px 3px rgba(0,0,0,0.04), 0 1px 2px rgba(0,0,0,0.06)',
    md: '0 4px 12px rgba(0,0,0,0.08)',
    lg: '0 8px 24px rgba(0,0,0,0.12)',
    bottomSheet: '0 -4px 24px rgba(0,0,0,0.12)',
  },
  motion: {
    duration: { fast: 150, normal: 250, slow: 350 },
    easing: {
      standard: [0.4, 0.0, 0.2, 1] as const,
      decelerate: [0.0, 0.0, 0.2, 1] as const,
      accelerate: [0.4, 0.0, 1, 1] as const,
      spring: { type: 'spring' as const, stiffness: 300, damping: 24 },
    },
  },
  breakpoint: {
    mobile: 0, tablet: 600, desktop: 1024,
  },
  zIndex: {
    bottomTabBar: 1000, topAppBar: 1100, modal: 1300, toast: 1400,
  },
} as const;

export type DesignTokens = typeof tokens;
```

- [ ] **Step 2: palette.ts 작성**

```typescript
// apps/web/src/app/theme/palette.ts
/**
 * MUI 팔레트 설정 — tokens.ts의 컬러를 MUI 형식으로 변환
 * 라이트/다크 모드를 모두 설정
 */
import { PaletteOptions } from '@mui/material/styles';
import { tokens } from './tokens';

export const lightPalette: PaletteOptions = {
  mode: 'light',
  primary: {
    main: tokens.color.primary,
    light: tokens.color.primaryLight,
    dark: tokens.color.primaryDark,
    contrastText: tokens.color.textOnPrimary,
  },
  success: { main: tokens.color.success },
  warning: { main: tokens.color.warning },
  error: { main: tokens.color.error },
  info: { main: tokens.color.info },
  background: {
    default: tokens.color.background,
    paper: tokens.color.surface,
  },
  text: {
    primary: tokens.color.textPrimary,
    secondary: tokens.color.textSecondary,
    disabled: tokens.color.textTertiary,
  },
  divider: tokens.color.divider,
};

export const darkPalette: PaletteOptions = {
  mode: 'dark',
  primary: {
    main: tokens.darkColor.primary,
    light: tokens.darkColor.primaryLight,
    dark: tokens.darkColor.primaryDark,
    contrastText: tokens.darkColor.textOnPrimary,
  },
  success: { main: tokens.darkColor.success },
  warning: { main: tokens.darkColor.warning },
  error: { main: tokens.darkColor.error },
  info: { main: tokens.darkColor.info },
  background: {
    default: tokens.darkColor.background,
    paper: tokens.darkColor.surface,
  },
  text: {
    primary: tokens.darkColor.textPrimary,
    secondary: tokens.darkColor.textSecondary,
    disabled: tokens.darkColor.textTertiary,
  },
  divider: tokens.darkColor.divider,
};
```

- [ ] **Step 3: typography.ts 작성**

```typescript
// apps/web/src/app/theme/typography.ts
/**
 * MUI 타이포그래피 설정 — tokens.ts의 폰트 설정을 MUI 형식으로 변환
 */
import { TypographyOptions } from '@mui/material/styles/createTypography';
import { tokens } from './tokens';

export const typography: TypographyOptions = {
  fontFamily: tokens.typography.fontFamily,
  h1: { ...tokens.typography.headline1 },
  h2: { ...tokens.typography.headline2 },
  h3: { ...tokens.typography.title1 },
  h4: { ...tokens.typography.title2 },
  body1: { ...tokens.typography.body1 },
  body2: { ...tokens.typography.body2 },
  caption: { ...tokens.typography.caption },
  button: { ...tokens.typography.button, textTransform: 'none' },
};
```

- [ ] **Step 4: components.ts 작성**

```typescript
// apps/web/src/app/theme/components.ts
/**
 * MUI 컴포넌트 오버라이드 — 토스 스타일로 기본 컴포넌트 스타일 변경
 */
import { Components, Theme } from '@mui/material/styles';
import { tokens } from './tokens';

export const components: Components<Theme> = {
  MuiButton: {
    styleOverrides: {
      root: {
        borderRadius: tokens.radius.md,
        padding: '14px 24px',
        fontSize: tokens.typography.button.fontSize,
        fontWeight: tokens.typography.button.fontWeight,
        boxShadow: 'none',
        '&:hover': { boxShadow: 'none' },
        '&.MuiButton-sizeLarge': { height: 52 },
      },
    },
  },
  MuiTextField: {
    defaultProps: { variant: 'outlined', fullWidth: true },
    styleOverrides: {
      root: {
        '& .MuiOutlinedInput-root': {
          borderRadius: tokens.radius.md,
          backgroundColor: tokens.color.surfaceVariant,
          '& fieldset': { border: 'none' },
          '&:hover fieldset': { border: 'none' },
          '&.Mui-focused fieldset': {
            border: `2px solid ${tokens.color.primary}`,
          },
        },
      },
    },
  },
  MuiCard: {
    styleOverrides: {
      root: {
        borderRadius: tokens.radius.lg,
        boxShadow: tokens.shadow.sm,
        border: `1px solid ${tokens.color.border}`,
      },
    },
  },
  MuiChip: {
    styleOverrides: {
      root: { borderRadius: tokens.radius.sm },
    },
  },
  MuiAppBar: {
    styleOverrides: {
      root: {
        boxShadow: 'none',
        borderBottom: `1px solid ${tokens.color.border}`,
      },
    },
  },
};
```

- [ ] **Step 5: theme/index.ts 작성**

```typescript
// apps/web/src/app/theme/index.ts
/**
 * MUI 테마 생성 및 내보내기
 * tokens → palette/typography/components를 조합하여 최종 테마 생성
 */
import { createTheme } from '@mui/material/styles';
import { lightPalette } from './palette';
import { typography } from './typography';
import { components } from './components';

export const theme = createTheme({
  palette: lightPalette,
  typography,
  components,
  shape: { borderRadius: 12 },
});

export { tokens } from './tokens';
```

- [ ] **Step 6: providers.tsx 작성**

```typescript
// apps/web/src/app/providers.tsx
/**
 * 앱 전역 프로바이더 합성
 * ThemeProvider, QueryClientProvider를 한 곳에서 관리
 */
import { ReactNode } from 'react';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { theme } from './theme';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5,    // 5분
      retry: 1,
    },
  },
});

interface ProvidersProps {
  children: ReactNode;
}

export function Providers({ children }: ProvidersProps) {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        {children}
      </ThemeProvider>
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  );
}
```

- [ ] **Step 7: App.tsx 작성 (임시 라우터)**

```typescript
// apps/web/src/app/App.tsx
/**
 * 루트 앱 컴포넌트
 * Providers로 감싸서 테마/쿼리 컨텍스트 제공
 * 라우터는 Phase 1 (Task 1.7)에서 완성
 */
import { Providers } from './providers';
import { Box, Typography } from '@mui/material';

export default function App() {
  return (
    <Providers>
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '100vh',
          bgcolor: 'background.default',
        }}
      >
        <Typography variant="h2" color="primary">
          커뮤니티
        </Typography>
      </Box>
    </Providers>
  );
}
```

- [ ] **Step 8: Vite 개발 서버 동작 확인**

```bash
cd apps/web && npm run dev
```

Expected: `http://localhost:5173` 에서 "커뮤니티" 텍스트 표시

- [ ] **Step 9: 커밋**

```bash
git add apps/web/src/
git commit -m "feat(web): MUI 7 테마 & 토스 스타일 디자인 토큰 설정"
```

---

## Task 6: Flyway 마이그레이션 (전체 DB 스키마)

**Files:**
- Create: `apps/api/src/main/resources/db/migration/V1__create_users_table.sql`
- Create: `apps/api/src/main/resources/db/migration/V2__create_categories_table.sql`
- Create: `apps/api/src/main/resources/db/migration/V3__create_posts_table.sql`
- Create: `apps/api/src/main/resources/db/migration/V4__create_comments_table.sql`
- Create: `apps/api/src/main/resources/db/migration/V5__create_votes_bookmarks.sql`
- Create: `apps/api/src/main/resources/db/migration/V6__create_notifications.sql`
- Create: `apps/api/src/main/resources/db/migration/V7__create_supporting_tables.sql`

- [ ] **Step 1: V1__create_users_table.sql 작성**

```sql
-- V1: 사용자 테이블 (인증/인가, 프로필의 핵심 엔티티)
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    nickname        VARCHAR(50) NOT NULL UNIQUE,
    bio             VARCHAR(300),
    profile_image   VARCHAR(500),
    role            VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at   TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_nickname ON users(nickname);
```

- [ ] **Step 2: V2__create_categories_table.sql 작성**

```sql
-- V2: 카테고리 테이블 + 기본 4개 카테고리 삽입
CREATE TABLE categories (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50) NOT NULL UNIQUE,
    slug            VARCHAR(50) NOT NULL UNIQUE,
    description     VARCHAR(200),
    sort_order      INT NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

INSERT INTO categories (name, slug, description, sort_order) VALUES
    ('자유', 'free', '자유롭게 이야기하는 공간', 1),
    ('질문', 'question', '궁금한 것을 물어보는 공간', 2),
    ('정보', 'info', '유용한 정보를 공유하는 공간', 3),
    ('리뷰', 'review', '리뷰와 후기를 나누는 공간', 4);
```

- [ ] **Step 3: V3__create_posts_table.sql 작성**

```sql
-- V3: 게시글 테이블 (vote_count, comment_count 비정규화, Soft Delete)
CREATE TABLE posts (
    id              BIGSERIAL PRIMARY KEY,
    author_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id     BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    title           VARCHAR(200) NOT NULL,
    content         TEXT NOT NULL,
    summary         VARCHAR(300),
    thumbnail_url   VARCHAR(500),
    view_count      INT NOT NULL DEFAULT 0,
    vote_count      INT NOT NULL DEFAULT 0,
    comment_count   INT NOT NULL DEFAULT 0,
    is_pinned       BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_posts_author ON posts(author_id);
CREATE INDEX idx_posts_category ON posts(category_id);
CREATE INDEX idx_posts_created ON posts(created_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_posts_vote ON posts(vote_count DESC) WHERE is_deleted = FALSE;
```

- [ ] **Step 4: V4__create_comments_table.sql 작성**

```sql
-- V4: 댓글 테이블 (Adjacency List 패턴으로 대댓글 지원, Soft Delete)
CREATE TABLE comments (
    id              BIGSERIAL PRIMARY KEY,
    post_id         BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    author_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    parent_id       BIGINT REFERENCES comments(id) ON DELETE CASCADE,
    content         TEXT NOT NULL,
    vote_count      INT NOT NULL DEFAULT 0,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_comments_post ON comments(post_id);
CREATE INDEX idx_comments_parent ON comments(parent_id);
```

- [ ] **Step 5: V5__create_votes_bookmarks.sql 작성**

```sql
-- V5: 추천(게시글/댓글 통합)과 북마크 테이블
CREATE TABLE votes (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_type     VARCHAR(20) NOT NULL CHECK (target_type IN ('POST', 'COMMENT')),
    target_id       BIGINT NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, target_type, target_id)
);

CREATE INDEX idx_votes_target ON votes(target_type, target_id);

CREATE TABLE bookmarks (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id         BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, post_id)
);
```

- [ ] **Step 6: V6__create_notifications.sql 작성**

```sql
-- V6: 알림 테이블 (댓글/추천/팔로우/관리자 공지)
CREATE TABLE notifications (
    id              BIGSERIAL PRIMARY KEY,
    recipient_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    actor_id        BIGINT REFERENCES users(id) ON DELETE SET NULL,
    type            VARCHAR(30) NOT NULL CHECK (type IN ('COMMENT', 'VOTE', 'FOLLOW', 'ADMIN_NOTICE')),
    target_type     VARCHAR(20),
    target_id       BIGINT,
    message         VARCHAR(300) NOT NULL,
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_recipient ON notifications(recipient_id, is_read, created_at DESC);
```

- [ ] **Step 7: V7__create_supporting_tables.sql 작성**

```sql
-- V7: 미디어, 신고, 리프레시 토큰 테이블
CREATE TABLE media (
    id              BIGSERIAL PRIMARY KEY,
    uploader_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    original_name   VARCHAR(255) NOT NULL,
    stored_path     VARCHAR(500) NOT NULL,
    thumbnail_path  VARCHAR(500),
    file_size       BIGINT NOT NULL,
    mime_type       VARCHAR(100) NOT NULL,
    width           INT,
    height          INT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE reports (
    id              BIGSERIAL PRIMARY KEY,
    reporter_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_type     VARCHAR(20) NOT NULL CHECK (target_type IN ('USER', 'POST', 'COMMENT')),
    target_id       BIGINT NOT NULL,
    reason          VARCHAR(50) NOT NULL CHECK (reason IN ('SPAM', 'ABUSE', 'INAPPROPRIATE', 'COPYRIGHT', 'OTHER')),
    description     VARCHAR(500),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'RESOLVED', 'DISMISSED')),
    resolved_by     BIGINT REFERENCES users(id),
    resolved_at     TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reports_status ON reports(status, created_at DESC);

CREATE TABLE refresh_tokens (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token           VARCHAR(500) NOT NULL UNIQUE,
    expires_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
```

- [ ] **Step 8: Spring Boot 실행으로 마이그레이션 자동 적용 확인**

먼저 인프라가 실행 중인지 확인:
```bash
make dev
```

Spring Boot 실행:
```bash
cd apps/api && ./gradlew bootRun --args='--spring.profiles.active=dev'
```

Expected: `Flyway: Successfully applied 7 migrations` 로그 출력

- [ ] **Step 9: DB 스키마 확인**

```bash
docker exec -it community-postgres psql -U community -d community -c "\dt"
```

Expected: 9개 테이블 목록 표시 (users, categories, posts, comments, votes, bookmarks, notifications, media, reports, refresh_tokens + flyway_schema_history)

- [ ] **Step 10: 커밋**

```bash
git add apps/api/src/main/resources/db/migration/
git commit -m "feat(db): Flyway V1-V7 전체 DB 스키마 마이그레이션"
```

---

## Task 7: GitHub Actions CI

**Files:**
- Create: `.github/workflows/ci.yml`

- [ ] **Step 1: CI 워크플로우 작성**

```yaml
# .github/workflows/ci.yml
# 커뮤니티 웹앱 CI 파이프라인
# PR 및 main 브랜치 푸시 시 자동 실행
name: CI

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  # 백엔드 검증 (컴파일 + 테스트)
  api-check:
    name: API (Java/Spring Boot)
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:18-alpine
        env:
          POSTGRES_DB: community_test
          POSTGRES_USER: community
          POSTGRES_PASSWORD: community1234
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    steps:
      - uses: actions/checkout@v4
      - name: Set up Java 25
        uses: actions/setup-java@v4
        with:
          java-version: '25'
          distribution: 'temurin'
      - name: Cache Gradle
        uses: actions/cache@v4
        with:
          path: ~/.gradle/caches
          key: ${{ runner.os }}-gradle-${{ hashFiles('apps/api/**/*.gradle*') }}
      - name: Compile
        working-directory: apps/api
        run: ./gradlew compileJava
      - name: Test
        working-directory: apps/api
        run: ./gradlew test
        env:
          SPRING_PROFILES_ACTIVE: test
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/community_test
          SPRING_DATASOURCE_USERNAME: community
          SPRING_DATASOURCE_PASSWORD: community1234

  # 프론트엔드 검증 (린트 + 타입체크 + 빌드)
  web-check:
    name: Web (React/Vite)
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Setup Node.js 22
        uses: actions/setup-node@v4
        with:
          node-version: '22'
          cache: 'npm'
          cache-dependency-path: apps/web/package-lock.json
      - name: Install dependencies
        working-directory: apps/web
        run: npm ci
      - name: Type check
        working-directory: apps/web
        run: npx tsc --noEmit
      - name: Lint
        working-directory: apps/web
        run: npx eslint src/ --max-warnings 0
      - name: Build
        working-directory: apps/web
        run: npm run build
```

- [ ] **Step 2: 커밋**

```bash
git add .github/
git commit -m "chore(ci): GitHub Actions 병렬 CI 파이프라인 (API + Web)"
```

---

## Task 8: 통합 검증

- [ ] **Step 1: 인프라 기동 및 백엔드 실행 확인**

```bash
make dev && sleep 10
make api &   # 백그라운드에서 실행
```

Expected: `Started CommunityApplication` 로그 출력, 포트 8080 대기

- [ ] **Step 2: Swagger UI 접근 확인**

브라우저에서 `http://localhost:8080/swagger-ui.html` 접속
Expected: Swagger UI 화면 표시

- [ ] **Step 3: 프론트엔드 실행 확인**

```bash
make web &
```

브라우저에서 `http://localhost:5173` 접속
Expected: "커뮤니티" 텍스트와 MUI 테마 컬러 표시

- [ ] **Step 4: make lint 통과 확인**

```bash
make lint
```

Expected: 컴파일 성공 + ESLint 경고 없음 + TypeScript 에러 없음

- [ ] **Step 5: phase-0-bootstrap.md 완료 표시**

`prompt/phases/phase-0-bootstrap.md` 검증 기준 모두 `[x]` 체크

- [ ] **Step 6: 최종 커밋**

```bash
git add prompt/phases/phase-0-bootstrap.md
git commit -m "docs(phase-0): Phase 0 완료 표시 업데이트"
```

---

## Spec Coverage 체크

| 요구사항 | Task |
|---------|------|
| 루트 구조 (.gitignore, .env.example, Makefile) | 1 |
| Docker Compose (PostgreSQL, Redis, MinIO, 모니터링, Nginx) | 2 |
| Spring Boot 초기화 (의존성, 전역 설정, 공통 클래스) | 3 |
| React 초기화 (Vite, TypeScript, package.json) | 4 |
| MUI 테마 & 디자인 토큰 (토스 스타일) | 5 |
| Flyway 마이그레이션 V1-V7 (전체 DB 스키마) | 6 |
| GitHub Actions CI | 7 |
| 통합 검증 (make dev, make api, make web) | 8 |
