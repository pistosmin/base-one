/**
 * ====================================================
 * 토큰 갱신 요청 DTO
 *
 * 액세스 토큰 만료 시 리프레시 토큰을 통해 새 토큰을 발급받기 위한 요청 데이터입니다.
 * 클라이언트는 만료된 액세스 토큰 대신 유효한 리프레시 토큰을 제공해야 합니다.
 *
 * 보안 특징:
 * - 리프레시 토큰은 DB에 저장되어 강제 만료 가능
 * - 토큰 갱신 시 기존 리프레시 토큰은 삭제되고 새로 발급됨
 *
 * 패키지: com.community.domain.auth.dto.request
 * 사용 위치: AuthController.refresh()
 * ====================================================
 */
package com.community.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 토큰 갱신 요청 DTO
 *
 * @param refreshToken 기존에 발급받은 리프레시 토큰 문자열 (UUID 형태)
 */
public record TokenRefreshRequest(
        @NotBlank(message = "리프레시 토큰을 입력해주세요.")
        String refreshToken
) {}