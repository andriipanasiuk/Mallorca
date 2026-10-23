import type { Card } from '../cards/index.ts';
import type { Action } from '../engine/index.ts';
import type { HandState } from './handState.ts';
import { HandStateObserver } from './handState.ts';
import type { Observation } from './observation.ts';
import type { HoleCards } from './spectrum.ts';
import { Spectrum } from './spectrum.ts';
import type { StreetName, StrengthManager } from './strength.ts';

export interface Advice {
  fold: number;
  passive: number;
  aggressive: number;
}

export interface VillainModel {
  readonly needsEquity: boolean;
  advise(state: HandState, cards: HoleCards): Advice;
}

const FACING_BET: Advice = { fold: 1 / 3, passive: 1 / 3, aggressive: 1 / 3 };
const NOT_FACING_BET: Advice = { fold: 0, passive: 0.5, aggressive: 0.5 };

export const randomVillainModel: VillainModel = {
  needsEquity: false,
  advise(state) {
    return { ...(state.amountToCall > 0 ? FACING_BET : NOT_FACING_BET) };
  },
};

function probabilityOf(advice: Advice, action: Action): number {
  if (action.kind === 'fold') {
    return advice.fold;
  }
  return action.kind === 'raise' ? advice.aggressive : advice.passive;
}

export class VillainSpectrumTracker {
  private readonly stateObserver = new HandStateObserver(false);
  private current = Spectrum.random();
  private randomSpectrum = Spectrum.random();
  private handState: HandState | undefined;
  private street: StreetName = 'preflop';
  private board: Card[] = [];

  private readonly model: VillainModel;
  private readonly strength: StrengthManager;

  constructor(model: VillainModel, strength: StrengthManager) {
    this.model = model;
    this.strength = strength;
  }

  get spectrum(): Spectrum {
    return this.current;
  }

  observe(o: Observation): void {
    switch (o.type) {
      case 'handStarted':
        this.stateObserver.observe(o);
        this.handState = o.heroOnButton ? undefined : this.stateObserver.situation();
        this.current = Spectrum.random();
        this.randomSpectrum = Spectrum.random();
        this.street = 'preflop';
        this.board = [];
        break;
      case 'holeCards':
        this.stateObserver.observe(o);
        this.current.remove(...o.cards);
        this.randomSpectrum.remove(...o.cards);
        break;
      case 'street': {
        this.stateObserver.observe(o);
        this.handState = this.stateObserver.situation();
        const dealt = o.board.slice(this.board.length);
        this.current.remove(...dealt);
        this.randomSpectrum.remove(...dealt);
        this.board = [...o.board];
        this.street = o.street;
        break;
      }
      case 'acted': {
        const before = this.handState ?? this.stateObserver.situation();
        this.stateObserver.observe(o);
        if (o.who === 'villain') {
          this.modifySpectrum(before, o.action);
        } else if (o.action.kind !== 'fold') {
          this.handState = this.stateObserver.situation();
        }
        break;
      }
    }
  }

  private modifySpectrum(state: HandState, action: Action): void {
    const strengthMap = this.model.needsEquity ? this.strength.map(this.street) : undefined;
    for (const cards of this.current.hands()) {
      const equity = strengthMap?.get(cards);
      if (equity !== undefined) {
        state.strength = equity.strength;
        state.positivePotential = equity.positivePotential;
        state.negativePotential = equity.negativePotential;
      }
      const percent = probabilityOf(this.model.advise(state, cards), action);
      if (percent === 0) {
        this.current.removeHand(cards);
      } else {
        this.current.add(cards, percent * this.current.weight(cards));
      }
    }
    if (this.current.isEmpty()) {
      this.current = this.randomSpectrum;
    }
  }
}
