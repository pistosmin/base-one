-- =============================================
-- V6: 알림 테이블 생성
--
-- 인앱 알림(댓글, 추천, 팔로우, 관리자 공지)을 저장합니다.
-- recipient_id + is_read + created_at 복합 인덱스로
-- "내 안읽은 알림 최신순" 조회를 최적화합니다.
-- =============================================

CREATE TABLE notifications (
    id              BIGSERIAL PRIMARY KEY,                       -- 알림 고유 ID
    recipient_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,    -- 알림 수신자 FK
    actor_id        BIGINT REFERENCES users(id) ON DELETE SET NULL,            -- 알림 발생자 FK (시스템 알림은 NULL)
    type            VARCHAR(30) NOT NULL CHECK (type IN ('COMMENT', 'VOTE', 'FOLLOW', 'ADMIN_NOTICE')),  -- 알림 유형
    target_type     VARCHAR(20),                                 -- 대상 유형 (POST, COMMENT 등)
    target_id       BIGINT,                                      -- 대상 ID
    message         VARCHAR(300) NOT NULL,                       -- 알림 메시지 (예: "홍길동님이 댓글을 달았습니다")
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,              -- 읽음 여부
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 내 알림 목록 조회 최적화
-- 쿼리 패턴: WHERE recipient_id = ? ORDER BY is_read ASC, created_at DESC
-- 안읽은 알림을 먼저, 그 안에서 최신순으로 정렬
CREATE INDEX idx_notifications_recipient ON notifications(recipient_id, is_read, created_at DESC);
