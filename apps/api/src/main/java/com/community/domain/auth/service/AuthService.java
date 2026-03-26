/**
 * ====================================================
 * 인증 서비스
 *
 * 회원가입, 로그인, 토큰 갱신, 로그아웃 비즈니스 로직을 담당합니다.
 *
 * 보안 설계:
 * - 비밀번호: BCrypt(cost=12) 해싱
 * - 액세스 토큰: JWT HS256, 15분 유효 (JwtTokenProvider가 생성)
 * - 리프레시 토큰: UUID 기반, 7일 유효, DB 저장 (강제 만료 가능)
 *
 * 트랜잭션 경계:
 * - 읽기 작업: @Transactional(readOnly = true) (성능 최적화)
 * - 쓰기 작업: @Transactional (기본값, 데이터 정합성 보장)
 *
 * 의존성: UserRepository, RefreshTokenRepository, JwtTokenProvider,
 *        BCryptPasswordEncoder, UserMapper
 * 사용 위치: AuthController
 * ====================================================
 */
package com.community.domain.auth.service;

import com.community.domain.auth.dto.request.LoginRequest;
import com.community.domain.auth.dto.request.SignupRequest;
import com.community.domain.auth.dto.request.TokenRefreshRequest;
import com.community.domain.auth.dto.response.TokenResponse;
import com.community.domain.auth.entity.RefreshToken;
import com.community.domain.auth.repository.RefreshTokenRepository;
import com.community.domain.user.entity.User;
import com.community.domain.user.entity.UserRole;
import com.community.domain.user.mapper.UserMapper;
import com.community.domain.user.repository.UserRepository;
import com.community.global.common.exception.BusinessException;
import com.community.global.common.response.ErrorCode;
import com.community.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 인증 서비스 클래스
 *
 * JWT 기반 인증 시스템의 핵심 비즈니스 로직을 구현합니다.
 * 회원가입부터 로그아웃까지의 전체 인증 플로우를 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    /**
     * 리프레시 토큰 만료 시간 (초 단위)
     * application.yml에서 주입받음 (기본값: 604800 = 7일)
     */
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * 액세스 토큰 만료 시간 (밀리초 단위)
     * 응답 시 초 단위로 변환하여 클라이언트에 전달
     */
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /**
     * 회원가입 처리
     *
     * 새 사용자 계정을 생성하고 즉시 로그인 처리합니다.
     * 이메일과 닉네임의 중복을 검사하고, 비밀번호를 안전하게 해싱합니다.
     *
     * @param request 회원가입 요청 정보 (이메일, 비밀번호, 닉네임)
     * @return 토큰 응답 (액세스 토큰, 리프레시 토큰, 사용자 정보)
     * @throws BusinessException 이메일 또는 닉네임이 중복된 경우
     */
    public TokenResponse signup(SignupRequest request) {
        log.info("회원가입 시도: email={}", request.email());

        // 이메일 중복 검사 - 보안상 명확한 에러 메시지 제공
        if (userRepository.existsByEmail(request.email())) {
            log.warn("회원가입 실패 - 이메일 중복: {}", request.email());
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 닉네임 중복 검사 - 사용자 편의를 위해 명확한 에러 메시지 제공
        if (userRepository.existsByNickname(request.nickname())) {
            log.warn("회원가입 실패 - 닉네임 중복: {}", request.nickname());
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        // 비밀번호 BCrypt 해싱 (cost=12, 약 250ms 소요)
        // cost 12는 보안과 성능의 균형을 고려한 권장값
        String hashedPassword = passwordEncoder.encode(request.password());

        // 새 사용자 생성 - 기본 역할은 일반 사용자
        User user = User.builder()
                .email(request.email())
                .passwordHash(hashedPassword)
                .nickname(request.nickname())
                .role(UserRole.USER) // 신규 가입자는 일반 사용자 권한
                .build();

        user = userRepository.save(user);
        log.info("회원가입 성공: userId={}, email={}", user.getId(), user.getEmail());

        // 가입과 동시에 로그인 처리 - 즉시 서비스 이용 가능
        return issueTokens(user);
    }

    /**
     * 로그인 처리
     *
     * 이메일과 비밀번호를 검증하고 JWT 토큰을 발급합니다.
     * 보안상 이메일/비밀번호 오류를 구분하지 않고 동일한 메시지로 응답합니다.
     *
     * @param request 로그인 요청 정보 (이메일, 비밀번호)
     * @return 토큰 응답 (액세스 토큰, 리프레시 토큰, 사용자 정보)
     * @throws BusinessException 인증 정보가 올바르지 않은 경우
     */
    public TokenResponse login(LoginRequest request) {
        log.info("로그인 시도: email={}", request.email());

        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 존재하지 않는 이메일: {}", request.email());
                    // 보안상 이메일이 없다는 정보를 노출하지 않음
                    return new BusinessException(ErrorCode.INVALID_CREDENTIALS);
                });

        // BCrypt 비밀번호 검증
        // matches() 메서드는 내부적으로 salt를 고려하여 안전하게 비교
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("로그인 실패 - 비밀번호 불일치: email={}", request.email());
            // 보안상 비밀번호가 틀렸다는 정보를 노출하지 않음
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 로그인 성공 - 마지막 로그인 시간 업데이트
        user.updateLastLogin();
        log.info("로그인 성공: userId={}, email={}", user.getId(), user.getEmail());

        return issueTokens(user);
    }

    /**
     * 토큰 갱신 처리
     *
     * 리프레시 토큰을 검증하고 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.
     * 기존 리프레시 토큰은 재사용 공격 방지를 위해 삭제됩니다.
     *
     * @param request 토큰 갱신 요청 정보 (리프레시 토큰)
     * @return 토큰 응답 (새로운 액세스 토큰, 리프레시 토큰, 사용자 정보)
     * @throws BusinessException 리프레시 토큰이 유효하지 않거나 만료된 경우
     */
    @Transactional(readOnly = false) // 명시적으로 쓰기 트랜잭션 설정 (토큰 삭제/저장)
    public TokenResponse refresh(TokenRefreshRequest request) {
        log.info("토큰 갱신 요청: token={}", request.refreshToken().substring(0, 10) + "...");

        // 리프레시 토큰 조회
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> {
                    log.warn("토큰 갱신 실패 - 존재하지 않는 리프레시 토큰");
                    return new BusinessException(ErrorCode.EXPIRED_TOKEN);
                });

        // 토큰 만료 여부 확인
        if (refreshToken.isExpired()) {
            log.warn("토큰 갱신 실패 - 만료된 리프레시 토큰: userId={}", refreshToken.getUserId());
            // 만료된 토큰은 DB에서 삭제하여 정리
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }

        // 토큰 소유자 조회
        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> {
                    log.error("토큰 갱신 실패 - 존재하지 않는 사용자: userId={}", refreshToken.getUserId());
                    // 사용자가 삭제된 경우 관련 토큰 정리
                    refreshTokenRepository.deleteByUserId(refreshToken.getUserId());
                    return new BusinessException(ErrorCode.EXPIRED_TOKEN);
                });

        log.info("토큰 갱신 성공: userId={}", user.getId());

        // 새로운 토큰 발급 (기존 토큰 삭제 후 재발급)
        return issueTokens(user);
    }

    /**
     * 로그아웃 처리
     *
     * 사용자의 모든 리프레시 토큰을 삭제하여 강제 로그아웃 처리합니다.
     * 여러 디바이스에서 로그인한 경우 모든 디바이스에서 로그아웃됩니다.
     *
     * @param userId 로그아웃할 사용자 ID (JWT에서 추출)
     */
    public void logout(Long userId) {
        log.info("로그아웃 처리: userId={}", userId);

        // 해당 사용자의 모든 리프레시 토큰 삭제
        // 여러 디바이스에서 로그인한 경우 모든 디바이스에서 로그아웃됨
        refreshTokenRepository.deleteByUserId(userId);

        log.info("로그아웃 완료: userId={}", userId);
    }

    /**
     * JWT 토큰 발급 (액세스 + 리프레시)
     *
     * 새로운 액세스 토큰과 리프레시 토큰을 생성합니다.
     * 보안을 위해 기존 리프레시 토큰은 모두 삭제하고 새로 발급합니다.
     *
     * @param user 토큰을 발급받을 사용자 엔티티
     * @return 토큰 응답 (액세스 토큰, 리프레시 토큰, 사용자 정보)
     */
    private TokenResponse issueTokens(User user) {
        // 보안상 기존 리프레시 토큰 모두 삭제
        // 토큰 재사용 공격 방지 및 동시 로그인 세션 정리
        refreshTokenRepository.deleteByUserId(user.getId());

        // 새로운 액세스 토큰 생성 (JWT, 15분 유효)
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getRole().name()
        );

        // 새로운 리프레시 토큰 생성 (UUID, 7일 유효)
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken();

        // 리프레시 토큰을 DB에 저장하여 추후 검증 및 강제 만료 가능하게 함
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .token(refreshTokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration))
                .build();

        refreshTokenRepository.save(refreshToken);

        // 액세스 토큰 만료 시간을 초 단위로 변환 (클라이언트 편의성)
        long expiresIn = accessTokenExpiration / 1000;

        return new TokenResponse(
                accessToken,
                refreshTokenValue,
                expiresIn,
                userMapper.toProfileResponse(user)
        );
    }
}