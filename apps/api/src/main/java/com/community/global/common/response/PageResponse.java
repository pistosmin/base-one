/**
 * 페이징 응답 래퍼
 *
 * Spring Data의 Page 객체를 프론트엔드에서 사용하기 편한 형태로 변환합니다.
 * TanStack Query의 useInfiniteQuery와 함께 사용하기 적합한 구조입니다.
 *
 * 응답 형식:
 * {
 *   "content": [...],
 *   "totalElements": 150,
 *   "totalPages": 8,
 *   "page": 0,
 *   "size": 20,
 *   "hasNext": true,
 *   "hasPrevious": false
 * }
 *
 * @param <T> 페이지 내용의 타입
 * @see ApiResponse - 이 응답을 ApiResponse.data에 담아서 반환
 */
package com.community.global.common.response;

import org.springframework.data.domain.Page;
import java.util.List;

/**
 * 페이지네이션 응답 래퍼
 *
 * @param content 현재 페이지의 데이터 목록
 * @param totalElements 전체 데이터 수
 * @param totalPages 전체 페이지 수
 * @param page 현재 페이지 번호 (0부터 시작)
 * @param size 페이지당 데이터 수
 * @param hasNext 다음 페이지 존재 여부 (무한 스크롤에서 사용)
 * @param hasPrevious 이전 페이지 존재 여부
 */
public record PageResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int page,
        int size,
        boolean hasNext,
        boolean hasPrevious
) {
    /**
     * Spring Data Page를 PageResponse로 변환하는 팩토리 메서드
     *
     * @param page Spring Data의 Page 객체
     * @return 프론트엔드 친화적인 PageResponse
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
