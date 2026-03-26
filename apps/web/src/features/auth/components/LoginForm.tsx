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
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  /**
   * 폼 제출 시 로그인 API 호출
   */
  const onSubmit = (data: LoginFormData) => {
    const payload: LoginPayload = {
      email: data.email,
      password: data.password,
    };
    mutation.mutate(payload);
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
      {/* 서버 에러 표시 */}
      {mutation.error && (
        <Alert severity="error">
          {mutation.error instanceof Error
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
            backgroundColor: '#F8F9FA',
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
            backgroundColor: '#F8F9FA',
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
          <Link
            to="/signup"
            style={{
              color: '#3182F6',
              textDecoration: 'none',
              fontWeight: 600,
            }}
          >
            회원가입
          </Link>
        </Typography>
      </Box>
    </Box>
  );
}