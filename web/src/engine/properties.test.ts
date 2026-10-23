import fc from 'fast-check';
import { describe, expect, it } from 'vitest';
import { fullDeck, mulberry32, shuffle } from '../cards/index.ts';
import { applyAction, startHandWith } from './hand.ts';
import { legalActions } from './legal.ts';
import { apply, CASH, createTable, legalActionsOf, startHand, toAct } from './table.ts';
import type { Table } from './table.ts';
import type { Action, EngineEvent, LegalActions, SeatIndex, Step } from './types.ts';

const MAX_ACTIONS = 50;
const MAX_ACTIONS_WITH_MIN_RAISES = 800;

function options(legal: LegalActions, withMinRaise = false): Action[] {
  const result: Action[] = [];
  if (legal.fold) result.push({ kind: 'fold' });
  if (legal.check) result.push({ kind: 'check' });
  if (legal.call !== null) result.push({ kind: 'call' });
  if (legal.raise !== null) {
    const { min, max } = legal.raise;
    if (withMinRaise) result.push({ kind: 'raise', to: min });
    result.push({ kind: 'raise', to: max });
    result.push({ kind: 'raise', to: Math.floor((min + max) / 2) });
  }
  return result;
}

function total(step: Step): number {
  return step.seats[0].stack + step.seats[1].stack + step.hand.pot;
}

const handArbitrary = fc.record({
  seed: fc.integer(),
  stackA: fc.integer({ min: 1, max: 4000 }),
  stackB: fc.integer({ min: 1, max: 4000 }),
  button: fc.constantFrom<SeatIndex>(0, 1),
  bigBlind: fc.constantFrom(20, 30, 50, 100, 2400),
  choices: fc.array(fc.nat(), { minLength: 1, maxLength: 60 }),
});

describe('hand properties', () => {
  it('conserves chips, accepts every legal action and ends within 50 actions', () => {
    fc.assert(
      fc.property(handArbitrary, ({ seed, stackA, stackB, button, bigBlind, choices }) => {
        const deck = shuffle(fullDeck(), mulberry32(seed));
        let step = startHandWith(
          [
            { name: 'A', stack: stackA },
            { name: 'B', stack: stackB },
          ],
          button,
          { small: Math.floor(bigBlind / 2), big: bigBlind },
          deck,
          1,
        );
        const expectedTotal = stackA + stackB;
        expect(total(step)).toBe(expectedTotal);
        let actions = 0;
        while (step.hand.toAct !== null) {
          const legal = legalActions(step.hand, step.seats);
          const available = options(legal);
          expect(available.length).toBeGreaterThan(0);
          const pick = available[
            (choices[actions % choices.length] as number) % available.length
          ] as Action;
          step = applyAction(step.hand, step.seats, pick);
          actions += 1;
          expect(total(step)).toBe(expectedTotal);
          expect(step.seats[0].stack).toBeGreaterThanOrEqual(0);
          expect(step.seats[1].stack).toBeGreaterThanOrEqual(0);
          expect(actions).toBeLessThanOrEqual(MAX_ACTIONS);
        }
        expect(step.hand.street).toBe('complete');
        expect(step.hand.pot).toBe(0);
        expect(step.seats[0].stack + step.seats[1].stack).toBe(expectedTotal);
        expect(step.events.at(-1)).toMatchObject({ type: 'handEnded' });
        expect(new Set(step.hand.board).size).toBe(step.hand.board.length);
      }),
      { numRuns: 500 },
    );
  });

  it('still ends when min raises are in play', () => {
    fc.assert(
      fc.property(handArbitrary, ({ seed, stackA, stackB, button, bigBlind, choices }) => {
        let step = startHandWith(
          [
            { name: 'A', stack: stackA },
            { name: 'B', stack: stackB },
          ],
          button,
          { small: Math.floor(bigBlind / 2), big: bigBlind },
          shuffle(fullDeck(), mulberry32(seed)),
          1,
        );
        let actions = 0;
        while (step.hand.toAct !== null) {
          const available = options(legalActions(step.hand, step.seats), true);
          const pick = available[
            (choices[actions % choices.length] as number) % available.length
          ] as Action;
          step = applyAction(step.hand, step.seats, pick);
          actions += 1;
          expect(actions).toBeLessThanOrEqual(MAX_ACTIONS_WITH_MIN_RAISES);
        }
        expect(step.hand.street).toBe('complete');
        expect(step.seats[0].stack + step.seats[1].stack).toBe(stackA + stackB);
      }),
      { numRuns: 300 },
    );
  });

  it('only awards pots that match what was committed', () => {
    fc.assert(
      fc.property(handArbitrary, ({ seed, stackA, stackB, button, bigBlind, choices }) => {
        const deck = shuffle(fullDeck(), mulberry32(seed));
        let step = startHandWith(
          [
            { name: 'A', stack: stackA },
            { name: 'B', stack: stackB },
          ],
          button,
          { small: Math.floor(bigBlind / 2), big: bigBlind },
          deck,
          1,
        );
        const events = [...step.events];
        let i = 0;
        while (step.hand.toAct !== null) {
          const available = options(legalActions(step.hand, step.seats));
          step = applyAction(
            step.hand,
            step.seats,
            available[(choices[i++ % choices.length] as number) % available.length] as Action,
          );
          events.push(...step.events);
        }
        const awarded = events
          .filter((e) => e.type === 'potAwarded' || e.type === 'betReturned')
          .reduce((sum, e) => sum + (e as { amount: number }).amount, 0);
        const posted = events
          .filter((e) => e.type === 'blindPosted' || e.type === 'acted')
          .reduce((sum, e) => sum + (e as { amount: number }).amount, 0);
        expect(awarded).toBe(posted);
        const paid: [number, number] = [0, 0];
        for (const e of events) {
          if (e.type === 'blindPosted' || e.type === 'acted') {
            paid[e.seat] += e.amount;
          }
        }
        const pot = events
          .filter((e) => e.type === 'potAwarded')
          .reduce((sum, e) => sum + (e as { amount: number }).amount, 0);
        expect(pot).toBe(2 * Math.min(paid[0], paid[1]));
      }),
      { numRuns: 300 },
    );
  });
});

describe('cash table properties', () => {
  it('starts every hand with at least the buy-in, topped-up seats exactly at it, and conserves chips', () => {
    fc.assert(
      fc.property(
        fc.integer(),
        fc.array(fc.nat(), { minLength: 1, maxLength: 60 }),
        (seed, choices) => {
          const rng = mulberry32(seed);
          let t: Table = createTable(['A', 'B'], CASH);
          let toppedUp = 0;
          let picks = 0;
          for (let hand = 0; hand < 30; hand++) {
            const started = startHand(t, rng);
            const events: EngineEvent[] = [...started.events];
            let state = started.state;
            const topped = started.events.filter((e) => e.type === 'toppedUp');
            for (const e of topped) toppedUp += e.amount;
            const handStarted = events.find((e) => e.type === 'handStarted') as Extract<
              EngineEvent,
              { type: 'handStarted' }
            >;
            expect(handStarted.stacks[0]).toBeGreaterThanOrEqual(2000);
            expect(handStarted.stacks[1]).toBeGreaterThanOrEqual(2000);
            for (const e of topped) expect(handStarted.stacks[e.seat]).toBe(2000);
            while (toAct(state) !== null) {
              const available = options(legalActionsOf(state));
              const pick = available[
                (choices[picks++ % choices.length] as number) % available.length
              ] as Action;
              state = apply(state, pick).state;
            }
            expect(state.winner).toBeNull();
            expect(state.seats[0].stack + state.seats[1].stack).toBe(4000 + toppedUp);
            t = state;
          }
        },
      ),
      { numRuns: 100 },
    );
  });
});
