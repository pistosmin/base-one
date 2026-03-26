/**
 * ====================================================
 * Axios 인스턴스 설정
 *
 * 앱 전체에서 사용하는 공통 HTTP 클라이언트입니다.
 * 인증 토큰 자동 주입과 토큰 만료 시 자동 갱신 로직을 포함합니다.
 *
 * 요청 인터셉터: authStore의 accessToken을 Authorization 헤더에 자동 추가
 * 응답 인터셉터: 401 응답 시 refreshToken으로 새 accessToken 발급 후 재시도
 *
 * 의존성: axios, features/auth/store/authStore (순환 참조 주의)
 * 사용 위치: features/auth/api/*.ts 등 모든 API 함수
 * ====================================================
 */

import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import type { ApiResponse } from '@/shared/api/apiTypes';
import type { TokenResponse } from '@/shared/types/user';

/** Axios 기본 인스턴스 설정 */
export const axiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ── 토큰 갱신 동시성 제어 변수 ──
// 여러 요청이 동시에 401을 받았을 때 갱신 요청이 중복으로 발생하는 것을 방지합니다.
// isRefreshing: 현재 토큰 갱신 중인지 여부
// failedQueue: 갱신 완료를 기다리는 대기 요청들의 Promise resolve/reject 콜백
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (error: unknown) => void;
}> = [];

/**
 * 대기 중인 요청들을 일괄 처리하는 함수
 *
 * 토큰 갱신 성공 시: 새 토큰으로 모든 대기 요청을 재시도 (resolve)
 * 토큰 갱신 실패 시: 모든 대기 요청에 에러 전파 (reject)
 */
function processQueue(error: unknown, token: string | null = null): void {
  failedQueue.forEach((promise) => {
    if (error) {
      promise.reject(error);
    } else if (token) {
      promise.resolve(token);
    }
  });
  failedQueue = [];
}

// ── 요청 인터셉터: 액세스 토큰 자동 주입 ──
// 모든 API 요청 전에 실행됩니다.
// authStore에서 토큰을 읽어 Authorization 헤더에 추가합니다.
// authStore를 직접 import하면 순환 참조가 발생할 수 있으므로
// 동적으로 getAuthStore를 통해 접근합니다.
axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // authStore를 lazy하게 가져옵니다 (순환 참조 방지)
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const store = (window as any).__authStore;
    const token = store?.getState?.()?.accessToken;

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

// ── 응답 인터셉터: 401 자동 토큰 갱신 ──
// 모든 API 응답 후에 실행됩니다.
// 401 응답이 오면 refreshToken으로 새 accessToken을 발급받고,
// 실패했던 원래 요청을 새 토큰으로 재시도합니다.
axiosInstance.interceptors.response.use(
  // 성공 응답은 그대로 통과
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
    };

    // 401이 아니거나 이미 재시도한 요청이면 에러 그대로 반환
    if (error.response?.status !== 401 || originalRequest._retry) {
      return Promise.reject(error);
    }

    // /auth/refresh 엔드포인트 자체가 401인 경우 → 갱신 불가 → 로그아웃
    if (originalRequest.url?.includes('/auth/refresh')) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const store = (window as any).__authStore;
      store?.getState?.()?.clearAuth?.();
      localStorage.removeItem('refreshToken');
      window.location.href = '/login';
      return Promise.reject(error);
    }

    // 이미 토큰 갱신 중인 경우 → 대기열에 추가하여 갱신 완료 후 재시도
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      }).then((token) => {
        originalRequest.headers.Authorization = `Bearer ${token}`;
        return axiosInstance(originalRequest);
      });
    }

    // 토큰 갱신 시작
    originalRequest._retry = true;
    isRefreshing = true;

    const refreshToken = localStorage.getItem('refreshToken');

    if (!refreshToken) {
      // 리프레시 토큰이 없으면 로그인 페이지로 이동
      isRefreshing = false;
      processQueue(error);
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const store = (window as any).__authStore;
      store?.getState?.()?.clearAuth?.();
      window.location.href = '/login';
      return Promise.reject(error);
    }

    try {
      // 새 액세스 토큰 발급 요청
      const response = await axiosInstance.post<ApiResponse<TokenResponse>>(
        '/auth/refresh',
        { refreshToken },
      );

      const { accessToken, refreshToken: newRefreshToken } = response.data.data;

      // 새 토큰을 스토어와 localStorage에 저장
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const store = (window as any).__authStore;
      const currentUser = store?.getState?.()?.user;
      store?.getState?.()?.setAuth?.(currentUser, accessToken);
      localStorage.setItem('refreshToken', newRefreshToken);

      // 대기 중이던 요청들에 새 토큰 전달하여 재시도
      processQueue(null, accessToken);

      // 원래 요청 재시도
      originalRequest.headers.Authorization = `Bearer ${accessToken}`;
      return axiosInstance(originalRequest);
    } catch (refreshError) {
      // 갱신 실패 → 모든 대기 요청 실패 처리 → 로그아웃
      processQueue(refreshError);
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const store = (window as any).__authStore;
      store?.getState?.()?.clearAuth?.();
      localStorage.removeItem('refreshToken');
      window.location.href = '/login';
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  },
);