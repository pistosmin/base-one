/**
 * Spring Security UserDetailsService 구현체
 *
 * 스프링 시큐리티의 인증 과정에서 사용자 정보를 로드합니다.
 * JWT 방식에서는 주로 비밀번호 인증(로그인) 시에만 사용되며,
 * 일반적인 API 요청에서는 JwtAuthFilter가 인증을 담당합니다.
 *
 * 사용자 엔티티를 Spring Security의 UserDetails로 변환하여 반환합니다.
 *
 * 패키지: com.community.global.security
 * 의존성: spring-security-core, domain.user
 * 사용 위치: 로그인 인증 시 AuthenticationManager
 */
package com.community.global.security;

import com.community.domain.user.entity.User;
import com.community.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 상세 정보 서비스
 *
 * Spring Security의 인증 과정에서 이메일을 기반으로 사용자 정보를 조회합니다.
 * 조회된 User 엔티티를 UserDetails 객체로 변환하여 반환합니다.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 이메일로 사용자 정보 조회
     *
     * Spring Security의 인증 매니저가 호출하는 메서드입니다.
     * 이메일을 username으로 사용하여 사용자를 조회하고,
     * User 엔티티를 Spring Security의 UserDetails로 변환합니다.
     *
     * @param email 사용자 이메일 (username 역할)
     * @return UserDetails 객체 (Spring Security 인증용)
     * @throws UsernameNotFoundException 해당 이메일의 사용자가 존재하지 않을 때
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "해당 이메일의 사용자를 찾을 수 없습니다: " + email
                ));

        // User 엔티티를 Spring Security UserDetails로 변환
        // - username: 이메일 주소
        // - password: 해시된 비밀번호
        // - roles: 사용자 권한 (USER, ADMIN 등)
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())           // 로그인 ID로 이메일 사용
                .password(user.getPasswordHash())    // BCrypt 해시된 비밀번호
                .roles(user.getRole().name())        // 권한 (ROLE_ 접두어 자동 추가)
                .accountExpired(false)               // 계정 만료 여부 (현재 미사용)
                .accountLocked(false)                // 계정 잠금 여부 (현재 미사용)
                .credentialsExpired(false)           // 자격증명 만료 여부 (현재 미사용)
                .disabled(false)                     // 계정 비활성화 여부 (현재 미사용)
                .build();
    }
}