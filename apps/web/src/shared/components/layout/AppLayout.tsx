/**
 * ====================================================
 * 앱 레이아웃 컴포넌트
 *
 * 일반 사용자 화면의 기본 레이아웃입니다.
 * 상단 TopAppBar + 메인 콘텐츠 + 하단 BottomTabBar로 구성됩니다.
 *
 * 패딩:
 * - paddingTop: 56px (TopAppBar 높이)
 * - paddingBottom: 64px (BottomTabBar 높이)
 *
 * 의존성: @mui/material, TopAppBar, BottomTabBar
 * 사용 위치: app/router.tsx에서 일반 페이지를 감싸는 레이아웃
 * ====================================================
 */

import Box from '@mui/material/Box';
import { Outlet } from 'react-router';
import { TopAppBar } from './TopAppBar';
import { BottomTabBar } from './BottomTabBar';

export function AppLayout() {
  return (
    <Box sx={{ backgroundColor: '#F2F4F6', minHeight: '100vh' }}>
      <TopAppBar />
      <Box
        component="main"
        sx={{
          pt: '56px', // TopAppBar 높이만큼 상단 패딩
          pb: '64px', // BottomTabBar 높이만큼 하단 패딩
          minHeight: '100vh',
        }}
      >
        <Outlet />
      </Box>
      <BottomTabBar />
    </Box>
  );
}