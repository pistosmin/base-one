/**
 * Spring Security 보안 설정
 *
 * JWT 기반 인증/인가를 위한 Spring Security 설정을 정의합니다.
 * 세션 없는 Stateless 방식으로 동작하며, 커스텀 JWT 필터를 통해 인증을 처리합니다.
 *
 * 주요 설정:
 * - CSRF 비활성화 (REST API 특성상 불필요)
 * - Session 정책: STATELESS (JWT 사용으로 세션 불필요)
 * - 인증 제외 경로 설정 (로그인, 회원가입, Swagger 등)
 * - JWT 필터를 인증 필터 체인에 추가
 * - 인증 실패 시 JSON 응답 처리
 *
 * 패키지: com.community.global.config
 * 의존성: spring-security-config, spring-security-web
 * 사용 위치: Spring Boot 애플리케이션 시작 시 자동 로드
 */
package com.community.global.config;

import com.community.global.security.CustomAuthEntryPoint;
import com.community.global.security.jwt.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 보안 설정 구성 클래스
 *
 * Spring Security의 필터 체인과 인증/인가 규칙을 정의합니다.
 * JWT 토큰 기반의 stateless 인증 방식을 구현합니다.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomAuthEntryPoint customAuthEntryPoint;

    /**
     * 보안 필터 체인 설정
     *
     * HTTP 요청에 대한 보안 규칙을 정의합니다.
     * JWT 인증 방식에 맞춰 세션을 사용하지 않고, 커스텀 필터를 통해 인증을 처리합니다.
     *
     * @param http Spring Security HTTP 보안 설정 객체
     * @return 구성된 보안 필터 체인
     * @throws Exception 설정 과정에서 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF 보호 기능 비활성화
                // REST API에서는 CSRF 토큰이 필요하지 않으며, JWT 토큰으로 보안을 담보
                // SPA(Single Page Application) 특성상 쿠키 기반 세션을 사용하지 않음
                .csrf(AbstractHttpConfigurer::disable)

                // 세션 관리 정책을 STATELESS로 설정
                // JWT 토큰에 모든 인증 정보가 포함되어 있으므로 서버에서 세션을 유지할 필요가 없음
                // 이는 서버 확장성과 성능에 유리함
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 경로 설정
                        .requestMatchers(
                                "/api/v1/auth/**",        // 로그인, 회원가입, 토큰 갱신
                                "/swagger-ui/**",         // Swagger UI 리소스
                                "/swagger-ui.html",       // Swagger UI 메인 페이지
                                "/v3/api-docs/**",        // OpenAPI 스펙 문서
                                "/actuator/**"            // 애플리케이션 모니터링 엔드포인트
                        ).permitAll()

                        // 위에 명시되지 않은 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // JWT 인증 필터를 기본 인증 필터보다 먼저 실행되도록 추가
                // UsernamePasswordAuthenticationFilter 전에 JWT 토큰 검증을 수행
                // 이를 통해 유효한 JWT 토큰이 있으면 폼 기반 인증을 건너뛸 수 있음
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // 인증 예외 처리 설정
                // 인증이 필요한 리소스에 인증되지 않은 요청이 올 때의 처리 방식 정의
                .exceptionHandling(exceptions -> exceptions
                        // HTML 로그인 페이지 리다이렉트 대신 JSON 에러 응답 반환
                        // 프론트엔드가 이를 파싱하여 적절한 UI 처리 (로그인 페이지 이동 등) 수행
                        .authenticationEntryPoint(customAuthEntryPoint)
                )

                .build();
    }

    /**
     * 비밀번호 암호화 빈 설정
     *
     * BCrypt 알고리즘을 사용하는 비밀번호 인코더를 제공합니다.
     * strength 12는 보안과 성능의 균형을 고려한 권장값입니다.
     *
     * BCrypt 특징:
     * - Salt가 자동으로 생성되어 레인보우 테이블 공격 방지
     * - 적응형 해시 함수로 하드웨어 발전에 따라 강도 조절 가능
     * - Spring Security에서 기본 권장하는 방식
     *
     * @return BCrypt 비밀번호 인코더 (strength = 12)
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        // strength 12: 2^12 = 4096 라운드
        // 일반적인 서버에서 약 100-300ms 소요 (로그인 응답성과 보안의 균형)
        return new BCryptPasswordEncoder(12);
    }
}