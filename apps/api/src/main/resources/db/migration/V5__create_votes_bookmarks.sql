-- =============================================
-- V5: 추천(votes)과 북마크(bookmarks) 테이블 생성
--
-- votes: 게시글/댓글에 대한 추천 (좋아요)
--   - target_type으로 게시글(POST)과 댓글(COMMENT)을 구분
--   - UNIQUE 제약으로 동일 사용자의 중복 추천 방지
--
-- bookmarks: 게시글 저장 (나중에 보기)
--   - UNIQUE 제약으로 동일 게시글 중복 북마크 방지
-- =============================================

-- 추천 테이블 (게시글/댓글 통합)
CREATE TABLE votes (
    id              BIGSERIAL PRIMARY KEY,                       -- 추천 고유 ID
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,    -- 추천한 사용자 FK
    target_type     VARCHAR(20) NOT NULL CHECK (target_type IN ('POST', 'COMMENT')),  -- 대상 유형
    target_id       BIGINT NOT NULL,                             -- 대상 ID (posts.id 또는 comments.id)
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    -- 동일 사용자가 같은 대상에 중복 추천 방지
    UNIQUE(user_id, target_type, target_id)
);

-- 대상별 추천 조회 (특정 게시글/댓글의 추천 목록)
CREATE INDEX idx_votes_target ON votes(target_type, target_id);

-- 북마크 테이블
CREATE TABLE bookmarks (
    id              BIGSERIAL PRIMARY KEY,                       -- 북마크 고유 ID
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,    -- 북마크한 사용자 FK
    post_id         BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,    -- 북마크한 게시글 FK
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    -- 동일 사용자가 같은 게시글 중복 북마크 방지
    UNIQUE(user_id, post_id)
);
