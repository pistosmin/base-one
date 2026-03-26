/**
 * ====================================================
 * 사용자 관련 타입 정의
 *
 * 백엔드 UserProfileResponse, UserSummaryResponse, TokenResponse와 1:1 매핑됩니다.
 * 인증 상태 관리(authStore), API 응답 파싱, 프로필 표시에 사용됩니다.
 *
 * 의존성: 없음 (순수 타입 정의)
 * 사용 위치: features/auth/store/authStore, features/auth/api/authApi, shared/components
 * ====================================================
 */

/** 사용자 역할 (백엔드 UserRole enum과 동일) */
export type UserRole = 'USER' | 'ADMIN';

/** 사용자 전체 프로필 정보 — 로그인 응답, 프로필 페이지에서 사용 */
export interface User {
  id: number;
  email: string;
  nickname: string;
  bio: string | null;
  profileImage: string | null;
  role: UserRole;
  createdAt: string;  // ISO 8601 형식 (예: "2024-01-15T10:30:00")
}

/** 사용자 요약 정보 — 게시글/댓글 작성자 표시에 사용 (최소 필드) */
export interface UserSummary {
  id: number;
  nickname: string;
  profileImage: string | null;
}

/** 로그인/회원가입 성공 시 서버에서 반환되는 토큰 정보 */
export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  /** 액세스 토큰 만료까지 남은 시간 (초) */
  expiresIn: number;
  user: User;
}