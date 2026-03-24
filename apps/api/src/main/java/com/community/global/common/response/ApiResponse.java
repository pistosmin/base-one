/**
 * 통합 API 응답 래퍼
 *
 * 모든 REST API 응답을 동일한 형식으로 감싸서 반환합니다.
 * 프론트엔드에서 일관된 응답 파싱이 가능합니다.
 *
 * 응답 형식:
 * - 성공: { "success": true, "data": {...}, "error": null }
 * - 실패: { "success": false, "data": null, "error": { "code": "...", "message": "..." } }
 *
 * record 클래스를 사용하여 불변성과 간결함을 확보합니다.
 *
 * @param <T> 응답 데이터의 타입
 * @see GlobalExceptionHandler - 에러 발생 시 이 래퍼를 사용
 */
package com.community.global.common.response;

/**
 * API 통합 응답 래퍼
 *
 * @param success 요청 성공 여부
 * @param data 성공 시 응답 데이터 (실패 시 null)
 * @param error 실패 시 에러 정보 (성공 시 null)
 */
public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorDetail error
) {
    /**
     * 에러 상세 정보
     *
     * @param code 에러 코드 (ErrorCode enum의 code 값, 예: "POST_NOT_FOUND")
     * @param message 사용자에게 표시할 에러 메시지
     */
    public record ErrorDetail(String code, String message) {
    }

    /**
     * 성공 응답 생성 (데이터 포함)
     *
     * @param data 응답 데이터
     * @return ApiResponse 성공 응답
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /**
     * 성공 응답 생성 (데이터 없음)
     *
     * @return ApiResponse 성공 응답 (data: null)
     */
    public static <T> ApiResponse<T> successWithoutData() {
        return new ApiResponse<>(true, null, null);
    }

    /**
     * 에러 응답 생성 (사용자 정의 메시지 포함)
     *
     * @param errorCode 에러 코드
     * @param message 사용자 정의 에러 메시지
     * @return ApiResponse 에러 응답
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, null, new ErrorDetail(errorCode.getCode(), message));
    }

    /**
     * ErrorCode를 사용한 에러 응답 생성
     *
     * @param errorCode 정의된 에러 코드 enum
     * @return ApiResponse 에러 응답
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, null,
                new ErrorDetail(errorCode.getCode(), errorCode.getMessage()));
    }
}
