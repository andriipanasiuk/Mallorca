import { cleanup, fireEvent, render, screen } from '@testing-library/preact';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { SessionOver } from './SessionOver.tsx';

afterEach(cleanup);

describe('SessionOver', () => {
  it('shows the result, the rate and the hand count', () => {
    const onPlayAgain = vi.fn();
    render(
      <SessionOver
        session={{ hands: 7, netBb: -4.5, bbPer100: -64.3 }}
        onPlayAgain={onPlayAgain}
      />,
    );
    const dialog = screen.getByTestId('session-over');
    expect(dialog.textContent).toContain('Session over');
    expect(dialog.textContent).toContain('-4.5 bb over 7 hands');
    expect(dialog.textContent).toContain('-64.3 bb/100');
    fireEvent.click(screen.getByTestId('play-again'));
    expect(onPlayAgain).toHaveBeenCalledOnce();
  });

  it('omits the rate with no hands', () => {
    render(<SessionOver session={{ hands: 0, netBb: 0, bbPer100: null }} onPlayAgain={vi.fn()} />);
    const dialog = screen.getByTestId('session-over');
    expect(dialog.textContent).toContain('0.0 bb over 0 hands');
    expect(dialog.textContent).not.toContain('bb/100');
  });
});
