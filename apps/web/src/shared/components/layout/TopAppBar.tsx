/**
 * ====================================================
 * 상단 앱 바 컴포넌트
 *
 * 모든 페이지 상단에 고정되는 네비게이션 바입니다.
 * 왼쪽에 뒤로가기 또는 앱 제목, 오른쪽에 사용자 아바타 또는 로그인 버튼을 표시합니다.
 *
 * 의존성: @mui/material, react-router, lucide-react,
 *        features/auth/store/authStore, shared/components/common/UserAvatar
 * 사용 위치: shared/components/layout/AppLayout, AdminLayout
 * ====================================================
 */

import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import Button from '@mui/material/Button';
import { ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router';
import { useAuthStore } from '@/features/auth/store/authStore';
import { UserAvatar } from '@/shared/components/common/UserAvatar';

interface TopAppBarProps {
  title?: string;
  showBackButton?: boolean;
}

export function TopAppBar({ title = '커뮤니티', showBackButton = false }: TopAppBarProps) {
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuthStore((state) => ({
    user: state.user,
    isAuthenticated: state.isAuthenticated,
  }));

  const handleBackClick = () => {
    navigate(-1);
  };

  const handleLoginClick = () => {
    navigate('/login');
  };

  return (
    <AppBar
      position="fixed"
      elevation={0}
      sx={{
        backgroundColor: '#FFFFFF',
        borderBottom: '1px solid #E5E8EB',
        color: '#191F28',
        zIndex: (theme) => theme.zIndex.appBar,
      }}
    >
      <Toolbar sx={{ minHeight: 56 }}>
        {/* 왼쪽: 뒤로가기 버튼 또는 앱 제목 */}
        {showBackButton ? (
          <IconButton
            edge="start"
            color="inherit"
            onClick={handleBackClick}
            aria-label="뒤로가기"
            sx={{ mr: 1 }}
          >
            <ArrowLeft size={24} />
          </IconButton>
        ) : (
          <Typography
            variant="h6"
            component="h1"
            sx={{
              fontSize: '17px',
              fontWeight: 600,
              flexGrow: 0,
            }}
          >
            {title}
          </Typography>
        )}

        {/* 가운데 빈 공간 */}
        <div style={{ flexGrow: 1 }} />

        {/* 오른쪽: 로그인 상태에 따라 아바타 또는 로그인 버튼 */}
        {isAuthenticated && user ? (
          <UserAvatar
            nickname={user.nickname}
            profileImage={user.profileImage}
            size="sm"
          />
        ) : (
          <Button
            color="inherit"
            onClick={handleLoginClick}
            sx={{
              fontSize: '14px',
              fontWeight: 500,
              minWidth: 'auto',
            }}
          >
            로그인
          </Button>
        )}
      </Toolbar>
    </AppBar>
  );
}