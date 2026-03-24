/**
 * ====================================================
 * MUI 팔레트 설정
 *
 * tokens.ts의 컬러 토큰을 MUI createTheme의 palette 포맷으로 변환합니다.
 * 라이트 모드와 다크 모드를 모두 정의합니다.
 *
 * MUI palette 구조:
 *   primary    → 브랜드 메인 색상
 *   secondary  → 보조 색상 (이 앱에서는 primary와 동일하게 사용)
 *   error      → 에러, 삭제, 위험 상태
 *   warning    → 경고, 주의
 *   success    → 성공, 완료
 *   info       → 정보성 알림
 *   background → 페이지/카드 배경
 *   text       → 텍스트 색상 계층
 *
 * 의존성: tokens.ts
 * 사용 위치: index.ts (createTheme)
 * ====================================================
 */

import type { PaletteOptions } from '@mui/material/styles';
import { tokens } from './tokens';

/**
 * 라이트 모드 팔레트
 * 토스 앱의 밝은 배경 + 깔끔한 텍스트 스타일을 참고
 */
export const lightPalette: PaletteOptions = {
  mode: 'light',

  // primary: 토스의 시그니처 블루를 메인 브랜드 색상으로 사용
  primary: {
    main: tokens.color.primary,
    light: tokens.color.primaryLight,
    dark: tokens.color.primaryDark,
    contrastText: tokens.color.textOnPrimary,
  },

  // secondary: 이 앱에서는 primary와 동일하게 설정 (단일 브랜드 색상 전략)
  secondary: {
    main: tokens.color.primary,
    light: tokens.color.primaryLight,
    dark: tokens.color.primaryDark,
  },

  // error: Apple HIG의 시스템 레드를 참고 — 삭제, 에러 상태에 사용
  error: {
    main: tokens.color.error,
  },

  // warning: Apple HIG의 시스템 오렌지 — 주의가 필요한 상태
  warning: {
    main: tokens.color.warning,
  },

  // success: Apple HIG의 시스템 그린 — 성공, 완료 상태
  success: {
    main: tokens.color.success,
  },

  // info: Apple HIG의 시스템 블루 — 정보 알림
  info: {
    main: tokens.color.info,
  },

  // background: 토스의 연한 회색 배경으로 카드가 돋보이게 함
  background: {
    default: tokens.color.background,    // 페이지 전체 배경
    paper: tokens.color.surface,          // 카드, 모달 등 표면
  },

  // text: 토스의 3단계 텍스트 계층 (진한 → 중간 → 연한)
  text: {
    primary: tokens.color.textPrimary,     // 제목, 본문
    secondary: tokens.color.textSecondary, // 날짜, 메타 정보
    disabled: tokens.color.textTertiary,   // 비활성 텍스트
  },

  // divider: 토스의 얇은 구분선 색상
  divider: tokens.color.divider,
};

/**
 * 다크 모드 팔레트
 * 토스 앱의 다크 모드와 GitHub Dark 테마를 참고
 * 밝기가 반전되면서도 가독성이 보장되도록 설계
 */
export const darkPalette: PaletteOptions = {
  mode: 'dark',

  primary: {
    main: tokens.darkColor.primary,
    light: tokens.darkColor.primaryLight,
    dark: tokens.darkColor.primaryDark,
    contrastText: tokens.darkColor.textOnPrimary,
  },

  secondary: {
    main: tokens.darkColor.primary,
    light: tokens.darkColor.primaryLight,
    dark: tokens.darkColor.primaryDark,
  },

  error: { main: tokens.darkColor.error },
  warning: { main: tokens.darkColor.warning },
  success: { main: tokens.darkColor.success },
  info: { main: tokens.darkColor.info },

  background: {
    default: tokens.darkColor.background,
    paper: tokens.darkColor.surface,
  },

  text: {
    primary: tokens.darkColor.textPrimary,
    secondary: tokens.darkColor.textSecondary,
    disabled: tokens.darkColor.textTertiary,
  },

  divider: tokens.darkColor.divider,
};
