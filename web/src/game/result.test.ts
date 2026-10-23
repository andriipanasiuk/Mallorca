import { describe, expect, it } from 'vitest';
import type { EngineEvent } from '../engine/index.ts';
import { resultOf } from './session.ts';

function showdown(ranks: [number, number]): EngineEvent {
  return {
    type: 'showdown',
    hands: [
      { cards: [0, 1], rank: ranks[0], category: 'pair' },
      { cards: [2, 3], rank: ranks[1], category: 'flush' },
    ],
  };
}

describe('resultOf', () => {
  it('names the short stack as the winner when it wins and the big stack gets a refund', () => {
    const events: EngineEvent[] = [
      showdown([100, 5000]),
      { type: 'potAwarded', seat: 0, amount: 3000 },
      { type: 'potAwarded', seat: 1, amount: 1000 },
    ];
    expect(resultOf(events)).toEqual({ winner: 1, handName: 'Flush', showdown: true });
  });

  it('names the big stack as the winner when the short stack loses', () => {
    const events: EngineEvent[] = [
      showdown([5000, 100]),
      { type: 'potAwarded', seat: 0, amount: 4000 },
    ];
    expect(resultOf(events)).toEqual({ winner: 0, handName: 'Pair', showdown: true });
  });

  it('reports a split when the ranks are equal even if a refund makes the awards unequal', () => {
    const events: EngineEvent[] = [
      showdown([300, 300]),
      { type: 'potAwarded', seat: 0, amount: 3500 },
      { type: 'potAwarded', seat: 1, amount: 500 },
    ];
    expect(resultOf(events).winner).toBeNull();
  });

  it('names the player who got the pot when there is no showdown', () => {
    const events: EngineEvent[] = [{ type: 'potAwarded', seat: 1, amount: 60 }];
    expect(resultOf(events)).toEqual({ winner: 1, handName: null, showdown: false });
  });
});
