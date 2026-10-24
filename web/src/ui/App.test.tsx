import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/preact';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { CASH, createSession, inProcessBot, memoryStore, TOURNAMENT } from '../game/index.ts';
import { App } from './App.tsx';

afterEach(cleanup);

function setup(mode = TOURNAMENT, store = memoryStore()) {
  const session = createSession({
    bot: inProcessBot(),
    seed: 42,
    botDelayMs: 0,
    humanSeat: 0,
    names: ['You', 'Mallorca'],
    mode,
  });
  const onNewGame = vi.fn();
  render(<App session={session} store={store} humanSeat={0} onNewGame={onNewGame} />);
  return { session, onNewGame };
}

describe('App', () => {
  it('starts on the start screen and deals after Start', async () => {
    const { session } = setup();
    expect(screen.getByTestId('start-tournament')).not.toBeNull();
    fireEvent.click(screen.getByTestId('start-tournament'));
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
    fireEvent.click(screen.getByTestId('start-tournament'));
    await waitFor(() => expect(session.getState().phase).toBe('humanToAct'));
    fireEvent.click(screen.getByTestId('fold'));
    await waitFor(() => expect(screen.queryByTestId('next-hand')).not.toBeNull());
    fireEvent.click(screen.getByTestId('next-hand'));
    await waitFor(() => expect(screen.queryByTestId('next-hand')).toBeNull());
    session.getState().dispose();
  });

  it('shows the running result and a leave button in a cash game', async () => {
    const { session, onNewGame } = setup(CASH);
    fireEvent.click(screen.getByTestId('start-cash'));
    await waitFor(() => expect(session.getState().phase).toBe('humanToAct'));
    expect(screen.getByTestId('leave').hasAttribute('disabled')).toBe(true);
    session.setState({ phase: 'handOver', session: { hands: 1, net: 250 } });
    await waitFor(() => expect(screen.getByTestId('session-result').textContent).toBe('+12.5 bb'));
    expect(screen.queryByTestId('blinds')).not.toBeNull();
    expect(screen.queryByTestId('leave')?.hasAttribute('disabled')).toBe(false);
    fireEvent.click(screen.getByTestId('leave'));
    await waitFor(() => expect(screen.queryByTestId('session-over')).not.toBeNull());
    expect(screen.getByTestId('session-over').textContent).toContain('+12.5 bb over 1 hand');
    fireEvent.click(screen.getByTestId('play-again'));
    expect(onNewGame).toHaveBeenCalledOnce();
    session.getState().dispose();
  });

  it('has no leave button or session result in a tournament', async () => {
    const { session } = setup();
    fireEvent.click(screen.getByTestId('start-tournament'));
    await waitFor(() => expect(session.getState().phase).toBe('humanToAct'));
    expect(screen.queryByTestId('leave')).toBeNull();
    expect(screen.queryByTestId('session-result')).toBeNull();
    session.getState().dispose();
  });

  it('clears the history only on the second tap and resets when the menu closes', async () => {
    const store = memoryStore();
    const clear = vi.spyOn(store, 'clear');
    const { session } = setup(CASH, store);
    fireEvent.click(screen.getByTestId('start-cash'));
    await waitFor(() => expect(session.getState().phase).toBe('humanToAct'));
    const button = screen.getByTestId('clear-history');
    expect(button.textContent).toBe('Clear history');
    fireEvent.click(button);
    expect(clear).not.toHaveBeenCalled();
    expect(screen.getByTestId('clear-history').textContent).toBe('Tap again to clear');
    const menu = document.querySelector('details.menu') as HTMLDetailsElement;
    menu.open = true;
    menu.open = false;
    fireEvent(menu, new Event('toggle'));
    await waitFor(() =>
      expect(screen.getByTestId('clear-history').textContent).toBe('Clear history'),
    );
    fireEvent.click(screen.getByTestId('clear-history'));
    expect(clear).not.toHaveBeenCalled();
    fireEvent.click(screen.getByTestId('clear-history'));
    await waitFor(() => expect(clear).toHaveBeenCalledOnce());
    expect(screen.getByTestId('clear-history').textContent).toBe('Clear history');
    session.getState().dispose();
  });
});
