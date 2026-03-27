/**
 * ====================================================
 * 인증 API 함수
 *
 * 백엔드 /api/v1/auth/** 엔드포인트와 통신하는 API 함수들입니다.
 * axiosInstance를 사용하여 baseURL(/api/v1)이 자동 적용됩니다.
 *
 * 모든 함수는 extractData 헬퍼로 success: false 시 ApiError를 throw합니다.
 *
 * 의존성: @/shared/api/axiosInstance, @/shared/api/apiTypes, @/shared/types/user
 * 사용 위치: features/auth/hooks/useAuth
 * ====================================================
 */

import { axiosInstance } from '@/shared/api/axiosInstance';
import { extractData, type ApiResponse } from '@/shared/api/apiTypes';
import type { TokenResponse } from '@/shared/types/user';

/** 회원가입 요청 데이터 */
export interface SignupPayload {
  email: string;
  password: string;
  nickname: string;
}

/** 로그인 요청 데이터 */
export interface LoginPayload {
  email: string;
  password: string;
}

/**
 * 회원가입 — POST /auth/signup
 *
 * 이메일, 비밀번호, 닉네임으로 새 계정을 생성합니다.
 * 성공 시 바로 로그인된 상태로 토큰을 반환합니다.
 *
 * @param data 회원가입 폼 데이터
 * @returns 액세스 토큰, 리프레시 토큰, 사용자 정보
 */
export async function signup(data: SignupPayload): Promise<TokenResponse> {
  const response = await axiosInstance.post<ApiResponse<TokenResponse>>('/auth/signup', data);
  return extractData(response.data);
}

/**
 * 로그인 — POST /auth/login
 *
 * 이메일과 비밀번호로 로그인합니다.
 * 성공 시 JWT 토큰과 사용자 정보를 반환합니다.
 *
 * @param data 로그인 폼 데이터
 * @returns 액세스 토큰, 리프레시 토큰, 사용자 정보
 */
export async function login(data: LoginPayload): Promise<TokenResponse> {
  const response = await axiosInstance.post<ApiResponse<TokenResponse>>('/auth/login', data);
  return extractData(response.data);
}

/**
 * 토큰 갱신 — POST /auth/refresh
 *
 * 리프레시 토큰으로 새로운 액세스 토큰을 발급받습니다.
 * axiosInstance의 응답 인터셉터에서 401 에러 시 자동으로 호출됩니다.
 *
 * @param token 리프레시 토큰 (localStorage에서 읽어옴)
 * @returns 새로운 액세스 토큰과 리프레시 토큰
 */
export async function refreshToken(token: string): Promise<TokenResponse> {
  const response = await axiosInstance.post<ApiResponse<TokenResponse>>('/auth/refresh', {
    refreshToken: token,
  });
  return extractData(response.data);
}

/**
 * 로그아웃 — POST /auth/logout
 *
 * 서버 측에서 리프레시 토큰을 무효화합니다.
 * 클라이언트 상태 정리는 useLogout 훅에서 처리됩니다.
 *
 * @returns void (성공 시 응답 데이터 없음)
 */
export async function logout(): Promise<void> {
  const response = await axiosInstance.post<ApiResponse<null>>('/auth/logout');
  extractData(response.data);
}