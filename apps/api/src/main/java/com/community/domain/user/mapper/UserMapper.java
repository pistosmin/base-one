/**
 * 사용자 매퍼
 *
 * User 엔티티와 DTO 간의 변환을 담당합니다.
 * MapStruct가 컴파일 시 자동으로 구현 클래스를 생성합니다.
 *
 * 패키지: com.community.domain.user.mapper
 * 사용 위치: AuthService (TokenResponse 생성 시 User → UserProfileResponse 변환)
 */
package com.community.domain.user.mapper;

import com.community.domain.user.dto.response.UserProfileResponse;
import com.community.domain.user.dto.response.UserSummaryResponse;
import com.community.domain.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    /** User 엔티티 → UserProfileResponse 변환 (프로필 조회, 로그인 응답) */
    UserProfileResponse toProfileResponse(User user);

    /** User 엔티티 → UserSummaryResponse 변환 (게시글/댓글 작성자 표시) */
    UserSummaryResponse toSummaryResponse(User user);
}