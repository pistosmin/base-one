/**
 * 전역 예외 처리 핸들러
 *
 * 애플리케이션에서 발생하는 모든 예외를 가로채서
 * 통일된 ApiResponse 형식으로 변환합니다.
 *
 * 처리하는 예외 유형:
 * 1. BusinessException → ErrorCode 기반 에러 응답
 * 2. MethodArgumentNotValidException → Bean Validation 검증 에러
 * 3. Exception → 500 내부 서버 에러 (예상치 못한 예외)
 *
 * @see BusinessException - 비즈니스 예외
 * @see ApiResponse - 통합 응답 래퍼
 */
package com.community.global.common.exception;

import com.community.global.common.response.ApiResponse;
import com.community.global.common.response.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 모든 예외를 가로채서 일관된 에러 응답을 생성하는 핸들러
 *
 * {@code @RestControllerAdvice}: 모든 Controller에서 던진 예외를 이 클래스가 처리
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 비즈니스 예외 처리
     *
     * Service 계층에서 throw한 BusinessException을 잡아서
     * ErrorCode에 정의된 HTTP 상태와 메시지로 응답합니다.
     *
     * @param e 비즈니스 예외
     * @return ErrorCode에 맞는 HTTP 상태와 에러 응답
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("비즈니스 예외 발생: {} - {}", e.getErrorCode().getCode(), e.getMessage());

        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(errorCode));
    }

    /**
     * Bean Validation 검증 에러 처리
     *
     * DTO에 {@code @Valid}로 검증한 결과 유효성 검사가 실패했을 때 처리합니다.
     * 첫 번째 필드 에러의 메시지를 사용자에게 반환합니다.
     *
     * @param e 유효성 검증 예외
     * @return 400 Bad Request와 검증 에러 메시지
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        // 첫 번째 필드 에러의 기본 메시지를 사용
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("입력값이 올바르지 않습니다.");

        log.warn("유효성 검증 실패: {}", message);

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.INVALID_INPUT, message));
    }

    /**
     * 예상치 못한 예외 처리 (500 Internal Server Error)
     *
     * 위에서 처리되지 않은 모든 예외를 잡아서 500 에러로 응답합니다.
     * 스택 트레이스를 로깅하여 디버깅에 활용합니다.
     *
     * @param e 예상치 못한 예외
     * @return 500 Internal Server Error 응답
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("예상치 못한 예외 발생", e);

        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR));
    }
}
