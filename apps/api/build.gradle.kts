/**
 * ====================================================
 * Gradle 빌드 설정 (Kotlin DSL)
 *
 * 커뮤니티 웹앱 백엔드 프로젝트의 빌드 설정입니다.
 * Spring Boot 4.0.3 + Java 21 기반이며,
 * QueryDSL, MapStruct, Lombok 어노테이션 프로세서를 포함합니다.
 *
 * 주요 의존성 그룹:
 *   implementation → 런타임에 필요한 라이브러리
 *   compileOnly → 컴파일 시에만 필요 (Lombok)
 *   annotationProcessor → 컴파일 시 코드 생성 (Lombok, MapStruct, QueryDSL)
 *   testImplementation → 테스트에만 필요
 * ====================================================
 */

plugins {
    java
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.community"
version = "0.0.1-SNAPSHOT"

// Java 25 사용 (로컬 환경에 Java 25가 설치됨)
// Spring Boot 4.0은 Java 17+ 지원, Java 25도 호환
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
}

// 의존성 버전 관리
val querydslVersion = "5.1.0"
val mapstructVersion = "1.6.3"
val jjwtVersion = "0.12.6"

dependencies {
    // ── Spring Boot 스타터 (핵심 모듈) ──

    // Spring MVC (REST API 개발)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring Data JPA (ORM, 데이터 접근 계층)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Spring Security (인증/인가, JWT와 함께 사용)
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Bean Validation (DTO 입력값 검증)
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Spring Cache (Redis 기반 캐싱 추상화)
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // Spring Actuator (메트릭, 헬스체크, Prometheus 연동)
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Spring Data Redis (Redis 캐시, 세션, Rate Limiting)
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // ── 데이터베이스 ──

    // PostgreSQL JDBC 드라이버
    runtimeOnly("org.postgresql:postgresql")

    // Flyway 데이터베이스 마이그레이션 (SQL 기반 스키마 버전 관리)
    implementation("org.flywaydb:flyway-core")
    // Flyway PostgreSQL 확장 모듈 (PostgreSQL 전용 기능 지원)
    implementation("org.flywaydb:flyway-database-postgresql")

    // ── QueryDSL (타입 안전 동적 쿼리) ──
    // jakarta classifier: Jakarta EE 호환 버전 사용
    implementation("com.querydsl:querydsl-jpa:${querydslVersion}:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:${querydslVersion}:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // ── JWT (JSON Web Token 인증) ──
    implementation("io.jsonwebtoken:jjwt-api:${jjwtVersion}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${jjwtVersion}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${jjwtVersion}")

    // ── API 문서화 (Swagger UI) ──
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")

    // ── MapStruct (compile-time DTO 매핑) ──
    implementation("org.mapstruct:mapstruct:${mapstructVersion}")

    // ── Prometheus 메트릭 (Actuator + Micrometer) ──
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // ── Lombok (보일러플레이트 코드 제거) ──
    compileOnly("org.projectlombok:lombok")

    // ── 어노테이션 프로세서 (컴파일 시 코드 생성) ──
    // ⚠️ 순서 중요: Lombok → lombok-mapstruct-binding → MapStruct
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
    annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")

    // ── 테스트 ──
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// QueryDSL Q클래스 생성 경로 설정
tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Aquerydsl.generatedAnnotationClass=jakarta.annotation.Generated")
}
