/**
 * 커스텀 인증 진입점 (Authentication Entry Point)
 *
 * 인증이 필요한 리소스에 인증되지 않은 사용자가 접근할 때 호출되는 핸들러입니다.
 * 기본 Spring Security는 HTML 로그인 페이지로 리다이렉트하지만,
 * REST API에서는 JSON 형태의 에러 응답을 반환해야 합니다.
 *
 * 프론트엔드의 Axios 인터셉터가 이 응답을 파싱하여 로그인 페이지로 이동하거나
 * 토큰 갱신을 시도할 수 있도록 일관된 JSON 형식을 제공합니다.
 *
 * 패키지: com.community.global.security
 * 의존성: spring-security-web, jackson-core
 * 사용 위치: SecurityConfig에서 exceptionHandling 설정
 */
package com.community.global.security;

import com.community.global.common.response.ApiResponse;
import com.community.global.common.response.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증 실패 시 JSON 응답 생성기
 *
 * 인증이 필요한 API에 인증되지 않은 요청이 들어올 때,
 * HTML 페이지 대신 JSON 에러 응답을 반환합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 인증 실패 시 JSON 에러 응답 생성
     *
     * Spring Security에서 인증이 실패했을 때 자동으로 호출되는 메서드입니다.
     * 기본 동작(로그인 페이지 리다이렉트) 대신 JSON 형태의 에러 응답을 작성합니다.
     *
     * 응답 형식은 프로젝트의 표준 ApiResponse 규격을 따라서,
     * 프론트엔드에서 일관되게 처리할 수 있도록 합니다.
     *
     * @param request 실패한 HTTP 요청 객체
     * @param response HTTP 응답 객체 (JSON 에러 정보 작성)
     * @param authException 인증 실패 예외 객체
     * @throws IOException JSON 응답 작성 실패 시
     */
    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {

        log.debug("인증되지 않은 요청: {} {}", request.getMethod(), request.getRequestURI());

        // HTTP 응답 헤더 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 401 상태 코드
        response.setContentType("application/json;charset=UTF-8"); // JSON 응답임을 명시

        // 프로젝트 표준 에러 응답 형식으로 변환
        // ErrorCode.INVALID_TOKEN: "유효하지 않은 인증 토큰입니다."
        ApiResponse<Object> errorResponse = ApiResponse.error(ErrorCode.INVALID_TOKEN);

        // JSON 응답 본문에 에러 정보 작성
        // ObjectMapper를 사용하여 ApiResponse 객체를 JSON 문자열로 직렬화
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);

        // 프론트엔드에서 이 응답을 받으면:
        // 1. Axios 인터셉터가 401 상태와 errorResponse.error.code를 확인
        // 2. 토큰 만료 등의 경우 자동으로 토큰 갱신 시도
        // 3. 갱신 실패 시 로그인 페이지로 리다이렉트
    }
}