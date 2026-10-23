import { cleanup, render } from '@testing-library/preact';
import { afterEach, describe, expect, it } from 'vitest';
import { Card } from './Card.tsx';

afterEach(cleanup);

describe('Card', () => {
  it('renders rank and suit for a face-up card', () => {
    const { container } = render(<Card card={51} />);
    expect(container.textContent).toContain('A');
    expect(container.textContent).toContain('♠');
  });

  it('writes ten as 10', () => {
    const { container } = render(<Card card={32} />);
    expect(container.textContent).toContain('10');
  });

  it('colours hearts and diamonds red and clubs and spades black', () => {
    const diamond = render(<Card card={1} />).container.querySelector('svg');
    const heart = render(<Card card={2} />).container.querySelector('svg');
    const club = render(<Card card={0} />).container.querySelector('svg');
    const spade = render(<Card card={3} />).container.querySelector('svg');
    expect(diamond?.classList.contains('card-red')).toBe(true);
    expect(heart?.classList.contains('card-red')).toBe(true);
    expect(club?.classList.contains('card-black')).toBe(true);
    expect(spade?.classList.contains('card-black')).toBe(true);
  });

  it('renders the back without rank text', () => {
    const { container } = render(<Card card="back" />);
    expect(container.querySelector('svg')).not.toBeNull();
    expect(container.querySelector('text')).toBeNull();
    expect(container.textContent).toBe('');
  });
});
