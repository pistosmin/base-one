/**
 * ====================================================
 * 회원가입 요청 DTO
 *
 * 사용자 회원가입 시 전달되는 데이터를 담는 record 클래스입니다.
 * Jakarta Bean Validation을 통해 입력값 검증을 수행합니다.
 *
 * 검증 규칙:
 * - 이메일: 형식 검증, 필수 입력
 * - 비밀번호: 최소 8자, 필수 입력
 * - 닉네임: 2~20자, 필수 입력
 *
 * 패키지: com.community.domain.auth.dto.request
 * 사용 위치: AuthController.signup()
 * ====================================================
 */
package com.community.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청 DTO
 *
 * @param email 이메일 주소 (로그인 ID로 사용, 형식 검증 필수)
 * @param password 비밀번호 (BCrypt로 해싱하여 저장, 최소 8자 이상)
 * @param nickname 사용자 닉네임 (게시글/댓글에 표시, 2~20자)
 */
public record SignupRequest(
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @NotBlank(message = "이메일을 입력해주세요.")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        String password,

        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
        String nickname
) {}