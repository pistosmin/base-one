/**
 * JPA Auditing 설정
 *
 * JPA Auditing 기능을 활성화하여 엔티티의 생성일시(createdAt)와
 * 수정일시(updatedAt)를 자동으로 관리합니다.
 *
 * {@code @EnableJpaAuditing}을 별도 설정 클래스에 분리한 이유:
 * - 테스트 시 JPA Auditing을 선택적으로 비활성화할 수 있음
 * - 메인 애플리케이션 클래스의 책임을 최소화
 *
 * @see BaseTimeEntity - 이 설정이 적용되는 기본 엔티티 클래스
 */
package com.community.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 감사(Auditing) 기능 활성화 설정
 *
 * 이 설정이 있어야 {@code @CreatedDate}, {@code @LastModifiedDate}가 동작합니다.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
