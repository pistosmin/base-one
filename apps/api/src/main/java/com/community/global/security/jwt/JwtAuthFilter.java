/**
 * JWT 토큰 기반 인증 필터
 *
 * 모든 HTTP 요청에서 Authorization 헤더의 JWT 토큰을 검증하고,
 * 유효한 경우 Spring Security Context에 인증 정보를 설정합니다.
 *
 * OncePerRequestFilter를 확장하여 요청당 한 번만 실행되도록 보장합니다.
 * Virtual Threads 환경에서 안전하게 작동합니다.
 *
 * 패키지: com.community.global.security.jwt
 * 의존성: jakarta.servlet, spring-security-web
 * 사용 위치: SecurityConfig에서 필터 체인에 등록
 */
package com.community.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 인증 필터
 *
 * 요청 헤더에서 JWT 토큰을 추출하여 검증하고,
 * 유효한 경우 사용자 정보를 Spring Security Context에 설정합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    /** JWT 토큰 헤더 이름 */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /** JWT 토큰 접두어 */
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 모든 HTTP 요청에 대해 JWT 토큰 검증을 수행
     *
     * 필터 처리 과정:
     * 1. Authorization 헤더에서 JWT 토큰 추출
     * 2. 토큰 유효성 검증
     * 3. 유효한 경우: 사용자 정보 추출 → Security Context 설정
     * 4. 유효하지 않은 경우: 인증 없이 다음 필터로 진행
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param filterChain 필터 체인
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {

        // 1. 요청 헤더에서 JWT 토큰 추출
        String token = extractTokenFromHeader(request);

        // 2. 토큰이 존재하고 유효한지 검증
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            try {
                // 3. 토큰에서 사용자 정보 추출
                Long userId = jwtTokenProvider.getUserId(token);
                String role = jwtTokenProvider.getRole(token);

                // 4. Spring Security 인증 토큰 생성
                // principal: 사용자 ID (컨트롤러에서 @AuthenticationPrincipal로 접근 가능)
                // credentials: null (JWT 방식에서는 불필요)
                // authorities: 권한 정보 (ROLE_ 접두어 자동 추가)
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userId,  // principal로 userId 설정
                        null,    // credentials는 JWT 방식에서 사용하지 않음
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

                // 5. Security Context에 인증 정보 설정
                // 이후 필터와 컨트롤러에서 SecurityContextHolder.getContext().getAuthentication()으로 접근 가능
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT 인증 성공 - 사용자 ID: {}, 권한: ROLE_{}", userId, role);

            } catch (Exception e) {
                // 토큰에서 사용자 정보 추출 실패 시 인증 없이 진행
                // SecurityContextHolder는 기본적으로 null이므로 별도 처리 불필요
                log.debug("JWT 토큰에서 사용자 정보 추출 실패: {}", e.getMessage());
            }
        }

        // 6. 다음 필터로 요청 전달
        // 인증에 성공했다면 Security Context에 정보가 설정된 상태
        // 실패했다면 인증 정보 없이 진행 (SecurityConfig에서 인가 처리)
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 JWT 토큰 추출
     *
     * Authorization 헤더에서 "Bearer " 접두어를 제거하고 실제 토큰 문자열을 반환합니다.
     * 헤더가 없거나 형식이 맞지 않으면 null을 반환합니다.
     *
     * @param request HTTP 요청 객체
     * @return JWT 토큰 문자열 또는 null
     */
    private String extractTokenFromHeader(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(authorizationHeader) &&
            authorizationHeader.startsWith(BEARER_PREFIX)) {
            // "Bearer " 접두어를 제거하고 실제 토큰 반환
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}