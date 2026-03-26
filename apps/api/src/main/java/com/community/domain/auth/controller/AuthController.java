/**
 * ====================================================
 * 인증 컨트롤러
 *
 * 사용자 인증과 관련된 REST API 엔드포인트를 제공합니다.
 * 회원가입, 로그인, 토큰 갱신, 로그아웃 기능을 담당합니다.
 *
 * API 설계:
 * - RESTful URI 규칙 준수 (/api/v1/auth/*)
 * - HTTP 상태 코드 활용 (201: 생성, 200: 성공)
 * - 표준 응답 형식 (ApiResponse<T>) 사용
 * - Bean Validation으로 입력값 검증
 *
 * 보안 특징:
 * - JWT 기반 인증 (stateless)
 * - 리프레시 토큰을 통한 액세스 토큰 갱신
 * - 로그아웃 시 서버 사이드 토큰 무효화
 *
 * 패키지: com.community.domain.auth.controller
 * 의존성: AuthService
 * 사용 위치: 프론트엔드의 모든 인증 관련 요청
 * ====================================================
 */
package com.community.domain.auth.controller;

import com.community.domain.auth.dto.request.LoginRequest;
import com.community.domain.auth.dto.request.SignupRequest;
import com.community.domain.auth.dto.request.TokenRefreshRequest;
import com.community.domain.auth.dto.response.TokenResponse;
import com.community.domain.auth.service.AuthService;
import com.community.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 REST API 컨트롤러
 *
 * Spring Security와 연동된 JWT 기반 인증 시스템의 HTTP 엔드포인트를 제공합니다.
 * 모든 응답은 ApiResponse<T> 형태로 통일되어 프론트엔드에서 일관된 처리가 가능합니다.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "인증", description = "회원가입, 로그인, 토큰 갱신, 로그아웃 API")
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입 API
     *
     * 새 사용자 계정을 생성하고 즉시 로그인 처리합니다.
     * 성공 시 201 Created 상태 코드와 함께 토큰 정보를 반환합니다.
     *
     * 검증 규칙:
     * - 이메일: 형식 검증 및 중복 검사
     * - 비밀번호: 최소 8자 이상
     * - 닉네임: 2~20자 및 중복 검사
     *
     * @param request 회원가입 요청 정보 (이메일, 비밀번호, 닉네임)
     * @return 201 Created + 토큰 응답 (액세스 토큰, 리프레시 토큰, 사용자 정보)
     */
    @PostMapping("/signup")
    @Operation(
            summary = "회원가입",
            description = "이메일, 비밀번호, 닉네임으로 새 계정을 생성합니다. " +
                         "가입과 동시에 로그인 처리되어 즉시 서비스 이용이 가능합니다."
    )
    public ResponseEntity<ApiResponse<TokenResponse>> signup(
            @Valid @RequestBody SignupRequest request) {

        log.info("회원가입 API 호출: email={}", request.email());

        TokenResponse response = authService.signup(request);

        // 201 Created: 새 리소스(사용자 계정) 생성 성공
        return ResponseEntity.status(201)
                .body(ApiResponse.success(response));
    }

    /**
     * 로그인 API
     *
     * 이메일과 비밀번호를 검증하여 JWT 토큰을 발급합니다.
     * 성공 시 200 OK 상태 코드와 함께 토큰 정보를 반환합니다.
     *
     * 보안 특징:
     * - BCrypt 비밀번호 검증
     * - 로그인 실패 시 이메일/비밀번호 오류 구분 안 함 (보안)
     * - 마지막 로그인 시간 자동 업데이트
     *
     * @param request 로그인 요청 정보 (이메일, 비밀번호)
     * @return 200 OK + 토큰 응답 (액세스 토큰, 리프레시 토큰, 사용자 정보)
     */
    @PostMapping("/login")
    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다. " +
                         "액세스 토큰은 API 인증에, 리프레시 토큰은 토큰 갱신에 사용됩니다."
    )
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("로그인 API 호출: email={}", request.email());

        TokenResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 토큰 갱신 API
     *
     * 만료된 액세스 토큰을 리프레시 토큰을 통해 새로 발급받습니다.
     * 보안을 위해 기존 리프레시 토큰은 무효화되고 새로운 토큰이 발급됩니다.
     *
     * 갱신 프로세스:
     * 1. 리프레시 토큰 유효성 검증 (DB 조회 + 만료시간 확인)
     * 2. 기존 리프레시 토큰 삭제 (재사용 공격 방지)
     * 3. 새로운 액세스 + 리프레시 토큰 발급
     *
     * @param request 토큰 갱신 요청 정보 (리프레시 토큰)
     * @return 200 OK + 토큰 응답 (새 액세스 토큰, 새 리프레시 토큰, 사용자 정보)
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "토큰 갱신",
            description = "리프레시 토큰으로 새 액세스 토큰을 발급합니다. " +
                         "보안을 위해 기존 리프레시 토큰은 무효화되고 새로운 토큰이 발급됩니다."
    )
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request) {

        log.info("토큰 갱신 API 호출");

        TokenResponse response = authService.refresh(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 로그아웃 API
     *
     * 현재 로그인한 사용자의 모든 리프레시 토큰을 삭제하여 강제 로그아웃 처리합니다.
     * 여러 디바이스에서 로그인한 경우 모든 디바이스에서 로그아웃됩니다.
     *
     * 인증 요구사항:
     * - 유효한 액세스 토큰 필수 (Authorization: Bearer <token>)
     * - Spring Security의 @PreAuthorize로 인증 상태 검증
     * - @AuthenticationPrincipal로 사용자 ID 추출
     *
     * @param userId JWT에서 추출된 현재 로그인 사용자 ID
     * @return 200 OK + 빈 성공 응답
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "로그아웃",
            description = "현재 사용자의 리프레시 토큰을 삭제합니다. " +
                         "여러 디바이스에서 로그인한 경우 모든 디바이스에서 로그아웃됩니다."
    )
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal Long userId) {

        log.info("로그아웃 API 호출: userId={}", userId);

        authService.logout(userId);

        return ResponseEntity.ok(ApiResponse.successWithoutData());
    }
}