/**
 * ====================================================
 * 회원가입 폼 컴포넌트
 *
 * react-hook-form + Zod를 사용한 회원가입 폼입니다.
 * useSignup 훅으로 회원가입 API를 호출하며, 성공 시 자동으로 홈(/)으로 이동합니다.
 *
 * 의존성: react-hook-form, @hookform/resolvers/zod, zod, @mui/material,
 *        features/auth/hooks/useAuth
 * 사용 위치: features/auth/pages/SignupPage
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
import { useSignup } from '../hooks/useAuth';
import type { SignupPayload } from '../api/authApi';
import { ApiError } from '@/shared/api/apiTypes';

/**
 * 회원가입 폼 유효성 검증 스키마
 */
const signupSchema = z.object({
  email: z.string().email('올바른 이메일을 입력하세요'),
  password: z.string().min(8, '비밀번호는 8자 이상이어야 합니다'),
  passwordConfirm: z.string(),
  nickname: z.string()
    .min(2, '닉네임은 2자 이상이어야 합니다')
    .max(20, '닉네임은 20자 이하여야 합니다'),
}).refine((data) => data.password === data.passwordConfirm, {
  message: '비밀번호가 일치하지 않습니다',
  path: ['passwordConfirm'],
});

type SignupFormData = z.infer<typeof signupSchema>;

/**
 * 회원가입 폼 컴포넌트
 */
export function SignupForm() {
  const mutation = useSignup();

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<SignupFormData>({
    resolver: zodResolver(signupSchema),
  });

  /**
   * 폼 제출 시 회원가입 API 호출
   * 이메일/닉네임 중복 오류는 해당 필드에 직접 표시
   */
  const onSubmit = (data: SignupFormData) => {
    const payload: SignupPayload = {
      email: data.email,
      password: data.password,
      nickname: data.nickname,
    };
    mutation.mutate(payload, {
      onError: (error) => {
        if (error instanceof ApiError) {
          if (error.code === 'DUPLICATE_EMAIL') {
            setError('email', { message: '이미 사용 중인 이메일입니다.' });
          } else if (error.code === 'DUPLICATE_NICKNAME') {
            setError('nickname', { message: '이미 사용 중인 닉네임입니다.' });
          }
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
      {/* 서버 에러 표시 (이메일/닉네임 중복 외 기타 오류만 표시) */}
      {mutation.error &&
        !(mutation.error instanceof ApiError &&
          (mutation.error.code === 'DUPLICATE_EMAIL' || mutation.error.code === 'DUPLICATE_NICKNAME')) && (
        <Alert severity="error">
          {mutation.error instanceof Error
            ? mutation.error.message
            : '회원가입 중 오류가 발생했습니다.'}
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

      {/* 닉네임 입력 */}
      <TextField
        {...register('nickname')}
        type="text"
        label="닉네임"
        variant="outlined"
        fullWidth
        disabled={mutation.isPending}
        error={!!errors.nickname}
        helperText={errors.nickname?.message}
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

      {/* 비밀번호 확인 입력 */}
      <TextField
        {...register('passwordConfirm')}
        type="password"
        label="비밀번호 확인"
        variant="outlined"
        fullWidth
        disabled={mutation.isPending}
        error={!!errors.passwordConfirm}
        helperText={errors.passwordConfirm?.message}
        sx={{
          '& .MuiOutlinedInput-root': {
            backgroundColor: '#F8F9FA',
          },
        }}
      />

      {/* 회원가입 버튼 */}
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
            회원가입 중...
          </>
        ) : (
          '회원가입'
        )}
      </Button>

      {/* 로그인 링크 */}
      <Box sx={{ textAlign: 'center', mt: 2 }}>
        <Typography variant="body2" color="textSecondary">
          이미 회원이신가요?{' '}
          <Link
            to="/login"
            style={{
              color: '#3182F6',
              textDecoration: 'none',
              fontWeight: 600,
            }}
          >
            로그인
          </Link>
        </Typography>
      </Box>
    </Box>
  );
}