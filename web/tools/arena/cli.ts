import { writeFileSync } from 'node:fs';
import { availableParallelism } from 'node:os';
import { parseArgs } from 'node:util';
import { BOTS } from './bots.ts';
import { runArena } from './run.ts';

function signed(value: number): string {
  const text = Math.abs(value).toFixed(1);
  return value < 0 ? `-${text}` : `+${text}`;
}

const { values } = parseArgs({
  options: {
    a: { type: 'string', default: 'math' },
    b: { type: 'string', default: 'always-call' },
    pairs: { type: 'string', default: '1000' },
    seed: { type: 'string', default: '1' },
    workers: { type: 'string', default: String(availableParallelism()) },
    out: { type: 'string' },
    'save-hands': { type: 'string' },
  },
});

const names = Object.keys(BOTS);
for (const name of [values.a, values.b]) {
  if (!names.includes(name)) {
    console.error(`unknown bot "${name}", choose from: ${names.join(', ')}`);
    process.exit(1);
  }
}

function integerFlag(name: string, raw: string, min: number): number {
  const value = Number(raw);
  if (raw.trim() === '' || !Number.isInteger(value) || value < min) {
    console.error(`--${name} must be an integer of at least ${min}, got "${raw}"`);
    process.exit(1);
  }
  return value;
}

const pairs = integerFlag('pairs', values.pairs, 1);
const seed = integerFlag('seed', values.seed, 0);
const workers = integerFlag('workers', values.workers, 1);

const report = await runArena({
  a: values.a,
  b: values.b,
  pairs,
  seed,
  workers,
  saveHands: values['save-hands'],
});

if (values.out !== undefined) writeFileSync(values.out, JSON.stringify(report, null, 2) + '\n');

const interval = report.ci95 === null ? 'n/a' : report.ci95.toFixed(1);
console.log(
  `${report.a} vs ${report.b}: ${signed(report.bbPer100)} ± ${interval} bb/100 over ${report.hands} hands in ${Math.round(report.durationMs / 1000)} s`,
);
