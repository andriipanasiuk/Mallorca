import { afterEach, describe, expect, it, vi } from 'vitest';
import type { Decision, Observation } from '../brains/index.ts';
import type { BotClient } from './botClient.ts';
import { inProcessBot, withFallback } from './botClient.ts';

const legal = { fold: true, check: false, call: 10, raise: { min: 40, max: 2000 } };

const hand: Observation[] = [
  {
    type: 'handStarted',
    handNumber: 1,
    heroOnButton: true,
    heroStack: 2000,
    villainStack: 2000,
    smallBlind: 10,
    bigBlind: 20,
  },
  { type: 'holeCards', cards: [48, 44] },
];

const decision: Decision = {
  action: { kind: 'call' },
  solverChoice: 'passive',
  evByAction: { fold: 0, passive: 0, aggressive: 0 },
};

afterEach(() => {
  vi.useRealTimers();
});

describe('inProcessBot', () => {
  it('decides with the brain that was current when decide was called', async () => {
    const bot = inProcessBot();
    bot.observe(hand);
    const pending = bot.decide(legal);
    bot.reset();
    await expect(pending).resolves.toMatchObject({ action: expect.any(Object) });
    await expect(bot.decide(legal)).rejects.toThrow();
  });
});

describe('withFallback', () => {
  it('does not spend the fallback on a request superseded by reset', async () => {
    vi.useFakeTimers();
    let calls = 0;
    const primary: BotClient = {
      observe: () => undefined,
      reset: () => undefined,
      decide: () => {
        calls += 1;
        return calls === 1 ? new Promise<Decision>(() => undefined) : Promise.resolve(decision);
      },
    };
    const bot = withFallback(primary, 100);
    const stale = bot.decide(legal);
    const staleOutcome = stale.then(
      () => 'resolved',
      () => 'rejected',
    );
    bot.reset();
    await expect(bot.decide(legal)).resolves.toBe(decision);
    await vi.advanceTimersByTimeAsync(100);
    await expect(staleOutcome).resolves.toBe('rejected');
    await expect(bot.decide(legal)).resolves.toBe(decision);
    expect(calls).toBe(3);
  });
});
