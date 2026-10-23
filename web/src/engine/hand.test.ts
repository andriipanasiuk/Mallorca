import { describe, expect, it } from 'vitest';
import { applyAction, IllegalActionError } from './hand.ts';
import { legalActions } from './legal.ts';
import { awardShowdown } from './showdown.ts';
import { call, check, deckOf, fold, play, raiseTo, startHand } from './testDeck.ts';

const deck = deckOf('Ah Ad', '7c 2d', 'Kc 9d 4s 6h Jc');

describe('startHandWith', () => {
  it('posts blinds, deals alternating from the seat after the button and gives the button the action', () => {
    const step = startHand([2000, 2000], deck);
    expect(step.hand.street).toBe('preflop');
    expect(step.hand.toAct).toBe(0);
    expect(step.hand.bets).toEqual([10, 20]);
    expect(step.hand.pot).toBe(30);
    expect(step.seats.map((s) => s.stack)).toEqual([1990, 1980]);
    expect(step.hand.holeCards).toEqual([
      [deck[1], deck[3]],
      [deck[0], deck[2]],
    ]);
    expect(step.hand.deck).toEqual(deck.slice(4));
    expect(step.events.map((e) => e.type)).toEqual([
      'handStarted',
      'blindPosted',
      'blindPosted',
      'holeCards',
      'holeCards',
    ]);
    expect(step.events[0]).toMatchObject({
      type: 'handStarted',
      handNumber: 1,
      button: 0,
      smallBlind: 10,
      bigBlind: 20,
      stacks: [2000, 2000],
    });
  });

  it('moves the small blind with the button', () => {
    const step = startHand([2000, 2000], deck, 1);
    expect(step.hand.toAct).toBe(1);
    expect(step.hand.bets).toEqual([20, 10]);
    expect(step.hand.holeCards).toEqual([
      [deck[0], deck[2]],
      [deck[1], deck[3]],
    ]);
  });
});

describe('legalActions', () => {
  it('offers fold, call and raise to the small blind', () => {
    const step = startHand([2000, 2000], deck);
    expect(legalActions(step.hand, step.seats)).toEqual({
      fold: true,
      check: false,
      call: 10,
      raise: { min: 40, max: 2000 },
    });
  });

  it('offers check and raise to the big blind after a limp', () => {
    const step = play(startHand([2000, 2000], deck), call);
    expect(legalActions(step.hand, step.seats)).toEqual({
      fold: false,
      check: true,
      call: null,
      raise: { min: 40, max: 2000 },
    });
  });

  it('returns nothing when the hand is over', () => {
    const step = play(startHand([2000, 2000], deck), fold);
    expect(step.hand.street).toBe('complete');
    expect(step.hand.toAct).toBeNull();
    expect(legalActions(step.hand, step.seats)).toEqual({
      fold: false,
      check: false,
      call: null,
      raise: null,
    });
  });
});

describe('applyAction', () => {
  it('throws for actions that are not legal', () => {
    const step = startHand([2000, 2000], deck);
    expect(() => applyAction(step.hand, step.seats, check)).toThrow(IllegalActionError);
    expect(() => applyAction(step.hand, step.seats, raiseTo(30))).toThrow(IllegalActionError);
    expect(() => applyAction(step.hand, step.seats, raiseTo(2001))).toThrow(IllegalActionError);
    expect(() => applyAction(step.hand, step.seats, raiseTo(40.5))).toThrow(IllegalActionError);
  });

  it('throws when nobody is to act', () => {
    const step = play(startHand([2000, 2000], deck), fold);
    expect(() => applyAction(step.hand, step.seats, call)).toThrow(IllegalActionError);
  });

  it('reports chips added in acted events', () => {
    const start = startHand([2000, 2000], deck);
    const step = applyAction(start.hand, start.seats, raiseTo(60));
    expect(step.events[0]).toEqual({
      type: 'acted',
      seat: 0,
      action: { kind: 'raise', to: 60 },
      amount: 50,
      allIn: false,
    });
  });
});

describe('awardShowdown', () => {
  it('gives the pot to the better hand', () => {
    expect(awardShowdown(200, [5, 3], 1)).toEqual([200, 0]);
    expect(awardShowdown(200, [3, 5], 1)).toEqual([0, 200]);
  });

  it('splits evenly on a tie', () => {
    expect(awardShowdown(200, [4, 4], 1)).toEqual([100, 100]);
  });

  it('gives the odd chip to the big blind', () => {
    expect(awardShowdown(201, [4, 4], 1)).toEqual([100, 101]);
    expect(awardShowdown(201, [4, 4], 0)).toEqual([101, 100]);
  });
});
