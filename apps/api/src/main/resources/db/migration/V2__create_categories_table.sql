-- =============================================
-- V2: 카테고리 테이블 생성
--
-- 게시글 카테고리를 관리하는 정적 테이블입니다.
-- 관리자가 카테고리를 추가/수정할 수 있습니다.
-- 기본 카테고리 4개를 함께 삽입합니다.
-- =============================================

CREATE TABLE categories (
    id              BIGSERIAL PRIMARY KEY,                       -- 카테고리 고유 ID
    name            VARCHAR(50) NOT NULL UNIQUE,                 -- 표시명 (예: "자유")
    slug            VARCHAR(50) NOT NULL UNIQUE,                 -- URL용 식별자 (예: "free")
    description     VARCHAR(200),                                -- 카테고리 설명
    sort_order      INT NOT NULL DEFAULT 0,                      -- 표시 순서 (오름차순 정렬)
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,               -- 활성 여부
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 기본 카테고리 삽입 (서비스 초기 데이터)
INSERT INTO categories (name, slug, description, sort_order) VALUES
    ('자유', 'free', '자유롭게 이야기하는 공간', 1),
    ('질문', 'question', '궁금한 것을 물어보는 공간', 2),
    ('정보', 'info', '유용한 정보를 공유하는 공간', 3),
    ('리뷰', 'review', '리뷰와 후기를 나누는 공간', 4);
