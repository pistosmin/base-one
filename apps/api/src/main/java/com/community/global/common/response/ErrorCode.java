/**
 * 에러 코드 열거형
 *
 * 모든 비즈니스 에러 코드를 중앙에서 관리합니다.
 * 각 에러 코드는 HTTP 상태 코드, 에러 코드 문자열, 사용자 메시지를 포함합니다.
 *
 * 사용 방법:
 *   throw new BusinessException(ErrorCode.POST_NOT_FOUND);
 *
 * 새 에러 코드 추가 시:
 *   1. 이 enum에 새 상수를 추가
 *   2. BusinessException과 함께 사용
 *   3. GlobalExceptionHandler가 자동으로 처리
 *
 * @see BusinessException - 이 에러 코드와 함께 던지는 예외
 * @see GlobalExceptionHandler - 에러 응답으로 변환하는 핸들러
 */
package com.community.global.common.response;

import org.springframework.http.HttpStatus;

/**
 * 커뮤니티 웹앱 에러 코드
 *
 * 명명 규칙: {도메인}_{에러유형} (예: POST_NOT_FOUND, AUTH_EXPIRED_TOKEN)
 */
public enum ErrorCode {

    // ── 공통 에러 ──
    /** 입력값 검증 실패 (예: 필수 필드 누락, 형식 불일치) */
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "입력값이 올바르지 않습니다."),
    /** 내부 서버 에러 (예상치 못한 예외) */
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 내부 오류가 발생했습니다."),

    // ── 인증 에러 ──
    /** 이메일 또는 비밀번호 불일치 */
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),
    /** JWT 토큰 만료 */
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_EXPIRED_TOKEN", "인증 토큰이 만료되었습니다. 다시 로그인해주세요."),
    /** 유효하지 않은 JWT 토큰 */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_TOKEN", "유효하지 않은 인증 토큰입니다."),
    /** 권한 부족 (인가 실패) */
    UNAUTHORIZED(HttpStatus.FORBIDDEN, "AUTH_UNAUTHORIZED", "이 작업을 수행할 권한이 없습니다."),

    // ── 사용자 에러 ──
    /** 이미 등록된 이메일 */
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER_DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다."),
    /** 이미 등록된 닉네임 */
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "USER_DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다."),
    /** 사용자를 찾을 수 없음 */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),

    // ── 게시글 에러 ──
    /** 게시글을 찾을 수 없음 */
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "게시글을 찾을 수 없습니다."),

    // ── 댓글 에러 ──
    /** 댓글을 찾을 수 없음 */
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다."),

    // ── 카테고리 에러 ──
    /** 카테고리를 찾을 수 없음 */
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다."),

    // ── 미디어 에러 ──
    /** 지원하지 않는 파일 형식 */
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "MEDIA_UNSUPPORTED_TYPE", "지원하지 않는 파일 형식입니다."),
    /** 파일 크기 초과 */
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "MEDIA_SIZE_EXCEEDED", "파일 크기가 제한을 초과했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
