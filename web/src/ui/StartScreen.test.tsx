import { cleanup, render, screen } from '@testing-library/preact';
import { afterEach, describe, expect, it, vi } from 'vitest';
import en from '../locales/en.json';
import { StartScreen } from './StartScreen.tsx';

afterEach(cleanup);

describe('StartScreen', () => {
  it('shows the farewell line in place of the tagline', () => {
    const { container } = render(<StartScreen seed={1} onStart={vi.fn()} farewell />);
    expect(screen.getByTestId('farewell').textContent).toBe('Sleep well. The table will be here.');
    expect(container.textContent).not.toContain(en.start.tagline);
  });

  it('shows the tagline by default', () => {
    const { container } = render(<StartScreen seed={1} onStart={vi.fn()} />);
    expect(screen.queryByTestId('farewell')).toBeNull();
    expect(container.textContent).toContain(en.start.tagline);
  });

  it('invites the player to take a seat over the dimmed room', () => {
    render(<StartScreen seed={1} onStart={vi.fn()} />);
    expect(screen.getByTestId('start').textContent).toBe('Take a seat');
    expect(screen.getByTestId('scene').classList.contains('scene-dimmed')).toBe(true);
  });
});
