import { describe, expect, it } from 'vitest';
import { mulberry32, parseCards } from '../cards/index.ts';
import type { WeightedHands } from './equity.ts';
import { realEquity, streetEquity } from './streetEquity.ts';

const hand = (text: string) => parseCards(text) as [number, number];
const opts = { rng: mulberry32(1), samples: 100 };

describe('streetEquity', () => {
  it('measures strength as the made hand against the range and potential one card ahead', () => {
    const range: WeightedHands = [{ cards: hand('Kd Qd'), weight: 1 }];
    const result = streetEquity(hand('Ah 2c'), range, parseCards('As 7h 9c 3d'), opts);
    expect(result.strength).toBe(1);
    expect(result.positivePotential).toBe(1);
    expect(result.negativePotential).toBeCloseTo(0, 12);
  });

  it('counts a tie as half of strength and keeps it out of the potentials', () => {
    const range: WeightedHands = [
      { cards: hand('Ad Kc'), weight: 1 },
      { cards: hand('Ks Qs'), weight: 1 },
    ];
    const result = streetEquity(hand('Ah Kd'), range, parseCards('As Kh 7c 2d'), opts);
    expect(result.strength).toBe(0.75);
  });

  it('computes the positive potential of a flush draw behind a pair on the turn', () => {
    const range: WeightedHands = [{ cards: hand('Kd Ks'), weight: 1 }];
    const result = streetEquity(hand('Ah 2h'), range, parseCards('Kh 7h 9c 3d'), opts);
    expect(result.strength).toBe(0);
    expect(result.positivePotential).toBeCloseTo(7 / 44, 12);
    expect(result.negativePotential).toBe(1);
  });

  it('computes the negative potential of a set against a flush draw on the turn', () => {
    const range: WeightedHands = [{ cards: hand('Ah 5h'), weight: 1 }];
    const result = streetEquity(hand('9d 9h'), range, parseCards('9c Kh 7h 2s'), opts);
    expect(result.strength).toBe(1);
    expect(result.negativePotential).toBeCloseTo(7 / 44, 12);
  });

  it('looks only one card ahead on the flop', () => {
    const range: WeightedHands = [{ cards: hand('Kd Ks'), weight: 1 }];
    const result = streetEquity(hand('Ah 2h'), range, parseCards('Kh 7h 9c'), opts);
    expect(result.positivePotential).toBeCloseTo(8 / 45, 12);
  });

  it('has no potential on the river', () => {
    const range: WeightedHands = [{ cards: hand('Kd Ks'), weight: 1 }];
    const result = streetEquity(hand('Ah 2h'), range, parseCards('Kh 7h 9c 3d 4s'), opts);
    expect(result).toEqual({ strength: 0, positivePotential: 0, negativePotential: 0 });
  });

  it('skips villain hands blocked by hero or board', () => {
    const range: WeightedHands = [
      { cards: hand('Ah Kd'), weight: 4 },
      { cards: hand('Qd Qs'), weight: 1 },
    ];
    const result = streetEquity(hand('Ah 2c'), range, parseCards('As 7h 9c 3d'), opts);
    expect(result.strength).toBe(1);
  });

  it('rejects a board shorter than the flop', () => {
    expect(() => streetEquity(hand('Ah 2c'), [], parseCards('As 7h'), opts)).toThrow();
  });
});

describe('realEquity', () => {
  it('combines strength with both potentials', () => {
    expect(
      realEquity({ strength: 0.6, positivePotential: 0.25, negativePotential: 0.1 }),
    ).toBeCloseTo(0.6 * 0.9 + 0.4 * 0.25, 12);
  });
});
