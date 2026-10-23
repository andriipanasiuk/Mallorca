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

function profitabilityIsGood(actionProfit: ActionProfit | null, lowEV: number): boolean {
  if (actionProfit === null) {
    return false;
  }
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

function correctProfits(
  game: GameContext,
  corrections: ProfitCorrections | null,
  toCall: number,
  raiseProfit: ActionProfit | null,
  passiveProfit: ActionProfit,
): void {
  const bb = game.bigBlind;
  const preflop = game.street === PREFLOP;
  if (raiseProfit !== null) {
    raiseProfit.evVarKoeff += correctionOf(corrections, true, toCall, preflop);
    raiseProfit.calculateEVVarChanged(bb);
  }
  passiveProfit.evVarKoeff += correctionOf(corrections, false, toCall, preflop);
  passiveProfit.calculateEVVarChanged(bb);
}

function bestAction(
  distribution: ActionDistribution,
  game: GameContext,
  fromTop: boolean,
  corrections: ProfitCorrections | null,
): ActionProfitEntry {
  let foldAction: ActionProfitEntry | null = null;
  let passiveAction: ActionProfitEntry | null = null;
  let raiseAction: ActionProfitEntry | null = null;
  let foldProfit: ActionProfit | null = null;
  let passiveProfit: ActionProfit | null = null;
  let raiseProfit: ActionProfit | null = null;
  let toCall = -1;
  for (const entry of distribution) {
    const actionProfit = new ActionProfit();
    if (entry.action.type === 'fold') {
      foldAction = entry;
      foldProfit = actionProfit;
    } else if (entry.action.type === 'passive') {
      toCall = entry.action.toCall;
      passiveAction = entry;
      passiveProfit = actionProfit;
    } else {
      raiseProfit = actionProfit;
      raiseAction = entry;
    }
    actionProfit.variance = entry.profit.getVariance();
    actionProfit.ev = entry.profit.getEV();
    actionProfit.calculate(game.bigBlind);
  }
  if (toCall === -1 || passiveAction === null || passiveProfit === null) {
    throw new Error('toCall cannot be -1 in this state');
  }
  if (foldAction === null || foldProfit === null) {
    throw new Error('The profit map has no fold');
  }
  if (fromTop) {
    correctProfits(game, corrections, toCall, raiseProfit, passiveProfit);
  }
  if (raiseAction !== null && profitabilityIsGood(raiseProfit, foldProfit.evBBKoeff)) {
    return raiseAction;
  }
  if (profitabilityIsGood(passiveProfit, foldProfit.evBBKoeff) || toCall === 0) {
    return passiveAction;
  }
  return foldAction;
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
