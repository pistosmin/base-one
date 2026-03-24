/**
 * Redis 설정
 *
 * RedisTemplate의 직렬화 방식을 JSON으로 설정합니다.
 * 기본 JDK 직렬화 대신 JSON을 사용하여:
 * - Redis CLI에서 데이터를 사람이 읽을 수 있음
 * - 다른 시스템(Node.js 등)과 호환 가능
 * - 클래스 변경 시 역직렬화 오류가 적음
 *
 * @see application-dev.yml - Redis 호스트/포트 설정
 */
package com.community.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 연결 및 직렬화 설정
 */
@Configuration
public class RedisConfig {

    /**
     * RedisTemplate 빈 등록
     *
     * Key: String 직렬화 (사람이 읽을 수 있는 키)
     * Value: JSON 직렬화 (범용 호환성)
     *
     * @param connectionFactory Spring Boot 자동 설정에 의해 주입되는 Redis 연결 팩토리
     * @return 설정된 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key: String 직렬화 (예: "user:123", "post:cache:456")
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value: JSON 직렬화 (사람이 읽을 수 있고, 범용 호환)
        template.setValueSerializer(GenericJacksonJsonRedisSerializer.builder().build());
        template.setHashValueSerializer(GenericJacksonJsonRedisSerializer.builder().build());

        template.afterPropertiesSet();
        return template;
    }
}
