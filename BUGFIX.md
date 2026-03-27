# 버그 수정 및 개선 이력

이 파일은 발견된 버그, 개선 사항, 수정 내용을 날짜순으로 기록합니다.

---

## 2026-03-27

### [개선] 회원가입 이메일 중복 오류 UX 개선

**증상**
이메일 중복 발생 시 409 Conflict 응답이 오지만, 상단 Alert에 일반 오류 메시지로만 표시되어 사용자가 어느 필드를 수정해야 하는지 불명확했음.

**원인**
`SignupForm.tsx`에서 `mutation.error`를 단순 Alert으로만 표시하고, 에러 코드(`DUPLICATE_EMAIL`, `DUPLICATE_NICKNAME`)를 구분하지 않았음.

**수정 내용**
- `apps/web/src/features/auth/components/SignupForm.tsx`
  - `mutation.mutate()`의 `onError` 콜백에서 `ApiError.code` 확인
  - `DUPLICATE_EMAIL` → 이메일 필드에 `setError`로 인라인 에러 표시
  - `DUPLICATE_NICKNAME` → 닉네임 필드에 `setError`로 인라인 에러 표시
  - 중복 에러는 상단 Alert 미표시, 그 외 서버 오류만 Alert 표시

---

### [개선] 로그인 실패 시 안내 메시지 개선

**증상**
이메일 또는 비밀번호가 틀렸을 때 서버 메시지가 그대로 노출되고, 어느 필드가 잘못되었는지 시각적 피드백이 없었음.

**수정 내용**
- `apps/web/src/features/auth/components/LoginForm.tsx`
  - `INVALID_CREDENTIALS` 에러 코드 감지 시 두 필드 모두 에러 스타일 표시 (`setError`)
  - 보안상 어느 쪽이 틀렸는지 명시하지 않고, Alert 메시지를 "입력하신 이메일 또는 비밀번호를 다시 확인해주세요."로 안내

---
