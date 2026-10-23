import { spawnSync } from 'node:child_process';
import { mkdtempSync, readFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { describe, expect, it } from 'vitest';
import { runArena } from './run.ts';

const webDir = fileURLToPath(new URL('../../', import.meta.url));

function cli(args: string[]) {
  return spawnSync('node', ['tools/arena/cli.ts', ...args], { cwd: webDir, encoding: 'utf8' });
}

describe('arena cli', () => {
  it('one worker and four workers give the same report', () => {
    const dir = mkdtempSync(join(tmpdir(), 'arena-'));
    const base = ['--a', 'always-call', '--b', 'always-raise', '--pairs', '8', '--seed', '4'];
    const one = cli([...base, '--workers', '1', '--out', join(dir, 'a.json')]);
    const four = cli([...base, '--workers', '4', '--out', join(dir, 'b.json')]);
    expect(one.status).toBe(0);
    expect(four.status).toBe(0);
    const x = JSON.parse(readFileSync(join(dir, 'a.json'), 'utf8'));
    const y = JSON.parse(readFileSync(join(dir, 'b.json'), 'utf8'));
    expect(x.bbPer100).toBe(y.bbPer100);
    expect(x.ci95).toBe(y.ci95);
    expect(x.hands).toBe(16);
    expect(one.stdout).toMatch(
      /^always-call vs always-raise: [+-]\d+\.\d ± \d+\.\d bb\/100 over 16 hands in \d+ s\n$/,
    );
  });

  it('an unknown bot exits with 1 and lists the bots', () => {
    const r = cli(['--a', 'nope']);
    expect(r.status).toBe(1);
    expect(r.stderr).toContain('always-call');
  });

  it('save-hands writes one JSON record per hand', () => {
    const dir = mkdtempSync(join(tmpdir(), 'arena-'));
    const file = join(dir, 'hands.jsonl');
    const r = cli([
      '--a',
      'always-call',
      '--b',
      'always-raise',
      '--pairs',
      '8',
      '--seed',
      '4',
      '--workers',
      '2',
      '--save-hands',
      file,
    ]);
    expect(r.status).toBe(0);
    const lines = readFileSync(file, 'utf8').trim().split('\n');
    expect(lines).toHaveLength(16);
    const first = JSON.parse(lines[0]!);
    expect(first.sessionId).toBe('arena-4');
    expect(first.humanSeat).toBeNull();
    expect(first.names).toEqual(['always-call', 'always-raise']);
    expect(JSON.parse(lines[1]!).names).toEqual(['always-raise', 'always-call']);
  });
});

describe('runArena', () => {
  it('runs in process', async () => {
    const report = await runArena({ a: 'math', b: 'always-call', pairs: 20, seed: 1, workers: 1 });
    expect(report.hands).toBe(40);
    expect(Number.isFinite(report.bbPer100)).toBe(true);
  }, 120_000);
});
