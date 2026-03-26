/**
 * ====================================================
 * 관리자 레이아웃 컴포넌트
 *
 * 관리자 페이지의 레이아웃입니다.
 * 데스크톱: 사이드 드로어 + 메인 콘텐츠
 * 모바일: TopAppBar + 하단 탭 없음 (관리자 기능이 복잡하므로)
 *
 * Phase 4에서 완성 예정. 현재는 기본 레이아웃만 구현.
 *
 * 의존성: @mui/material, TopAppBar
 * 사용 위치: app/router.tsx (관리자 라우트)
 * ====================================================
 */

import Box from '@mui/material/Box';
import { Outlet } from 'react-router';
import { TopAppBar } from './TopAppBar';

export function AdminLayout() {
  return (
    <Box sx={{ backgroundColor: '#F2F4F6', minHeight: '100vh' }}>
      <TopAppBar title="관리자 대시보드" showBackButton />
      <Box
        component="main"
        sx={{
          pt: '56px', // TopAppBar 높이만큼 상단 패딩
          p: 2,
          minHeight: '100vh',
        }}
      >
        <Outlet />
      </Box>
    </Box>
  );
}