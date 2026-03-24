/**
 * 커뮤니티 웹앱 메인 애플리케이션 진입점
 *
 * Spring Boot 4.0.3 기반의 REST API 서버입니다.
 * Virtual Threads를 활성화하여 높은 동시성을 확보합니다.
 *
 * @see application.yml - 공통 설정
 * @see application-dev.yml - 개발 환경 설정
 */
package com.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 커뮤니티 웹앱 메인 클래스
 *
 * {@code @SpringBootApplication}은 다음 3개 어노테이션을 합친 것:
 * - {@code @Configuration}: 스프링 설정 파일로 등록
 * - {@code @EnableAutoConfiguration}: 자동 설정 활성화
 * - {@code @ComponentScan}: com.community 패키지 하위 모든 빈 스캔
 */
@SpringBootApplication
public class CommunityApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }
}
