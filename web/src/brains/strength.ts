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

type Standing = 'ahead' | 'tied' | 'behind';

interface PotentialTally {
  countPositive: number;
  winsPositive: number;
  countNegative: number;
  winsNegative: number;
}

interface NextCardDeal {
  readonly live: readonly Card[];
  readonly heroNext: Card[];
  readonly villainNext: Card[];
  readonly last: number;
  readonly tally: PotentialTally;
}

function tallyFromTie(tally: PotentialTally, heroAfter: number, villainAfter: number): void {
  tally.countPositive += 0.5;
  tally.countNegative += 0.5;
  if (heroAfter > villainAfter) {
    tally.winsPositive += 0.5;
  }
  if (heroAfter < villainAfter) {
    tally.winsNegative += 0.5;
  }
}

function tallyFromBehind(tally: PotentialTally, heroAfter: number, villainAfter: number): void {
  tally.countPositive++;
  if (heroAfter > villainAfter) {
    tally.winsPositive++;
  } else if (heroAfter === villainAfter) {
    tally.winsPositive += 0.5;
  }
}

function tallyFromAhead(tally: PotentialTally, heroAfter: number, villainAfter: number): void {
  tally.countNegative++;
  if (heroAfter < villainAfter) {
    tally.winsNegative++;
  } else if (heroAfter === villainAfter) {
    tally.winsNegative += 0.5;
  }
}

const TALLY_BY_STANDING: Record<
  Standing,
  (tally: PotentialTally, heroAfter: number, villainAfter: number) => void
> = { tied: tallyFromTie, behind: tallyFromBehind, ahead: tallyFromAhead };

function tallyNextCards(deal: NextCardDeal, first: Card, second: Card, standing: Standing): void {
  const tallyNext = TALLY_BY_STANDING[standing];
  for (const next of deal.live) {
    if (next === first || next === second) {
      continue;
    }
    deal.heroNext[deal.last] = next;
    deal.villainNext[deal.last] = next;
    tallyNext(deal.tally, pheEvaluator.rank(deal.heroNext), pheEvaluator.rank(deal.villainNext));
  }
}

function standingOf(heroRank: number, villainRank: number): Standing {
  if (heroRank === villainRank) {
    return 'tied';
  }
  return heroRank < villainRank ? 'behind' : 'ahead';
}

export function equityVsRandom(hero: HoleCards, board: readonly Card[]): StreetEquity {
  const live = fullDeck().filter(
    (card) => card !== hero[0] && card !== hero[1] && !board.includes(card),
  );
  const heroCurrent = pheEvaluator.rank([hero[0], hero[1], ...board]);
  const villainCurrent = [0, 0, ...board];
  const tally: PotentialTally = {
    countPositive: 0,
    winsPositive: 0,
    countNegative: 0,
    winsNegative: 0,
  };
  const deal: NextCardDeal = {
    live,
    heroNext: [hero[0], hero[1], ...board, 0],
    villainNext: [0, 0, ...board, 0],
    last: board.length + 2,
    tally,
  };
  let count = 0;
  let wins = 0;
  let draws = 0;
  for (let f = 0; f < live.length; f++) {
    const first = live[f] as Card;
    for (let s = f + 1; s < live.length; s++) {
      const second = live[s] as Card;
      villainCurrent[0] = first;
      villainCurrent[1] = second;
      deal.villainNext[0] = first;
      deal.villainNext[1] = second;
      const standing = standingOf(heroCurrent, pheEvaluator.rank(villainCurrent));
      if (standing === 'tied') {
        draws++;
      } else if (standing === 'ahead') {
        wins++;
      }
      count++;
      tallyNextCards(deal, first, second, standing);
    }
  }
  return {
    strength: (draws / 2 + wins) / count,
    positivePotential: tally.countPositive !== 0 ? tally.winsPositive / tally.countPositive : 1,
    negativePotential: tally.countNegative !== 0 ? tally.winsNegative / tally.countNegative : 1,
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
