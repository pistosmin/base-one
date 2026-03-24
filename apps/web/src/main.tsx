/**
 * ====================================================
 * 애플리케이션 엔트리 포인트
 *
 * React 앱의 최초 진입 파일입니다.
 * ReactDOM.createRoot로 루트 DOM에 App 컴포넌트를 마운트합니다.
 * StrictMode를 활성화하여 개발 중 잠재적 문제를 미리 감지합니다.
 * ====================================================
 */

import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { App } from '@/app/App';

// #root DOM 요소에 React 앱을 마운트
const rootElement = document.getElementById('root');

if (!rootElement) {
  throw new Error('루트 DOM 요소(#root)를 찾을 수 없습니다. index.html을 확인하세요.');
}

createRoot(rootElement).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
