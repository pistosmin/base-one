-- =============================================
-- V3: 게시글 테이블 생성
--
-- 커뮤니티의 핵심 콘텐츠인 게시글을 저장합니다.
-- vote_count, comment_count는 비정규화 컬럼으로,
-- 목록 조회 시 매번 COUNT 쿼리를 실행하지 않기 위해 유지합니다.
-- Soft Delete 방식으로 삭제합니다 (is_deleted = TRUE).
-- =============================================

CREATE TABLE posts (
    id              BIGSERIAL PRIMARY KEY,                       -- 게시글 고유 ID
    author_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE, -- 작성자 FK (사용자 삭제 시 게시글도 삭제)
    category_id     BIGINT REFERENCES categories(id) ON DELETE SET NULL,    -- 카테고리 FK (카테고리 삭제 시 NULL)
    title           VARCHAR(200) NOT NULL,                       -- 게시글 제목
    content         TEXT NOT NULL,                               -- 게시글 본문 (TipTap 에디터의 HTML)
    summary         VARCHAR(300),                                -- 목록 미리보기용 요약 (content에서 추출)
    thumbnail_url   VARCHAR(500),                                -- 대표 이미지 URL (선택)
    view_count      INT NOT NULL DEFAULT 0,                      -- 조회수
    vote_count      INT NOT NULL DEFAULT 0,                      -- 비정규화: 추천 수
    comment_count   INT NOT NULL DEFAULT 0,                      -- 비정규화: 댓글 수
    is_pinned       BOOLEAN NOT NULL DEFAULT FALSE,              -- 관리자 공지 고정 여부
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,              -- Soft Delete 여부
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 작성자별 게시글 조회 (내 글 목록)
CREATE INDEX idx_posts_author ON posts(author_id);

-- 카테고리별 게시글 필터링
CREATE INDEX idx_posts_category ON posts(category_id);

-- 최신순 정렬 (기본 글 목록) — 삭제되지 않은 글만 대상 (조건부 인덱스)
CREATE INDEX idx_posts_created ON posts(created_at DESC) WHERE is_deleted = FALSE;

-- 인기순 정렬 (추천 많은 순) — 삭제되지 않은 글만 대상
CREATE INDEX idx_posts_vote ON posts(vote_count DESC) WHERE is_deleted = FALSE;
