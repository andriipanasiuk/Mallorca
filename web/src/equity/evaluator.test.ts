import { describe, expect, it } from 'vitest';
import { parseCards } from '../cards/index.ts';
import type { HandCategory } from './evaluator.ts';
import { pheEvaluator } from './evaluator.ts';

const rank = (text: string) => pheEvaluator.rank(parseCards(text));

describe('pheEvaluator', () => {
  it('ranks a royal flush above quads', () => {
    expect(rank('As Ks Qs Js Ts 2c 3d')).toBeGreaterThan(rank('9s 9h 9d 9c Ad Kc 2h'));
  });

  it.each<[string, HandCategory]>([
    ['As Kd 9h 7c 4d 3s 2h', 'high-card'],
    ['As Ad 9h 7c 4d 3s 2h', 'pair'],
    ['As Ad 9h 9c 4d 3s 2h', 'two-pair'],
    ['As Ad Ah 7c 4d 3s 2h', 'trips'],
    ['9s 8d 7h 6c 5d Ks 2h', 'straight'],
    ['As Ks 9s 7s 4s 3d 2h', 'flush'],
    ['As Ad Ah 7c 7d 3s 2h', 'full-house'],
    ['As Ad Ah Ac 4d 3s 2h', 'quads'],
    ['9s 8s 7s 6s 5s Kd 2h', 'straight-flush'],
  ])('categorises %s as %s', (text, expected) => {
    expect(pheEvaluator.category(rank(text))).toBe(expected);
  });

  it('puts the wheel above high card and below the six-high straight', () => {
    const wheel = rank('As 2d 3h 4c 5d');
    expect(wheel).toBeGreaterThan(rank('As Kd Qh Jc 9d'));
    expect(wheel).toBeLessThan(rank('2s 3d 4h 5c 6d'));
  });

  it('ranks seven cards as the best five of them', () => {
    const cards = parseCards('As Kd 9h 7c 4d 9s Ah');
    let best = -1;
    for (let skipA = 0; skipA < 7; skipA++) {
      for (let skipB = skipA + 1; skipB < 7; skipB++) {
        const five = cards.filter((_, i) => i !== skipA && i !== skipB);
        best = Math.max(best, pheEvaluator.rank(five));
      }
    }
    expect(pheEvaluator.rank(cards)).toBe(best);
  });

  it('ties identical hands in different suits', () => {
    expect(rank('As Kd 9h 7c 4d')).toBe(rank('Ah Ks 9d 7s 4c'));
  });
});
