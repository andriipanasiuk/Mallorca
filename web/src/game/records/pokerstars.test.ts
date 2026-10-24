import { describe, expect, it } from 'vitest';
import {
  call,
  check,
  deckOf,
  fold,
  playEvents,
  raiseTo,
  startHand,
} from '../../engine/testDeck.ts';
import { CASH } from '../../engine/index.ts';
import type { Action } from '../../engine/index.ts';
import { toPokerStars } from './pokerstars.ts';
import { handRecord, MATH_BOT } from './record.ts';
import type { HandRecord } from './record.ts';

const PLAYED_AT = '2026-10-23T18:00:00.000Z';

function recordOf(
  stacks: [number, number],
  bigBlindHole: string,
  buttonHole: string,
  board: string,
  actions: Action[],
  humanSeat: 0 | 1 | null = 0,
): HandRecord {
  const step = playEvents(startHand(stacks, deckOf(bigBlindHole, buttonHole, board)), ...actions);
  return handRecord(step.events, [], {
    sessionId: 's1',
    mode: CASH,
    names: ['You', 'Mallorca'],
    humanSeat,
    bot: MATH_BOT,
    playedAt: PLAYED_AT,
  });
}

const checks = (count: number): Action[] => Array.from({ length: count }, () => check);

describe('toPokerStars', () => {
  it('formats a flop fold exactly', () => {
    const record = recordOf([2000, 2000], 'Qs Qh', 'Ah Kd', '2c 7d 9h Ts 3s', [
      raiseTo(60),
      call,
      check,
      raiseTo(60),
      fold,
    ]);
    expect(toPokerStars([record])).toBe(
      [
        "PokerStars Hand #1792778400000001: Hold'em No Limit (10/20) - 2026/10/23 18:00:00 UTC",
        "Table 'Mallorca' 2-max Seat #1 is the button",
        'Seat 1: You (2000 in chips)',
        'Seat 2: Mallorca (2000 in chips)',
        'You: posts small blind 10',
        'Mallorca: posts big blind 20',
        '*** HOLE CARDS ***',
        'Dealt to You [Ah Kd]',
        'You: raises 40 to 60',
        'Mallorca: calls 40',
        '*** FLOP *** [2c 7d 9h]',
        'Mallorca: checks',
        'You: bets 60',
        'Mallorca: folds',
        'Uncalled bet (60) returned to You',
        'You collected 120 from pot',
        '*** SUMMARY ***',
        'Total pot 120 | Rake 0',
        'Board [2c 7d 9h]',
        'Seat 1: You (button) (small blind) collected (120)',
        'Seat 2: Mallorca (big blind) folded on the Flop',
      ].join('\n'),
    );
  });

  it('formats a showdown on the river', () => {
    const record = recordOf([2000, 2000], 'Qs Qh', 'Ah Kd', '2c 7d 9h Ts 3s', [
      raiseTo(60),
      call,
      ...checks(6),
    ]);
    expect(toPokerStars([record])).toBe(
      [
        "PokerStars Hand #1792778400000001: Hold'em No Limit (10/20) - 2026/10/23 18:00:00 UTC",
        "Table 'Mallorca' 2-max Seat #1 is the button",
        'Seat 1: You (2000 in chips)',
        'Seat 2: Mallorca (2000 in chips)',
        'You: posts small blind 10',
        'Mallorca: posts big blind 20',
        '*** HOLE CARDS ***',
        'Dealt to You [Ah Kd]',
        'You: raises 40 to 60',
        'Mallorca: calls 40',
        '*** FLOP *** [2c 7d 9h]',
        'Mallorca: checks',
        'You: checks',
        '*** TURN *** [2c 7d 9h] [Ts]',
        'Mallorca: checks',
        'You: checks',
        '*** RIVER *** [2c 7d 9h Ts] [3s]',
        'Mallorca: checks',
        'You: checks',
        '*** SHOW DOWN ***',
        'Mallorca: shows [Qs Qh] (a pair)',
        'You: shows [Ah Kd] (high card)',
        'Mallorca collected 120 from pot',
        '*** SUMMARY ***',
        'Total pot 120 | Rake 0',
        'Board [2c 7d 9h Ts 3s]',
        'Seat 1: You (button) (small blind) showed [Ah Kd] and lost with high card',
        'Seat 2: Mallorca (big blind) showed [Qs Qh] and won (120) with a pair',
      ].join('\n'),
    );
  });

  it('formats a split pot with a collected line for each seat', () => {
    const record = recordOf([2000, 2000], '2d 3c', '2c 3d', 'Ts Jc Qd Kh Ac', [
      call,
      check,
      ...checks(6),
    ]);
    const lines = toPokerStars([record]).split('\n');
    expect(lines).toContain('You: calls 10');
    expect(lines).toContain('You collected 20 from pot');
    expect(lines).toContain('Mallorca collected 20 from pot');
    expect(lines).toContain('Total pot 40 | Rake 0');
    expect(lines).toContain(
      'Seat 1: You (button) (small blind) showed [2c 3d] and won (20) with a straight',
    );
    expect(lines).toContain(
      'Seat 2: Mallorca (big blind) showed [2d 3c] and won (20) with a straight',
    );
  });

  it('formats a preflop all-in with the run-out', () => {
    const record = recordOf([500, 2000], 'Qs Qh', 'Ah Kd', '2c 7d 9h Ts 3s', [raiseTo(500), call]);
    expect(toPokerStars([record])).toBe(
      [
        "PokerStars Hand #1792778400000001: Hold'em No Limit (10/20) - 2026/10/23 18:00:00 UTC",
        "Table 'Mallorca' 2-max Seat #1 is the button",
        'Seat 1: You (500 in chips)',
        'Seat 2: Mallorca (2000 in chips)',
        'You: posts small blind 10',
        'Mallorca: posts big blind 20',
        '*** HOLE CARDS ***',
        'Dealt to You [Ah Kd]',
        'You: raises 480 to 500 and is all-in',
        'Mallorca: calls 480',
        '*** FLOP *** [2c 7d 9h]',
        '*** TURN *** [2c 7d 9h] [Ts]',
        '*** RIVER *** [2c 7d 9h Ts] [3s]',
        '*** SHOW DOWN ***',
        'Mallorca: shows [Qs Qh] (a pair)',
        'You: shows [Ah Kd] (high card)',
        'Mallorca collected 1000 from pot',
        '*** SUMMARY ***',
        'Total pot 1000 | Rake 0',
        'Board [2c 7d 9h Ts 3s]',
        'Seat 1: You (button) (small blind) showed [Ah Kd] and lost with high card',
        'Seat 2: Mallorca (big blind) showed [Qs Qh] and won (1000) with a pair',
      ].join('\n'),
    );
  });

  it('returns an uncalled bet before the run-out and marks a short call all-in', () => {
    const record = recordOf([2000, 500], 'Qs Qh', 'Ah Kd', '2c 7d 9h Ts 3s', [raiseTo(1000), call]);
    const lines = toPokerStars([record]).split('\n');
    const uncalled = lines.indexOf('Uncalled bet (500) returned to You');
    expect(uncalled).toBeGreaterThan(-1);
    expect(uncalled).toBeLessThan(lines.indexOf('*** FLOP *** [2c 7d 9h]'));
    expect(lines).toContain('Mallorca: calls 480 and is all-in');
  });

  it('treats a null human seat as seat 0 for the hero', () => {
    const record = recordOf([2000, 2000], 'Qs Qh', 'Ah Kd', '2c 7d 9h Ts 3s', [fold], null);
    const lines = toPokerStars([record]).split('\n');
    expect(lines).toContain('Dealt to You [Ah Kd]');
    expect(lines).toContain('Seat 2: Mallorca (big blind) collected (20)');
  });

  it('joins records with two blank lines', () => {
    const record = recordOf([2000, 2000], 'Qs Qh', 'Ah Kd', '2c 7d 9h Ts 3s', [fold]);
    const text = toPokerStars([record, record]);
    const single = toPokerStars([record]);
    expect(text).toBe(`${single}\n\n\n${single}`);
  });
});
