/**
 * ====================================================
 * 루트 앱 컴포넌트
 *
 * 모든 Provider와 Router를 합성하는 최상위 컴포넌트입니다.
 * - BrowserRouter: HTML5 History API 기반 클라이언트 사이드 라우팅
 * - Providers: TanStack Query, MUI Theme 등 Context Provider 합성
 * - AppRouter: 라우트 정의
 *
 * 의존성: react-router, @/app/providers, @/app/router
 * 사용 위치: main.tsx에서 <App /> 형태로 사용
 * ====================================================
 */

import { BrowserRouter } from 'react-router';
import { Providers } from '@/app/providers';
import { AppRouter } from '@/app/router';

/**
 * 앱 루트 컴포넌트
 *
 * Provider 감싸는 순서:
 * 1. BrowserRouter - 라우팅 기반
 * 2. Providers - 상태 관리, 테마 등
 * 3. AppRouter - 페이지 라우팅
 */
export function App() {
  return (
    <BrowserRouter>
      <Providers>
        <AppRouter />
      </Providers>
    </BrowserRouter>
  );
}
