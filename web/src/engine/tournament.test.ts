import { describe, expect, it } from 'vitest';
import { mulberry32 } from '../cards/index.ts';
import {
  apply,
  BIG_BLINDS,
  blindsFor,
  createTournament,
  legalActionsOf,
  startHand,
  toAct,
} from './tournament.ts';
import type { Tournament } from './tournament.ts';
import type { EngineEvent } from './types.ts';

function shoveHand(t: Tournament): { state: Tournament; events: EngineEvent[] } {
  let state = t;
  const events: EngineEvent[] = [];
  while (toAct(state) !== null) {
    const legal = legalActionsOf(state);
    const action = legal.raise
      ? ({ kind: 'raise', to: legal.raise.max } as const)
      : { kind: 'call' as const };
    const step = apply(state, action);
    state = step.state;
    events.push(...step.events);
  }
  return { state, events };
}

describe('blindsFor', () => {
  it('follows the schedule and holds the last level', () => {
    expect(blindsFor(1)).toEqual({ small: 10, big: 20 });
    expect(blindsFor(10)).toEqual({ small: 10, big: 20 });
    expect(blindsFor(11)).toEqual({ small: 15, big: 30 });
    expect(blindsFor(500)).toEqual({ small: 1200, big: 2400 });
    expect(BIG_BLINDS).toHaveLength(21);
  });
});

describe('tournament', () => {
  it('creates a tournament with equal stacks and no hand', () => {
    const t = createTournament(['Mallorca', 'You']);
    expect(t.seats).toEqual([
      { name: 'Mallorca', stack: 2000 },
      { name: 'You', stack: 2000 },
    ]);
    expect(t).toMatchObject({ button: 0, handNumber: 0, hand: null, winner: null });
    expect(toAct(t)).toBeNull();
    expect(legalActionsOf(t)).toEqual({ fold: false, check: false, call: null, raise: null });
  });

  it('uses the first button on hand one and alternates afterwards', () => {
    const rng = mulberry32(1);
    let t = createTournament(['Mallorca', 'You'], 2000, 1);
    const buttons: number[] = [];
    for (let i = 0; i < 3; i++) {
      const started = startHand(t, rng);
      buttons.push(started.state.button);
      expect(started.state.handNumber).toBe(i + 1);
      expect(started.events[0]).toMatchObject({ type: 'handStarted', handNumber: i + 1 });
      t = apply(started.state, { kind: 'fold' }).state;
    }
    expect(buttons).toEqual([1, 0, 1]);
  });

  it('plays all-in hands until one stack is empty and keeps the chip total', () => {
    const rng = mulberry32(3);
    let t = createTournament(['Mallorca', 'You']);
    let ended: EngineEvent[] = [];
    for (let i = 0; i < 500 && t.winner === null; i++) {
      const started = startHand(t, rng);
      const played = shoveHand(started.state);
      t = played.state;
      ended = played.events.filter((e) => e.type === 'tournamentEnded');
      expect(t.seats[0].stack + t.seats[1].stack).toBe(4000);
    }
    expect(t.winner).not.toBeNull();
    expect(ended).toEqual([{ type: 'tournamentEnded', winner: t.winner }]);
    expect(t.seats[t.winner as 0 | 1].stack).toBe(4000);
  });

  it('refuses to start a hand once the tournament is over', () => {
    const rng = mulberry32(3);
    let t = createTournament(['Mallorca', 'You']);
    while (t.winner === null) {
      t = shoveHand(startHand(t, rng).state).state;
    }
    expect(() => startHand(t, rng)).toThrow();
  });
});
