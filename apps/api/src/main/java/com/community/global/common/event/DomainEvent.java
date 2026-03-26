package com.community.global.common.event;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 프로젝트 내 모든 도메인 이벤트의 최상위 추상 클래스
 * 
 * 차후 비동기 MQ(Kafka, Redis Stream 등)로 확장하기 쉽도록
 * 공통된 식별자와 발생 시간을 포함합니다.
 */
@Getter
public abstract class DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredAt;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
    }
}
