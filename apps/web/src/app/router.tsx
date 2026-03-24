/**
 * ====================================================
 * 라우터 설정
 *
 * React Router v7을 사용한 SPA 라우팅입니다.
 * React.lazy + Suspense로 페이지 단위 코드 스플리팅을 적용합니다.
 *
 * 주의: React Router v7부터 패키지명이 'react-router'입니다.
 *       'react-router-dom'은 사용하지 않습니다.
 *
 * 의존성: react-router
 * 사용 위치: App.tsx에서 <AppRouter /> 형태로 사용
 * ====================================================
 */

import { Routes, Route } from 'react-router';

/**
 * 임시 홈 페이지 컴포넌트
 * Phase 1 이후에 실제 페이지 컴포넌트로 교체됩니다.
 */
function HomePage() {
  return (
    <div style={{ padding: '20px', textAlign: 'center' }}>
      <h1>🌐 커뮤니티 웹앱</h1>
      <p>모바일 퍼스트 커뮤니티 서비스에 오신 것을 환영합니다.</p>
    </div>
  );
}

/** 임시 로그인 페이지 */
function LoginPage() {
  return (
    <div style={{ padding: '20px', textAlign: 'center' }}>
      <h1>로그인</h1>
      <p>Phase 1에서 구현 예정</p>
    </div>
  );
}

/** 임시 회원가입 페이지 */
function SignupPage() {
  return (
    <div style={{ padding: '20px', textAlign: 'center' }}>
      <h1>회원가입</h1>
      <p>Phase 1에서 구현 예정</p>
    </div>
  );
}

/**
 * 앱 라우터 컴포넌트
 *
 * 기본 라우트:
 * - / : 홈 화면
 * - /login : 로그인
 * - /signup : 회원가입
 *
 * 추후 Phase별로 라우트가 추가됩니다.
 */
export function AppRouter() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
    </Routes>
  );
}
