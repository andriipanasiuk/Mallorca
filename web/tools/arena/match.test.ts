import { describe, expect, it } from 'vitest';
import type { EngineEvent, SeatIndex } from '../../src/engine/index.ts';
import { BOTS } from './bots.ts';
import { playPair } from './match.ts';

const hole = (hand: EngineEvent[], seat: SeatIndex) =>
  hand.find((e) => e.type === 'holeCards' && e.seat === seat);

describe('playPair', () => {
  it('the math bot against itself nets exactly zero per pair', () => {
    for (let i = 0; i < 10; i++) {
      expect(playPair(BOTS.math, BOTS.math, 1, i).net).toBe(0);
    }
  });

  it('a pair is the same deck with the seats swapped', () => {
    const { hands } = playPair(BOTS['always-call'], BOTS['always-raise'], 5, 3);
    const first = hole(hands[0]!, 0);
    expect(first).toBeDefined();
    expect(first).toEqual(hole(hands[1]!, 0));
  });

  it('is deterministic for a seed and index', () => {
    expect(playPair(BOTS.random, BOTS.math, 9, 2)).toEqual(playPair(BOTS.random, BOTS.math, 9, 2));
  });

  it('chips are conserved in every hand', () => {
    const { hands } = playPair(BOTS['always-raise'], BOTS.random, 4, 1);
    expect(hands).toHaveLength(2);
    for (const hand of hands) {
      const ended = hand.find((e) => e.type === 'handEnded');
      expect(ended?.type === 'handEnded' ? ended.stacks[0] + ended.stacks[1] : null).toBe(4000);
    }
  });
});
