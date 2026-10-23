import type { Card } from '../cards/index.ts';
import type { WeightedHands } from '../equity/index.ts';
import { holeCardsHash, JAVA_DECK } from './javaCard.ts';

export type HoleCards = readonly [Card, Card];

interface Entry {
  cards: HoleCards;
  weight: number;
  sequence: number;
}

const INITIAL_CAPACITY = 16;
const LOAD_FACTOR = 0.75;
const TREEIFY_THRESHOLD = 8;
const MIN_TREEIFY_CAPACITY = 64;

export class Spectrum {
  private readonly weights = new Map<number, Entry>();
  private capacity = INITIAL_CAPACITY;
  private bucketSizes = new Map<number, number>();
  private nextSequence = 0;
  private ordered: Entry[] | null = null;

  static empty(): Spectrum {
    return new Spectrum();
  }

  static random(): Spectrum {
    const result = new Spectrum();
    for (let i = 0; i < JAVA_DECK.length; i++) {
      for (let j = i + 1; j < JAVA_DECK.length; j++) {
        result.put([JAVA_DECK[i] as Card, JAVA_DECK[j] as Card], 1);
      }
    }
    return result;
  }

  get size(): number {
    return this.weights.size;
  }

  isEmpty(): boolean {
    return this.weights.size === 0;
  }

  weight(cards: HoleCards): number {
    return this.weights.get(holeCardsHash(cards[0], cards[1]))?.weight ?? 0;
  }

  add(cards: HoleCards, weight: number): void {
    if (weight !== 0) {
      this.put(cards, weight);
    }
  }

  remove(...cards: Card[]): void {
    for (const [hash, entry] of [...this.weights]) {
      if (cards.includes(entry.cards[0]) || cards.includes(entry.cards[1])) {
        this.delete(hash);
      }
    }
  }

  removeHand(cards: HoleCards): void {
    this.delete(holeCardsHash(cards[0], cards[1]));
  }

  summaryWeight(): number {
    let sum = 0;
    for (const entry of this.order()) {
      sum += entry.weight;
    }
    return sum;
  }

  entries(): WeightedHands {
    return this.order().map(({ cards, weight }) => ({ cards, weight }));
  }

  hands(): HoleCards[] {
    return this.order().map(({ cards }) => cards);
  }

  copy(): Spectrum {
    const result = new Spectrum();
    result.capacity = this.capacity;
    result.nextSequence = this.nextSequence;
    result.bucketSizes = new Map(this.bucketSizes);
    for (const [hash, entry] of this.weights) {
      result.weights.set(hash, { ...entry });
    }
    return result;
  }

  private put(cards: HoleCards, weight: number): void {
    const hash = holeCardsHash(cards[0], cards[1]);
    const existing = this.weights.get(hash);
    if (existing !== undefined) {
      existing.weight = weight;
      this.ordered = null;
      return;
    }
    const bucket = hash & (this.capacity - 1);
    const binCount = this.bucketSizes.get(bucket) ?? 0;
    this.weights.set(hash, { cards, weight, sequence: this.nextSequence++ });
    this.bucketSizes.set(bucket, binCount + 1);
    if (binCount >= TREEIFY_THRESHOLD && this.capacity < MIN_TREEIFY_CAPACITY) {
      this.resize();
    }
    if (this.weights.size > this.capacity * LOAD_FACTOR) {
      this.resize();
    }
    this.ordered = null;
  }

  private delete(hash: number): void {
    if (this.weights.delete(hash)) {
      const bucket = hash & (this.capacity - 1);
      this.bucketSizes.set(bucket, (this.bucketSizes.get(bucket) ?? 1) - 1);
      this.ordered = null;
    }
  }

  private resize(): void {
    this.capacity *= 2;
    this.bucketSizes = new Map();
    for (const hash of this.weights.keys()) {
      const bucket = hash & (this.capacity - 1);
      this.bucketSizes.set(bucket, (this.bucketSizes.get(bucket) ?? 0) + 1);
    }
  }

  private order(): Entry[] {
    if (this.ordered === null) {
      const mask = this.capacity - 1;
      this.ordered = [...this.weights]
        .sort(([a, x], [b, y]) => (a & mask) - (b & mask) || x.sequence - y.sequence)
        .map(([, entry]) => entry);
    }
    return this.ordered;
  }
}
