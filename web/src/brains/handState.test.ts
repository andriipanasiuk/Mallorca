import { describe, expect, it } from 'vitest';
import { parseCards } from '../cards/index.ts';
import decisions from './fixtures/decisions.json' with { type: 'json' };
import { GameContext, HandStateObserver } from './handState.ts';
import type { Observation, ObservationJson } from './observation.ts';
import { parseObservation } from './observation.ts';

const start: Observation = {
  type: 'handStarted',
  handNumber: 1,
  heroOnButton: false,
  heroStack: 2000,
  villainStack: 2000,
  smallBlind: 10,
  bigBlind: 20,
};
const script: Observation[] = [
  start,
  { type: 'holeCards', cards: parseCards('Ah Kd') as [number, number] },
  { type: 'acted', who: 'villain', action: { kind: 'raise', to: 60 }, amount: 50 },
  { type: 'acted', who: 'hero', action: { kind: 'call' }, amount: 40 },
  { type: 'street', street: 'flop', board: parseCards('Qs Jh 7d') },
  { type: 'acted', who: 'villain', action: { kind: 'raise', to: 40 }, amount: 40 },
  { type: 'acted', who: 'hero', action: { kind: 'raise', to: 120 }, amount: 120 },
  { type: 'acted', who: 'villain', action: { kind: 'call' }, amount: 80 },
];

function observeUpTo(observer: HandStateObserver, count: number): HandStateObserver {
  script.slice(0, count).forEach((o) => observer.observe(o));
  return observer;
}

describe('HandStateObserver for the hero', () => {
  it('starts in the big blind with nothing to call and the button on the villain', () => {
    const state = observeUpTo(new HandStateObserver(true), 2).situation();
    expect(state.street).toBe(0);
    expect(state.isHeroOnButton).toBe(false);
    expect(state.amountToCall).toBe(0);
    expect(state.canRaise).toBe(true);
  });

  it('faces the raise of the villain by its size beyond the big blind', () => {
    const state = observeUpTo(new HandStateObserver(true), 3).situation();
    expect(state.amountToCall).toBe(40);
    expect(state.heroActionCount).toBe(0);
    expect(state.villainActionCount).toBe(1);
    expect(state.villainAggressionActionCount).toBe(1);
    expect(state.wasVillainPreviousAggressive).toBe(true);
    expect(state.potOdds).toBeCloseTo(40 / 120, 12);
    expect(state.potToStackOdds).toBeCloseTo(120 / (120 + 1940), 12);
  });

  it('keeps the old amount to call after the hero calls until the street changes', () => {
    expect(observeUpTo(new HandStateObserver(true), 4).situation().amountToCall).toBe(40);
    const flop = observeUpTo(new HandStateObserver(true), 5).situation();
    expect(flop.street).toBe(1);
    expect(flop.amountToCall).toBe(0);
    expect(flop.heroActionCount).toBe(1);
    expect(flop.wasHeroPreviousAggressive).toBe(false);
  });

  it('counts a flop bet and a raise for both players', () => {
    const facingBet = observeUpTo(new HandStateObserver(true), 6).situation();
    expect(facingBet.amountToCall).toBe(40);
    expect(facingBet.wasVillainPreviousAggressive).toBe(true);
    expect(facingBet.villainAggressionActionCount).toBe(2);
    const afterCall = observeUpTo(new HandStateObserver(true), 8).situation();
    expect(afterCall.amountToCall).toBe(0);
    expect(afterCall.heroActionCount).toBe(2);
    expect(afterCall.heroAggressionActionCount).toBe(1);
    expect(afterCall.wasHeroPreviousAggressive).toBe(true);
    expect(afterCall.villainActionCount).toBe(3);
    expect(afterCall.wasVillainPreviousAggressive).toBe(false);
    expect(afterCall.potToStackOdds).toBeCloseTo(360 / (360 + 1820), 12);
  });
});

describe('HandStateObserver for the villain', () => {
  it('sees the villain on the button facing the small blind remainder', () => {
    const state = observeUpTo(new HandStateObserver(false), 2).situation();
    expect(state.isHeroOnButton).toBe(true);
    expect(state.amountToCall).toBe(10);
  });

  it('swaps hero and villain counts', () => {
    const state = observeUpTo(new HandStateObserver(false), 6).situation();
    expect(state.heroActionCount).toBe(2);
    expect(state.heroAggressionActionCount).toBe(2);
    expect(state.villainActionCount).toBe(1);
    expect(state.wasVillainPreviousAggressive).toBe(false);
    expect(state.amountToCall).toBe(0);
  });
});

interface DecisionFixture {
  id: string;
  observations: ObservationJson[];
  java: { pot: number; amountToCall: number; bankRollAtRisk: number; bigBlind: number };
}

describe('GameContext against the Java engine', () => {
  it.each((decisions as DecisionFixture[]).map((d) => [d.id, d] as const))(
    'reproduces pot, amount to call and bankroll at risk in %s',
    (_id, decision) => {
      const context = new GameContext();
      decision.observations.forEach((o) => context.observe(parseObservation(o)));
      expect(context.pot).toBe(decision.java.pot);
      expect(context.amountToCall('hero')).toBe(decision.java.amountToCall);
      expect(context.bankRollAtRisk).toBe(decision.java.bankRollAtRisk);
      expect(context.bigBlind).toBe(decision.java.bigBlind);
    },
  );

  it.each((decisions as DecisionFixture[]).map((d) => [d.id, d] as const))(
    'agrees with the hero hand state on the amount to call in %s',
    (_id, decision) => {
      const observer = new HandStateObserver(true);
      decision.observations.forEach((o) => observer.observe(parseObservation(o)));
      expect(observer.situation().amountToCall).toBe(decision.java.amountToCall);
    },
  );

  it('remembers the pot of each finished street', () => {
    const context = new GameContext();
    script.forEach((o) => context.observe(o));
    expect(context.potOnStreet(0)).toBe(120);
    expect(context.pot).toBe(360);
    expect(context.board).toEqual(parseCards('Qs Jh 7d'));
    expect(context.heroCards).toEqual(parseCards('Ah Kd'));
  });
});
