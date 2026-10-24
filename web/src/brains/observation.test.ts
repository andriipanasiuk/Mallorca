import { describe, expect, it } from 'vitest';
import { parseCards } from '../cards/index.ts';
import { startHandWith } from '../engine/hand.ts';
import { call, check, deckOf, playEvents, raiseTo } from '../engine/testDeck.ts';
import type { Seat } from '../engine/types.ts';
import { parseObservation, toObservations } from './observation.ts';

const seats: [Seat, Seat] = [
  { name: 'Mallorca', stack: 1500 },
  { name: 'You', stack: 2500 },
];

describe('toObservations', () => {
  it('keeps what the hero sees and drops villain hole cards and the showdown', () => {
    const deck = deckOf('Ah Ad', '7c 2d', 'Qs Jh 7d 4c 9h');
    const start = startHandWith(
      seats,
      { button: 0, blinds: { small: 10, big: 20 }, handNumber: 3 },
      deck,
    );
    const end = playEvents(start, raiseTo(60), call, check, check, check, check, check, check);
    expect(toObservations(end.events, 0)).toEqual([
      {
        type: 'handStarted',
        handNumber: 3,
        heroOnButton: true,
        heroStack: 1500,
        villainStack: 2500,
        smallBlind: 10,
        bigBlind: 20,
      },
      { type: 'holeCards', cards: parseCards('7c 2d') },
      { type: 'acted', who: 'hero', action: { kind: 'raise', to: 60 }, amount: 50 },
      { type: 'acted', who: 'villain', action: { kind: 'call' }, amount: 40 },
      { type: 'street', street: 'flop', board: parseCards('Qs Jh 7d') },
      { type: 'acted', who: 'villain', action: { kind: 'check' }, amount: 0 },
      { type: 'acted', who: 'hero', action: { kind: 'check' }, amount: 0 },
      { type: 'street', street: 'turn', board: parseCards('Qs Jh 7d 4c') },
      { type: 'acted', who: 'villain', action: { kind: 'check' }, amount: 0 },
      { type: 'acted', who: 'hero', action: { kind: 'check' }, amount: 0 },
      { type: 'street', street: 'river', board: parseCards('Qs Jh 7d 4c 9h') },
      { type: 'acted', who: 'villain', action: { kind: 'check' }, amount: 0 },
      { type: 'acted', who: 'hero', action: { kind: 'check' }, amount: 0 },
    ]);
  });

  it('sees the hand from the other seat', () => {
    const deck = deckOf('Ah Ad', '7c 2d', 'Qs Jh 7d 4c 9h');
    const start = startHandWith(
      seats,
      { button: 0, blinds: { small: 10, big: 20 }, handNumber: 3 },
      deck,
    );
    const observations = toObservations(start.events, 1);
    expect(observations[0]).toMatchObject({ heroOnButton: false, heroStack: 2500 });
    expect(observations[1]).toEqual({ type: 'holeCards', cards: parseCards('Ah Ad') });
  });
});

describe('parseObservation', () => {
  it('reads the fixture form with two-character cards', () => {
    expect(parseObservation({ type: 'holeCards', cards: ['As', '8d'] })).toEqual({
      type: 'holeCards',
      cards: parseCards('As 8d'),
    });
    expect(parseObservation({ type: 'street', street: 'flop', board: ['9s', '3c', 'Qh'] })).toEqual(
      { type: 'street', street: 'flop', board: parseCards('9s 3c Qh') },
    );
    expect(
      parseObservation({
        type: 'acted',
        who: 'hero',
        action: { kind: 'raise', to: 90 },
        amount: 75,
      }),
    ).toEqual({ type: 'acted', who: 'hero', action: { kind: 'raise', to: 90 }, amount: 75 });
  });
});
