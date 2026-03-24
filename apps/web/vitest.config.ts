/**
 * ====================================================
 * Vitest 설정 파일
 *
 * React 컴포넌트 테스트를 위한 Vitest 설정입니다.
 * - jsdom 환경에서 DOM API 시뮬레이션
 * - v8 프로바이더로 코드 커버리지 측정
 * - @/ 경로 별칭 지원
 * ====================================================
 */

import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import { resolve } from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': resolve(__dirname, './src'),
    },
  },
  test: {
    // jsdom 환경에서 React 컴포넌트 테스트 (브라우저 DOM 시뮬레이션)
    environment: 'jsdom',

    // 전역 테스트 유틸 자동 import (describe, it, expect 등)
    globals: true,

    // 테스트 실행 전 자동으로 실행할 설정 파일
    setupFiles: ['./src/test/setup.ts'],

    // 코드 커버리지 설정
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'src/test/',
      ],
    },
  },
});
