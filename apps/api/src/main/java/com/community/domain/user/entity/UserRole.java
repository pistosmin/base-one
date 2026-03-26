/**
 * 사용자 역할 열거형
 *
 * 커뮤니티 서비스의 사용자 권한을 구분합니다.
 * USER: 일반 사용자 (게시글/댓글 작성, 추천 등)
 * ADMIN: 관리자 (사용자 관리, 게시글 관리, 신고 처리 등)
 *
 * DB에 VARCHAR(20)로 저장됩니다. (JPA @Enumerated(EnumType.STRING))
 *
 * 패키지: com.community.domain.user.entity
 * 사용 위치: User 엔티티, Spring Security 권한 확인
 */
package com.community.domain.user.entity;

public enum UserRole {
    /** 일반 사용자: 게시글/댓글 CRUD, 추천, 북마크 가능 */
    USER,
    /** 관리자: USER 권한 + 사용자 관리, 게시글 관리, 신고 처리 */
    ADMIN
}