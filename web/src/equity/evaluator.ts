import { evaluateCardCodes, handRank, ranks } from 'phe';
import type { Card } from '../cards/index.ts';

export type HandCategory =
  | 'high-card'
  | 'pair'
  | 'two-pair'
  | 'trips'
  | 'straight'
  | 'flush'
  | 'full-house'
  | 'quads'
  | 'straight-flush';

export interface HandEvaluator {
  rank(cards: readonly Card[]): number;
  category(rank: number): HandCategory;
}

const WORST_VALUE = 7462;

const CATEGORY_BY_PHE_RANK = new Map<number, HandCategory>([
  [ranks.HIGH_CARD, 'high-card'],
  [ranks.ONE_PAIR, 'pair'],
  [ranks.TWO_PAIR, 'two-pair'],
  [ranks.THREE_OF_A_KIND, 'trips'],
  [ranks.STRAIGHT, 'straight'],
  [ranks.FLUSH, 'flush'],
  [ranks.FULL_HOUSE, 'full-house'],
  [ranks.FOUR_OF_A_KIND, 'quads'],
  [ranks.STRAIGHT_FLUSH, 'straight-flush'],
]);

export const pheEvaluator: HandEvaluator = {
  rank(cards) {
    return WORST_VALUE - evaluateCardCodes(cards);
  },
  category(rank) {
    const category = CATEGORY_BY_PHE_RANK.get(handRank(WORST_VALUE - rank));
    if (category === undefined) {
      throw new Error(`Invalid hand rank: ${rank}`);
    }
    return category;
  },
};
