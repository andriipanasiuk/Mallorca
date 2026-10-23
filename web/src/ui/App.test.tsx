import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/preact';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { createSession, inProcessBot } from '../game/index.ts';
import { App } from './App.tsx';

afterEach(cleanup);

function setup() {
  const session = createSession({
    bot: inProcessBot(),
    seed: 42,
    botDelayMs: 0,
    humanSeat: 0,
    names: ['You', 'Mallorca'],
  });
  const onNewTournament = vi.fn();
  render(<App session={session} humanSeat={0} onNewTournament={onNewTournament} />);
  return { session, onNewTournament };
}

describe('App', () => {
  it('starts on the start screen and deals after Start', async () => {
    const { session } = setup();
    expect(screen.getByTestId('start')).not.toBeNull();
    fireEvent.click(screen.getByTestId('start'));
    await waitFor(() => expect(screen.queryByTestId('seat-0')).not.toBeNull());
    await waitFor(() => expect(session.getState().phase).toBe('humanToAct'));
    expect(screen.getByTestId('seat-0').querySelectorAll('[data-testid="card"]').length).toBe(2);
    expect(screen.getByTestId('seat-1').querySelectorAll('[data-testid="card-back"]').length).toBe(
      2,
    );
    const stacks =
      Number(screen.getByTestId('stack-0').textContent) +
      Number(screen.getByTestId('stack-1').textContent);
    const bets = [...document.querySelectorAll('[data-testid^="bet-"]')].length;
    expect(bets).toBeGreaterThan(0);
    expect(stacks).toBeLessThan(4000);
    expect(screen.getByTestId('hand-log')).not.toBeNull();
    session.getState().dispose();
  });

  it('plays a fold to the next hand', async () => {
    const { session } = setup();
    fireEvent.click(screen.getByTestId('start'));
    await waitFor(() => expect(session.getState().phase).toBe('humanToAct'));
    fireEvent.click(screen.getByTestId('fold'));
    await waitFor(() => expect(screen.queryByTestId('next-hand')).not.toBeNull());
    fireEvent.click(screen.getByTestId('next-hand'));
    await waitFor(() => expect(screen.queryByTestId('next-hand')).toBeNull());
    session.getState().dispose();
  });
});
