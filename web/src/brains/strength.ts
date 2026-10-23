import type { Card } from '../cards/index.ts';
import { fullDeck } from '../cards/index.ts';
import type { StreetEquity } from '../equity/index.ts';
import { pheEvaluator } from '../equity/index.ts';
import { holeCardsHash, rankPairIndex } from './javaCard.ts';
import type { Observation } from './observation.ts';
import { preflopEquityVsRandom } from './preflopEquilator.ts';
import type { HoleCards } from './spectrum.ts';
import { Spectrum } from './spectrum.ts';

export type StreetName = 'preflop' | 'flop' | 'turn' | 'river';

export interface StrengthMap {
  get(cards: HoleCards): StreetEquity | undefined;
}

const BOARD_BY_STREET: Record<StreetName, number> = { preflop: 0, flop: 3, turn: 4, river: 5 };

let javaOrder: HoleCards[] | undefined;

function randomSpectrumOrder(): HoleCards[] {
  javaOrder ??= Spectrum.random().hands();
  return javaOrder;
}

function isBlocked(cards: HoleCards, dead: readonly Card[]): boolean {
  return dead.includes(cards[0]) || dead.includes(cards[1]);
}

export function strengthVsRandom(hero: HoleCards, board: readonly Card[]): number {
  const live = fullDeck().filter(
    (card) => card !== hero[0] && card !== hero[1] && !board.includes(card),
  );
  const heroRank = pheEvaluator.rank([hero[0], hero[1], ...board]);
  const villain = [0, 0, ...board];
  let count = 0;
  let wins = 0;
  let draws = 0;
  for (let i = 0; i < live.length; i++) {
    villain[0] = live[i] as Card;
    for (let j = i + 1; j < live.length; j++) {
      villain[1] = live[j] as Card;
      const villainRank = pheEvaluator.rank(villain);
      if (heroRank > villainRank) {
        wins++;
      } else if (heroRank === villainRank) {
        draws++;
      }
      count++;
    }
  }
  return (draws / 2 + wins) / count;
}

export function equityVsRandom(hero: HoleCards, board: readonly Card[]): StreetEquity {
  const live = fullDeck().filter(
    (card) => card !== hero[0] && card !== hero[1] && !board.includes(card),
  );
  const heroCurrent = pheEvaluator.rank([hero[0], hero[1], ...board]);
  const heroNext = [hero[0], hero[1], ...board, 0];
  const villainCurrent = [0, 0, ...board];
  const villainNext = [0, 0, ...board, 0];
  const last = board.length + 2;
  let count = 0;
  let wins = 0;
  let draws = 0;
  let countPositive = 0;
  let winsPositive = 0;
  let countNegative = 0;
  let winsNegative = 0;
  for (let f = 0; f < live.length; f++) {
    const first = live[f] as Card;
    for (let s = f + 1; s < live.length; s++) {
      const second = live[s] as Card;
      villainCurrent[0] = first;
      villainCurrent[1] = second;
      villainNext[0] = first;
      villainNext[1] = second;
      const villainRank = pheEvaluator.rank(villainCurrent);
      const behind = heroCurrent < villainRank;
      const tied = heroCurrent === villainRank;
      if (tied) {
        draws++;
      } else if (!behind) {
        wins++;
      }
      count++;
      for (const next of live) {
        if (next === first || next === second) {
          continue;
        }
        heroNext[last] = next;
        villainNext[last] = next;
        const heroAfter = pheEvaluator.rank(heroNext);
        const villainAfter = pheEvaluator.rank(villainNext);
        if (tied) {
          countPositive += 0.5;
          countNegative += 0.5;
          if (heroAfter > villainAfter) {
            winsPositive += 0.5;
          }
          if (heroAfter < villainAfter) {
            winsNegative += 0.5;
          }
        } else if (behind) {
          countPositive++;
          if (heroAfter > villainAfter) {
            winsPositive++;
          } else if (heroAfter === villainAfter) {
            winsPositive += 0.5;
          }
        } else {
          countNegative++;
          if (heroAfter < villainAfter) {
            winsNegative++;
          } else if (heroAfter === villainAfter) {
            winsNegative += 0.5;
          }
        }
      }
    }
  }
  return {
    strength: (draws / 2 + wins) / count,
    positivePotential: countPositive !== 0 ? winsPositive / countPositive : 1,
    negativePotential: countNegative !== 0 ? winsNegative / countNegative : 1,
  };
}

class PreflopMap implements StrengthMap {
  private readonly cache = new Map<number, StreetEquity>();

  get(cards: HoleCards): StreetEquity {
    const hash = holeCardsHash(cards[0], cards[1]);
    let equity = this.cache.get(hash);
    if (equity === undefined) {
      equity = preflopEquityVsRandom(cards);
      this.cache.set(hash, equity);
    }
    return equity;
  }
}

class PostflopMap implements StrengthMap {
  private readonly cache = new Map<number, StreetEquity>();
  private readonly potentials = new Map<number, StreetEquity>();

  private readonly board: readonly Card[];

  constructor(board: readonly Card[]) {
    this.board = board;
  }

  get(cards: HoleCards): StreetEquity | undefined {
    if (isBlocked(cards, this.board)) {
      return undefined;
    }
    const hash = holeCardsHash(cards[0], cards[1]);
    let equity = this.cache.get(hash);
    if (equity === undefined) {
      const strength = strengthVsRandom(cards, this.board);
      equity =
        this.board.length === BOARD_BY_STREET.river
          ? { strength, positivePotential: 0, negativePotential: 0 }
          : { ...this.potentialsOf(cards), strength };
      this.cache.set(hash, equity);
    }
    return equity;
  }

  private potentialsOf(cards: HoleCards): StreetEquity {
    const pair = rankPairIndex(cards[0], cards[1]);
    let cached = this.potentials.get(pair);
    if (cached === undefined) {
      const first =
        randomSpectrumOrder().find(
          (combo) => rankPairIndex(combo[0], combo[1]) === pair && !isBlocked(combo, this.board),
        ) ?? cards;
      cached = equityVsRandom(first, this.board);
      this.potentials.set(pair, cached);
    }
    return cached;
  }
}

export class StrengthManager {
  private maps = new Map<StreetName, StrengthMap>();

  observe(o: Observation): void {
    if (o.type === 'handStarted') {
      this.maps = new Map([['preflop', new PreflopMap()]]);
    } else if (o.type === 'street') {
      this.maps.set(o.street, new PostflopMap(o.board.slice(0, BOARD_BY_STREET[o.street])));
    }
  }

  map(street: StreetName): StrengthMap {
    const result = this.maps.get(street);
    if (result === undefined) {
      throw new Error(`No ${street} has been observed in this hand`);
    }
    return result;
  }
}
