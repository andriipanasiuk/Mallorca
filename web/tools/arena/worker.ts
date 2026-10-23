import { parentPort, workerData } from 'node:worker_threads';
import type { EngineEvent } from '../../src/engine/index.ts';
import { BOTS } from './bots.ts';
import type { BotName } from './bots.ts';
import { playPair } from './match.ts';

export interface WorkerInput {
  a: BotName;
  b: BotName;
  seed: number;
  indices: number[];
  withHands: boolean;
}

export interface WorkerResult {
  index: number;
  net: number;
  hands?: EngineEvent[][];
}

const input = workerData as WorkerInput;
for (const index of input.indices) {
  const { net, hands } = playPair(BOTS[input.a], BOTS[input.b], input.seed, index);
  const result: WorkerResult = input.withHands ? { index, net, hands } : { index, net };
  parentPort?.postMessage(result);
}
