/**
 * ====================================================
 * 사용자 아바타 컴포넌트
 *
 * 프로필 이미지가 있으면 이미지를, 없으면 닉네임 첫 글자를 보여줍니다.
 * 배경색은 닉네임 해시로 결정되어 같은 사용자는 항상 같은 색상입니다.
 *
 * 의존성: @mui/material
 * 사용 위치: shared/components/layout/TopAppBar, features/post 등
 * ====================================================
 */

import Avatar from '@mui/material/Avatar';

/** 아바타 크기 변형 */
type AvatarSize = 'sm' | 'md' | 'lg';

const sizeMap: Record<AvatarSize, number> = {
  sm: 32,
  md: 40,
  lg: 64,
};

/** 닉네임 해시 기반 색상 팔레트 (Material Design 500 계열) */
const colorPalette = [
  '#F44336',
  '#E91E63',
  '#9C27B0',
  '#673AB7',
  '#3F51B5',
  '#2196F3',
  '#03A9F4',
  '#00BCD4',
  '#009688',
  '#4CAF50',
  '#8BC34A',
  '#FF9800',
];

/** 닉네임 문자열에서 일관된 색상 인덱스 계산 */
function getColorFromNickname(nickname: string): string {
  let hash = 0;
  for (let i = 0; i < nickname.length; i++) {
    hash = nickname.charCodeAt(i) + ((hash << 5) - hash);
  }
  return colorPalette[Math.abs(hash) % colorPalette.length];
}

interface UserAvatarProps {
  nickname: string;
  profileImage?: string | null;
  size?: AvatarSize;
}

export function UserAvatar({ nickname, profileImage, size = 'md' }: UserAvatarProps) {
  const px = sizeMap[size];
  const bgColor = getColorFromNickname(nickname);

  return (
    <Avatar
      src={profileImage ?? undefined}
      sx={{
        width: px,
        height: px,
        backgroundColor: bgColor,
        fontSize: px * 0.4,
        fontWeight: 600,
      }}
    >
      {/* 이미지가 없을 때 첫 글자 표시 */}
      {nickname.charAt(0).toUpperCase()}
    </Avatar>
  );
}