import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/preact';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { CASH, memoryStore } from '../game/index.ts';
import type { HandRecord } from '../game/index.ts';
import { SessionOver } from './SessionOver.tsx';

const download = vi.hoisted(() => vi.fn());
vi.mock('./download.ts', () => ({ download }));

afterEach(() => {
  cleanup();
  download.mockClear();
});

function sample(sessionId: string, handNumber: number): HandRecord {
  return {
    version: 1,
    id: `${sessionId}-${handNumber}`,
    sessionId,
    mode: CASH,
    handNumber,
    playedAt: '2026-10-23T10:00:00.000Z',
    names: ['You', 'Bot'],
    humanSeat: 0,
    bot: { id: 'math', version: '1' },
    holeCards: [
      ['Ah', 'Kd'],
      ['Qc', 'Qd'],
    ],
    board: [],
    events: [],
    decisions: [],
  };
}

function over(hands: number, netBb: number, bbPer100: number | null, sessionId = 'abc') {
  const store = memoryStore();
  render(
    <SessionOver
      session={{ hands, netBb, bbPer100 }}
      store={store}
      sessionId={sessionId}
      onPlayAgain={vi.fn()}
    />,
  );
  return store;
}

describe('SessionOver', () => {
  it('shows the result, the rate and the hand count', () => {
    const onPlayAgain = vi.fn();
    render(
      <SessionOver
        session={{ hands: 7, netBb: -4.5, bbPer100: -64.3 }}
        store={memoryStore()}
        sessionId="abc"
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
    over(0, 0, null);
    const dialog = screen.getByTestId('session-over');
    expect(dialog.textContent).toContain('0.0 bb over 0 hands');
    expect(dialog.textContent).not.toContain('bb/100');
  });

  it('uses the singular for one hand', () => {
    over(1, 2, 200);
    expect(screen.getByTestId('session-summary').textContent).toBe('+2.0 bb over 1 hand');
  });

  it('exports the records of the session as json and as text', async () => {
    const store = over(1, 0, 0, 's-1');
    await store.add(sample('s-1', 1));
    await store.add(sample('other', 1));
    fireEvent.click(screen.getByTestId('export-json'));
    await waitFor(() => expect(download).toHaveBeenCalledTimes(1));
    expect(download.mock.calls[0]?.[0]).toBe('mallorca-s-1.json');
    expect(download.mock.calls[0]?.[2]).toBe('application/json');
    expect(JSON.parse(download.mock.calls[0]?.[1] as string)).toHaveLength(1);
    fireEvent.click(screen.getByTestId('export-text'));
    await waitFor(() => expect(download).toHaveBeenCalledTimes(2));
    expect(download.mock.calls[1]?.[0]).toBe('mallorca-s-1.txt');
    expect(download.mock.calls[1]?.[2]).toBe('text/plain');
  });
});
