/**
 * JWT 토큰 전송 객체
 *
 * JwtTokenProvider가 생성한 액세스 토큰과 리프레시 토큰을 함께 담는 DTO입니다.
 * AuthService에서 로그인/회원가입 응답 생성에 사용됩니다.
 *
 * 패키지: com.community.global.security.jwt
 * 사용 위치: JwtTokenProvider.generateTokens(), AuthService
 */
package com.community.global.security.jwt;

/**
 * JWT 토큰 쌍 (액세스 + 리프레시)
 *
 * @param accessToken  API 인증에 사용하는 단기 토큰 (15분 유효)
 * @param refreshToken 토큰 갱신에 사용하는 장기 토큰 (7일 유효, UUID 기반)
 * @param expiresIn    액세스 토큰 만료까지 남은 시간 (초 단위)
 */
public record JwtTokenDto(
        String accessToken,
        String refreshToken,
        long expiresIn
) {}