/**
 * 사용자 Repository
 *
 * Spring Data JPA를 사용한 사용자 데이터 접근 계층입니다.
 * 인증(이메일 조회), 중복 검증(이메일/닉네임), 프로필 조회에 사용됩니다.
 *
 * 패키지: com.community.domain.user.repository
 * 사용 위치: AuthService, UserService, UserDetailsServiceImpl
 */
package com.community.domain.user.repository;

import com.community.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /** 이메일로 사용자 조회 — 로그인, UserDetailsService에서 사용 */
    Optional<User> findByEmail(String email);

    /** 이메일 중복 여부 확인 — 회원가입 시 중복 검증 */
    boolean existsByEmail(String email);

    /** 닉네임 중복 여부 확인 — 회원가입, 프로필 수정 시 중복 검증 */
    boolean existsByNickname(String nickname);
}