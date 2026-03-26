/**
 * ====================================================
 * 관리자 가드 컴포넌트
 *
 * ADMIN 역할이 있는 사용자만 접근할 수 있는 페이지를 보호합니다.
 * 미인증 사용자나 일반 사용자를 /로 리다이렉트합니다.
 *
 * 의존성: react-router, features/auth/store/authStore
 * 사용 위치: app/router.tsx (관리자 라우트)
 * ====================================================
 */

import { Navigate, Outlet } from 'react-router';
import { useAuthStore } from '@/features/auth/store/authStore';

/** 관리자 전용 페이지 가드 */
export function AdminGuard() {
  const user = useAuthStore((state) => state.user);

  if (user?.role !== 'ADMIN') {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}