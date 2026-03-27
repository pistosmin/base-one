/**
 * ====================================================
 * 로그인 폼 컴포넌트
 *
 * react-hook-form + Zod를 사용한 로그인 폼입니다.
 * useLogin 훅으로 로그인 API를 호출하며, 성공 시 자동으로 홈(/)으로 이동합니다.
 *
 * 의존성: react-hook-form, @hookform/resolvers/zod, zod, @mui/material,
 *        features/auth/hooks/useAuth
 * 사용 위치: features/auth/pages/LoginPage
 * ====================================================
 */

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Box,
  TextField,
  Button,
  CircularProgress,
  Alert,
  Typography,
} from '@mui/material';
import { Link } from 'react-router';
import { useLogin } from '../hooks/useAuth';
import type { LoginPayload } from '../api/authApi';
import { ApiError } from '@/shared/api/apiTypes';

/**
 * 로그인 폼 유효성 검증 스키마
 */
const loginSchema = z.object({
  email: z.string().email('올바른 이메일을 입력하세요'),
  password: z.string().min(8, '비밀번호는 8자 이상이어야 합니다'),
});

type LoginFormData = z.infer<typeof loginSchema>;

/**
 * 로그인 폼 컴포넌트
 */
export function LoginForm() {
  const mutation = useLogin();

  const {
    register,
    handleSubmit,
    setError,
    clearErrors,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  /**
   * 폼 제출 시 로그인 API 호출
   * 인증 실패 시 어느 쪽이 틀렸는지 명시하지 않고 양쪽 필드에 안내 표시
   */
  const onSubmit = (data: LoginFormData) => {
    clearErrors();
    const payload: LoginPayload = {
      email: data.email,
      password: data.password,
    };
    mutation.mutate(payload, {
      onError: (error) => {
        if (error instanceof ApiError && error.code === 'INVALID_CREDENTIALS') {
          // 보안상 어느 쪽이 틀렸는지 노출하지 않음 — 양쪽 필드에 에러 스타일만 표시
          setError('email', { message: '' });
          setError('password', { message: '' });
        }
      },
    });
  };

  return (
    <Box
      component="form"
      onSubmit={handleSubmit(onSubmit)}
      sx={{
        display: 'flex',
        flexDirection: 'column',
        gap: 2,
      }}
    >
      {/* 로그인 실패 안내 (인증 실패 또는 기타 오류) */}
      {mutation.error && (
        <Alert severity="error">
          {mutation.error instanceof ApiError && mutation.error.code === 'INVALID_CREDENTIALS'
            ? '입력하신 이메일 또는 비밀번호를 다시 확인해주세요.'
            : mutation.error instanceof Error
              ? mutation.error.message
              : '로그인 중 오류가 발생했습니다.'}
        </Alert>
      )}

      {/* 이메일 입력 */}
      <TextField
        {...register('email')}
        type="email"
        label="이메일"
        variant="outlined"
        fullWidth
        disabled={mutation.isPending}
        error={!!errors.email}
        helperText={errors.email?.message}
        sx={{
          '& .MuiOutlinedInput-root': {
            backgroundColor: (theme) => theme.palette.mode === 'dark' ? '#312F2D' : '#F2EFEA',
          },
        }}
      />

      {/* 비밀번호 입력 */}
      <TextField
        {...register('password')}
        type="password"
        label="비밀번호"
        variant="outlined"
        fullWidth
        disabled={mutation.isPending}
        error={!!errors.password}
        helperText={errors.password?.message}
        sx={{
          '& .MuiOutlinedInput-root': {
            backgroundColor: (theme) => theme.palette.mode === 'dark' ? '#312F2D' : '#F2EFEA',
          },
        }}
      />

      {/* 로그인 버튼 */}
      <Button
        type="submit"
        variant="contained"
        fullWidth
        size="large"
        disabled={mutation.isPending}
        sx={{
          mt: 1,
          py: 1.5,
          fontSize: '16px',
          fontWeight: 600,
        }}
      >
        {mutation.isPending ? (
          <>
            <CircularProgress size={20} color="inherit" sx={{ mr: 1 }} />
            로그인 중...
          </>
        ) : (
          '로그인'
        )}
      </Button>

      {/* 회원가입 링크 */}
      <Box sx={{ textAlign: 'center', mt: 2 }}>
        <Typography variant="body2" color="textSecondary">
          아직 회원이 아니신가요?{' '}
          <Box
            component={Link}
            to="/signup"
            sx={{
              color: 'primary.main',
              textDecoration: 'none',
              fontWeight: 600,
            }}
          >
            회원가입
          </Box>
        </Typography>
      </Box>
    </Box>
  );
}