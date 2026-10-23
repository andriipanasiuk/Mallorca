import type { StreetEquity } from '../equity/index.ts';
import table from './data/preflop-strength.json' with { type: 'json' };
import { fromJavaInt, rankPairIndex, toJavaInt } from './javaCard.ts';
import type { HoleCards, Spectrum } from './spectrum.ts';

const STRENGTH: readonly (readonly number[])[] = table;
const DECK_SIZE = 52;
const PAIR = 100000;

function strengthAt(hero: number, villain: number): number {
  return STRENGTH[hero]?.[villain] ?? Number.NaN;
}

export function preflopStrength(hero: HoleCards, villain: HoleCards): number {
  return strengthAt(rankPairIndex(hero[0], hero[1]), rankPairIndex(villain[0], villain[1]));
}

function combination(card1: number, card2: number): number {
  const value1 = card1 >> 2;
  const value2 = card2 >> 2;
  if (value1 === value2) {
    return PAIR + value1;
  }
  const suited = (card1 & 3) === (card2 & 3) ? 1 : 0;
  return card1 > card2
    ? value1 * 1000 + value2 * 10 + suited
    : value2 * 1000 + value1 * 10 + suited;
}

export function preflopEquityVsRandom(hero: HoleCards): StreetEquity {
  const hero1 = toJavaInt(hero[0]);
  const hero2 = toJavaInt(hero[1]);
  const heroCombination = combination(hero1, hero2);
  const heroIndex = rankPairIndex(hero[0], hero[1]);
  let wins = 0;
  let count = 0;
  let positive = 0;
  let countPositive = 0;
  let negative = 0;
  let countNegative = 0;
  for (let i1 = 0; i1 < DECK_SIZE; i1++) {
    if (i1 === hero1 || i1 === hero2) {
      continue;
    }
    for (let i2 = i1 + 1; i2 < DECK_SIZE; i2++) {
      if (i2 === hero1 || i2 === hero2) {
        continue;
      }
      count++;
      const strengthToRiver = strengthAt(
        heroIndex,
        rankPairIndex(fromJavaInt(i1), fromJavaInt(i2)),
      );
      if (combination(i1, i2) < heroCombination) {
        wins++;
        countNegative++;
        negative += 1 - strengthToRiver;
      } else {
        countPositive++;
        positive += strengthToRiver;
      }
    }
  }
  return {
    strength: wins / count,
    positivePotential: countPositive !== 0 ? positive / countPositive : 0,
    negativePotential: countNegative !== 0 ? negative / countNegative : 0,
  };
}

export function preflopStrengthVsSpectrum(hero: HoleCards, spectrum: Spectrum): number {
  let count = 0;
  let wins = 0;
  for (const { cards, weight } of spectrum.entries()) {
    wins += preflopStrength(hero, cards) * weight;
    count += weight;
  }
  return wins / count;
}
