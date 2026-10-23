import { describe, expect, it } from 'vitest';
import { rulesOf } from './rules.ts';
import { blindsFor, CASH, TOURNAMENT } from './table.ts';
import { seatsOf } from './testDeck.ts';

describe('rulesOf', () => {
  it('cash blinds are half and full big blind for every hand', () => {
    expect(rulesOf(CASH).blinds(1)).toEqual({ small: 10, big: 20 });
    expect(rulesOf(CASH).blinds(500)).toEqual({ small: 10, big: 20 });
  });

  it('tops up only the seat below the buy-in', () => {
    const r = rulesOf(CASH).beforeHand(seatsOf(3400, 600));
    expect(r.seats.map((s) => s.stack)).toEqual([3400, 2000]);
    expect(r.events).toEqual([{ type: 'toppedUp', seat: 1, amount: 1400 }]);
  });

  it('tops up a busted seat from zero', () => {
    expect(rulesOf(CASH).beforeHand(seatsOf(0, 4000)).events).toEqual([
      { type: 'toppedUp', seat: 0, amount: 2000 },
    ]);
  });

  it('a cash table is never over', () => {
    expect(rulesOf(CASH).isOver(seatsOf(0, 4000))).toBeNull();
  });

  it('tournament rules keep blindsFor, no top-up, over at zero', () => {
    expect(rulesOf(TOURNAMENT).blinds(11)).toEqual(blindsFor(11));
    expect(rulesOf(TOURNAMENT).beforeHand(seatsOf(0, 4000)).events).toEqual([]);
    expect(rulesOf(TOURNAMENT).isOver(seatsOf(0, 4000))).toBe(1);
  });
});
