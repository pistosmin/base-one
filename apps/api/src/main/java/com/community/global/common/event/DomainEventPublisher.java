package com.community.global.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 이벤트를 발행하는 공통 래퍼 클래스
 * 
 * 모놀리스 구조에서는 Spring ApplicationEventPublisher를 사용하지만,
 * 향후 MSA나 비동기 인프라(Kafka/RabbitMQ) 도입 시 외부 의존성 수정을 
 * 최소화하기 위해 이 클래스의 내부 로직만 브로커 전송으로 변경하면 됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(DomainEvent event) {
        log.info("Publishing event: {} (ID: {})", event.getClass().getSimpleName(), event.getEventId());
        applicationEventPublisher.publishEvent(event);
    }
}
