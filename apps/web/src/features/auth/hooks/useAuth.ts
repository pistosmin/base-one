/**
 * ====================================================
 * 인증 커스텀 훅
 *
 * 로그인, 회원가입, 로그아웃 mutation과 현재 사용자 정보 접근을 제공합니다.
 * TanStack Query v5 useMutation을 활용합니다.
 *
 * 사용 예시:
 *   const { mutate: loginMutate, isPending } = useLogin();
 *   loginMutate({ email, password });
 *
 * 의존성: @tanstack/react-query, react-router, features/auth/api, features/auth/store
 * 사용 위치: features/auth/components/LoginForm, SignupForm
 * ====================================================
 */

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router';
import { login, logout as logoutApi, signup, type SignupPayload, type LoginPayload } from '../api/authApi';
import { useAuthStore } from '../store/authStore';

/**
 * 로그인 훅
 *
 * onSuccess: 토큰을 스토어와 localStorage에 저장, 홈으로 이동
 * onError: ApiError를 그대로 throw (컴포넌트에서 error 상태로 접근)
 */
export function useLogin() {
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);

  return useMutation({
    mutationFn: (data: LoginPayload) => login(data),
    onSuccess: (tokenResponse) => {
      // 액세스 토큰은 메모리(스토어), 리프레시 토큰은 localStorage에 저장
      setAuth(tokenResponse.user, tokenResponse.accessToken);
      localStorage.setItem('refreshToken', tokenResponse.refreshToken);
      navigate('/');
    },
  });
}

/**
 * 회원가입 훅
 *
 * onSuccess: 로그인과 동일하게 토큰 저장 후 홈으로 이동
 */
export function useSignup() {
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);

  return useMutation({
    mutationFn: (data: SignupPayload) => signup(data),
    onSuccess: (tokenResponse) => {
      setAuth(tokenResponse.user, tokenResponse.accessToken);
      localStorage.setItem('refreshToken', tokenResponse.refreshToken);
      navigate('/');
    },
  });
}

/**
 * 로그아웃 훅
 *
 * 서버 측 리프레시 토큰 삭제 + 클라이언트 상태 초기화
 * API 실패 시에도 클라이언트 상태는 초기화 (사용자 경험 보장)
 */
export function useLogout() {
  const navigate = useNavigate();
  const clearAuth = useAuthStore((state) => state.clearAuth);
  const queryClient = useQueryClient();

  return async () => {
    try {
      // 서버에서 리프레시 토큰 무효화 시도
      await logoutApi();
    } finally {
      // API 실패 시에도 로컬 상태 정리 (사용자는 항상 로그아웃되어야 함)
      clearAuth();
      localStorage.removeItem('refreshToken');

      // 모든 쿼리 캐시 정리 (개인정보 제거)
      queryClient.clear();

      // 홈으로 이동 (로그인 페이지가 아닌 홈으로 이동하여 자연스러운 UX)
      navigate('/');
    }
  };
}

/**
 * 현재 로그인한 사용자 정보 반환
 *
 * @returns 로그인된 경우 User 객체, 비로그인 시 null
 */
export function useCurrentUser() {
  return useAuthStore((state) => state.user);
}

/**
 * 관리자 여부 확인
 *
 * @returns 현재 사용자가 관리자인 경우 true
 */
export function useIsAdmin() {
  const user = useAuthStore((state) => state.user);
  return user?.role === 'ADMIN';
}

/**
 * 인증 여부 확인
 *
 * @returns 로그인된 경우 true (액세스 토큰 유무와 관계없이 스토어 상태 기준)
 */
export function useIsAuthenticated() {
  return useAuthStore((state) => state.isAuthenticated);
}