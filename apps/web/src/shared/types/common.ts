/**
 * ====================================================
 * 공통 타입 정의
 *
 * 애플리케이션 전반에서 사용되는 공통 타입과 인터페이스를 정의합니다.
 * API 페이지네이션 요청/응답, 정렬 방향 등을 포함합니다.
 *
 * 의존성: 없음 (순수 타입 정의)
 * 사용 위치: features/post, features/comment 등 목록 조회가 필요한 모든 feature
 * ====================================================
 */

/** 페이지네이션 요청 파라미터 */
export interface PageRequest {
  page?: number;
  size?: number;
  sort?: string;
}

/** 정렬 방향 (게시글 목록 조회 시 사용) */
export type SortDirection = 'LATEST' | 'POPULAR';