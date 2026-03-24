/**
 * ====================================================
 * MUI 테마 설정 (진입점)
 *
 * palette, typography, components를 합쳐서 하나의 MUI 테마를 생성합니다.
 * 이 파일에서 내보낸 theme 객체를 ThemeProvider에 전달합니다.
 *
 * 테마 변경 방법:
 *   1. tokens.ts의 디자인 토큰 값 변경 → 자동으로 전체 반영
 *   2. palette.ts, typography.ts, components.ts에서 MUI 매핑 조정
 *
 * 의존성: palette.ts, typography.ts, components.ts
 * 사용 위치: providers.tsx (ThemeProvider)
 * ====================================================
 */

import { createTheme } from '@mui/material/styles';
import { lightPalette } from './palette';
import { typography } from './typography';
import { components } from './components';
import { tokens } from './tokens';

/**
 * MUI 커스텀 테마
 *
 * 디자인 토큰 기반으로 생성된 토스 스타일 테마입니다.
 * 기본적으로 라이트 모드를 사용합니다.
 * 다크 모드 전환은 추후 테마 스토어를 통해 구현 예정입니다.
 */
const theme = createTheme({
  // 컬러 팔레트 (Light 모드)
  palette: lightPalette,

  // 타이포그래피 (Pretendard 기반)
  typography,

  // 컴포넌트 기본 스타일 오버라이드
  components,

  // 커스텀 shape 설정
  shape: {
    borderRadius: tokens.radius.md,  // MUI 기본 border-radius → 12px
  },

  // 브레이크포인트 설정 (모바일 퍼스트)
  breakpoints: {
    values: {
      xs: tokens.breakpoint.mobile,     // 0px (모바일)
      sm: tokens.breakpoint.tablet,     // 600px (태블릿)
      md: tokens.breakpoint.desktop,    // 1024px (데스크탑)
      lg: 1200,                          // 대형 데스크탑
      xl: 1536,                          // 초대형 화면
    },
  },
});

export default theme;
