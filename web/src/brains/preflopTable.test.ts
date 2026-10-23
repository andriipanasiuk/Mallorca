import { describe, expect, it } from 'vitest';
import table from './data/preflop-strength.json' with { type: 'json' };
import javaTable from './fixtures/preflop-strength-java.json' with { type: 'json' };

const SIZE = 169;

describe('regenerated preflop strength table', () => {
  it('has the shape of the Java table', () => {
    expect(table).toHaveLength(SIZE);
    for (const row of table) {
      expect(row).toHaveLength(SIZE);
    }
  });

  it('matches the Java table within 0.01 in every cell', () => {
    const java = javaTable as number[][];
    const ours = table as number[][];
    const offCells: string[] = [];
    for (let i = 0; i < SIZE; i++) {
      for (let j = 0; j < SIZE; j++) {
        const expected = java[i]?.[j] ?? Number.NaN;
        const actual = ours[i]?.[j] ?? Number.NaN;
        if (!(Math.abs(actual - expected) <= 0.01 + 1e-9)) {
          offCells.push(`${i},${j}: ${actual} vs ${expected}`);
        }
      }
    }
    expect(offCells).toEqual([]);
  });
});
