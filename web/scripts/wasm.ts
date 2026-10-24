import { execFileSync } from 'node:child_process';
import { copyFileSync, existsSync } from 'node:fs';
import { homedir } from 'node:os';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';

const solver = fileURLToPath(new URL('../../solver/', import.meta.url));
const built = join(solver, 'target/wasm32-unknown-unknown/release/mallorca_wasm.wasm');
const target = fileURLToPath(new URL('../src/cfr/mallorca.wasm', import.meta.url));
const installed = join(homedir(), '.cargo/bin/cargo');
const cargo = existsSync(installed) ? installed : 'cargo';

execFileSync(
  cargo,
  ['build', '--release', '-p', 'mallorca-wasm', '--target', 'wasm32-unknown-unknown'],
  { cwd: solver, stdio: 'inherit' },
);
copyFileSync(built, target);
