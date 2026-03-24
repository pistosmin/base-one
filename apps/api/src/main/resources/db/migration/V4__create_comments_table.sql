-- =============================================
-- V4: 댓글 테이블 생성
--
-- Adjacency List 패턴으로 대댓글을 지원합니다.
-- parent_id가 NULL이면 최상위 댓글,
-- 값이 있으면 해당 댓글에 대한 대댓글입니다.
-- =============================================

CREATE TABLE comments (
    id              BIGSERIAL PRIMARY KEY,                       -- 댓글 고유 ID
    post_id         BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,    -- 소속 게시글 FK
    author_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,    -- 작성자 FK
    parent_id       BIGINT REFERENCES comments(id) ON DELETE CASCADE,          -- 부모 댓글 FK (NULL이면 최상위)
    content         TEXT NOT NULL,                               -- 댓글 내용
    vote_count      INT NOT NULL DEFAULT 0,                      -- 비정규화: 추천 수
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,              -- Soft Delete
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 게시글별 댓글 조회 (게시글 상세 페이지에서 사용)
CREATE INDEX idx_comments_post ON comments(post_id);

-- 부모 댓글별 대댓글 조회 (트리 구조 빌드 시 사용)
CREATE INDEX idx_comments_parent ON comments(parent_id);
