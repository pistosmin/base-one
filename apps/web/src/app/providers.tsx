/**
 * ====================================================
 * 프로바이더 합성 컴포넌트
 *
 * 앱에 필요한 모든 Context Provider를 한 곳에서 합성합니다.
 * - MUI Theme: 토스 스타일 디자인 시스템
 * - CssBaseline: 브라우저 기본 CSS 리셋 + 전역 스타일
 * - TanStack Query: 서버 상태 관리 (API 데이터 캐싱)
 *
 * 의존성: @tanstack/react-query, @mui/material, @/app/theme
 * 사용 위치: App.tsx에서 라우터를 감싸는 최상위 래퍼
 * ====================================================
 */

import type { ReactNode } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import theme from '@/app/theme';

/**
 * TanStack Query 클라이언트 인스턴스
 * - staleTime: 데이터를 30초 동안 신선한 것으로 간주 (불필요한 재요청 방지)
 * - gcTime: 사용하지 않는 캐시를 5분 후 가비지 컬렉션
 * - retry: 실패 시 1회 재시도 (기본 3회에서 축소)
 * - refetchOnWindowFocus: 윈도우 포커스 시 자동 재요청 (프로덕션에서 유용)
 */
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 30,        // 30초
      gcTime: 1000 * 60 * 5,       // 5분
      retry: 1,                     // 1회 재시도
      refetchOnWindowFocus: true,   // 윈도우 포커스 시 재요청
    },
  },
});

/** 프로바이더 컴포넌트의 Props */
interface ProvidersProps {
  children: ReactNode;
}

/**
 * 모든 Context Provider를 합성하는 래퍼 컴포넌트
 *
 * Provider 순서:
 * 1. ThemeProvider — MUI 테마 (토스 스타일)
 * 2. CssBaseline — 브라우저 CSS 리셋 + 전역 스타일
 * 3. QueryClientProvider — TanStack Query (서버 상태)
 */
export function Providers({ children }: ProvidersProps) {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <QueryClientProvider client={queryClient}>
        {children}
      </QueryClientProvider>
    </ThemeProvider>
  );
}
