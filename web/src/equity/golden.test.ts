import { describe, expect, it } from 'vitest';
import type { Card } from '../cards/index.ts';
import { mulberry32, parseCard, parseCards } from '../cards/index.ts';
import type { WeightedHands } from './equity.ts';
import fixtures from './fixtures/equity.json' with { type: 'json' };
import { streetEquity } from './streetEquity.ts';

interface EquityFixture {
  hero: string;
  board: string;
  range: string;
  strength: number;
  positivePotential: number;
  negativePotential: number;
}

const FLOP_SIZE = 3;

function parseRange(text: string): WeightedHands {
  return text.split(' ').map((item) => {
    const [cards = '', weight = ''] = item.split(':');
    const hand: [Card, Card] = [parseCard(cards.slice(0, 2)), parseCard(cards.slice(2, 4))];
    return { cards: hand, weight: Number(weight) };
  });
}

describe('streetEquity against the Java equilator', () => {
  it.each((fixtures as EquityFixture[]).map((fixture, index) => [index, fixture] as const))(
    'matches entry %i',
    (_index, fixture) => {
      const board = parseCards(fixture.board);
      const hero = parseCards(fixture.hero) as [Card, Card];
      const tolerance = board.length === FLOP_SIZE ? 0.02 : 0.01;
      const result = streetEquity(hero, parseRange(fixture.range), board, {
        rng: mulberry32(1),
        samples: 20000,
      });
      expect(Math.abs(result.strength - fixture.strength)).toBeLessThanOrEqual(tolerance);
      expect(Math.abs(result.positivePotential - fixture.positivePotential)).toBeLessThanOrEqual(
        tolerance,
      );
      expect(Math.abs(result.negativePotential - fixture.negativePotential)).toBeLessThanOrEqual(
        tolerance,
      );
    },
  );
});
