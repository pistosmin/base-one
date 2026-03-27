/**
 * ====================================================
 * 하단 탭 바 컴포넌트
 *
 * 모바일 하단에 고정되는 메인 네비게이션 탭입니다.
 * 현재 경로에 따라 활성 탭이 하이라이트됩니다.
 * 미로그인 사용자가 글쓰기/알림/프로필 탭 클릭 시 로그인 페이지로 이동합니다.
 *
 * 의존성: @mui/material, react-router, lucide-react,
 *        features/auth/store/authStore
 * 사용 위치: shared/components/layout/AppLayout
 * ====================================================
 */

import BottomNavigation from '@mui/material/BottomNavigation';
import BottomNavigationAction from '@mui/material/BottomNavigationAction';
import { Home, PenSquare, Bell, User } from 'lucide-react';
import { useNavigate, useLocation } from 'react-router';
import { useAuthStore } from '@/features/auth/store/authStore';

/** 탭 정보 */
const tabs = [
  { label: '홈', icon: Home, path: '/', requireAuth: false },
  { label: '글쓰기', icon: PenSquare, path: '/posts/new', requireAuth: true },
  { label: '알림', icon: Bell, path: '/notifications', requireAuth: true },
  { label: '프로필', icon: User, path: '/profile', requireAuth: true },
] as const;

export function BottomTabBar() {
  const navigate = useNavigate();
  const location = useLocation();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  // 현재 경로에서 활성 탭 인덱스 계산
  const getCurrentTabIndex = (): number => {
    const currentPath = location.pathname;

    // 홈 탭은 정확히 '/'일 때만 활성
    if (currentPath === '/') return 0;

    // 다른 탭들은 경로가 시작하는지 확인
    for (let i = 1; i < tabs.length; i++) {
      if (currentPath.startsWith(tabs[i].path)) {
        return i;
      }
    }

    return 0; // 기본값: 홈 탭
  };

  const handleTabChange = (_: unknown, newValue: number) => {
    const selectedTab = tabs[newValue];

    // 인증이 필요한 탭이면서 미로그인 상태인 경우
    if (selectedTab.requireAuth && !isAuthenticated) {
      navigate('/login');
      return;
    }

    // 정상 네비게이션
    navigate(selectedTab.path);
  };

  const currentTabIndex = getCurrentTabIndex();

  return (
    <BottomNavigation
      value={currentTabIndex}
      onChange={handleTabChange}
      sx={{
        position: 'fixed',
        bottom: 0,
        left: 0,
        right: 0,
        zIndex: 1000,
        height: 64,
        backgroundColor: 'background.paper',
        borderTop: 1,
        borderColor: 'divider',
        boxShadow: '0 -4px 24px rgba(0,0,0,0.12)',
        '& .MuiBottomNavigationAction-root': {
          color: 'text.secondary',
          fontSize: '12px',
          minWidth: 0,
          paddingTop: '6px',
          '&.Mui-selected': {
            color: 'primary.main',
          },
        },
      }}
    >
      {tabs.map((tab) => (
        <BottomNavigationAction
          key={tab.label}
          label={tab.label}
          icon={<tab.icon size={20} />}
          aria-label={tab.label}
        />
      ))}
    </BottomNavigation>
  );
}