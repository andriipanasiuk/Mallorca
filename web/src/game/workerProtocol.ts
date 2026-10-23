import type { Decision, Observation } from '../brains/index.ts';
import type { LegalActions } from '../engine/index.ts';

export type WorkerRequest =
  | { kind: 'reset' }
  | { kind: 'observe'; observations: Observation[] }
  | { kind: 'decide'; id: number; legal: LegalActions };

export type WorkerReply = { id: number; decision: Decision } | { id: number; error: string };
