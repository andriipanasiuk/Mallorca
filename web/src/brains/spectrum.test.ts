import { describe, expect, it } from 'vitest';
import type { Card } from '../cards/index.ts';
import { formatCard, parseCard, parseCards } from '../cards/index.ts';
import { Spectrum } from './spectrum.ts';

const hand = (text: string) => parseCards(text) as [Card, Card];
const names = (spectrum: Spectrum) =>
  spectrum.entries().map(({ cards }) => `${formatCard(cards[0])}${formatCard(cards[1])}`);

describe('Spectrum', () => {
  it('holds every one of the 1326 hole-card pairs at weight 1 when random', () => {
    const spectrum = Spectrum.random();
    expect(spectrum.size).toBe(1326);
    expect(spectrum.summaryWeight()).toBe(1326);
    expect(spectrum.weight(hand('As Kd'))).toBe(1);
    expect(spectrum.weight(hand('Kd As'))).toBe(1);
  });

  it('leaves 1275 pairs after removing one dead card and 1225 after removing two', () => {
    const spectrum = Spectrum.random();
    spectrum.remove(parseCard('As'));
    expect(spectrum.size).toBe(1275);
    expect(spectrum.weight(hand('As Kd'))).toBe(0);
    spectrum.remove(parseCard('Kd'));
    expect(spectrum.size).toBe(1225);
  });

  it('removes every pair that holds any of several cards', () => {
    const spectrum = Spectrum.random();
    spectrum.remove(...parseCards('As Kd 7h'));
    expect(spectrum.size).toBe(1326 - 3 * 51 + 3);
  });

  it('removes one hand and ignores a zero weight on add', () => {
    const spectrum = Spectrum.random();
    spectrum.removeHand(hand('Qh Qc'));
    spectrum.add(hand('2c 3c'), 0);
    spectrum.add(hand('Ah Ad'), 0.25);
    expect(spectrum.size).toBe(1325);
    expect(spectrum.weight(hand('Qc Qh'))).toBe(0);
    expect(spectrum.weight(hand('2c 3c'))).toBe(1);
    expect(spectrum.weight(hand('Ad Ah'))).toBe(0.25);
  });

  it('iterates in the order of the Java HashMap of HoleCards', () => {
    const random = names(Spectrum.random());
    expect(random.slice(0, 6)).toEqual(['6d9c', '6h9c', '7s9c', '7c9c', '7d9c', '7h9c']);
    const small = Spectrum.empty();
    small.add(hand('Ac Kc'), 1);
    small.add(hand('2s 2c'), 0.5);
    small.add(hand('3d 4d'), 0.5);
    expect(names(small)).toEqual(['3d4d', 'AcKc', '2s2c']);
  });

  it('copies independently', () => {
    const spectrum = Spectrum.random();
    const copy = spectrum.copy();
    copy.remove(parseCard('2c'));
    expect(spectrum.size).toBe(1326);
    expect(copy.size).toBe(1275);
    expect(names(copy)).toEqual(names(spectrum).filter((name) => !name.includes('2c')));
  });
});
