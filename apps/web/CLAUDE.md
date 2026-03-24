# Frontend Context — React 19.2 + Vite + MUI 7.3

## 빌드 & 테스트 명령어
```bash
cd apps/web
npm run dev                    # Vite 개발 서버 (localhost:5173)
npm run build                  # 프로덕션 빌드
npm test -- --run              # 테스트 실행 (Vitest)
npx eslint src/ --max-warnings 0  # ESLint 린트
npx tsc --noEmit               # TypeScript 타입 체크
```

## 디렉토리 구조 (Feature-Sliced Design)
```
src/
├── app/                           # 앱 설정
│   ├── App.tsx                    # 루트 컴포넌트
│   ├── router.tsx                 # React Router 설정 (React.lazy + Suspense)
│   ├── providers.tsx              # QueryClient, ThemeProvider 등 프로바이더 합성
│   └── theme/                     # MUI 테마 (토스 스타일)
│       ├── index.ts               # createTheme() 내보내기
│       ├── tokens.ts              # 디자인 토큰 (컬러, 타이포, 스페이싱)
│       ├── palette.ts             # MUI 컬러 팔레트
│       ├── typography.ts          # Pretendard 타이포그래피
│       └── components.ts          # MUI 컴포넌트 오버라이드
├── features/                      # 기능별 모듈 (각 feature는 독립적)
│   ├── auth/                      # 인증 (api, hooks, store, components, pages)
│   ├── post/                      # 게시글
│   ├── comment/                   # 댓글
│   ├── user/                      # 프로필
│   ├── notification/              # 알림
│   ├── admin/                     # 관리자
│   └── home/                      # 홈
├── shared/                        # 공유 리소스 (feature 간 공용)
│   ├── api/                       # axiosInstance, apiTypes
│   ├── components/layout/         # AppLayout, TopAppBar, BottomTabBar
│   ├── components/feedback/       # LoadingSpinner, EmptyState, ErrorFallback
│   ├── components/common/         # VoteButton, UserAvatar, TimeAgo
│   ├── hooks/                     # useInfiniteScroll, useDebounce
│   ├── guards/                    # AuthGuard, AdminGuard
│   ├── types/                     # user.ts, post.ts, comment.ts, common.ts
│   └── utils/                     # format, validation, storage
└── main.tsx                       # 엔트리 포인트
```

## Feature 간 의존 규칙
- 각 feature 폴더는 **독립적**으로 동작
- 다른 feature를 **직접 import 금지**
- feature 간 공유가 필요한 코드는 반드시 `shared/`에 배치
- feature 공개 API는 `index.ts` barrel export 사용

## React Router v7 주의사항
```typescript
// ✅ 올바른 import (v7)
import { BrowserRouter, Routes, Route, Link, useNavigate, useParams, useLocation } from 'react-router';
// ❌ 절대 사용 금지 (v6 이하)
import { ... } from 'react-router-dom';
```

## MUI v7 주의사항
```typescript
// ✅ Grid2가 Grid로 승격
import Grid from '@mui/material/Grid';  // 이것은 Grid2
// ❌ 구버전 Grid API
import { Grid } from '@mui/material';   // v6 이하 방식

// Theme은 이미 providers.tsx에서 설정됨
// 컴포넌트에서 직접 사용: sx prop으로 토큰 참조
<Box sx={{ p: 2, borderRadius: 'sm' }} />
```

## TanStack Query v5 패턴
```typescript
// 목록 조회 (무한 스크롤)
useInfiniteQuery({
  queryKey: ['posts', { categoryId, sort }],
  queryFn: ({ pageParam = 0 }) => getPosts({ ...params, page: pageParam }),
  getNextPageParam: (lastPage) => lastPage.hasNext ? lastPage.page + 1 : undefined,
})

// 단일 조회
useQuery({ queryKey: ['post', id], queryFn: () => getPost(id), enabled: !!id })

// 변경 (생성/수정/삭제)
useMutation({
  mutationFn: createPost,
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['posts'] })
  },
})
```

## Zustand v5 패턴
```typescript
import { create } from 'zustand'
import { persist } from 'zustand/middleware'

// create<StoreType>()(persist(...))  ← 이중 괄호 주의
const useAuthStore = create<AuthStore>()(
  persist(
    (set) => ({ ... }),
    { name: 'auth-storage' }
  )
)
```

## 폼 관리 패턴
```typescript
// React Hook Form + Zod
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'

const schema = z.object({
  email: z.string().email('올바른 이메일을 입력하세요'),
  password: z.string().min(8, '비밀번호는 8자 이상이어야 합니다'),
})

const { register, handleSubmit, formState: { errors } } = useForm({
  resolver: zodResolver(schema),
})
```

## 디자인 토큰 수정 가이드
디자인 수정 요청 시 `apps/web/src/app/theme/tokens.ts`의 값을 변경:
- 색상: `tokens.color.primary`, `tokens.color.background`
- 타이포: `tokens.typography.headline1`, `tokens.typography.body1`
- 간격: `tokens.spacing.lg`, `tokens.spacing.page`
- 둥글기: `tokens.radius.md`, `tokens.radius.lg`

## Vite 설정 참고
- 별칭: `@` → `./src` (모든 import에서 `@/` 사용)
- 프록시: `/api` → `http://localhost:8080` (백엔드 포워딩)
- Node.js 20.19+ 필요 (Vite 7)

## 테스트 작성 가이드
- 프레임워크: Vitest + Testing Library
- 설정: `vitest.config.ts` (jsdom 환경)
- 컴포넌트: `render(<Component />)` + `screen.getByText(...)` + `fireEvent`
- 훅: `renderHook(() => useCustomHook())`
- 모킹: `vi.mock('@/shared/api/axiosInstance')`
