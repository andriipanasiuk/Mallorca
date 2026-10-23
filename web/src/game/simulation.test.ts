import { describe, expect, it } from 'vitest';
import { mulberry32 } from '../cards/index.ts';
import type { Rng } from '../cards/index.ts';
import { legalActionsOf, toAct } from '../engine/index.ts';
import type { LegalActions } from '../engine/index.ts';
import { inProcessBot } from './botClient.ts';
import { createSession } from './session.ts';
import type { Intent, Session } from './session.ts';

const TOTAL_CHIPS = 4000;
const TOURNAMENTS = 9;
const TINY_STACK_MAX = 10;
const MAX_HANDS = 400;
const MAX_MOVES_PER_HAND = 60;
const HUMAN_SEAT = 1;

function randomIntent(legal: LegalActions, rng: Rng): Intent {
  const options: Intent[] = [];
  if (legal.fold) options.push({ kind: 'fold' });
  if (legal.check || legal.call !== null) options.push({ kind: 'passive' });
  if (legal.raise !== null) {
    const { min, max } = legal.raise;
    options.push({ kind: 'raise', to: min + Math.floor(rng.next() * (max - min + 1)) });
  }
  return options[Math.floor(rng.next() * options.length)] as Intent;
}

function chipsOnTable(session: Session): number {
  const { tournament } = session.getState();
  const pot = tournament.hand === null ? 0 : tournament.hand.pot;
  return tournament.seats[0].stack + tournament.seats[1].stack + pot;
}

function expectSettled(session: Session): void {
  const { phase, tournament } = session.getState();
  if (phase === 'humanToAct') {
    expect(toAct(tournament)).toBe(HUMAN_SEAT);
    expect(tournament.hand?.street).not.toBe('complete');
    return;
  }
  expect(['handOver', 'tournamentOver']).toContain(phase);
  expect(tournament.hand?.street).toBe('complete');
  expect(tournament.winner !== null).toBe(phase === 'tournamentOver');
}

function startingStacksFor(seed: number, rng: Rng): [number, number] | undefined {
  switch (seed % 3) {
    case 1: {
      const tiny = 1 + Math.floor(rng.next() * TINY_STACK_MAX);
      return seed % 2 === 0 ? [tiny, TOTAL_CHIPS - tiny] : [TOTAL_CHIPS - tiny, tiny];
    }
    case 2: {
      const first = 1 + Math.floor(rng.next() * (TOTAL_CHIPS - 1));
      return [first, TOTAL_CHIPS - first];
    }
    default:
      return undefined;
  }
}

async function playTournament(seed: number): Promise<number> {
  const rng = mulberry32(seed ^ 0x9e3779b9);
  const session = createSession({
    bot: inProcessBot(),
    seed,
    botDelayMs: 0,
    humanSeat: HUMAN_SEAT,
    names: ['Mallorca', 'Human'],
    startingStacks: startingStacksFor(seed, rng),
  });
  const unsubscribe = session.subscribe(() => {
    expect(chipsOnTable(session)).toBe(TOTAL_CHIPS);
  });
  try {
    await session.getState().start();
    expectSettled(session);
    let hands = 1;
    let moves = 0;
    while (session.getState().phase !== 'tournamentOver') {
      const { phase, tournament } = session.getState();
      if (phase === 'handOver') {
        expect(hands).toBeLessThan(MAX_HANDS);
        hands += 1;
        moves = 0;
        await session.getState().nextHand();
      } else {
        expect(moves).toBeLessThan(MAX_MOVES_PER_HAND);
        moves += 1;
        await session.getState().act(randomIntent(legalActionsOf(tournament), rng));
      }
      expectSettled(session);
    }
    const { seats, winner } = session.getState().tournament;
    expect(winner).not.toBeNull();
    expect(seats[winner as number]?.stack).toBe(TOTAL_CHIPS);
    return hands;
  } finally {
    unsubscribe();
    session.getState().dispose();
  }
}

describe('tournament simulation', () => {
  it('plays full tournaments against a random human without stalling or losing chips', async () => {
    for (let seed = 1; seed <= TOURNAMENTS; seed += 1) {
      await playTournament(seed);
    }
  }, 60_000);
});
