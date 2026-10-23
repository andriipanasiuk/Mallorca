import { describe, expect, it } from 'vitest';
import { startHandWith } from './hand.ts';
import { handHistory } from './history.ts';
import { call, check, deckOf, fold, playEvents, raiseTo } from './testDeck.ts';
import type { Seat } from './types.ts';

const seats: [Seat, Seat] = [
  { name: 'Mallorca', stack: 2000 },
  { name: 'You', stack: 2000 },
];
const names: [string, string] = ['Mallorca', 'You'];

describe('handHistory', () => {
  it('describes a hand that goes to showdown', () => {
    const deck = deckOf('Ah Ad', '7c 2d', 'Qs Jh 7d 4c 9h');
    const start = startHandWith(seats, 0, { small: 10, big: 20 }, deck, 1);
    const end = playEvents(start, raiseTo(60), call, check, check, check, check, check, check);
    expect(handHistory(end.events, names)).toEqual([
      'Hand 1, blinds 10/20',
      'Mallorca posts 10',
      'You post 20',
      'Mallorca raises to 60',
      'You call 40',
      'Flop Qs Jh 7d',
      'You check',
      'Mallorca checks',
      'Turn 4c',
      'You check',
      'Mallorca checks',
      'River 9h',
      'You check',
      'Mallorca checks',
      'Showdown: Mallorca shows 7c 2d, pair',
      'Showdown: You show Ah Ad, pair',
      'You win 120',
    ]);
  });

  it('describes folds, all-ins and the end of the tournament', () => {
    const deck = deckOf('Ah Ad', '7c 2d', 'Qs Jh 7d 4c 9h');
    const start = startHandWith(seats, 1, { small: 10, big: 20 }, deck, 4);
    const folded = playEvents(start, fold);
    expect(handHistory(folded.events, names)).toEqual([
      'Hand 4, blinds 10/20',
      'You post 10',
      'Mallorca posts 20',
      'You fold',
      'Uncalled bet of 10 returned to Mallorca',
      'Mallorca wins 20',
    ]);
    const shove = playEvents(
      startHandWith(seats, 0, { small: 10, big: 20 }, deck, 1),
      raiseTo(2000),
      call,
    );
    const lines = handHistory([...shove.events, { type: 'tournamentEnded', winner: 1 }], names);
    expect(lines).toContain('Mallorca is all-in');
    expect(lines).toContain('You are all-in');
    expect(lines.at(-1)).toBe('You win the tournament');
  });
});
