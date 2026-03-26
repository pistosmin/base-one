/**
 * ====================================================
 * Auth Feature 공개 API (Barrel Export)
 *
 * 다른 feature나 shared에서 auth feature를 사용할 때
 * 이 파일에서만 import해야 합니다. (Feature-Sliced Design 원칙)
 *
 * 사용 위치: app/router.tsx, shared/guards/AuthGuard
 * ====================================================
 */

// 페이지 (Task 1.6에서 구현 완료)
export { LoginPage } from './pages/LoginPage';
export { SignupPage } from './pages/SignupPage';

// 훅
export { useLogin, useSignup, useLogout, useCurrentUser, useIsAdmin, useIsAuthenticated } from './hooks/useAuth';

// 스토어
export { useAuthStore } from './store/authStore';

// 타입
export type { SignupPayload, LoginPayload } from './api/authApi';