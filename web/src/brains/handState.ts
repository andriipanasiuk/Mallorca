import type { Card } from '../cards/index.ts';
import type { Action } from '../engine/index.ts';
import type { Observation, Who } from './observation.ts';

export const PREFLOP = 0;
export const FLOP = 1;
export const TURN = 2;
export const RIVER = 3;

export const DEFAULT_POTENTIAL = 0.1;

const STREET_INDEX = { flop: FLOP, turn: TURN, river: RIVER } as const;

export interface AggressionInfo {
  readonly heroActionCount: number;
  readonly villainActionCount: number;
  readonly heroAggressionActionCount: number;
  readonly villainAggressionActionCount: number;
  readonly wasHeroPreviousAggressive: boolean;
  readonly wasVillainPreviousAggressive: boolean;
}

export class HandState implements AggressionInfo {
  strength = -1;
  isHeroOnButton = false;
  canRaise = false;
  amountToCall = 0;
  heroAggressionActionCount = 0;
  villainAggressionActionCount = 0;
  heroActionCount = 0;
  villainActionCount = 0;
  wasHeroPreviousAggressive = false;
  wasVillainPreviousAggressive = false;
  potToStackOdds = -1;
  potOdds = -1;
  positivePotential = DEFAULT_POTENTIAL;
  negativePotential = DEFAULT_POTENTIAL;

  readonly street: number;

  constructor(street: number) {
    this.street = street;
  }

  setAggressionInfoOnlyCount(info: AggressionInfo, revert: boolean): void {
    if (revert) {
      this.villainActionCount = info.heroActionCount;
      this.heroActionCount = info.villainActionCount;
      this.villainAggressionActionCount = info.heroAggressionActionCount;
      this.heroAggressionActionCount = info.villainAggressionActionCount;
    } else {
      this.villainActionCount = info.villainActionCount;
      this.heroActionCount = info.heroActionCount;
      this.villainAggressionActionCount = info.villainAggressionActionCount;
      this.heroAggressionActionCount = info.heroAggressionActionCount;
    }
  }
}

function other(who: Who): Who {
  return who === 'hero' ? 'villain' : 'hero';
}

export function isAggressive(action: Action): boolean {
  return action.kind === 'raise';
}

export class GameContext {
  street = PREFLOP;
  board: Card[] = [];
  heroCards: [Card, Card] | undefined;
  heroOnButton = false;
  bigBlind = 0;
  pot = 0;
  private stacks = { hero: 0, villain: 0 };
  private bets = { hero: 0, villain: 0 };
  private potsOnStreet = [0, 0, 0, 0];

  observe(o: Observation): void {
    switch (o.type) {
      case 'handStarted': {
        this.street = PREFLOP;
        this.board = [];
        this.heroCards = undefined;
        this.heroOnButton = o.heroOnButton;
        this.bigBlind = o.bigBlind;
        this.pot = 0;
        this.potsOnStreet = [0, 0, 0, 0];
        this.stacks = { hero: o.heroStack, villain: o.villainStack };
        this.bets = { hero: 0, villain: 0 };
        const button: Who = o.heroOnButton ? 'hero' : 'villain';
        this.put(button, Math.min(o.smallBlind, this.stacks[button]));
        this.put(other(button), Math.min(o.bigBlind, this.stacks[other(button)]));
        break;
      }
      case 'holeCards':
        this.heroCards = o.cards;
        break;
      case 'acted':
        this.put(o.who, o.amount);
        break;
      case 'street':
        this.potsOnStreet[this.street] = this.pot;
        this.street = STREET_INDEX[o.street];
        this.board = [...o.board];
        this.bets = { hero: 0, villain: 0 };
        break;
    }
  }

  bet(who: Who): number {
    return this.bets[who];
  }

  stack(who: Who): number {
    return this.stacks[who];
  }

  amountToCall(who: Who): number {
    return Math.max(0, this.bets[other(who)] - this.bets[who]);
  }

  get bankRollAtRisk(): number {
    const difference = this.bets.hero - this.bets.villain;
    if (difference > 0) {
      return Math.min(this.stacks.hero, this.stacks.villain - difference);
    }
    return Math.min(this.stacks.hero + difference, this.stacks.villain);
  }

  potOnStreet(street: number): number {
    return this.potsOnStreet[street] ?? 0;
  }

  private put(who: Who, amount: number): void {
    this.stacks[who] -= amount;
    this.bets[who] += amount;
    this.pot += amount;
  }
}

export class HandStateObserver {
  private readonly context = new GameContext();
  private heroActionCount = 0;
  private heroAggressionCount = 0;
  private villainActionCount = 0;
  private villainAggressionCount = 0;
  private wasHeroPreviousAggressive = false;
  private wasVillainPreviousAggressive = false;
  private effectiveStack = 0;
  private pot = 0;
  private toCall = 0;
  private playerToCall = 0;
  private onButton = false;

  private readonly trackHero: boolean;

  constructor(trackHero: boolean) {
    this.trackHero = trackHero;
  }

  observe(o: Observation): void {
    switch (o.type) {
      case 'handStarted':
        this.context.observe(o);
        this.onHandStarted(o.heroOnButton === this.trackHero, o.bigBlind);
        break;
      case 'acted':
        this.onActed(o.who, o.action);
        this.context.observe(o);
        break;
      case 'street':
        this.context.observe(o);
        this.toCall = 0;
        this.playerToCall = 0;
        break;
      default:
        this.context.observe(o);
    }
  }

  situation(): HandState {
    const potAfterCall = this.playerToCall + this.pot;
    const result = new HandState(this.context.street);
    result.heroAggressionActionCount = this.heroAggressionCount;
    result.heroActionCount = this.heroActionCount;
    result.villainAggressionActionCount = this.villainAggressionCount;
    result.villainActionCount = this.villainActionCount;
    result.wasHeroPreviousAggressive = this.wasHeroPreviousAggressive;
    result.wasVillainPreviousAggressive = this.wasVillainPreviousAggressive;
    result.potOdds = this.playerToCall / potAfterCall;
    result.amountToCall = this.playerToCall;
    result.isHeroOnButton = this.onButton;
    result.potToStackOdds = potAfterCall / (potAfterCall + this.effectiveStack);
    result.canRaise = this.effectiveStack > 0;
    return result;
  }

  private onHandStarted(trackedOnButton: boolean, bigBlind: number): void {
    this.onButton = trackedOnButton;
    this.effectiveStack = this.context.bankRollAtRisk;
    this.playerToCall = trackedOnButton ? bigBlind / 2 : 0;
    this.pot = (bigBlind * 3) / 2;
    this.toCall = bigBlind / 2;
    this.heroActionCount = 0;
    this.heroAggressionCount = 0;
    this.villainActionCount = 0;
    this.villainAggressionCount = 0;
    this.wasHeroPreviousAggressive = false;
    this.wasVillainPreviousAggressive = false;
  }

  private onActed(who: Who, action: Action): void {
    if (action.kind !== 'fold') {
      this.pot += this.toCall;
    }
    let amount = 0;
    if (action.kind === 'raise') {
      amount = Math.min(action.to - this.context.bet(other(who)), this.effectiveStack);
    }
    const tracked = (who === 'hero') === this.trackHero;
    if (!tracked) {
      this.playerToCall = amount;
    }
    this.toCall = amount;
    this.pot += amount;
    this.effectiveStack -= amount;
    const aggressive = isAggressive(action);
    if (tracked) {
      this.wasHeroPreviousAggressive = aggressive;
      this.heroAggressionCount += aggressive ? 1 : 0;
      this.heroActionCount++;
    } else {
      this.wasVillainPreviousAggressive = aggressive;
      this.villainAggressionCount += aggressive ? 1 : 0;
      this.villainActionCount++;
    }
  }
}
