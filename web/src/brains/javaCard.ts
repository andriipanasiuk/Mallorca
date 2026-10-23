import type { Card } from '../cards/index.ts';

const SUITS = 4;
const DECK_SIZE = 52;
const HASH_FACTOR = 70;

export function toJavaInt(card: Card): number {
  return (card >> 2) * SUITS + (((card & 3) + 1) & 3);
}

export function fromJavaInt(value: number): Card {
  return (value >> 2) * SUITS + (((value & 3) + 3) & 3);
}

export const JAVA_DECK: readonly Card[] = Array.from({ length: DECK_SIZE }, (_, i) =>
  fromJavaInt(i),
);

export function holeCardsHash(first: Card, second: Card): number {
  const a = toJavaInt(first);
  const b = toJavaInt(second);
  return a > b ? a * HASH_FACTOR + b : b * HASH_FACTOR + a;
}

export function rankPairIndex(first: Card, second: Card): number {
  const a = first >> 2;
  const b = second >> 2;
  return a > b ? a * 13 + b : b * 13 + a;
}
