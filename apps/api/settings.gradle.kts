/**
 * Gradle 프로젝트 설정
 * 프로젝트 이름: community-api
 */
rootProject.name = "community-api"

// foojay 툴체인 자동 프로비저닝 플러그인 (JDK 자동 다운로드)
// Gradle 9.4.1 + foojay 0.9.0 호환 버전
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}
