import { describe, expect, it } from 'vitest';
import { createSession } from './session.ts';
import type { Session } from './session.ts';
import type { BotClient } from './botClient.ts';
import { callingBot, scriptedBot, shovingBot } from './testBot.ts';
import { toViewModel } from './viewModel.ts';

function open(bot: BotClient): Session {
  return createSession({
    bot,
    seed: 3,
    botDelayMs: 0,
    humanSeat: 1,
    names: ['Mallorca', 'You'],
  });
}

describe('toViewModel', () => {
  it('describes an idle session', () => {
    const session = open(callingBot());
    const vm = toViewModel(session.getState(), 1);
    expect(vm.seats[0].cards).toEqual([]);
    expect(vm.controls.enabled).toBe(false);
    expect(vm.handNumber).toBe(0);
    expect(vm.result).toBeNull();
    expect(vm.tournamentWinner).toBeNull();
  });

  it('hides the bot cards and shows the human cards during a hand', async () => {
    const session = open(callingBot());
    await session.getState().start();
    const vm = toViewModel(session.getState(), 1);
    expect(vm.seats[0].cards).toEqual(['back', 'back']);
    const hole = session.getState().tournament.hand?.holeCards[1];
    expect(vm.seats[1].cards).toEqual(hole);
    expect(vm.seats[0].name).toBe('Mallorca');
    expect(vm.seats[1].name).toBe('You');
    expect(vm.seats[0].isButton).toBe(true);
    expect(vm.seats[1].isToAct).toBe(true);
    expect(vm.seats[0].lastAction).toEqual({ key: 'call', params: { amount: 10 } });
    expect(vm.blinds).toEqual({ small: 10, big: 20 });
    expect(vm.handNumber).toBe(1);
    expect(vm.board).toEqual([]);
    expect(vm.pot).toBe(40);
    expect(vm.controls.enabled).toBe(true);
    expect(vm.controls.fold).toBe(false);
    expect(vm.controls.passive).toEqual({ kind: 'check', amount: 0 });
    expect(vm.controls.raise?.min).toBe(40);
    expect(vm.controls.raise?.max).toBe(1980 + 20);
  });

  it('sizes the presets from the pot after a call', async () => {
    const session = open(
      scriptedBot((legal) => (legal.raise ? { kind: 'raise', to: 60 } : { kind: 'call' })),
    );
    await session.getState().start();
    const vm = toViewModel(session.getState(), 1);
    expect(vm.pot).toBe(80);
    expect(vm.controls.fold).toBe(true);
    expect(vm.controls.passive).toEqual({ kind: 'call', amount: 40 });
    expect(vm.controls.raise?.presets).toEqual({ halfPot: 120, pot: 180, allIn: 2000 });
  });

  it('shows the bot cards and the result at showdown', async () => {
    const session = open(shovingBot());
    await session.getState().start();
    await session.getState().act({ kind: 'raise', to: 2000 });
    const state = session.getState();
    const vm = toViewModel(state, 1);
    expect(vm.board).toHaveLength(5);
    expect(vm.seats[0].cards).toEqual(state.tournament.hand?.holeCards[0]);
    expect(vm.result).not.toBeNull();
    expect(vm.result?.category).toEqual(expect.any(String));
    expect(vm.result?.amount).toBe(vm.result?.winner === null ? 2000 : 4000);
    expect(vm.controls.enabled).toBe(false);
    expect(vm.controls.raise).toBeNull();
    expect(vm.tournamentWinner).toBe(state.tournament.winner);
  });

  it('keeps the bot cards hidden when a hand ends by a fold', async () => {
    const session = open(shovingBot());
    await session.getState().start();
    await session.getState().act({ kind: 'fold' });
    const state = session.getState();
    const vm = toViewModel(state, 1);
    expect(vm.seats[0].cards).toEqual(['back', 'back']);
    expect(vm.result?.winner).toBe(0);
    expect(vm.result?.category).toBeNull();
    expect(vm.result?.amount).toBe((state.tournament.seats[0].stack - 2000) * 2);
    expect(vm.result?.amount).toBeGreaterThan(0);
  });
});
