import type { Action, LegalActions } from '../engine/index.ts';

export type Intent = { kind: 'fold' | 'passive' } | { kind: 'raise'; to: number };

export function toIntent(action: Action): Intent {
  switch (action.kind) {
    case 'fold':
      return { kind: 'fold' };
    case 'raise':
      return { kind: 'raise', to: action.to };
    default:
      return { kind: 'passive' };
  }
}

export function resolveIntent(intent: Intent, legal: LegalActions): Action {
  if (intent.kind === 'raise' && legal.raise !== null) {
    const { min, max } = legal.raise;
    const to = Math.min(Math.max(Math.round(intent.to), min), max);
    return { kind: 'raise', to };
  }
  if (intent.kind === 'fold' && legal.fold) {
    return { kind: 'fold' };
  }
  if (legal.check) {
    return { kind: 'check' };
  }
  if (legal.call !== null) {
    return { kind: 'call' };
  }
  return { kind: 'fold' };
}
