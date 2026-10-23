import { fullDeck, shuffle } from '../cards/index.ts';
import type { Rng } from '../cards/index.ts';
import { applyAction, startHandWith } from './hand.ts';
import { legalActions, otherSeat } from './legal.ts';
import type { Action, EngineEvent, Hand, LegalActions, Seat, SeatIndex, Step } from './types.ts';

export type TableMode = { kind: 'tournament' } | { kind: 'cash'; bigBlind: number; buyIn: number };

export const TOURNAMENT: TableMode = { kind: 'tournament' };

export const CASH: TableMode = { kind: 'cash', bigBlind: 20, buyIn: 2000 };

const TOURNAMENT_STACK = 2000;

export interface Table {
  mode: TableMode;
  seats: [Seat, Seat];
  button: SeatIndex;
  handNumber: number;
  hand: Hand | null;
  winner: SeatIndex | null;
}

export const BIG_BLINDS = [
  20, 30, 40, 50, 60, 80, 100, 120, 160, 200, 240, 300, 400, 500, 600, 800, 1000, 1200, 1600, 2000,
  2400,
];

const HANDS_PER_LEVEL = 10;

export function blindsFor(handNumber: number): { small: number; big: number } {
  const level = Math.min(
    Math.max(Math.floor((handNumber - 1) / HANDS_PER_LEVEL), 0),
    BIG_BLINDS.length - 1,
  );
  const big = BIG_BLINDS[level] as number;
  return { small: Math.floor(big / 2), big };
}

export function createTable(
  names: [string, string],
  mode: TableMode,
  firstButton: SeatIndex = 0,
): Table {
  const startingStack = mode.kind === 'cash' ? mode.buyIn : TOURNAMENT_STACK;
  return {
    mode,
    seats: [
      { name: names[0], stack: startingStack },
      { name: names[1], stack: startingStack },
    ],
    button: firstButton,
    handNumber: 0,
    hand: null,
    winner: null,
  };
}

export function startHand(t: Table, rng: Rng): { state: Table; events: EngineEvent[] } {
  if (t.winner !== null) {
    throw new Error('The tournament is over');
  }
  if (t.hand !== null && t.hand.street !== 'complete') {
    throw new Error('A hand is still in progress');
  }
  const handNumber = t.handNumber + 1;
  const button = t.handNumber === 0 ? t.button : otherSeat(t.button);
  const step = startHandWith(
    t.seats,
    button,
    blindsFor(handNumber),
    shuffle(fullDeck(), rng),
    handNumber,
  );
  return conclude({ ...t, button, handNumber }, step);
}

function conclude(t: Table, step: Step): { state: Table; events: EngineEvent[] } {
  const events = [...step.events];
  let winner: SeatIndex | null = null;
  if (step.hand.street === 'complete') {
    const broke = step.seats[0].stack === 0 ? 0 : step.seats[1].stack === 0 ? 1 : null;
    if (broke !== null) {
      winner = otherSeat(broke);
      events.push({ type: 'tournamentEnded', winner });
    }
  }
  return { state: { ...t, seats: step.seats, hand: step.hand, winner }, events };
}

export function legalActionsOf(t: Table): LegalActions {
  if (t.hand === null) {
    return { fold: false, check: false, call: null, raise: null };
  }
  return legalActions(t.hand, t.seats);
}

export function toAct(t: Table): SeatIndex | null {
  return t.hand === null ? null : t.hand.toAct;
}

export function apply(t: Table, action: Action): { state: Table; events: EngineEvent[] } {
  if (t.hand === null) {
    throw new Error('No hand in progress');
  }
  return conclude(t, applyAction(t.hand, t.seats, action));
}
