/**
 * ====================================================
 * MUI 컴포넌트 스타일 오버라이드
 *
 * MUI 기본 컴포넌트의 스타일을 토스/Apple 스타일로 커스터마이징합니다.
 * 각 오버라이드에 어떤 디자인 참조를 기반으로 했는지 주석으로 설명합니다.
 *
 * 오버라이드 대상:
 *   MuiButton          → 토스 스타일 풀너비 버튼 (52px 높이)
 *   MuiCard            → 토스 스타일 둥근 카드
 *   MuiTextField       → 토스 스타일 배경색 입력 필드 (테두리 없음)
 *   MuiAppBar          → 토스 스타일 플랫 앱바
 *   MuiBottomNavigation → Apple 스타일 하단 탭 바
 *   MuiChip            → 토스 스타일 작은 칩/태그
 *   MuiDialog          → Apple 스타일 둥근 모달
 *   MuiIconButton      → 토스 스타일 아이콘 버튼 호버
 *
 * 의존성: tokens.ts
 * 사용 위치: index.ts (createTheme)
 * ====================================================
 */

import type { Components, Theme } from '@mui/material/styles';
import { tokens } from './tokens';

/**
 * MUI 컴포넌트 기본 스타일 오버라이드
 */
export const components: Components<Theme> = {
  /**
   * MuiButton 오버라이드
   * 참고: 토스 앱의 CTA(Call-to-Action) 버튼
   * - 높이 52px로 모바일에서 터치하기 편한 크기
   * - border-radius: 12px (tokens.radius.md)
   * - 텍스트 대문자 변환 비활성화 (한국어 UI에 불필요)
   * - 그림자 제거 (토스 스타일의 플랫 디자인)
   */
  MuiButton: {
    styleOverrides: {
      root: {
        borderRadius: tokens.radius.md,
        textTransform: 'none',
        fontWeight: 600,
        boxShadow: 'none',
        '&:hover': {
          boxShadow: 'none',
        },
      },
      // contained 변형: 메인 CTA 버튼 (52px 높이)
      containedSizeLarge: {
        height: 52,
        fontSize: tokens.typography.button.fontSize,
      },
      // 풀너비 옵션 (login, signup 등에서 사용)
      fullWidth: {
        height: 52,
      },
    },
    defaultProps: {
      // 기본적으로 그림자 없는 스타일
      disableElevation: true,
    },
  },

  /**
   * MuiCard 오버라이드
   * 참고: 토스 앱의 카드 컴포넌트
   * - 둥근 모서리 (tokens.radius.lg = 16px)
   * - 부드러운 그림자 (Apple 스타일)
   * - 넉넉한 패딩 (tokens.spacing.lg = 16px)
   */
  MuiCard: {
    styleOverrides: {
      root: {
        borderRadius: tokens.radius.lg,
        boxShadow: tokens.shadow.sm,
        padding: tokens.spacing.lg,
      },
    },
    defaultProps: {
      elevation: 0,
    },
  },

  /**
   * MuiTextField 오버라이드
   * 참고: 토스 앱의 입력 필드
   * - 배경색이 있는 스타일 (surfaceVariant)
   * - 테두리 없음 (outlined 변형이지만 시각적으로 플랫)
   * - 둥근 모서리 (tokens.radius.md = 12px)
   */
  MuiTextField: {
    styleOverrides: {
      root: {
        '& .MuiOutlinedInput-root': {
          borderRadius: tokens.radius.md,
          backgroundColor: tokens.color.surfaceVariant,
          '& fieldset': {
            borderColor: 'transparent',
          },
          '&:hover fieldset': {
            borderColor: tokens.color.border,
          },
          '&.Mui-focused fieldset': {
            borderColor: tokens.color.primary,
            borderWidth: 1,
          },
        },
      },
    },
    defaultProps: {
      variant: 'outlined',
      fullWidth: true,
    },
  },

  /**
   * MuiAppBar 오버라이드
   * 참고: 토스 앱의 상단 헤더
   * - 그림자 없음 (플랫한 디자인)
   * - 배경색 surface (흰색)
   * - 텍스트 색상 textPrimary (검정)
   */
  MuiAppBar: {
    styleOverrides: {
      root: {
        boxShadow: 'none',
        backgroundColor: tokens.color.surface,
        color: tokens.color.textPrimary,
        borderBottom: `1px solid ${tokens.color.divider}`,
      },
    },
    defaultProps: {
      elevation: 0,
      color: 'inherit',
    },
  },

  /**
   * MuiBottomNavigation 오버라이드
   * 참고: Apple HIG의 탭 바 + 토스 앱의 하단 네비게이션
   * - 위쪽에 부드러운 그림자 (Apple 스타일)
   * - 배경색 surface
   */
  MuiBottomNavigation: {
    styleOverrides: {
      root: {
        boxShadow: tokens.shadow.bottomSheet,
        backgroundColor: tokens.color.surface,
        height: 56,
      },
    },
  },

  /**
   * MuiChip 오버라이드
   * 참고: 토스 앱의 카테고리 필터 칩
   * - 작은 둥근 모서리 (tokens.radius.sm = 8px)
   * - 작은 크기
   */
  MuiChip: {
    styleOverrides: {
      root: {
        borderRadius: tokens.radius.sm,
      },
    },
    defaultProps: {
      size: 'small',
    },
  },

  /**
   * MuiDialog 오버라이드
   * 참고: Apple HIG의 모달 시트
   * - 큰 둥근 모서리 (tokens.radius.xl = 20px)
   */
  MuiDialog: {
    styleOverrides: {
      paper: {
        borderRadius: tokens.radius.xl,
      },
    },
  },

  /**
   * MuiIconButton 오버라이드
   * 참고: 토스 앱의 아이콘 버튼
   * - 호버 시 primary의 연한 배경색 (primaryLight)
   */
  MuiIconButton: {
    styleOverrides: {
      root: {
        '&:hover': {
          backgroundColor: tokens.color.primaryLight,
        },
      },
    },
  },

  /**
   * MuiCssBaseline 오버라이드
   * 전역 CSS 스타일 (body 배경색, 스크롤바 등)
   */
  MuiCssBaseline: {
    styleOverrides: {
      body: {
        backgroundColor: tokens.color.background,
        // 부드러운 스크롤 (Apple 스타일)
        scrollBehavior: 'smooth',
        // 웹킷 탭 하이라이트 제거 (모바일)
        WebkitTapHighlightColor: 'transparent',
      },
    },
  },
};
