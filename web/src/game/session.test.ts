import { afterEach, describe, expect, it, vi } from 'vitest';
import type { Decision } from '../brains/index.ts';
import { legalActionsOf, toAct } from '../engine/index.ts';
import type { BotClient } from './botClient.ts';
import { inProcessBot } from './botClient.ts';
import { createSession } from './session.ts';
import type { Session } from './session.ts';
import { callingBot, scriptedBot, shovingBot } from './testBot.ts';

function open(bot: BotClient, overrides: { botDelayMs?: number; seed?: number } = {}): Session {
  return createSession({
    bot,
    seed: overrides.seed ?? 7,
    botDelayMs: overrides.botDelayMs ?? 0,
    humanSeat: 1,
    names: ['Mallorca', 'You'],
  });
}

afterEach(() => {
  vi.useRealTimers();
});

describe('session', () => {
  it('starts idle', () => {
    const session = open(inProcessBot());
    expect(session.getState().phase).toBe('idle');
  });

  it('deals hand 1 and waits for the human after the bot acts first', async () => {
    const bot = callingBot();
    const session = open(bot);
    await session.getState().start();
    const state = session.getState();
    expect(state.phase).toBe('humanToAct');
    expect(state.tournament.handNumber).toBe(1);
    expect(toAct(state.tournament)).toBe(1);
    expect(bot.decideCalls).toBe(1);
    expect(state.log.length).toBeGreaterThan(0);
    expect(state.lastActions[0]).toBe('Call 10');
  });

  it('deals the same cards for the same seed', async () => {
    const first = open(callingBot(), { seed: 11 });
    const second = open(callingBot(), { seed: 11 });
    const other = open(callingBot(), { seed: 12 });
    await Promise.all([
      first.getState().start(),
      second.getState().start(),
      other.getState().start(),
    ]);
    expect(first.getState().tournament.hand?.holeCards).toEqual(
      second.getState().tournament.hand?.holeCards,
    );
    expect(first.getState().tournament.hand?.holeCards).not.toEqual(
      other.getState().tournament.hand?.holeCards,
    );
  });

  it('clamps a raise below the minimum to the minimum', async () => {
    const session = open(callingBot());
    await session.getState().start();
    const legal = legalActionsOf(session.getState().tournament);
    await session.getState().act({ kind: 'raise', to: 1 });
    expect(session.getState().lastActions[1]).toBe(`Raise to ${legal.raise?.min}`);
  });

  it('rounds a fractional raise and clamps it to the maximum', async () => {
    const first = open(callingBot());
    await first.getState().start();
    const legal = legalActionsOf(first.getState().tournament);
    const target = (legal.raise?.min ?? 0) + 10.6;
    await first.getState().act({ kind: 'raise', to: target });
    expect(first.getState().lastActions[1]).toBe(`Raise to ${Math.round(target)}`);

    const second = open(callingBot());
    await second.getState().start();
    await second.getState().act({ kind: 'raise', to: 999999 });
    expect(second.getState().lastActions[1]).toBe('All-in');
  });

  it('turns a fold into a check when checking is free', async () => {
    const session = open(callingBot());
    await session.getState().start();
    await session.getState().act({ kind: 'fold' });
    expect(session.getState().lastActions[1]).toBe('Check');
    expect(session.getState().tournament.hand?.street).toBe('flop');
  });

  it('ignores act while the bot is thinking', async () => {
    let release: (d: Decision) => void = () => undefined;
    let calls = 0;
    const pending: BotClient = {
      observe: () => undefined,
      reset: () => undefined,
      decide: () => {
        calls += 1;
        return new Promise<Decision>((resolve) => {
          release = resolve;
        });
      },
    };
    const session = open(pending);
    const started = session.getState().start();
    expect(session.getState().phase).toBe('botThinking');
    const before = session.getState().log.length;
    await session.getState().act({ kind: 'passive' });
    expect(session.getState().log.length).toBe(before);
    expect(session.getState().phase).toBe('botThinking');
    release({
      action: { kind: 'call' },
      solverChoice: 'passive',
      evByAction: { fold: 0, passive: 0, aggressive: 0 },
    });
    await started;
    expect(session.getState().phase).toBe('humanToAct');
    expect(calls).toBe(1);
  });

  it('falls back to an in-process bot once when decide rejects', async () => {
    let calls = 0;
    const broken: BotClient = {
      observe: () => undefined,
      reset: () => undefined,
      decide: () => {
        calls += 1;
        return Promise.reject(new Error('worker crashed'));
      },
    };
    const session = open(broken);
    await session.getState().start();
    expect(['humanToAct', 'handOver']).toContain(session.getState().phase);
    expect(session.getState().lastActions[0]).not.toBeNull();
    expect(session.getState().lastDecision).not.toBeNull();
    while (session.getState().phase === 'humanToAct') {
      await session.getState().act({ kind: 'passive' });
    }
    expect(calls).toBe(1);
    expect(['handOver', 'tournamentOver']).toContain(session.getState().phase);
  });

  it('falls back after a timeout when decide never resolves', async () => {
    vi.useFakeTimers();
    const silent: BotClient = {
      observe: () => undefined,
      reset: () => undefined,
      decide: () => new Promise<Decision>(() => undefined),
    };
    const session = open(silent);
    const started = session.getState().start();
    await vi.advanceTimersByTimeAsync(4999);
    expect(session.getState().phase).toBe('botThinking');
    await vi.advanceTimersByTimeAsync(1);
    await started;
    expect(['humanToAct', 'handOver']).toContain(session.getState().phase);
    expect(session.getState().lastDecision).not.toBeNull();
  });

  it('waits botDelayMs before the bot acts', async () => {
    vi.useFakeTimers();
    const session = open(callingBot(), { botDelayMs: 700 });
    const started = session.getState().start();
    await vi.advanceTimersByTimeAsync(699);
    expect(session.getState().phase).toBe('animating');
    expect(session.getState().lastActions[0]).toBeNull();
    await vi.advanceTimersByTimeAsync(1);
    await started;
    expect(session.getState().phase).toBe('humanToAct');
    expect(session.getState().lastActions[0]).toBe('Call 10');
  });

  it('keeps the last bot decision', async () => {
    const session = open(callingBot());
    expect(session.getState().lastDecision).toBeNull();
    await session.getState().start();
    expect(session.getState().lastDecision?.action).toEqual({ kind: 'call' });
  });

  it('ends the hand when a player folds and starts the next on request', async () => {
    const session = open(shovingBot());
    await session.getState().start();
    await session.getState().act({ kind: 'fold' });
    const state = session.getState();
    expect(['handOver', 'tournamentOver']).toContain(state.phase);
    expect(state.result).not.toBeNull();
    if (state.phase === 'handOver') {
      await state.nextHand();
      expect(session.getState().tournament.handNumber).toBe(2);
    }
  });

  it('plays the tournament to the end when the human always folds', async () => {
    const session = open(shovingBot());
    await session.getState().start();
    for (let i = 0; i < 4000 && session.getState().phase !== 'tournamentOver'; i += 1) {
      const { phase } = session.getState();
      if (phase === 'humanToAct') {
        await session.getState().act({ kind: 'fold' });
      } else if (phase === 'handOver') {
        await session.getState().nextHand();
      }
    }
    const state = session.getState();
    expect(state.phase).toBe('tournamentOver');
    expect(state.tournament.winner).toBe(0);
  });

  it('plays a hand against the real brain', async () => {
    const session = open(inProcessBot());
    await session.getState().start();
    for (let i = 0; i < 40 && session.getState().phase === 'humanToAct'; i += 1) {
      await session.getState().act({ kind: 'passive' });
    }
    expect(['handOver', 'tournamentOver']).toContain(session.getState().phase);
  });

  it('restarts cleanly', async () => {
    const session = open(
      scriptedBot((legal) => (legal.check ? { kind: 'check' } : { kind: 'call' })),
    );
    await session.getState().start();
    await session.getState().act({ kind: 'passive' });
    await session.getState().start();
    expect(session.getState().tournament.handNumber).toBe(1);
    expect(session.getState().phase).toBe('humanToAct');
  });
});
