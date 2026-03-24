/**
 * 모든 엔티티의 공통 시간 필드
 *
 * JPA Auditing을 통해 생성일시(createdAt)와 수정일시(updatedAt)를
 * 자동으로 관리하는 매핑된 슈퍼클래스입니다.
 *
 * 모든 도메인 엔티티는 이 클래스를 상속받아야 합니다:
 *   {@code public class Post extends BaseTimeEntity { ... }}
 *
 * @see JpaConfig - JPA Auditing 활성화 설정
 */
package com.community.global.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 엔티티 공통 시간 필드 (생성일시, 수정일시)
 *
 * {@code @MappedSuperclass}: JPA에서 테이블에 매핑되지 않고, 하위 엔티티에 필드만 상속
 * {@code @EntityListeners}: JPA 이벤트 리스너 등록 (Auditing)
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    /** 생성일시 — 최초 저장 시 자동 설정, 이후 수정 불가 */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /** 수정일시 — 저장/수정 시 자동 갱신 */
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
