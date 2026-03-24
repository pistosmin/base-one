-- =============================================
-- V7: 보조 테이블 생성 (미디어, 신고, 리프레시 토큰)
--
-- media: 업로드된 파일 메타데이터 (실제 파일은 MinIO에 저장)
-- reports: 사용자/게시글/댓글 신고 접수 및 처리
-- refresh_tokens: JWT 리프레시 토큰 관리 (로그인 유지)
-- =============================================

-- 미디어 (업로드 파일) 테이블
CREATE TABLE media (
    id              BIGSERIAL PRIMARY KEY,                       -- 미디어 고유 ID
    uploader_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,    -- 업로더 FK
    original_name   VARCHAR(255) NOT NULL,                       -- 원본 파일명
    stored_path     VARCHAR(500) NOT NULL,                       -- MinIO 저장 경로
    thumbnail_path  VARCHAR(500),                                -- 썸네일 경로 (이미지인 경우)
    file_size       BIGINT NOT NULL,                             -- 바이트 단위 파일 크기
    mime_type       VARCHAR(100) NOT NULL,                       -- MIME 타입 (예: image/jpeg)
    width           INT,                                         -- 이미지 너비 (픽셀, 이미지인 경우)
    height          INT,                                         -- 이미지 높이 (픽셀)
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 신고 테이블
CREATE TABLE reports (
    id              BIGSERIAL PRIMARY KEY,                       -- 신고 고유 ID
    reporter_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,    -- 신고자 FK
    target_type     VARCHAR(20) NOT NULL CHECK (target_type IN ('USER', 'POST', 'COMMENT')),  -- 신고 대상 유형
    target_id       BIGINT NOT NULL,                             -- 신고 대상 ID
    reason          VARCHAR(50) NOT NULL CHECK (reason IN ('SPAM', 'ABUSE', 'INAPPROPRIATE', 'COPYRIGHT', 'OTHER')),  -- 신고 사유
    description     VARCHAR(500),                                -- 상세 설명 (선택)
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'RESOLVED', 'DISMISSED')),   -- 처리 상태
    resolved_by     BIGINT REFERENCES users(id),                 -- 처리한 관리자 FK
    resolved_at     TIMESTAMP WITH TIME ZONE,                    -- 처리 일시
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 신고 상태별 조회 (관리자 대시보드: 미처리 신고 최신순)
CREATE INDEX idx_reports_status ON reports(status, created_at DESC);

-- 리프레시 토큰 테이블 (JWT 토큰 갱신용)
CREATE TABLE refresh_tokens (
    id              BIGSERIAL PRIMARY KEY,                       -- 토큰 고유 ID
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,    -- 토큰 소유자 FK
    token           VARCHAR(500) NOT NULL UNIQUE,                -- 리프레시 토큰 값 (랜덤 UUID)
    expires_at      TIMESTAMP WITH TIME ZONE NOT NULL,           -- 만료 시각
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 사용자별 리프레시 토큰 조회 (로그아웃 시 삭제, 토큰 갱신 시 검증)
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);

-- 토큰값으로 검증 (리프레시 요청 시 사용)
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
