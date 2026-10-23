import { describe, expect, it } from 'vitest';
import { fullDeck } from './card.ts';
import { mulberry32, shuffle } from './rng.ts';

describe('mulberry32', () => {
  it('produces a stable sequence', () => {
    const rng = mulberry32(1);
    expect([rng.next(), rng.next(), rng.next()]).toEqual([
      0.6270739405881613, 0.002735721180215478, 0.5274470399599522,
    ]);
  });

  it('stays in the unit interval', () => {
    const rng = mulberry32(12345);
    for (let i = 0; i < 10000; i++) {
      const v = rng.next();
      expect(v).toBeGreaterThanOrEqual(0);
      expect(v).toBeLessThan(1);
    }
  });

  it('repeats for the same seed', () => {
    const a = mulberry32(99);
    const b = mulberry32(99);
    expect([a.next(), a.next()]).toEqual([b.next(), b.next()]);
  });
});

describe('shuffle', () => {
  it('returns a permutation and leaves the input alone', () => {
    const deck = fullDeck();
    const shuffled = shuffle(deck, mulberry32(7));
    expect(deck).toEqual(fullDeck());
    expect(shuffled).not.toBe(deck);
    expect([...shuffled].sort((x, y) => x - y)).toEqual(fullDeck());
  });

  it('is deterministic per seed', () => {
    expect(shuffle(fullDeck(), mulberry32(7))).toEqual(shuffle(fullDeck(), mulberry32(7)));
  });

  it('differs between seeds', () => {
    expect(shuffle(fullDeck(), mulberry32(7))).not.toEqual(shuffle(fullDeck(), mulberry32(8)));
  });

  it('actually reorders the deck', () => {
    expect(shuffle(fullDeck(), mulberry32(7))).not.toEqual(fullDeck());
  });
});
