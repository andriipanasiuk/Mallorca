import type { Card } from '../cards/index.ts';
import type { EquityOptions, WeightedHands } from './equity.ts';
import { isBlocked, remainingDeck } from './equity.ts';
import { pheEvaluator } from './evaluator.ts';

export interface StreetEquity {
  strength: number;
  positivePotential: number;
  negativePotential: number;
}

const FLOP_SIZE = 3;
const RIVER_SIZE = 5;

interface Tally {
  count: number;
  wins: number;
  draws: number;
  countPositive: number;
  winsPositive: number;
  countNegative: number;
  winsNegative: number;
}

interface Matchup {
  hero: readonly [Card, Card];
  villain: readonly [Card, Card];
  board: readonly Card[];
  dead: readonly Card[];
}

function tallyPotential(tally: Tally, matchup: Matchup, weight: number, heroBehind: boolean): void {
  const { hero, villain, board, dead } = matchup;
  for (const next of remainingDeck([...dead, ...villain])) {
    const heroAfter = pheEvaluator.rank([...hero, ...board, next]);
    const villainAfter = pheEvaluator.rank([...villain, ...board, next]);
    if (heroBehind) {
      tally.countPositive += weight;
      if (heroAfter > villainAfter) {
        tally.winsPositive += weight;
      }
    } else {
      tally.countNegative += weight;
      if (heroAfter < villainAfter) {
        tally.winsNegative += weight;
      }
    }
  }
}

function tallyVillain(tally: Tally, matchup: Matchup, weight: number, heroRank: number): void {
  const villainRank = pheEvaluator.rank([...matchup.villain, ...matchup.board]);
  tally.count += weight;
  if (heroRank === villainRank) {
    tally.draws += weight;
    return;
  }
  const heroBehind = heroRank < villainRank;
  if (!heroBehind) {
    tally.wins += weight;
  }
  if (matchup.board.length < RIVER_SIZE) {
    tallyPotential(tally, matchup, weight, heroBehind);
  }
}

export function streetEquity(
  hero: readonly [Card, Card],
  range: WeightedHands,
  board: readonly Card[],
  _opts?: EquityOptions,
): StreetEquity {
  if (board.length < FLOP_SIZE || board.length > RIVER_SIZE) {
    throw new Error(`Street equity needs a board of 3 to 5 cards, got ${board.length}`);
  }
  const dead = [...hero, ...board];
  const heroRank = pheEvaluator.rank([...hero, ...board]);
  const tally: Tally = {
    count: 0,
    wins: 0,
    draws: 0,
    countPositive: 0,
    winsPositive: 0,
    countNegative: 0,
    winsNegative: 0,
  };
  for (const { cards: villain, weight } of range) {
    if (!isBlocked(villain, dead)) {
      tallyVillain(tally, { hero, villain, board, dead }, weight, heroRank);
    }
  }
  const noPotential = board.length < RIVER_SIZE ? 1 : 0;
  return {
    strength: (tally.draws / 2 + tally.wins) / tally.count,
    positivePotential: potential(tally.winsPositive, tally.countPositive, noPotential),
    negativePotential: potential(tally.winsNegative, tally.countNegative, noPotential),
  };
}

function potential(wins: number, count: number, fallback: number): number {
  return count !== 0 ? wins / count : fallback;
}

export function realEquity(e: StreetEquity): number {
  return e.strength * (1 - e.negativePotential) + (1 - e.strength) * e.positivePotential;
}
