import { createStore } from 'zustand/vanilla';
import type { StoreApi } from 'zustand/vanilla';
import { mulberry32 } from '../cards/index.ts';
import type { Rng } from '../cards/index.ts';
import type { Decision } from '../brains/index.ts';
import { toObservations } from '../brains/index.ts';
import {
  apply,
  createTournament,
  handHistory,
  legalActionsOf,
  startHand,
  toAct,
} from '../engine/index.ts';
import type {
  Action,
  EngineEvent,
  LegalActions,
  LogEntry,
  SeatIndex,
  Tournament,
} from '../engine/index.ts';
import type { HandCategory } from '../equity/index.ts';
import { withFallback } from './botClient.ts';
import type { BotClient } from './botClient.ts';

export type Phase =
  'idle' | 'humanToAct' | 'botThinking' | 'animating' | 'handOver' | 'tournamentOver';

export type Intent = { kind: 'fold' | 'passive' } | { kind: 'raise'; to: number };

export interface HandResult {
  winner: SeatIndex | null;
  category: HandCategory | null;
  showdown: boolean;
  amount: number;
}

export interface SessionState {
  tournament: Tournament;
  phase: Phase;
  log: LogEntry[];
  lastActions: [LogEntry | null, LogEntry | null];
  lastDecision: Decision | null;
  seed: number;
  result: HandResult | null;
}

export interface SessionActions {
  start(): Promise<void>;
  nextHand(): Promise<void>;
  act(intent: Intent): Promise<void>;
  dispose(): void;
}

export type Session = StoreApi<SessionState & SessionActions>;

export interface SessionOptions {
  bot: BotClient;
  seed: number;
  botDelayMs: number;
  humanSeat: SeatIndex;
  names: [string, string];
  startingStacks?: [number, number];
}

const STARTING_STACK = 2000;

function label(event: Extract<EngineEvent, { type: 'acted' }>): LogEntry {
  if (event.allIn && event.action.kind !== 'fold') {
    return { key: 'allIn', params: {} };
  }
  switch (event.action.kind) {
    case 'fold':
    case 'check':
      return { key: event.action.kind, params: {} };
    case 'call':
      return { key: 'call', params: { amount: event.amount } };
    case 'raise':
      return { key: 'raiseTo', params: { amount: event.action.to } };
  }
}

export function resultOf(events: EngineEvent[]): Omit<HandResult, 'amount'> {
  const awards: [number, number] = [0, 0];
  let hands: Extract<EngineEvent, { type: 'showdown' }>['hands'] | null = null;
  for (const event of events) {
    if (event.type === 'potAwarded') {
      awards[event.seat] += event.amount;
    } else if (event.type === 'showdown') {
      hands = event.hands;
    }
  }
  let winner: SeatIndex | null;
  if (hands !== null) {
    winner = hands[0].rank === hands[1].rank ? null : hands[0].rank > hands[1].rank ? 0 : 1;
  } else {
    winner = awards[0] > 0 ? 0 : 1;
  }
  const shown = hands === null ? null : hands[winner ?? 0];
  return {
    winner,
    category: shown === null ? null : shown.category,
    showdown: hands !== null,
  };
}

export function potCollected(paid: [number, number], winner: SeatIndex | null): number {
  const matched = Math.min(paid[0], paid[1]);
  return winner === null ? matched : matched * 2;
}

function awardedTo(events: EngineEvent[], seat: SeatIndex): number {
  let total = 0;
  for (const event of events) {
    if (event.type === 'potAwarded' && event.seat === seat) {
      total += event.amount;
    }
  }
  return total;
}

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

type Store = SessionState & SessionActions;
type Step = { state: Tournament; events: EngineEvent[] };

interface Timers {
  sleep(ms: number): Promise<void>;
  cancel(): void;
}

interface Runtime {
  set: StoreApi<Store>['setState'];
  get: StoreApi<Store>['getState'];
  bot: BotClient;
  rng: Rng;
  timers: Timers;
  humanSeat: SeatIndex;
  botSeat: SeatIndex;
  botDelayMs: number;
  epoch: number;
  startStacks: [number, number];
}

function createTimers(): Timers {
  const pending = new Map<ReturnType<typeof setTimeout>, () => void>();
  return {
    sleep: (ms) =>
      new Promise((resolve) => {
        const timer = setTimeout(() => {
          pending.delete(timer);
          resolve();
        }, ms);
        pending.set(timer, resolve);
      }),
    cancel: () => {
      for (const [timer, resolve] of pending) {
        clearTimeout(timer);
        resolve();
      }
      pending.clear();
    },
  };
}

function freshTournament(options: SessionOptions): Tournament {
  const { names, startingStacks } = options;
  const tournament = createTournament(names, STARTING_STACK, 0);
  if (startingStacks === undefined) {
    return tournament;
  }
  return {
    ...tournament,
    seats: [
      { name: names[0], stack: startingStacks[0] },
      { name: names[1], stack: startingStacks[1] },
    ],
  };
}

function record(rt: Runtime, events: EngineEvent[]): void {
  rt.bot.observe(toObservations(events, rt.botSeat));
  const lastActions: [LogEntry | null, LogEntry | null] = [...rt.get().lastActions];
  for (const event of events) {
    if (event.type === 'acted') {
      lastActions[event.seat] = label(event);
    }
  }
  rt.set({ log: [...rt.get().log, ...handHistory(events)], lastActions });
}

function conclude(rt: Runtime, step: Step): void {
  if (step.state.hand?.street !== 'complete') {
    return;
  }
  const { seats } = step.state;
  const paid = (seat: SeatIndex) =>
    rt.startStacks[seat] - seats[seat].stack + awardedTo(step.events, seat);
  rt.set({
    result: {
      ...resultOf(step.events),
      amount: potCollected([paid(0), paid(1)], resultOf(step.events).winner),
    },
    phase: step.state.winner === null ? 'handOver' : 'tournamentOver',
  });
}

function play(rt: Runtime, action: Action): void {
  const step = apply(rt.get().tournament, action);
  rt.set({ tournament: step.state });
  record(rt, step.events);
  conclude(rt, step);
}

async function decide(rt: Runtime, legal: LegalActions): Promise<Decision | null> {
  try {
    return await rt.bot.decide(legal);
  } catch {
    return null;
  }
}

async function botTurn(rt: Runtime, myEpoch: number): Promise<boolean> {
  rt.set({ phase: 'botThinking' });
  const legal = legalActionsOf(rt.get().tournament);
  const decision = await decide(rt, legal);
  if (myEpoch !== rt.epoch) {
    return false;
  }
  if (decision !== null) {
    rt.set({ lastDecision: decision });
  }
  if (rt.botDelayMs > 0) {
    rt.set({ phase: 'animating' });
    await rt.timers.sleep(rt.botDelayMs);
    if (myEpoch !== rt.epoch) {
      return false;
    }
  }
  const intent: Intent = decision === null ? { kind: 'passive' } : toIntent(decision.action);
  play(rt, resolveIntent(intent, legal));
  return true;
}

async function drive(rt: Runtime, myEpoch: number): Promise<void> {
  for (;;) {
    const { tournament } = rt.get();
    if (tournament.hand === null || tournament.hand.street === 'complete') {
      return;
    }
    if (toAct(tournament) === rt.humanSeat) {
      rt.set({ phase: 'humanToAct' });
      return;
    }
    if (!(await botTurn(rt, myEpoch))) {
      return;
    }
  }
}

function beginHand(rt: Runtime): Promise<void> {
  const { seats } = rt.get().tournament;
  rt.startStacks = [seats[0].stack, seats[1].stack];
  const step = startHand(rt.get().tournament, rt.rng);
  rt.bot.reset();
  rt.set({ tournament: step.state, lastActions: [null, null], result: null });
  record(rt, step.events);
  conclude(rt, step);
  return drive(rt, rt.epoch);
}

function sessionActions(rt: Runtime, options: SessionOptions): SessionActions {
  return {
    start() {
      rt.epoch += 1;
      rt.timers.cancel();
      rt.set({ tournament: freshTournament(options), log: [], lastDecision: null, result: null });
      return beginHand(rt);
    },
    dispose() {
      rt.epoch += 1;
      rt.timers.cancel();
      rt.bot.dispose?.();
    },
    nextHand() {
      if (rt.get().phase !== 'handOver') {
        return Promise.resolve();
      }
      return beginHand(rt);
    },
    act(intent) {
      const { phase, tournament } = rt.get();
      if (phase !== 'humanToAct' || toAct(tournament) !== rt.humanSeat) {
        return Promise.resolve();
      }
      play(rt, resolveIntent(intent, legalActionsOf(tournament)));
      return drive(rt, rt.epoch);
    },
  };
}

export function createSession(options: SessionOptions): Session {
  const { seed, botDelayMs, humanSeat } = options;
  const bot = withFallback(options.bot);
  const rng = mulberry32(seed);
  const timers = createTimers();
  return createStore<Store>((set, get) => {
    const rt: Runtime = {
      set,
      get,
      bot,
      rng,
      timers,
      humanSeat,
      botSeat: humanSeat === 0 ? 1 : 0,
      botDelayMs,
      epoch: 0,
      startStacks: [0, 0],
    };
    return {
      tournament: freshTournament(options),
      phase: 'idle',
      log: [],
      lastActions: [null, null],
      lastDecision: null,
      seed,
      result: null,
      ...sessionActions(rt, options),
    };
  });
}
