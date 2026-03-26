/**
 * ====================================================
 * 인증 상태 스토어 (Zustand)
 *
 * 로그인 상태, 사용자 정보, 액세스 토큰을 전역으로 관리합니다.
 *
 * 저장 전략:
 * - user: localStorage에 persist (페이지 새로고침 후에도 로그인 유지)
 * - accessToken: 메모리에만 저장 (보안: localStorage에 JWT 저장하면 XSS 취약)
 *
 * window.__authStore 등록:
 * - axiosInstance의 응답 인터셉터가 토큰 갱신 시 authStore에 접근해야 함
 * - import 시 순환 참조가 발생하므로 window 전역 객체를 통해 접근
 *
 * 의존성: zustand, zustand/middleware, @/shared/types/user
 * 사용 위치: features/auth/hooks/useAuth, shared/api/axiosInstance, shared/components/layout
 * ====================================================
 */

import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User } from '@/shared/types/user';

/** 인증 스토어 상태 타입 */
interface AuthState {
  user: User | null;
  accessToken: string | null;
  isAuthenticated: boolean;
  setAuth: (user: User, accessToken: string) => void;
  clearAuth: () => void;
  updateUser: (partial: Partial<User>) => void;
}

/** 인증 스토어 생성 (Zustand v5 이중 괄호 패턴) */
export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      accessToken: null,
      isAuthenticated: false,

      /**
       * 로그인 성공 시 사용자 정보와 액세스 토큰 설정
       *
       * @param user 로그인한 사용자 정보
       * @param accessToken JWT 액세스 토큰 (메모리에만 저장)
       */
      setAuth: (user: User, accessToken: string) =>
        set({
          user,
          accessToken,
          isAuthenticated: true,
        }),

      /**
       * 로그아웃 시 모든 인증 정보 초기화
       *
       * localStorage의 persist 데이터도 함께 정리됨
       */
      clearAuth: () =>
        set({
          user: null,
          accessToken: null,
          isAuthenticated: false,
        }),

      /**
       * 현재 사용자 정보 부분 업데이트 (프로필 수정 시 사용)
       *
       * @param partial 업데이트할 사용자 필드들
       */
      updateUser: (partial: Partial<User>) =>
        set((state) => ({
          user: state.user ? { ...state.user, ...partial } : null,
        })),
    }),
    {
      name: 'auth-storage',
      // accessToken은 보안상 저장하지 않음 (메모리에만 유지)
      // 페이지 새로고침 시 refreshToken으로 재발급받아야 함
      partialize: (state) => ({ user: state.user }),
    },
  ),
);

// ── axiosInstance의 토큰 갱신 인터셉터에서 접근하기 위해 window에 등록 ──
// (순환 참조 방지를 위한 전역 객체 활용)
declare global {
  interface Window {
    __authStore: typeof useAuthStore;
  }
}

// axiosInstance.ts에서 window.__authStore로 접근할 수 있도록 등록
window.__authStore = useAuthStore;

export default useAuthStore;