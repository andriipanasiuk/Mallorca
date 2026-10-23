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
  render(
    <App session={session} humanSeat={0} onNewTournament={onNewTournament} onLeave={vi.fn()} />,
  );
  return { session, onNewTournament };
}

describe('App', () => {
  it('switches to the simple table from the menu and remembers it', async () => {
    localStorage.clear();
    const { session } = setup();
    expect(screen.getByTestId('scene').classList.contains('scene-simple')).toBe(false);
    fireEvent.click(screen.getByTestId('start'));
    await waitFor(() => expect(session.getState().phase).toBe('humanToAct'));
    const handBefore = screen.getByTestId('hand-number').textContent;
    const toggle = screen.getByTestId('simple-table') as HTMLInputElement;
    expect(toggle.checked).toBe(false);
    fireEvent.click(toggle);
    expect(screen.getByTestId('scene').classList.contains('scene-simple')).toBe(true);
    expect(localStorage.getItem('mallorca.tableStyle')).toBe('simple');
    expect(screen.getByTestId('hand-number').textContent).toBe(handBefore);
    expect(session.getState().phase).toBe('humanToAct');
    fireEvent.click(screen.getByTestId('simple-table'));
    expect(screen.getByTestId('scene').classList.contains('scene-simple')).toBe(false);
    expect(localStorage.getItem('mallorca.tableStyle')).toBe('room');
    session.getState().dispose();
    localStorage.clear();
  });

  it('opens on the simple table when it was chosen before', () => {
    localStorage.setItem('mallorca.tableStyle', 'simple');
    const { session } = setup();
    expect(screen.getByTestId('scene').classList.contains('scene-simple')).toBe(true);
    session.getState().dispose();
    localStorage.clear();
  });

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
