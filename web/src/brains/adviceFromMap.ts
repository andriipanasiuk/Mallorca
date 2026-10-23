import type { GameContext } from './handState.ts';
import { PREFLOP } from './handState.ts';
import type {
  ActionDistribution,
  ActionProfitEntry,
  ProfitCorrections,
  RandomVariable,
  SolverAction,
} from './profit.ts';

export interface AdviceCreatorFromMap {
  create(distribution: ActionDistribution, game: GameContext): SolverAction;
}

class ActionProfit {
  evBBKoeff = 0;
  evVarKoeff = 0;
  varBBKoeff = 0;
  ev = 0;
  variance = 0;

  calculate(bb: number): void {
    this.evBBKoeff = this.ev / bb;
    this.varBBKoeff = this.variance / bb;
    this.evVarKoeff = this.ev / this.variance;
  }

  calculateEVVarChanged(bb: number): void {
    this.ev = this.evVarKoeff * this.variance;
    this.evBBKoeff = this.ev / bb;
    this.varBBKoeff = this.variance / bb;
  }
}

const PROFITABLE_EV_VAR_ABOVE_VAR_BB_BELOW: readonly (readonly [number, number])[] = [
  [0.1, 2],
  [0.2, 5],
  [0.25, 10],
  [0.3, 25],
  [0.4, 30],
  [0.48, 38],
  [0.5, 40],
  [0.55, 50],
  [0.75, 100],
];

function profitabilityIsGood(actionProfit: ActionProfit, lowEV: number): boolean {
  if (actionProfit.evBBKoeff < lowEV) {
    return false;
  }
  const evVar = actionProfit.evVarKoeff;
  const varBB = actionProfit.varBBKoeff;
  if (evVar >= 0 && varBB <= 0) {
    return true;
  }
  return PROFITABLE_EV_VAR_ABOVE_VAR_BB_BELOW.some(
    ([minEvVar, maxVarBB]) => evVar > minEvVar && varBB < maxVarBB,
  );
}

function correctionOf(
  corrections: ProfitCorrections | null,
  aggro: boolean,
  toCall: number,
  preflop: boolean,
): number {
  if (corrections === null) {
    return 0;
  }
  if (preflop) {
    if (aggro) {
      return toCall === 0 ? corrections.preflopBet : corrections.preflopRaise;
    }
    return toCall === 0 ? corrections.preflopCheck : corrections.preflopCall;
  }
  if (aggro) {
    return toCall === 0 ? corrections.postflopBet : corrections.postflopRaise;
  }
  return toCall === 0 ? corrections.postflopCheck : corrections.postflopCall;
}

interface ScoredAction {
  readonly entry: ActionProfitEntry;
  readonly profit: ActionProfit;
}

interface ScoredDistribution {
  fold: ScoredAction | null;
  passive: ScoredAction | null;
  raise: ScoredAction | null;
  toCall: number;
}

interface CompleteDistribution {
  readonly fold: ScoredAction;
  readonly passive: ScoredAction;
  readonly raise: ScoredAction | null;
  readonly toCall: number;
}

function scoreDistribution(distribution: ActionDistribution, bb: number): ScoredDistribution {
  const scored: ScoredDistribution = { fold: null, passive: null, raise: null, toCall: -1 };
  for (const entry of distribution) {
    const profit = new ActionProfit();
    if (entry.action.type === 'fold') {
      scored.fold = { entry, profit };
    } else if (entry.action.type === 'passive') {
      scored.toCall = entry.action.toCall;
      scored.passive = { entry, profit };
    } else {
      scored.raise = { entry, profit };
    }
    profit.variance = entry.profit.getVariance();
    profit.ev = entry.profit.getEV();
    profit.calculate(bb);
  }
  return scored;
}

function completed(scored: ScoredDistribution): CompleteDistribution {
  const { fold, passive, raise, toCall } = scored;
  if (toCall === -1 || passive === null) {
    throw new Error('toCall cannot be -1 in this state');
  }
  if (fold === null) {
    throw new Error('The profit map has no fold');
  }
  return { fold, passive, raise, toCall };
}

function correctProfits(
  game: GameContext,
  corrections: ProfitCorrections | null,
  scored: CompleteDistribution,
): void {
  const bb = game.bigBlind;
  const preflop = game.street === PREFLOP;
  if (scored.raise !== null) {
    scored.raise.profit.evVarKoeff += correctionOf(corrections, true, scored.toCall, preflop);
    scored.raise.profit.calculateEVVarChanged(bb);
  }
  scored.passive.profit.evVarKoeff += correctionOf(corrections, false, scored.toCall, preflop);
  scored.passive.profit.calculateEVVarChanged(bb);
}

function bestAction(
  distribution: ActionDistribution,
  game: GameContext,
  fromTop: boolean,
  corrections: ProfitCorrections | null,
): ActionProfitEntry {
  const scored = completed(scoreDistribution(distribution, game.bigBlind));
  if (fromTop) {
    correctProfits(game, corrections, scored);
  }
  const lowEV = scored.fold.profit.evBBKoeff;
  if (scored.raise !== null && profitabilityIsGood(scored.raise.profit, lowEV)) {
    return scored.raise.entry;
  }
  if (profitabilityIsGood(scored.passive.profit, lowEV) || scored.toCall === 0) {
    return scored.passive.entry;
  }
  return scored.fold.entry;
}

export function getOptimal(
  distribution: ActionDistribution,
  game: GameContext,
  corrections: ProfitCorrections | null = null,
): RandomVariable {
  return bestAction(distribution, game, false, corrections).profit;
}

export class LessVarianceActionFromMap implements AdviceCreatorFromMap {
  private readonly corrections: ProfitCorrections | null;

  constructor(corrections: ProfitCorrections | null = null) {
    this.corrections = corrections;
  }

  create(distribution: ActionDistribution, game: GameContext): SolverAction {
    return bestAction(distribution, game, true, this.corrections).action;
  }
}
