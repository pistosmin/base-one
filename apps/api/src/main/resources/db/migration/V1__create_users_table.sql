-- =============================================
-- V1: 사용자 테이블 생성
--
-- 서비스의 핵심 엔티티로, 인증/인가, 게시글 작성,
-- 프로필 관리, 관리자 기능의 기반이 됩니다.
-- =============================================

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,                       -- 사용자 고유 ID (자동 증가)
    email           VARCHAR(255) NOT NULL UNIQUE,                -- 로그인 이메일 (고유)
    password_hash   VARCHAR(255) NOT NULL,                       -- BCrypt로 해싱된 비밀번호
    nickname        VARCHAR(50) NOT NULL UNIQUE,                 -- 표시 닉네임 (2~20자, 고유)
    bio             VARCHAR(300),                                -- 자기소개 (선택)
    profile_image   VARCHAR(500),                                -- MinIO URL (프로필 이미지, 선택)
    role            VARCHAR(20) NOT NULL DEFAULT 'USER',         -- 역할: USER 또는 ADMIN
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,               -- 계정 활성 상태 (정지 시 FALSE)
    last_login_at   TIMESTAMP WITH TIME ZONE,                    -- 마지막 로그인 시각
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),  -- 가입일시
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()   -- 정보 수정일시
);

-- 이메일로 사용자 조회 (로그인 시 사용) — 로그인 API에서 매 요청마다 사용되므로 인덱스 필수
CREATE INDEX idx_users_email ON users(email);

-- 닉네임으로 사용자 조회 (중복 확인, 프로필 조회)
CREATE INDEX idx_users_nickname ON users(nickname);
