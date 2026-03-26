/**
 * ====================================================
 * 로그인 요청 DTO
 *
 * 사용자 로그인 시 전달되는 이메일과 비밀번호를 담는 record 클래스입니다.
 * Jakarta Bean Validation을 통해 입력값 검증을 수행합니다.
 *
 * 보안 고려사항:
 * - 이메일 형식 검증으로 잘못된 요청 필터링
 * - 비밀번호 길이 제한은 하지 않음 (기존 사용자 호환성)
 *
 * 패키지: com.community.domain.auth.dto.request
 * 사용 위치: AuthController.login()
 * ====================================================
 */
package com.community.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 DTO
 *
 * @param email 로그인 이메일 주소 (User 테이블의 email 컬럼과 매칭)
 * @param password 평문 비밀번호 (서버에서 BCrypt 검증)
 */
public record LoginRequest(
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @NotBlank(message = "이메일을 입력해주세요.")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {}