/**
 * ====================================================
 * 회원가입 페이지
 *
 * 토스 스타일의 심플한 회원가입 화면입니다.
 * 중앙 정렬된 카드 레이아웃에 회원가입 폼을 표시합니다.
 *
 * 디자인: 흰색 배경, maxWidth 400px, Framer Motion fade-in
 * 의존성: framer-motion, @mui/material, features/auth/components/SignupForm
 * 사용 위치: app/router.tsx
 * ====================================================
 */

import { Box, Typography } from '@mui/material';
import { motion } from 'framer-motion';
import { SignupForm } from '../components/SignupForm';

/**
 * 회원가입 페이지 컴포넌트
 */
export function SignupPage() {
  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#F2F4F6',
        px: 2.5,
      }}
    >
      <motion.div
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
        style={{ width: '100%', maxWidth: 400 }}
      >
        <Box
          sx={{
            backgroundColor: '#FFFFFF',
            borderRadius: 3,
            p: 4,
          }}
        >
          {/* 앱 제목 */}
          <Typography
            variant="h4"
            sx={{
              fontWeight: 700,
              color: '#191F28',
              mb: 3,
              fontSize: '26px',
              letterSpacing: '-0.02em',
            }}
          >
            회원가입
          </Typography>

          {/* 회원가입 폼 */}
          <SignupForm />
        </Box>
      </motion.div>
    </Box>
  );
}