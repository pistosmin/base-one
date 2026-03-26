/**
 * 사용자 요약 정보 응답 DTO
 *
 * 게시글/댓글 작성자 표시용 최소 사용자 정보를 담은 응답 객체입니다.
 * 프로필 이미지, 닉네임 등 UI에서 사용자를 식별하는데 필요한 핵심 정보만 포함합니다.
 *
 * 목적: 게시글/댓글 작성자 표시용 최소 사용자 정보
 * 사용 위치:
 * - PostDetailResponse: 게시글 작성자 필드
 * - CommentResponse: 댓글 작성자 필드
 * - NotificationResponse: 알림 발송자 정보
 *
 * 패키지: com.community.domain.user.dto.response
 * 변환: UserMapper.toSummaryResponse(User) → UserSummaryResponse
 */
package com.community.domain.user.dto.response;

public record UserSummaryResponse(
    /** 사용자 고유 식별자 */
    Long id,
    /** 닉네임 — 게시글/댓글에서 표시되는 이름 */
    String nickname,
    /** 프로필 이미지 URL (nullable) — 아바타 이미지 표시용 */
    String profileImage
) {}