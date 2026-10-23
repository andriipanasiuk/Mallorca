import { fullDeck, parseCards } from '../cards/index.ts';
import type { Card } from '../cards/index.ts';
import { applyAction, startHandWith } from './hand.ts';
import type { Action, Seat, SeatIndex, Step } from './types.ts';

export function deckOf(bigBlindHole: string, buttonHole: string, board: string): Card[] {
  const [b1, b2] = parseCards(bigBlindHole);
  const [d1, d2] = parseCards(buttonHole);
  const head = [b1, d1, b2, d2, ...parseCards(board)] as Card[];
  const used = new Set(head);
  return [...head, ...fullDeck().filter((card) => !used.has(card))];
}

export function seatsOf(a: number, b: number): [Seat, Seat] {
  return [
    { name: 'A', stack: a },
    { name: 'B', stack: b },
  ];
}

export function startHand(
  stacks: [number, number],
  deck: Card[],
  button: SeatIndex = 0,
  blinds = { small: 10, big: 20 },
): Step {
  return startHandWith(seatsOf(stacks[0], stacks[1]), button, blinds, deck, 1);
}

export function play(step: Step, ...actions: Action[]): Step {
  return actions.reduce(
    (current, action) => applyAction(current.hand, current.seats, action),
    step,
  );
}

export function playEvents(step: Step, ...actions: Action[]): Step {
  let events = [...step.events];
  let current = step;
  for (const action of actions) {
    current = applyAction(current.hand, current.seats, action);
    events = [...events, ...current.events];
  }
  return { ...current, events };
}

export const fold: Action = { kind: 'fold' };
export const check: Action = { kind: 'check' };
export const call: Action = { kind: 'call' };
export const raiseTo = (to: number): Action => ({ kind: 'raise', to });
