/**
 * 사용자 프로필 응답 DTO
 *
 * 사용자의 상세 프로필 정보를 클라이언트에 전달하는 응답 객체입니다.
 * 로그인/회원가입 성공 시, 프로필 조회 API에서 사용됩니다.
 *
 * 목적: 사용자 프로필 정보 응답 (상세 정보 포함)
 * 사용 위치:
 * - AuthController: 로그인/회원가입 응답의 user 필드
 * - UserController: 프로필 조회 API 응답
 * - AdminController: 사용자 관리 상세 정보
 *
 * 패키지: com.community.domain.user.dto.response
 * 변환: UserMapper.toProfileResponse(User) → UserProfileResponse
 */
package com.community.domain.user.dto.response;

import com.community.domain.user.entity.UserRole;

import java.time.LocalDateTime;

public record UserProfileResponse(
    /** 사용자 고유 식별자 */
    Long id,
    /** 이메일 주소 */
    String email,
    /** 닉네임 */
    String nickname,
    /** 프로필 소개 (nullable) */
    String bio,
    /** 프로필 이미지 URL (nullable) */
    String profileImage,
    /** 사용자 권한 (USER/ADMIN) */
    UserRole role,
    /** 계정 생성일시 */
    LocalDateTime createdAt
) {}