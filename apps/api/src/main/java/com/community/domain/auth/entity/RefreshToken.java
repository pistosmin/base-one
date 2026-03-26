/**
 * ====================================================
 * 리프레시 토큰 엔티티
 *
 * JWT 리프레시 토큰을 DB에 저장하여 토큰 갱신과 로그아웃을 관리합니다.
 * 액세스 토큰은 무상태(stateless)이지만, 리프레시 토큰은 DB에 저장하여
 * 강제 만료(로그아웃, 계정 정지)가 가능합니다.
 *
 * DB 테이블: refresh_tokens (V7__create_supporting_tables.sql)
 * 패키지: com.community.domain.auth.entity
 * 사용 위치: AuthService.login(), AuthService.refresh(), AuthService.logout()
 * ====================================================
 */
package com.community.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 리프레시 토큰 엔티티
 *
 * JWT 기반 인증에서 리프레시 토큰을 관리합니다.
 * 액세스 토큰 재발급과 로그아웃 처리를 위해 DB에 저장됩니다.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RefreshToken {

    /**
     * 리프레시 토큰 고유 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 토큰 소유 사용자 ID
     *
     * User 엔티티와 연관관계를 맺지 않고 ID만 저장하는 이유:
     * 1. 성능: JOIN 쿼리 없이 빠른 조회 가능
     * 2. 단순화: 토큰 관리 로직에서 User 정보가 불필요
     * 3. 확장성: 다른 서비스로 분리 시 의존성 최소화
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 리프레시 토큰 문자열
     *
     * UUID 기반으로 생성되며, 고유성을 보장합니다.
     * length=500: UUID + 추후 확장성 고려
     */
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    /**
     * 토큰 만료 시각
     *
     * 이 시각이 지나면 토큰을 사용할 수 없으며,
     * 새로운 토큰 발급이 필요합니다.
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * 토큰 생성 시각
     *
     * 토큰이 언제 발급되었는지 추적하기 위한 필드입니다.
     * 보안 로그 및 통계 분석에 활용됩니다.
     */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 토큰 만료 여부 확인
     *
     * 현재 시각이 만료 시각을 지났는지 확인합니다.
     * 토큰 갱신 요청 시 유효성 검사에 사용됩니다.
     *
     * @return 토큰이 만료되었으면 true, 아니면 false
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}