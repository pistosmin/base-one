/**
 * ====================================================
 * 인증 가드 컴포넌트
 *
 * 로그인이 필요한 페이지를 보호합니다.
 * 미인증 사용자를 /login으로 리다이렉트하고,
 * 인증된 사용자에게는 자식 라우트를 렌더링합니다.
 *
 * 사용 방법: <Route element={<AuthGuard />}> 감싸기
 * 의존성: react-router, features/auth/store/authStore
 * 사용 위치: app/router.tsx
 * ====================================================
 */

import { Navigate, Outlet } from 'react-router';
import { useAuthStore } from '@/features/auth/store/authStore';

/** 미인증 사용자를 /login으로 리다이렉트하는 가드 */
export function AuthGuard() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  if (!isAuthenticated) {
    // replace: true — 뒤로가기 시 로그인 페이지로 돌아오지 않도록
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}