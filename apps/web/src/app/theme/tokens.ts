/**
 * ====================================================
 * 디자인 토큰 정의 파일
 *
 * 앱의 모든 시각적 속성을 중앙에서 관리하는 "변수" 모음입니다.
 * 이 파일의 값을 변경하면 앱 전체의 시각적 스타일이 일괄 변경됩니다.
 *
 * AI에게 디자인 수정을 요청할 때 이 파일의 키를 참조하세요.
 * 예: "tokens.color.primary를 #10B981로 변경해줘"
 *
 * MUI 테마와의 관계:
 *   tokens.ts → palette.ts, typography.ts, components.ts에서 MUI 포맷으로 변환
 *   → index.ts에서 createTheme()에 전달
 *
 * 의존성: 없음 (순수 데이터)
 * 사용 위치: palette.ts, typography.ts, components.ts
 * ====================================================
 */

export const tokens = {
  // === 컬러 팔레트 ===
  // 토스 앱의 컬러 시스템을 참고하되, Material Design 3의 체계를 따릅니다
  color: {
    // 메인 브랜드 색상 (토스의 시그니처 블루)
    primary: '#3182F6',
    primaryLight: '#EBF3FE',     // primary의 10% 불투명도 버전 (배경용)
    primaryDark: '#1B64DA',      // hover/active 상태용

    // 시맨틱 컬러 (Apple HIG의 시스템 컬러 참고)
    success: '#34C759',          // 성공, 완료
    warning: '#FF9500',          // 경고, 주의
    error: '#FF3B30',            // 에러, 삭제, 위험
    info: '#007AFF',             // 정보성 알림

    // 배경 컬러 (토스의 회색 계열)
    background: '#F2F4F6',       // 페이지 전체 배경 (연한 회색)
    surface: '#FFFFFF',          // 카드, 모달 등 표면 배경
    surfaceVariant: '#F8F9FA',   // 입력 필드 배경 등 약간 다른 표면

    // 텍스트 컬러 (토스의 텍스트 계층)
    textPrimary: '#191F28',      // 본문, 제목 (거의 검정)
    textSecondary: '#8B95A1',    // 보조 텍스트, 날짜, 메타 정보
    textTertiary: '#B0B8C1',     // 비활성 텍스트, 플레이스홀더
    textOnPrimary: '#FFFFFF',    // primary 배경 위의 텍스트 (흰색)

    // 구분선 & 테두리
    border: '#E5E8EB',           // 카드 테두리, 구분선
    divider: '#F2F4F6',          // 리스트 아이템 사이 얇은 구분선
  },

  // === 다크 모드 컬러 ===
  // 라이트 모드 대비 반전된 밝기 + 가독성 보장
  darkColor: {
    primary: '#4D96FF',
    primaryLight: '#1A2B4A',
    primaryDark: '#3182F6',

    success: '#30D158',
    warning: '#FFD60A',
    error: '#FF453A',
    info: '#0A84FF',

    background: '#0D1117',
    surface: '#161B22',
    surfaceVariant: '#21262D',

    textPrimary: '#E6EDF3',
    textSecondary: '#8B949E',
    textTertiary: '#484F58',
    textOnPrimary: '#FFFFFF',

    border: '#30363D',
    divider: '#21262D',
  },

  // === 타이포그래피 ===
  // Pretendard 폰트 사용 (한국어 최적화, 가변 폰트)
  // index.html에 <link> 태그로 Pretendard 웹폰트를 로딩해야 합니다
  typography: {
    fontFamily: '"Pretendard Variable", "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',

    // 각 스타일의 이름은 토스 TDS의 명명 규칙을 참고
    headline1: { fontSize: '26px', fontWeight: 700, lineHeight: 1.35, letterSpacing: '-0.02em' },
    headline2: { fontSize: '22px', fontWeight: 700, lineHeight: 1.36, letterSpacing: '-0.02em' },
    title1:    { fontSize: '19px', fontWeight: 600, lineHeight: 1.42 },
    title2:    { fontSize: '17px', fontWeight: 600, lineHeight: 1.41 },
    body1:     { fontSize: '16px', fontWeight: 400, lineHeight: 1.5 },
    body2:     { fontSize: '14px', fontWeight: 400, lineHeight: 1.43 },
    caption:   { fontSize: '12px', fontWeight: 400, lineHeight: 1.33 },
    button:    { fontSize: '16px', fontWeight: 600, lineHeight: 1.5 },
  },

  // === 간격 시스템 (4px 배수) ===
  // 토스 앱의 간격 체계를 참고합니다
  spacing: {
    xs: 4,        // 아이콘과 텍스트 사이 등 최소 간격
    sm: 8,        // 칩 사이, 작은 요소 간 간격
    md: 12,       // 카드 내부 요소 간 간격
    lg: 16,       // 카드 패딩, 리스트 아이템 패딩
    xl: 20,       // 섹션 내 요소 간 간격
    xxl: 24,      // 섹션 간 간격
    section: 28,  // 큰 섹션 구분 (예: 홈 화면의 "인기 글" ↔ "최신 글")
    page: 20,     // 화면 좌우 패딩 (모바일 기준)
  },

  // === 모서리 둥글기 ===
  radius: {
    sm: 8,        // 태그, 뱃지, 작은 칩
    md: 12,       // 버튼, 입력 필드
    lg: 16,       // 카드, 이미지
    xl: 20,       // 바텀시트, 모달
    full: 9999,   // 완전한 원형 (아바타, FAB)
  },

  // === 그림자 (Apple 스타일 부드러운 그림자) ===
  shadow: {
    sm: '0 1px 3px rgba(0,0,0,0.04), 0 1px 2px rgba(0,0,0,0.06)',
    md: '0 4px 12px rgba(0,0,0,0.08)',
    lg: '0 8px 24px rgba(0,0,0,0.12)',
    bottomSheet: '0 -4px 24px rgba(0,0,0,0.12)',
  },

  // === 애니메이션 (Framer Motion에서 사용) ===
  motion: {
    duration: {
      fast: 150,     // 밀리초. 버튼 피드백, 토글 등 즉각적 반응
      normal: 250,   // 페이지 전환, 카드 등장
      slow: 350,     // 바텀시트, 모달 등장
    },
    easing: {
      standard: [0.4, 0.0, 0.2, 1] as const,       // Material 표준 이징
      decelerate: [0.0, 0.0, 0.2, 1] as const,     // 요소가 화면에 진입할 때
      accelerate: [0.4, 0.0, 1, 1] as const,       // 요소가 화면에서 퇴장할 때
      spring: { type: 'spring' as const, stiffness: 300, damping: 24 }, // 토스 스타일 탄성
    },
  },

  // === 반응형 브레이크포인트 (모바일 퍼스트) ===
  breakpoint: {
    mobile: 0,       // 0 ~ 599px  (기본, 모바일 우선 설계)
    tablet: 600,     // 600 ~ 1023px
    desktop: 1024,   // 1024px ~ (관리자 대시보드 등)
  },

  // === Z-Index 계층 ===
  zIndex: {
    bottomTabBar: 1000,
    topAppBar: 1100,
    modal: 1300,
    toast: 1400,
  },
} as const;

// 타입 내보내기 (다른 파일에서 참조용)
export type DesignTokens = typeof tokens;
