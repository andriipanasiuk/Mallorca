import { cleanup, fireEvent, render, screen } from '@testing-library/preact';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { CASH, TOURNAMENT } from '../game/index.ts';
import en from '../locales/en.json';
import { StartScreen } from './StartScreen.tsx';

afterEach(cleanup);

describe('StartScreen', () => {
  it('starts a cash game or a tournament', () => {
    const onStart = vi.fn();
    render(<StartScreen seed={42} onStart={onStart} />);
    fireEvent.click(screen.getByTestId('start-cash'));
    expect(onStart).toHaveBeenLastCalledWith(CASH, 'math');
    fireEvent.click(screen.getByTestId('start-tournament'));
    expect(onStart).toHaveBeenLastCalledWith(TOURNAMENT, 'math');
  });

  it('starts against the chosen opponent', () => {
    const onStart = vi.fn();
    render(<StartScreen seed={42} onStart={onStart} />);
    expect(screen.getByTestId('opponent-math').getAttribute('aria-pressed')).toBe('true');
    fireEvent.click(screen.getByTestId('opponent-cfr'));
    expect(screen.getByTestId('opponent-cfr').getAttribute('aria-pressed')).toBe('true');
    expect(screen.getByTestId('opponent-math').getAttribute('aria-pressed')).toBe('false');
    fireEvent.click(screen.getByTestId('start-cash'));
    expect(onStart).toHaveBeenLastCalledWith(CASH, 'cfr');
  });

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

  it('sits over the dimmed room', () => {
    render(<StartScreen seed={1} onStart={vi.fn()} />);
    expect(screen.getByTestId('scene').classList.contains('scene-dimmed')).toBe(true);
  });
});
