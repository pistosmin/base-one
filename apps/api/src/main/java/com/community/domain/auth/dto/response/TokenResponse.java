/**
 * ====================================================
 * 인증 토큰 응답 DTO
 *
 * 로그인/회원가입/토큰 갱신 성공 시 반환됩니다.
 * 액세스 토큰, 리프레시 토큰, 만료 시간, 사용자 프로필을 포함합니다.
 *
 * 토큰 구성:
 * - 액세스 토큰: JWT 형태, API 인증에 사용, 15분 유효
 * - 리프레시 토큰: UUID 형태, DB 저장, 7일 유효
 * - 만료 시간: 액세스 토큰 만료까지 남은 시간(초)
 *
 * 패키지: com.community.domain.auth.dto.response
 * 사용 위치: AuthController의 모든 응답
 * ====================================================
 */
package com.community.domain.auth.dto.response;

import com.community.domain.user.dto.response.UserProfileResponse;

/**
 * 인증 토큰 응답 DTO
 *
 * @param accessToken  Bearer 토큰 (API 인증에 사용, 15분 유효)
 * @param refreshToken 갱신 토큰 (새 액세스 토큰 발급에 사용, 7일 유효)
 * @param expiresIn    액세스 토큰 만료까지 남은 시간 (초)
 * @param user         로그인한 사용자 정보 (ID, 닉네임, 이메일, 역할)
 */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UserProfileResponse user
) {}