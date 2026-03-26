/**
 * ====================================================
 * 앱 라우터 설정
 *
 * React Router v7을 사용한 SPA 라우팅입니다.
 * React.lazy + Suspense로 페이지별 코드 스플리팅을 적용합니다.
 *
 * 라우트 구조:
 *   AppLayout (일반 레이아웃)
 *     /                → HomePage (placeholder)
 *     /posts           → PostListPage (placeholder)
 *     /posts/:id       → PostDetailPage (placeholder)
 *   AuthGuard (인증 필요)
 *     /posts/new       → PostEditorPage (placeholder)
 *     /notifications   → NotificationPage (placeholder)
 *     /profile         → ProfilePage (placeholder)
 *   /login             → LoginPage (로그인 상태면 /로 리다이렉트)
 *   /signup            → SignupPage (로그인 상태면 /로 리다이렉트)
 *   AdminGuard (관리자 전용)
 *     AdminLayout
 *       /admin/dashboard → AdminDashboardPage (placeholder)
 *       /admin/users     → AdminUsersPage (placeholder)
 *
 * 의존성: react-router, features/auth, shared/guards, shared/components/layout
 * 사용 위치: app/App.tsx
 * ====================================================
 */

import { lazy, Suspense } from 'react';
import { Navigate, Route, Routes } from 'react-router';
import CircularProgress from '@mui/material/CircularProgress';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

import { AppLayout } from '@/shared/components/layout/AppLayout';
import { AdminLayout } from '@/shared/components/layout/AdminLayout';
import { AuthGuard } from '@/shared/guards/AuthGuard';
import { AdminGuard } from '@/shared/guards/AdminGuard';
import { useAuthStore } from '@/features/auth/store/authStore';

// 인증 페이지 (lazy loading)
const LoginPage = lazy(() =>
  import('@/features/auth').then((m) => ({ default: m.LoginPage })),
);
const SignupPage = lazy(() =>
  import('@/features/auth').then((m) => ({ default: m.SignupPage })),
);

/** 로딩 스피너 (Suspense fallback) */
function PageLoader() {
  return (
    <Box
      sx={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
      }}
    >
      <CircularProgress />
    </Box>
  );
}

/** placeholder 페이지 컴포넌트 생성 헬퍼 */
function PlaceholderPage({ name }: { name: string }) {
  return (
    <Box sx={{ p: 4 }}>
      <Typography variant="h6" color="text.secondary">
        {name} (준비 중)
      </Typography>
    </Box>
  );
}

/**
 * 로그인/회원가입 페이지 래퍼
 * 이미 로그인한 사용자는 홈으로 리다이렉트
 */
function GuestOnlyRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }
  return <>{children}</>;
}

/** 앱 라우터 */
export function AppRouter() {
  return (
    <Suspense fallback={<PageLoader />}>
      <Routes>
        {/* 일반 레이아웃 (TopAppBar + BottomTabBar) */}
        <Route element={<AppLayout />}>
          <Route path="/" element={<PlaceholderPage name="홈" />} />
          <Route path="/posts" element={<PlaceholderPage name="게시글 목록" />} />
          <Route path="/posts/:id" element={<PlaceholderPage name="게시글 상세" />} />

          {/* 인증 필요 라우트 */}
          <Route element={<AuthGuard />}>
            <Route path="/posts/new" element={<PlaceholderPage name="글쓰기" />} />
            <Route path="/posts/:id/edit" element={<PlaceholderPage name="글 수정" />} />
            <Route path="/notifications" element={<PlaceholderPage name="알림" />} />
            <Route path="/profile" element={<PlaceholderPage name="프로필" />} />
          </Route>
        </Route>

        {/* 인증 페이지 (레이아웃 없음) */}
        <Route
          path="/login"
          element={
            <GuestOnlyRoute>
              <LoginPage />
            </GuestOnlyRoute>
          }
        />
        <Route
          path="/signup"
          element={
            <GuestOnlyRoute>
              <SignupPage />
            </GuestOnlyRoute>
          }
        />

        {/* 관리자 페이지 */}
        <Route element={<AdminGuard />}>
          <Route element={<AdminLayout />}>
            <Route path="/admin/dashboard" element={<PlaceholderPage name="관리자 대시보드" />} />
            <Route path="/admin/users" element={<PlaceholderPage name="사용자 관리" />} />
          </Route>
        </Route>

        {/* 404 처리 */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  );
}
