/**
 * JWT 토큰 생성 및 검증을 담당하는 프로바이더
 *
 * JJWT 0.12.x 라이브러리를 사용하여 HS256 알고리즘으로 JWT 토큰을 생성/검증합니다.
 * 액세스 토큰은 사용자 정보와 권한을 포함하며, 리프레시 토큰은 UUID 기반 랜덤 문자열입니다.
 *
 * Virtual Threads 환경에서 안전하게 작동하도록 동기화 블록 없이 구현했습니다.
 *
 * 패키지: com.community.global.security.jwt
 * 의존성: io.jsonwebtoken:jjwt-* (0.12.x)
 * 사용 위치: AuthService, JwtAuthFilter
 */
package com.community.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 토큰 생성 및 검증 컴포넌트
 *
 * 액세스 토큰: 사용자 식별(userId)과 권한(role) 정보를 포함
 * 리프레시 토큰: UUID 기반 랜덤 문자열 (DB에 별도 저장하여 검증)
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;

    /**
     * JWT 토큰 프로바이더 생성자
     *
     * application.yml의 JWT 설정값을 주입받아 HMAC 서명키를 생성합니다.
     * 비밀키는 최소 32바이트(256비트) 이상이어야 HS256 알고리즘에서 안전합니다.
     *
     * @param secretString JWT 서명용 비밀 문자열 (환경변수로 주입 권장)
     * @param accessTokenExpiration 액세스 토큰 만료 시간 (밀리초)
     */
    public JwtTokenProvider(@Value("${jwt.secret}") String secretString,
                           @Value("${jwt.access-token-expiration}") long accessTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
    }

    /**
     * 액세스 토큰 생성
     *
     * 사용자 ID와 권한 정보를 Claims에 포함하여 JWT 토큰을 생성합니다.
     * subject에는 userId를 문자열로, role claim에는 권한 정보를 저장합니다.
     *
     * @param userId 사용자 식별자
     * @param role 사용자 권한 (USER, ADMIN)
     * @return JWT 액세스 토큰 문자열
     */
    public String generateAccessToken(Long userId, String role) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(userId.toString())  // 사용자 ID를 subject로 설정
                .claim("role", role)        // 권한 정보를 별도 claim으로 추가
                .issuedAt(now)              // 토큰 발행 시각
                .expiration(expireDate)     // 만료 시각 (15분 후)
                .signWith(secretKey)        // HS256 알고리즘으로 서명
                .compact();
    }

    /**
     * 리프레시 토큰 생성
     *
     * UUID 기반의 랜덤 문자열을 생성합니다.
     * 실제 검증은 DB에 저장된 값과 비교하여 수행하므로, JWT 형태가 아닙니다.
     * 이 방식은 토큰 무효화가 쉽고, 보안상 더 안전합니다.
     *
     * @return UUID 기반 리프레시 토큰 문자열
     */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * JWT 토큰 유효성 검증
     *
     * 토큰의 서명과 만료 시간을 검증합니다.
     * 검증 실패 시 예외를 던지지 않고 false를 반환하여 호출자가 적절히 처리할 수 있게 합니다.
     *
     * @param token 검증할 JWT 토큰 문자열
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)     // 서명 검증용 키 설정
                .build()
                .parseSignedClaims(token); // 파싱 및 검증 수행
            return true;
        } catch (Exception e) {
            // 토큰 파싱 실패, 서명 불일치, 만료 등 모든 예외를 false로 처리
            // 구체적인 에러 로그는 디버깅을 위해 남기되, 외부에는 단순히 실패만 알림
            log.debug("JWT 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰에서 사용자 ID 추출
     *
     * JWT 토큰의 subject claim에서 사용자 ID를 추출합니다.
     * 토큰이 유효하지 않거나 subject가 숫자 형식이 아니면 예외가 발생할 수 있으므로,
     * 호출 전에 validateToken()으로 검증하는 것을 권장합니다.
     *
     * @param token 유효한 JWT 토큰
     * @return 토큰에 포함된 사용자 ID
     * @throws RuntimeException 토큰이 유효하지 않거나 파싱에 실패한 경우
     */
    public Long getUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    /**
     * 토큰에서 사용자 권한 추출
     *
     * JWT 토큰의 role claim에서 사용자 권한을 추출합니다.
     * Spring Security의 권한 체계와 연동하기 위해 문자열 형태로 반환합니다.
     *
     * @param token 유효한 JWT 토큰
     * @return 토큰에 포함된 사용자 권한 (USER, ADMIN 등)
     * @throws RuntimeException 토큰이 유효하지 않거나 파싱에 실패한 경우
     */
    public String getRole(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("role", String.class);
    }
}