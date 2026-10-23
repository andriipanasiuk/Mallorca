import type { GameContext, HandState } from './handState.ts';
import { PREFLOP } from './handState.ts';
import type { HoleCards, Spectrum } from './spectrum.ts';
import type { StrengthManager } from './strength.ts';

export type SolverActionType = 'fold' | 'passive' | 'aggressive';

export interface SolverAction {
  readonly type: SolverActionType;
  amount: number;
  readonly toCall: number;
  readonly percentOfPot: number;
}

const THRESHOLD_FOR_GOING_ALLIN = 0.6;

export function fold(): SolverAction {
  return { type: 'fold', amount: 0, toCall: 0, percentOfPot: 0 };
}

export function check(): SolverAction {
  return { type: 'passive', amount: 0, toCall: 0, percentOfPot: 0 };
}

export function call(toCall: number): SolverAction {
  return { type: 'passive', amount: toCall, toCall, percentOfPot: 0 };
}

export function aggressive(amount: number, toCall: number, percentOfPot = 0): SolverAction {
  return { type: 'aggressive', amount, toCall, percentOfPot };
}

export function createRaiseAction(
  toCall: number,
  pot: number,
  effectiveStack: number,
  percent: number,
): SolverAction {
  let raiseAmount = Math.round(percent * (toCall + pot));
  let percentOfPot = percent;
  if (effectiveStack - raiseAmount < THRESHOLD_FOR_GOING_ALLIN * (pot + toCall + 2 * raiseAmount)) {
    raiseAmount = effectiveStack;
    percentOfPot = raiseAmount / (pot + toCall);
  }
  return aggressive(raiseAmount, toCall, percentOfPot);
}

export function createBetAction(
  pot: number,
  effectiveStack: number,
  percent: number,
): SolverAction {
  return createRaiseAction(0, pot, effectiveStack, percent);
}

export class RandomVariable {
  private readonly probabilities: number[] = [];
  private readonly values: number[] = [];
  private ev: number | undefined;
  private variance: number | undefined;

  static constant(value: number): RandomVariable {
    const result = new RandomVariable();
    result.ev = value;
    result.variance = 0;
    result.add(1, value);
    return result;
  }

  get size(): number {
    return this.values.length;
  }

  add(probability: number, value: number): void {
    this.probabilities.push(probability);
    this.values.push(value);
  }

  addVariable(probability: number, variable: RandomVariable): void {
    for (let i = 0; i < variable.values.length; i++) {
      this.add(probability * (variable.probabilities[i] as number), variable.values[i] as number);
    }
  }

  getEV(): number {
    if (this.ev === undefined) {
      let result = 0;
      for (let i = 0; i < this.values.length; i++) {
        result += (this.probabilities[i] as number) * (this.values[i] as number);
      }
      this.ev = result;
    }
    return this.ev;
  }

  getVariance(): number {
    if (this.variance === undefined) {
      const ev = this.getEV();
      let result = 0;
      for (let i = 0; i < this.values.length; i++) {
        const deviation = (this.values[i] as number) - ev;
        result += (this.probabilities[i] as number) * (deviation * deviation);
      }
      this.variance = Math.sqrt(result);
    }
    return this.variance;
  }
}

export interface ActionProfitEntry {
  readonly action: SolverAction;
  readonly profit: RandomVariable;
}

export type ActionDistribution = ActionProfitEntry[];

export interface ProfitCorrections {
  readonly preflopCall: number;
  readonly preflopRaise: number;
  readonly preflopCheck: number;
  readonly preflopBet: number;
  readonly postflopCall: number;
  readonly postflopRaise: number;
  readonly postflopCheck: number;
  readonly postflopBet: number;
}

export interface GameSolver {
  onFirstActionPreflop(
    game: GameContext,
    info: HandState,
    effectiveStack: number,
    pot: number,
    spectrum: Spectrum,
    hero: HoleCards,
    strength: StrengthManager,
    isHeroOnButton: boolean,
    bigBlind: number,
  ): ActionDistribution;
  onFirstAction(
    game: GameContext,
    info: HandState,
    effectiveStack: number,
    pot: number,
    spectrum: Spectrum,
    hero: HoleCards,
    strength: StrengthManager,
    wasVillainPreviousAggressive: boolean,
    isHeroOnButton: boolean,
  ): ActionDistribution;
  onSecondAction(
    game: GameContext,
    info: HandState,
    effectiveStack: number,
    pot: number,
    toCall: number,
    spectrum: Spectrum,
    hero: HoleCards,
    strength: StrengthManager,
    isHeroOnButton: boolean,
  ): ActionDistribution;
}

export function profitMap(
  solver: GameSolver,
  game: GameContext,
  situation: HandState,
  hero: HoleCards,
  spectrum: Spectrum,
  strength: StrengthManager,
): ActionDistribution {
  const onButton = situation.isHeroOnButton;
  const amountToCall = situation.amountToCall;
  if (game.street === PREFLOP) {
    if (onButton && situation.heroActionCount === 0) {
      return solver.onFirstActionPreflop(
        game,
        situation,
        game.bankRollAtRisk,
        game.pot,
        spectrum,
        hero,
        strength,
        true,
        game.bigBlind,
      );
    }
    return solver.onSecondAction(
      game,
      situation,
      game.bankRollAtRisk,
      game.pot,
      amountToCall,
      spectrum,
      hero,
      strength,
      onButton,
    );
  }
  if (amountToCall === 0 && !onButton) {
    return solver.onFirstAction(
      game,
      situation,
      game.bankRollAtRisk,
      game.pot,
      spectrum,
      hero,
      strength,
      situation.wasVillainPreviousAggressive,
      onButton,
    );
  }
  return solver.onSecondAction(
    game,
    situation,
    game.bankRollAtRisk,
    game.pot,
    amountToCall,
    spectrum,
    hero,
    strength,
    onButton,
  );
}
