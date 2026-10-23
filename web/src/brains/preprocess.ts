import type { Action, LegalActions } from '../engine/index.ts';
import type { GameContext } from './handState.ts';
import { PREFLOP } from './handState.ts';
import type { SolverAction } from './profit.ts';
import { call, check, createRaiseAction, fold } from './profit.ts';

const FIRST_BET_PERCENT = 0.75;

export function preprocessAction(
  action: SolverAction,
  game: GameContext,
  toCall: number,
): SolverAction {
  const pot = game.pot;
  const effectiveStack = game.bankRollAtRisk;
  if (action.type === 'fold') {
    return toCall === 0 ? check() : fold();
  }
  if (action.type === 'passive') {
    return toCall !== 0 ? call(toCall) : check();
  }
  if (toCall !== 0 && effectiveStack <= 0) {
    return call(toCall);
  }
  if (game.street === PREFLOP) {
    return createRaiseAction(toCall, pot, effectiveStack, 1);
  }
  const result = createRaiseAction(
    toCall,
    pot,
    effectiveStack,
    toCall !== 0 ? 1 : FIRST_BET_PERCENT,
  );
  if (result.amount !== effectiveStack) {
    result.amount = Math.round(result.amount / game.bigBlind) * game.bigBlind;
  }
  return result;
}

function passive(legal: LegalActions): Action {
  return legal.check ? { kind: 'check' } : { kind: 'call' };
}

export function toLegalAction(
  action: SolverAction,
  legal: LegalActions,
  opponentBet: number,
): Action {
  if (action.type === 'fold') {
    return legal.fold ? { kind: 'fold' } : passive(legal);
  }
  if (action.type === 'passive' || legal.raise === null) {
    return passive(legal);
  }
  const to = Math.round(opponentBet + action.amount);
  return { kind: 'raise', to: Math.min(legal.raise.max, Math.max(legal.raise.min, to)) };
}
