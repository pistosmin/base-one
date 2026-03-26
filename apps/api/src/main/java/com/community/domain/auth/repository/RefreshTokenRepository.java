/**
 * ====================================================
 * 리프레시 토큰 Repository
 *
 * 리프레시 토큰의 저장, 조회, 삭제 작업을 담당합니다.
 * JWT 토큰 갱신과 로그아웃 처리에 필요한 데이터 접근 계층입니다.
 *
 * 주요 기능:
 * - 토큰 문자열을 통한 토큰 조회 (갱신 시 검증)
 * - 사용자별 토큰 삭제 (로그아웃, 계정 정지)
 * - 특정 토큰 삭제 (디바이스별 로그아웃)
 *
 * 패키지: com.community.domain.auth.repository
 * 사용 위치: AuthService
 * ====================================================
 */
package com.community.domain.auth.repository;

import com.community.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 리프레시 토큰 Repository
 *
 * Spring Data JPA를 통해 리프레시 토큰 엔티티에 대한
 * 기본적인 CRUD 작업과 커스텀 쿼리를 제공합니다.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 토큰 문자열로 리프레시 토큰 조회
     *
     * 토큰 갱신 요청이 들어왔을 때 해당 토큰이 유효한지 검증하기 위해 사용합니다.
     * 토큰 값은 고유(unique)하므로 최대 1개의 결과만 반환됩니다.
     *
     * @param token 조회할 리프레시 토큰 문자열
     * @return 토큰이 존재하면 RefreshToken 엔티티, 없으면 empty Optional
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 사용자 ID로 리프레시 토큰 삭제
     *
     * 특정 사용자의 모든 리프레시 토큰을 삭제합니다.
     * 다음과 같은 상황에서 사용됩니다:
     * - 로그아웃 (해당 사용자의 모든 디바이스에서 로그아웃)
     * - 계정 정지 (보안상 모든 토큰 무효화)
     * - 비밀번호 변경 (보안상 기존 토큰 무효화)
     *
     * @param userId 토큰을 삭제할 사용자 ID
     */
    void deleteByUserId(Long userId);

    /**
     * 토큰 문자열로 삭제
     *
     * 특정 토큰만 삭제합니다.
     * 디바이스별 로그아웃이나 특정 토큰의 강제 만료에 사용됩니다.
     * (예: 모바일 앱에서만 로그아웃, 웹은 로그인 상태 유지)
     *
     * @param token 삭제할 리프레시 토큰 문자열
     */
    void deleteByToken(String token);
}