/**
 * ====================================================
 * API 응답 공통 타입 정의
 *
 * 백엔드의 통합 응답 형식(ApiResponse<T>)과 1:1 매핑되는 타입입니다.
 * 모든 API 호출 결과는 이 형식으로 반환됩니다.
 *
 * ApiError를 통해 에러를 타입 안전하게 처리하고,
 * extractData 헬퍼로 성공/실패를 선언적으로 처리합니다.
 *
 * 의존성: 없음 (순수 타입/클래스 정의)
 * 사용 위치: shared/api/axiosInstance, features/auth/api/*.ts 등
 * ====================================================
 */

/** 페이지네이션 응답 형식 (목록 API에서 사용) */
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

/** 백엔드 에러 상세 정보 */
interface ErrorDetail {
  code: string;
  message: string;
}

/**
 * 백엔드 통합 응답 래퍼
 *
 * 성공: { success: true, data: T, error: null }
 * 실패: { success: false, data: null, error: { code, message } }
 */
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error: ErrorDetail | null;
}

/**
 * API 에러 클래스
 *
 * 백엔드에서 success: false 응답이 오거나 네트워크 에러 발생 시 throw됩니다.
 * 컴포넌트에서 instanceof ApiError로 타입 가드 가능합니다.
 */
export class ApiError extends Error {
  constructor(
    public readonly code: string,
    message: string,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

/**
 * API 응답에서 데이터를 추출하는 헬퍼 함수
 *
 * success가 false이면 ApiError를 throw하여 TanStack Query의 onError 핸들러로 전달합니다.
 * axiosInstance의 응답 인터셉터와 함께 사용됩니다.
 *
 * @param response 백엔드 ApiResponse 래퍼
 * @returns 성공 시 data 필드의 값
 * @throws ApiError 실패 시 (success: false)
 */
export function extractData<T>(response: ApiResponse<T>): T {
  if (!response.success || response.error) {
    throw new ApiError(
      response.error?.code ?? 'UNKNOWN_ERROR',
      response.error?.message ?? '알 수 없는 오류가 발생했습니다.',
    );
  }
  return response.data;
}