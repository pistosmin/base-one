/**
 * 사용자 엔티티
 *
 * 커뮤니티 서비스의 사용자 계정 정보를 관리하는 핵심 도메인 엔티티입니다.
 * 인증(로그인), 프로필 관리, 권한 제어, 계정 상태 관리를 담당합니다.
 *
 * 의존성:
 * - BaseTimeEntity: 생성일시/수정일시 자동 관리
 * - UserRole: 사용자 권한(USER/ADMIN) 열거형
 *
 * 사용 위치:
 * - AuthService: 로그인/회원가입 처리
 * - UserDetailsServiceImpl: Spring Security 인증 객체 생성
 * - UserService: 프로필 CRUD
 * - AdminService: 사용자 관리(정지/해제, 권한 변경)
 *
 * DB 테이블: users
 */
package com.community.domain.user.entity;

import com.community.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 엔티티
 *
 * 이메일/비밀번호 기반 인증을 사용하며, 닉네임과 이메일은 유니크 제약조건을 갖습니다.
 * 계정 정지(isActive=false) 시 로그인을 차단하고, 관리자는 사용자 권한을 변경할 수 있습니다.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    /** 사용자 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 이메일 주소 — 로그인 아이디로 사용, 중복 불가 */
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /** 암호화된 비밀번호 — BCrypt 해시 저장 */
    @Column(nullable = false, length = 255)
    private String passwordHash;

    /** 닉네임 — 게시글/댓글에서 표시되는 이름, 중복 불가 */
    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    /** 프로필 소개 — 최대 300자, 선택사항 */
    @Column(length = 300)
    private String bio;

    /** 프로필 이미지 URL — S3/MinIO 업로드 후 URL 저장, 선택사항 */
    @Column(length = 500)
    private String profileImage;

    /** 사용자 권한 — USER(일반) 또는 ADMIN(관리자) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.USER;

    /** 계정 활성화 상태 — false 시 로그인 차단 (정지) */
    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    /** 마지막 로그인 일시 — 로그인 시마다 갱신 */
    private LocalDateTime lastLoginAt;

    /**
     * 프로필 정보 수정
     *
     * 닉네임과 소개를 동시에 변경합니다. 닉네임 중복은 서비스 레이어에서 사전 검증해야 합니다.
     *
     * @param nickname 새 닉네임 (50자 이내, 유니크)
     * @param bio 새 소개 (300자 이내, nullable)
     */
    public void updateProfile(String nickname, String bio) {
        this.nickname = nickname;
        this.bio = bio;
    }

    /**
     * 프로필 이미지 URL 수정
     *
     * 파일 업로드 후 생성된 URL을 저장합니다.
     *
     * @param url S3/MinIO에서 생성된 이미지 URL
     */
    public void updateProfileImage(String url) {
        this.profileImage = url;
    }

    /**
     * 마지막 로그인 시각 갱신
     *
     * 로그인 성공 시 호출하여 현재 시각을 기록합니다.
     * 사용자 활동 추적 및 비활성 사용자 식별에 사용됩니다.
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 계정 정지
     *
     * 관리자가 부적절한 사용자를 일시적으로 차단할 때 사용합니다.
     * 정지된 사용자는 로그인이 불가능하며, 기존 세션도 무효화됩니다.
     */
    public void ban() {
        this.isActive = false;
    }

    /**
     * 계정 정지 해제
     *
     * 정지된 사용자의 접근을 다시 허용합니다.
     */
    public void unban() {
        this.isActive = true;
    }

    /**
     * 사용자 권한 변경
     *
     * 일반 사용자를 관리자로 승격하거나 관리자를 일반 사용자로 강등할 때 사용합니다.
     *
     * @param role 변경할 권한 (USER 또는 ADMIN)
     */
    public void changeRole(UserRole role) {
        this.role = role;
    }
}