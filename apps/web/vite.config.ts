/**
 * ====================================================
 * Vite 설정 파일
 *
 * 이 파일은 Vite 빌드 도구의 설정을 정의합니다.
 * - 경로 별칭 (@/ → src/)
 * - API 프록시 (/api → localhost:8080)
 * - 빌드 최적화 (vendor 청크 분리)
 * ====================================================
 */

import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { resolve } from 'path';

export default defineConfig({
  // React 플러그인 (JSX 변환, Fast Refresh 등)
  plugins: [react()],

  // 경로 별칭 설정
  // import 시 @/shared/api/axiosInstance 처럼 사용 가능
  // 상대 경로(../../)를 피하고 코드 가독성을 높임
  resolve: {
    alias: {
      '@': resolve(__dirname, './src'),
    },
  },

  // 개발 서버 설정
  server: {
    // 개발 서버 포트 (기본: 5173)
    port: 5173,

    // API 프록시 설정
    // 프론트엔드에서 /api 요청 시 백엔드(localhost:8080)로 포워딩
    // 개발 환경에서 CORS 문제를 해결함
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },

  // 프로덕션 빌드 설정
  build: {
    rollupOptions: {
      output: {
        // vendor 청크 분리 — 라이브러리 코드를 별도 파일로 분리하여 캐싱 효율 극대화
        manualChunks(id: string) {
          if (id.includes('node_modules')) {
            // React 코어 라이브러리
            if (id.includes('react-dom') || id.includes('/react/')) {
              return 'vendor-react';
            }
            // MUI UI 프레임워크
            if (id.includes('@mui') || id.includes('@emotion')) {
              return 'vendor-mui';
            }
            // 라우팅 + 상태 관리
            if (id.includes('react-router') || id.includes('zustand') || id.includes('@tanstack')) {
              return 'vendor-state';
            }
          }
        },
      },
    },
  },
});
