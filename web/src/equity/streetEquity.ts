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
  const canImprove = board.length < RIVER_SIZE;
  let count = 0;
  let wins = 0;
  let draws = 0;
  let countPositive = 0;
  let winsPositive = 0;
  let countNegative = 0;
  let winsNegative = 0;
  for (const { cards: villain, weight } of range) {
    if (isBlocked(villain, dead)) {
      continue;
    }
    const villainRank = pheEvaluator.rank([...villain, ...board]);
    count += weight;
    if (heroRank === villainRank) {
      draws += weight;
      continue;
    }
    const heroBehind = heroRank < villainRank;
    if (!heroBehind) {
      wins += weight;
    }
    if (!canImprove) {
      continue;
    }
    for (const next of remainingDeck([...dead, ...villain])) {
      const heroAfter = pheEvaluator.rank([...hero, ...board, next]);
      const villainAfter = pheEvaluator.rank([...villain, ...board, next]);
      if (heroBehind) {
        countPositive += weight;
        if (heroAfter > villainAfter) {
          winsPositive += weight;
        }
      } else {
        countNegative += weight;
        if (heroAfter < villainAfter) {
          winsNegative += weight;
        }
      }
    }
  }
  const noPotential = canImprove ? 1 : 0;
  return {
    strength: (draws / 2 + wins) / count,
    positivePotential: countPositive !== 0 ? winsPositive / countPositive : noPotential,
    negativePotential: countNegative !== 0 ? winsNegative / countNegative : noPotential,
  };
}

export function realEquity(e: StreetEquity): number {
  return e.strength * (1 - e.negativePotential) + (1 - e.strength) * e.positivePotential;
}
