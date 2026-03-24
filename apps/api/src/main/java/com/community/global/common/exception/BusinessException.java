/**
 * 비즈니스 예외 클래스
 *
 * 비즈니스 로직에서 발생하는 예외를 표현합니다.
 * ErrorCode와 함께 던져서, GlobalExceptionHandler가 일관된 에러 응답을 생성합니다.
 *
 * 사용 예:
 *   throw new BusinessException(ErrorCode.POST_NOT_FOUND);
 *   throw new BusinessException(ErrorCode.UNAUTHORIZED);
 *
 * @see ErrorCode - 에러 코드 정의
 * @see GlobalExceptionHandler - 이 예외를 잡아서 ApiResponse로 변환
 */
package com.community.global.common.exception;

import com.community.global.common.response.ErrorCode;
import lombok.Getter;

/**
 * 비즈니스 예외
 *
 * RuntimeException을 확장하여 checked exception 없이 사용 가능합니다.
 * Service 계층에서 비즈니스 규칙 위반 시 던집니다.
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 이 예외에 해당하는 에러 코드 */
    private final ErrorCode errorCode;

    /**
     * ErrorCode로 비즈니스 예외 생성
     *
     * @param errorCode 에러 코드 (HTTP 상태, 코드, 메시지 포함)
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode와 커스텀 메시지로 비즈니스 예외 생성
     *
     * @param errorCode 에러 코드
     * @param message 커스텀 메시지 (ErrorCode 기본 메시지 대신 사용)
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
