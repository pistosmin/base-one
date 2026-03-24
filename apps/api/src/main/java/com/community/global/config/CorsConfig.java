/**
 * CORS 정책 설정
 *
 * 프론트엔드(React)에서 백엔드(Spring Boot) API를 호출할 수 있도록
 * Cross-Origin Resource Sharing 정책을 설정합니다.
 *
 * 개발 환경에서는 localhost:5173 (Vite 개발 서버)을 허용합니다.
 * 프로덕션에서는 실제 도메인으로 변경해야 합니다.
 *
 * 참고: Vite의 proxy 설정으로도 개발 시 CORS를 우회할 수 있지만,
 * 서버 측 CORS 설정은 프로덕션 대비를 위해 항상 필요합니다.
 */
package com.community.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 허용 오리진 설정
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * CORS 매핑 추가
     *
     * /api/** 경로에 대해 CORS를 허용합니다:
     * - 허용 오리진: localhost:5173 (프론트엔드 개발 서버)
     * - 허용 메서드: GET, POST, PATCH, DELETE, OPTIONS
     * - 허용 헤더: 모든 헤더 (Authorization 등)
     * - 자격 증명(쿠키): 허용
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // 개발: Vite 개발 서버 허용
                // 프로덕션: 실제 도메인으로 교체 필요
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                // preflight 캐시 시간 (1시간)
                .maxAge(3600);
    }
}
