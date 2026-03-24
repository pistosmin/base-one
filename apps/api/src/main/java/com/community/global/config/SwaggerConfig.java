/**
 * Swagger/OpenAPI 문서 설정
 *
 * SpringDoc OpenAPI를 사용하여 REST API 문서를 자동 생성합니다.
 * Swagger UI: http://localhost:8080/swagger-ui.html
 * OpenAPI JSON: http://localhost:8080/v3/api-docs
 *
 * @see application.yml - springdoc 관련 설정
 */
package com.community.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI API 문서 자동 생성 설정
 */
@Configuration
public class SwaggerConfig {

    /**
     * OpenAPI 3.0 기본 정보 설정
     *
     * Swagger UI에 표시되는 API 문서의 제목, 설명, 버전 등을 정의합니다.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("커뮤니티 웹앱 API")
                        .description("모바일 퍼스트 커뮤니티 웹앱의 REST API 문서입니다.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Community Team")));
    }
}
