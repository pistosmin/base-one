/**
 * UserAvatar 컴포넌트 테스트
 */
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { UserAvatar } from '../UserAvatar';

describe('UserAvatar', () => {
  it('닉네임 첫 글자를 표시한다', () => {
    render(<UserAvatar nickname="테스트유저" />);

    // 닉네임 첫 글자가 표시되는지 확인
    expect(screen.getByText('테')).toBeInTheDocument();
  });

  it('프로필 이미지가 있을 때 이미지를 표시한다', () => {
    render(
      <UserAvatar
        nickname="테스트유저"
        profileImage="https://example.com/avatar.jpg"
      />
    );

    // img 태그가 올바른 src를 가지고 있는지 확인
    const img = screen.getByRole('img');
    expect(img).toHaveAttribute('src', 'https://example.com/avatar.jpg');
  });

  it('영어 닉네임의 경우 대문자로 표시한다', () => {
    render(<UserAvatar nickname="john" />);

    expect(screen.getByText('J')).toBeInTheDocument();
  });
});