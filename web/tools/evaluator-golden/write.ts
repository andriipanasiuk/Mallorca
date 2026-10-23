import { writeFileSync } from 'node:fs';
import { pheEvaluator } from '../../src/equity/index.ts';
import { formatCard, fullDeck, mulberry32, shuffle } from '../../src/cards/index.ts';

const SEED = 20261023;
const HANDS_PER_SIZE = 20000;
const SIZES = [5, 6, 7];

const target = process.argv[2];
if (target === undefined) {
  throw new Error('usage: node tools/evaluator-golden/write.ts <output file>');
}

const rng = mulberry32(SEED);
const deck = fullDeck();
const lines: string[] = [];
for (const size of SIZES) {
  for (let i = 0; i < HANDS_PER_SIZE; i++) {
    const cards = shuffle(deck, rng).slice(0, size);
    lines.push(`${cards.map(formatCard).join(' ')},${pheEvaluator.rank(cards)}`);
  }
}
writeFileSync(target, `${lines.join('\n')}\n`);
