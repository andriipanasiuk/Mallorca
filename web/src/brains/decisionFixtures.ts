import type { Action, LegalActions } from '../engine/index.ts';
import fixtures from './fixtures/decisions.json' with { type: 'json' };
import type { ObservationJson } from './observation.ts';

export interface DecisionFixture {
  id: string;
  observations: ObservationJson[];
  legal: LegalActions;
  evByAction: { fold: number | null; passive: number | null; aggressive: number | null };
  chosen: Action;
  java: { street: string; pot: number };
}

export const decisionFixtures = fixtures as DecisionFixture[];
