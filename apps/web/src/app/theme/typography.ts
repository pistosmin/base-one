/**
 * ====================================================
 * MUI 타이포그래피 설정
 *
 * tokens.ts의 타이포그래피 토큰을 MUI createTheme의 typography 포맷으로 변환합니다.
 * Pretendard Variable 폰트를 기반으로 한국어에 최적화된 자간과 행간을 설정합니다.
 *
 * MUI typography 구조 → tokens.typography 매핑:
 *   h1        → headline1 (26px, 볼드) — 페이지 메인 제목
 *   h2        → headline2 (22px, 볼드) — 섹션 제목
 *   h3, h4    → title1, title2 (19px, 17px) — 카드/리스트 제목
 *   body1     → body1 (16px) — 본문 텍스트
 *   body2     → body2 (14px) — 보조 텍스트
 *   caption   → caption (12px) — 메타 정보
 *   button    → button (16px, 세미볼드) — 버튼 텍스트
 *
 * 의존성: tokens.ts
 * 사용 위치: index.ts (createTheme)
 * ====================================================
 */

import type { TypographyOptions } from '@mui/material/styles';
import { tokens } from './tokens';

/**
 * MUI 타이포그래피 설정
 * 토스 TDS의 타이포그래피 스케일을 MUI 포맷으로 매핑
 */
export const typography: TypographyOptions = {
  // 전역 폰트 패밀리 (Pretendard → 시스템 폰트 폴백 체인)
  fontFamily: tokens.typography.fontFamily,

  // h1: 페이지 메인 제목 (예: "인기 게시글", "내 프로필")
  h1: {
    fontSize: tokens.typography.headline1.fontSize,
    fontWeight: tokens.typography.headline1.fontWeight,
    lineHeight: tokens.typography.headline1.lineHeight,
    letterSpacing: tokens.typography.headline1.letterSpacing,
  },

  // h2: 섹션 제목 (예: "최신 글", "카테고리별 보기")
  h2: {
    fontSize: tokens.typography.headline2.fontSize,
    fontWeight: tokens.typography.headline2.fontWeight,
    lineHeight: tokens.typography.headline2.lineHeight,
    letterSpacing: tokens.typography.headline2.letterSpacing,
  },

  // h3: 카드 제목, 리스트 아이템 제목
  h3: {
    fontSize: tokens.typography.title1.fontSize,
    fontWeight: tokens.typography.title1.fontWeight,
    lineHeight: tokens.typography.title1.lineHeight,
  },

  // h4: 서브 타이틀, 작은 제목
  h4: {
    fontSize: tokens.typography.title2.fontSize,
    fontWeight: tokens.typography.title2.fontWeight,
    lineHeight: tokens.typography.title2.lineHeight,
  },

  // h5, h6: 필요시 사용 (기본값 유지)
  h5: {
    fontSize: '15px',
    fontWeight: 600,
    lineHeight: 1.4,
  },
  h6: {
    fontSize: '13px',
    fontWeight: 600,
    lineHeight: 1.4,
  },

  // body1: 메인 본문 텍스트 (게시글 내용, 댓글 등)
  body1: {
    fontSize: tokens.typography.body1.fontSize,
    fontWeight: tokens.typography.body1.fontWeight,
    lineHeight: tokens.typography.body1.lineHeight,
  },

  // body2: 보조 본문 텍스트 (메타 정보, 부가 설명)
  body2: {
    fontSize: tokens.typography.body2.fontSize,
    fontWeight: tokens.typography.body2.fontWeight,
    lineHeight: tokens.typography.body2.lineHeight,
  },

  // caption: 가장 작은 텍스트 (타임스탬프, 뱃지 등)
  caption: {
    fontSize: tokens.typography.caption.fontSize,
    fontWeight: tokens.typography.caption.fontWeight,
    lineHeight: tokens.typography.caption.lineHeight,
  },

  // button: 버튼 텍스트 (세미볼드, 대문자 변환 비활성화)
  button: {
    fontSize: tokens.typography.button.fontSize,
    fontWeight: tokens.typography.button.fontWeight,
    lineHeight: tokens.typography.button.lineHeight,
    textTransform: 'none',  // MUI 기본 대문자 변환 비활성화 (토스 스타일)
  },
};
